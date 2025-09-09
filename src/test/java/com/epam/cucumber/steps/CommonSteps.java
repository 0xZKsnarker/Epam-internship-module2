package com.epam.cucumber.steps;

import com.epam.cucumber.support.TestContext;
import io.cucumber.java.After;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.Map;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class CommonSteps {

    private final TestContext testContext;

    @LocalServerPort
    private int port;

    @Autowired
    public CommonSteps(TestContext testContext) {
        this.testContext = testContext;
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

    @Given("the system is initialized")
    public void systemIsInitialized() {
        Response response = req()
                .when()
                .get("/actuator/health")
                .then()
                .extract().response();

        testContext.setResponse(response);
        assertThat(response.getStatusCode()).as("Health endpoint should be UP").isEqualTo(200);
    }

    @And("I am authenticated as {string}")
    public void iAmAuthenticatedAs(String username) {
        configureRestAssured();

        Map<String, String> body = Map.of(
                "username", username,
                "password", "admin"
        );

        Response response = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/api/auth/login")
                .then()
                .extract().response();

        testContext.setResponse(response);
        assertThat(response.getStatusCode()).isEqualTo(200);

        String token = response.jsonPath().getString("jwt");
        assertThat(token).isNotBlank();
        testContext.setJwtToken(token);
    }

    @And("I am not authenticated")
    public void iAmNotAuthenticated() {
        testContext.setJwtToken(null);
    }

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int expected) {
        assertThat(testContext.getResponse().getStatusCode()).isEqualTo(expected);
    }
    @Then("the error message should contain {string}")
    public void the_error_message_should_contain(String errorMessage) {
        String responseBody = testContext.getResponse().getBody().asString();
        assertThat(responseBody).contains(errorMessage);
    }


    @After
    public void tearDown() {
        testContext.reset();
    }
}