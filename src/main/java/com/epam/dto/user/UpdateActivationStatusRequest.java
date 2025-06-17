package com.epam.dto.user;

import jakarta.validation.constraints.NotBlank;

public class UpdateActivationStatusRequest {

    @NotBlank
    private String username;
    @NotBlank
    private boolean isActive;
    public UpdateActivationStatusRequest(){}

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
