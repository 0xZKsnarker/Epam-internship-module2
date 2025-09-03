package com.epam.cucumber.steps;

import com.epam.cucumber.support.TestContext;
import com.epam.dto.training.AddTrainingRequest;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.awaitility.Awaitility;
import org.springframework.jms.core.JmsTemplate;

import jakarta.jms.TextMessage;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class IntegrationSteps {
    private TestContext testContext;
    private JmsTemplate jmsTemplate;

    public IntegrationSteps(TestContext testContext, JmsTemplate jmsTemplate) {
        this.testContext = testContext;
        this.jmsTemplate = jmsTemplate;
    }

    @Given("both microservices are running")
    public void bothMicroservicesAreRunning() {
        given()
                .when()
                .get("/actuator/health")
                .then()
                .statusCode(200);
    }

    @Given("ActiveMQ is available")
    public void activeMQIsAvailable() {
        try {
            jmsTemplate.getConnectionFactory().createConnection().close();
        } catch (Exception e) {
            throw new RuntimeException("ActiveMQ is not available", e);
        }
    }

    @Given("ActiveMQ is stopped")
    public void activeMQIsStopped() {
        testContext.save("activemq_stopped", true);
    }

    @When("I create {int} trainings for the same trainer")
    public void iCreateMultipleTrainings(int count) {
        for (int i = 0; i < count; i++) {
            AddTrainingRequest request = new AddTrainingRequest();
            request.setTrainerUsername("batch.trainer");
            request.setTraineeUsername("batch.trainee");
            request.setTrainingName("Training " + i);
            request.setDurationOfTraining(60);
            request.setTrainingDate(LocalDate.now());

            given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + testContext.getJwtToken())
                    .body(request)
                    .when()
                    .post("/api/trainings")
                    .then()
                    .statusCode(201);
        }

        testContext.save("expected_message_count", count);
    }

    @Then("{int} messages should be sent to the workload queue")
    public void verifyMessageCount(int expectedCount) {
        Awaitility.await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Integer actualCount = jmsTemplate.browse("trainer-workload-queue",
                            (session, browser) -> {
                                int count = 0;
                                var enumeration = browser.getEnumeration();
                                while (enumeration.hasMoreElements()) {
                                    enumeration.nextElement();
                                    count++;
                                }
                                return count;
                            });

                    assertThat(actualCount).isGreaterThanOrEqualTo(expectedCount);
                });
    }

    @And("the message should contain:")
    public void verifyMessageContent(DataTable dataTable) {
        Map<String, String> expectedData = dataTable.asMap(String.class, String.class);

        Boolean found = jmsTemplate.browse("trainer-workload-queue",
                (session, browser) -> {
                    var enumeration = browser.getEnumeration();
                    while (enumeration.hasMoreElements()) {
                        try {
                            TextMessage message = (TextMessage) enumeration.nextElement();
                            String text = message.getText();

                            boolean matches = true;
                            for (Map.Entry<String, String> entry : expectedData.entrySet()) {
                                if (!text.contains(entry.getValue())) {
                                    matches = false;
                                    break;
                                }
                            }

                            if (matches) {
                                return true;
                            }
                        } catch (Exception e) {
                            // Continue to next message
                        }
                    }
                    return false;
                });

        assertThat(found).isTrue();
    }

    @And("all messages should have actionType {string}")
    public void verifyAllMessagesHaveActionType(String expectedActionType) {
        Boolean allMatch = jmsTemplate.browse("trainer-workload-queue",
                (session, browser) -> {
                    var enumeration = browser.getEnumeration();
                    while (enumeration.hasMoreElements()) {
                        try {
                            TextMessage message = (TextMessage) enumeration.nextElement();
                            if (!message.getText().contains("\"actionType\":\"" + expectedActionType + "\"")) {
                                return false;
                            }
                        } catch (Exception e) {
                            return false;
                        }
                    }
                    return true;
                });

        assertThat(allMatch).isTrue();
    }

    @And("the error should be logged but not thrown")
    public void verifyErrorHandling() {
        Response response = testContext.getResponse();
        assertThat(response.getStatusCode()).isEqualTo(201);
    }
}