package com.epam.cucumber.steps;

import com.epam.cucumber.support.TestContext;
import com.epam.dto.auth.LoginRequest;
import io.cucumber.java.Before;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import io.cucumber.spring.ScenarioScope;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ScenarioScope
public class CommonSteps {

    @LocalServerPort
    private int port;

    private TestContext testContext;

    @Before
    public void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @After
    public void tearDown() {
        testContext.reset();
    }

    @Given("the system is initialized")
    public void systemIsInitialized() {
        Response health = given()
                .when()
                .get("/actuator/health")
                .then()
                .statusCode(200)
                .extract()
                .response();

        assertThat(health.jsonPath().getString("status")).isEqualTo("UP");
    }

    @Given("I am authenticated as {string}")
    public void iAmAuthenticatedAs(String username) {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword("password123");

        Response response = given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when()
                .post("/api/auth/login")
                .then()
                .extract()
                .response();

        if (response.statusCode() == 200) {
            String token = response.jsonPath().getString("jwt");
            testContext.setJwtToken(token);
        } else {
            testContext.setJwtToken("mock-jwt-token-for-" + username);
        }
    }

    @Given("I am not authenticated")
    public void iAmNotAuthenticated() {
        testContext.setJwtToken(null);
    }

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int expectedStatus) {
        Response response = testContext.getResponse();
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
    }

    @Then("the response should contain field {string} with value {string}")
    public void theResponseShouldContainFieldWithValue(String field, String expectedValue) {
        Response response = testContext.getResponse();
        String actualValue = response.jsonPath().getString(field);
        assertThat(actualValue).isEqualTo(expectedValue);
    }

    @Then("the error message should contain {string}")
    public void theErrorMessageShouldContain(String expectedMessage) {
        Response response = testContext.getResponse();
        String responseBody = response.getBody().asString();
        assertThat(responseBody).contains(expectedMessage);
    }
}