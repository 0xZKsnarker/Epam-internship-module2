package com.epam.cucumber.steps;

import com.epam.cucumber.support.TestContext;
import com.epam.dao.TrainerDao;
import com.epam.dao.TrainingTypeDao;
import com.epam.domain.Trainer;
import com.epam.domain.TrainingType;
import com.epam.domain.User;
import com.epam.dto.trainer.TrainerRegistrationRequest;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.datatable.DataTable;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class TrainerSteps {


    private TestContext testContext;
    private TrainerDao trainerDao;
    private TrainingTypeDao trainingTypeDao;

    @Given("a trainer {string} exists")
    @Transactional
    public void aTrainerExists(String username) {
        if (trainerDao.findByUsername(username).isPresent()) {
            return;
        }

        Trainer trainer = new Trainer();
        User user = new User();
        user.setUsername(username);
        user.setFirstName("Test");
        user.setLastName("Trainer");
        user.setPassword("$2a$10$YourHashedPasswordHere");
        user.setActive(true);
        trainer.setUser(user);

        TrainingType specialization = trainingTypeDao.findById(1L)
                .orElseGet(() -> {
                    TrainingType type = new TrainingType();
                    type.setName("Fitness");
                    trainingTypeDao.create(type);
                    return type;
                });
        trainer.setSpecialization(specialization);

        trainerDao.create(trainer);
        testContext.save("trainer_" + username, trainer);
    }

    @When("I register a new trainer with:")
    public void iRegisterANewTrainerWith(DataTable dataTable) {
        Map<String, String> data = dataTable.asMap(String.class, String.class);

        TrainerRegistrationRequest request = new TrainerRegistrationRequest();
        request.setFirstName(data.get("firstName"));
        request.setLastName(data.get("lastName"));
        request.setSpecializationId(Long.parseLong(data.getOrDefault("specializationId", "1")));

        Response response = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/trainers")
                .then()
                .extract()
                .response();

        testContext.setResponse(response);

        if (response.statusCode() == 201) {
            String username = response.jsonPath().getString("username");
            testContext.save("createdTrainerUsername", username);
        }
    }

    @Then("the trainer should be created with username pattern {string}")
    public void theTrainerShouldBeCreatedWithUsernamePattern(String pattern) {
        String username = testContext.get("createdTrainerUsername", String.class);
        assertThat(username).matches(pattern);
    }

    @Then("the trainer {string} should exist in the database")
    public void theTrainerShouldExistInTheDatabase(String username) {
        boolean exists = trainerDao.findByUsername(username).isPresent();
        assertThat(exists).isTrue();
    }

    @When("I get trainer profile for {string}")
    public void iGetTrainerProfileFor(String username) {
        String token = testContext.getJwtToken();

        Response response = given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/trainers/{username}", username)
                .then()
                .extract()
                .response();

        testContext.setResponse(response);
    }
}