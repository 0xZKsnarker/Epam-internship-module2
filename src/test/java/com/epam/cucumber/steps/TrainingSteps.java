package com.epam.cucumber.steps;

import com.epam.cucumber.support.TestContext;
import com.epam.dao.TrainingDao;
import com.epam.dto.training.AddTrainingRequest;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
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

import java.time.LocalDate;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class TrainingSteps {

    private static final String WORKLOAD_QUEUE = "trainer-workload-queue";

    private final TestContext testContext;
    private final TrainingDao trainingDao;
    private final JmsTemplate jmsTemplate;

    @LocalServerPort
    private int port;

    @Autowired
    public TrainingSteps(TestContext testContext, TrainingDao trainingDao, JmsTemplate jmsTemplate) {
        this.testContext = testContext;
        this.trainingDao = trainingDao;
        this.jmsTemplate = jmsTemplate;
    }

    private void configureRestAssured() {
        baseURI = "http://localhost";
        io.restassured.RestAssured.port = port;
    }

    private RequestSpecification reqAuthJson() {
        configureRestAssured();
        RequestSpecification r = given().contentType(ContentType.JSON);
        String token = testContext.getJwtToken();
        if (token != null && !token.isBlank()) {
            r.header("Authorization", "Bearer " + token);
        }
        return r;
    }

    @When("I create a training with:")
    public void iCreateTrainingWith(DataTable dataTable) {
        Map<String, String> data = dataTable.asMaps().get(0);

        AddTrainingRequest request = new AddTrainingRequest();
        request.setTrainerUsername(data.get("trainerUsername"));
        request.setTraineeUsername(data.get("traineeUsername"));
        request.setTrainingName(data.get("trainingName"));
        request.setDurationOfTraining(Integer.parseInt(data.get("duration")));
        request.setTrainingDate(LocalDate.parse(data.get("date")));

        testContext.save("trainingRequest", request);

        Response response = reqAuthJson()
                .body(request)
                .when()
                .post("/api/trainings")
                .then()
                .extract().response();

        testContext.setResponse(response);
    }

    @Then("the training should be created successfully")
    public void theTrainingShouldBeCreatedSuccessfully() {
        Response response = testContext.getResponse();
        assertThat(response.getStatusCode()).isEqualTo(201);
    }

    @And("the training should exist in the database")
    public void theTrainingShouldExistInTheDatabase() {
        AddTrainingRequest request = testContext.get("trainingRequest", AddTrainingRequest.class);

        var trainings = trainingDao.findAll();

        boolean found = trainings.stream().anyMatch(t ->
                t.getTrainer().getUser().getUsername().equals(request.getTrainerUsername()) &&
                        t.getTrainee().getUser().getUsername().equals(request.getTraineeUsername()) &&
                        t.getTrainingName().equals(request.getTrainingName())
        );

        assertThat(found).isTrue();
    }

    @And("a JMS message should be sent to the workload queue")
    public void aJmsMessageShouldBeSentToTheWorkloadQueue() {
        AddTrainingRequest request = testContext.get("trainingRequest", AddTrainingRequest.class);

        Awaitility.await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    Boolean found = jmsTemplate.browse(WORKLOAD_QUEUE, (Session session, QueueBrowser browser) -> {
                        Enumeration<?> enumeration = browser.getEnumeration();
                        while (enumeration.hasMoreElements()) {
                            Message msg = (Message) enumeration.nextElement();
                            if (msg instanceof TextMessage tm) {
                                String text = tm.getText();
                                if (text != null && text.contains(request.getTrainerUsername())) {
                                    return true;
                                }
                            }
                        }
                        return false;
                    });

                    assertThat(found).isTrue();
                });
    }
}