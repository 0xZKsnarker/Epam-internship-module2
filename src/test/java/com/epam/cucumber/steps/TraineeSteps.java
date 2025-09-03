package com.epam.cucumber.steps;

import com.epam.cucumber.support.TestContext;
import com.epam.dao.TraineeDao;
import com.epam.domain.Trainee;
import com.epam.domain.User;
import com.epam.dto.trainee.TraineeRegistrationRequest;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.datatable.DataTable;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class TraineeSteps {
    private TestContext testContext;
    private TraineeDao traineeDao;

    @Given("a trainee {string} exists")
    @Transactional
    public void aTraineeExists(String username) {
        if (traineeDao.findByUsername(username).isPresent()) {
            return;
        }

        Trainee trainee = new Trainee();
        User user = new User();
        user.setUsername(username);
        user.setFirstName("Test");
        user.setLastName("Trainee");
        user.setPassword("$2a$10$YourHashedPasswordHere");
        user.setActive(true);

        trainee.setUser(user);
        trainee.setAddress("Test Address");
        trainee.setDateOfBirth(LocalDate.of(1990, 1, 1));

        traineeDao.create(trainee);
        testContext.save("trainee_" + username, trainee);
    }

    @When("I register a new trainee with:")
    public void iRegisterANewTraineeWith(DataTable dataTable) {
        Map<String, String> data = dataTable.asMap(String.class, String.class);

        TraineeRegistrationRequest request = new TraineeRegistrationRequest();
        request.setFirstName(data.get("firstName"));
        request.setLastName(data.get("lastName"));
        request.setDateOfBirth(LocalDate.parse(data.get("dateOfBirth")));
        request.setAddress(data.get("address"));

        Response response = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/trainees")
                .then()
                .extract()
                .response();

        testContext.setResponse(response);

        if (response.statusCode() == 201) {
            String username = response.jsonPath().getString("username");
            testContext.save("createdTraineeUsername", username);
        }
    }

    @Then("the trainee {string} should exist in the database")
    public void theTraineeShouldExistInTheDatabase(String username) {
        boolean exists = traineeDao.findByUsername(username).isPresent();
        assertThat(exists).isTrue();
    }

    @When("I get trainee profile for {string}")
    public void iGetTraineeProfileFor(String username) {
        String token = testContext.getJwtToken();

        Response response = given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/trainees/{username}", username)
                .then()
                .extract()
                .response();

        testContext.setResponse(response);
    }
}