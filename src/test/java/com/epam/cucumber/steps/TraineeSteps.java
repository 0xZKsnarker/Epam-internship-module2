package com.epam.cucumber.steps;

import com.epam.cucumber.support.TestContext;
import com.epam.dao.TraineeDao;
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

public class TraineeSteps {

    private final TestContext testContext;
    private final TraineeDao traineeDao;

    @LocalServerPort
    private int port;

    @Autowired
    public TraineeSteps(TestContext testContext, TraineeDao traineeDao) {
        this.testContext = testContext;
        this.traineeDao = traineeDao;
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

    @When("I register a new trainee with:")
    public void iRegisterANewTraineeWith(DataTable table) {
        Map<String, String> body = table.asMaps().get(0);

        Response response = reqAuthJson()
                .body(body)
                .when()
                .post("/api/trainees")
                .then()
                .extract().response();

        testContext.setResponse(response);
    }

    @And("the trainee {string} should exist in the database")
    public void theTraineeShouldExistInTheDatabase(String username) {
        boolean exists = traineeDao.findAll().stream()
                .anyMatch(t -> t.getUser() != null
                        && username.equals(t.getUser().getUsername()));
        assertThat(exists).isTrue();
    }

    @Given("a trainee {string} exists")
    public void aTraineeExists(String username) {
        boolean exists = traineeDao.findAll().stream()
                .anyMatch(t -> t.getUser() != null
                        && username.equals(t.getUser().getUsername()));
        assertThat(exists).as("Trainee must pre-exist for this scenario").isTrue();
    }
}