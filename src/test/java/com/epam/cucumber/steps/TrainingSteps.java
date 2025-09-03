package com.epam.cucumber.steps;

import com.epam.cucumber.support.TestContext;
import com.epam.dao.TrainingDao;
import com.epam.dto.training.AddTrainingRequest;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import io.cucumber.datatable.DataTable;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.awaitility.Awaitility;
import org.springframework.jms.core.JmsTemplate;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class TrainingSteps {

    private TestContext testContext;
    private TrainingDao trainingDao;
    private JmsTemplate jmsTemplate;

    @When("I create a training with:")
    public void iCreateTrainingWith(DataTable dataTable) {
        Map<String, String> data = dataTable.asMap(String.class, String.class);

        AddTrainingRequest request = new AddTrainingRequest();
        request.setTrainerUsername(data.get("trainerUsername"));
        request.setTraineeUsername(data.get("traineeUsername"));
        request.setTrainingName(data.get("trainingName"));
        request.setDurationOfTraining(Integer.parseInt(data.get("duration")));
        request.setTrainingDate(LocalDate.parse(data.get("date")));

        testContext.save("trainingRequest", request);

        String token = testContext.getJwtToken();

        Response response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(request)
                .when()
                .post("/api/trainings")
                .then()
                .extract()
                .response();

        testContext.setResponse(response);
    }

    @When("I create a training with invalid data:")
    public void iCreateTrainingWithInvalidData(DataTable dataTable) {
        Map<String, String> data = dataTable.asMap(String.class, String.class);

        AddTrainingRequest request = new AddTrainingRequest();

        if (data.containsKey("trainerUsername")) {
            request.setTrainerUsername(data.get("trainerUsername"));
        }
        if (data.containsKey("traineeUsername")) {
            request.setTraineeUsername(data.get("traineeUsername"));
        }
        if (data.containsKey("trainingName")) {
            request.setTrainingName(data.get("trainingName"));
        }
        if (data.containsKey("duration") && !data.get("duration").isEmpty()) {
            try {
                request.setDurationOfTraining(Integer.parseInt(data.get("duration")));
            } catch (NumberFormatException e) {
                request.setDurationOfTraining(-1);
            }
        }
        if (data.containsKey("date")) {
            request.setTrainingDate(LocalDate.parse(data.get("date")));
        }

        String token = testContext.getJwtToken();

        Response response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(request)
                .when()
                .post("/api/trainings")
                .then()
                .extract()
                .response();

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
                .atMost(5, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    Boolean found = jmsTemplate.browse("trainer-workload-queue", (session, browser) -> {
                        var enumeration = browser.getEnumeration();
                        while (enumeration.hasMoreElements()) {
                            try {
                                jakarta.jms.Message msg = (jakarta.jms.Message) enumeration.nextElement();
                                String text = msg.toString();
                                if (text.contains(request.getTrainerUsername())) {
                                    return true;
                                }
                            } catch (Exception e) {
                            }
                        }
                        return false;
                    });

                    assertThat(found).isTrue();
                });
    }

    @When("I get trainings for trainer {string}")
    public void iGetTrainingsForTrainer(String username) {
        String token = testContext.getJwtToken();

        Response response = given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/trainings/trainers/{username}", username)
                .then()
                .extract()
                .response();

        testContext.setResponse(response);
    }

    @Then("the response should contain a training with name {string}")
    public void theResponseShouldContainTrainingWithName(String trainingName) {
        Response response = testContext.getResponse();
        var trainings = response.jsonPath().getList("$");

        boolean found = trainings.stream()
                .anyMatch(t -> ((Map<?, ?>) t).get("trainingName").equals(trainingName));

        assertThat(found).isTrue();
    }
}