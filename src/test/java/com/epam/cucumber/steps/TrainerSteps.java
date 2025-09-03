package com.epam.cucumber.steps;

import com.epam.cucumber.support.TestContext;
import com.epam.dao.TrainerDao;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.Map;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class TrainerSteps {

    private final TestContext testContext;
    private final TrainerDao trainerDao;

    @LocalServerPort
    private int port;

    @Autowired
    public TrainerSteps(TestContext testContext, TrainerDao trainerDao) {
        this.testContext = testContext;
        this.trainerDao = trainerDao;
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

    @When("I register a new trainer with:")
    public void iRegisterANewTrainerWith(DataTable table) {
        Map<String, String> body = table.asMaps().get(0);

        Response response = reqAuthJson()
                .body(body)
                .when()
                .post("/api/trainers")
                .then()
                .extract().response();

        testContext.setResponse(response);
    }

    @And("the trainer {string} should exist in the database")
    public void theTrainerShouldExistInTheDatabase(String username) {
        boolean exists = trainerDao.findAll().stream()
                .anyMatch(t -> t.getUser() != null
                        && username.equals(t.getUser().getUsername()));
        assertThat(exists).isTrue();
    }

    @Given("a trainer {string} exists")
    public void aTrainerExists(String username) {
        boolean exists = trainerDao.findAll().stream()
                .anyMatch(t -> t.getUser() != null
                        && username.equals(t.getUser().getUsername()));
        assertThat(exists).as("Trainer must pre-exist for this scenario").isTrue();
    }

    @And("the response should contain field {string} with value {string}")
    public void theResponseShouldContainFieldWithValue(String field, String value) {
        testContext.getResponse().then().body(field, org.hamcrest.Matchers.equalTo(value));
    }

    @And("the trainer should be created with username pattern {string}")
    public void theTrainerShouldBeCreatedWithUsernamePattern(String pattern) {
        String username = testContext.getResponse().jsonPath().getString("username");
        assertThat(username).matches(pattern);
    }
}