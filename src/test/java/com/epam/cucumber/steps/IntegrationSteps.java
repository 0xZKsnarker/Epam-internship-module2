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
import io.restassured.specification.RequestSpecification;
import jakarta.jms.Message;
import jakarta.jms.QueueBrowser;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import org.awaitility.Awaitility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static io.restassured.RestAssured.*;
import static org.assertj.core.api.Assertions.assertThat;

public class IntegrationSteps {

    private static final String WORKLOAD_QUEUE = "trainer-workload-queue";

    private final TestContext testContext;
    private final JmsTemplate jmsTemplate;

    @LocalServerPort
    private int port;

    @Autowired
    public IntegrationSteps(TestContext testContext, JmsTemplate jmsTemplate) {
        this.testContext = testContext;
        this.jmsTemplate = jmsTemplate;
    }

    private void configureRestAssured() {
        baseURI = "http://localhost";
        io.restassured.RestAssured.port = port;
    }

    private RequestSpecification req() {
        configureRestAssured();
        RequestSpecification r = given().accept(ContentType.JSON);
        String token = testContext.getJwtToken();
        if (token != null && !token.isBlank()) {
            r.header("Authorization", "Bearer " + token);
        }
        return r;
    }

    @Given("both microservices are running")
    public void bothMicroservicesAreRunning() {
        Response response = req()
                .when()
                .get("/actuator/health")
                .then()
                .extract().response();

        testContext.setResponse(response);
        assertThat(response.getStatusCode()).isEqualTo(200);
    }

    @And("ActiveMQ is available")
    public void activeMQIsAvailable() {
        String pingQueue = "test-activemq-health";
        jmsTemplate.convertAndSend(pingQueue, "ping");
        Boolean found = jmsTemplate.browse(pingQueue, (Session s, QueueBrowser b) -> b.getEnumeration().hasMoreElements());
        assertThat(found).isTrue();
    }

    @When("I create {int} trainings for the same trainer")
    public void iCreateMultipleTrainings(int count) {
        String token = testContext.getJwtToken();
        assertThat(token).as("Must be authenticated before creating trainings").isNotBlank();

        for (int i = 0; i < count; i++) {
            AddTrainingRequest reqBody = new AddTrainingRequest();
            Map<String, String> defaults = Map.of(
                    "trainerUsername", "batch.trainer",
                    "traineeUsername", "batch.trainee",
                    "trainingName", "Batch-" + i
            );
            reqBody.setTrainerUsername(defaults.get("trainerUsername"));
            reqBody.setTraineeUsername(defaults.get("traineeUsername"));
            reqBody.setTrainingName(defaults.get("trainingName"));
            reqBody.setDurationOfTraining(45);
            reqBody.setTrainingDate(LocalDate.now().minusDays(i));

            Response response = given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + token)
                    .body(reqBody)
                    .when()
                    .post("/api/trainings")
                    .then()
                    .extract().response();

            assertThat(response.getStatusCode()).isIn(200, 201);
        }
    }

    @And("the message should contain:")
    public void verifyMessageContent(DataTable table) {
        List<Map<String, String>> kvs = table.asMaps(String.class, String.class);

        Awaitility.await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    Boolean ok = jmsTemplate.browse(WORKLOAD_QUEUE, (Session session, QueueBrowser browser) -> {
                        Enumeration<?> e = browser.getEnumeration();
                        while (e.hasMoreElements()) {
                            Message m = (Message) e.nextElement();
                            if (m instanceof TextMessage tm) {
                                String text = tm.getText();
                                boolean allMatch = kvs.stream().allMatch(kv ->
                                        text != null && text.contains(kv.get("value")));
                                if (allMatch) return true;
                            }
                        }
                        return false;
                    });
                    assertThat(ok).isTrue();
                });
    }

    @Then("{int} messages should be sent to the workload queue")
    public void verifyMessageCount(int expected) {
        Awaitility.await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    Integer count = jmsTemplate.browse(WORKLOAD_QUEUE, (Session s, QueueBrowser b) -> {
                        AtomicInteger c = new AtomicInteger();
                        Enumeration<?> e = b.getEnumeration();
                        while (e.hasMoreElements()) {
                            e.nextElement();
                            c.incrementAndGet();
                        }
                        return c.get();
                    });
                    assertThat(count).isGreaterThanOrEqualTo(expected);
                });
    }

    @And("all messages should have actionType {string}")
    public void verifyAllMessagesHaveActionType(String action) {
        Awaitility.await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    Boolean ok = jmsTemplate.browse(WORKLOAD_QUEUE, (Session s, QueueBrowser b) -> {
                        Enumeration<?> e = b.getEnumeration();
                        boolean sawAny = false;
                        while (e.hasMoreElements()) {
                            Message m = (Message) e.nextElement();
                            if (m instanceof TextMessage tm) {
                                String text = tm.getText();
                                if (text != null) {
                                    sawAny = true;
                                    if (!text.contains("\"actionType\":\"" + action + "\"")
                                            && !text.contains("actionType\":\"" + action + "\"")
                                            && !text.contains("actionType:" + action)) {
                                        return false;
                                    }
                                }
                            }
                        }
                        return sawAny;
                    });
                    assertThat(ok).isTrue();
                });
    }
}
