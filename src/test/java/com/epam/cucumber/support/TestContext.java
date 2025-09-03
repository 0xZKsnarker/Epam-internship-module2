package com.epam.cucumber.support;

import io.cucumber.spring.ScenarioScope;
import io.restassured.response.Response;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ScenarioScope
public class TestContext {

    private final Map<String, Object> context = new HashMap<>();
    private Response lastResponse;
    private String jwtToken;

    public void save(String key, Object value) {
        context.put(key, value);
    }

    public <T> T get(String key, Class<T> type) {
        return type.cast(context.get(key));
    }

    public void setResponse(Response response) {
        this.lastResponse = response;
    }

    public Response getResponse() {
        return lastResponse;
    }

    public void setJwtToken(String token) {
        this.jwtToken = token;
    }

    public String getJwtToken() {
        return jwtToken;
    }

    public void reset() {
        context.clear();
        lastResponse = null;
        jwtToken = null;
    }
}