package com.epam.cucumber.support;

import io.cucumber.spring.ScenarioScope;
import io.restassured.response.Response;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
        Object value = context.get(key);
        return value == null ? null : type.cast(value);
    }

    public <T> Optional<T> find(String key, Class<T> type) {
        return Optional.ofNullable(get(key, type));
    }

    public void remove(String key) {
        context.remove(key);
    }

    public Map<String, Object> snapshot() {
        return Collections.unmodifiableMap(new HashMap<>(context));
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


    public String getAuthorizationHeaderValue() {
        if (jwtToken == null || jwtToken.isBlank()) return null;
        return jwtToken.startsWith("Bearer ") ? jwtToken : "Bearer " + jwtToken;
    }

    public Map<String, String> authHeaderIfPresent() {
        String auth = getAuthorizationHeaderValue();
        return auth == null ? Collections.emptyMap() : Collections.singletonMap("Authorization", auth);
    }


    public void reset() {
        context.clear();
        lastResponse = null;
        jwtToken = null;
    }
}
