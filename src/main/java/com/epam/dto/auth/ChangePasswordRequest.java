package com.epam.dto.auth;

import jakarta.validation.constraints.NotBlank;

public class ChangePasswordRequest {

    @NotBlank
    private String username;
    @NotBlank
    private String oldPass;
    @NotBlank
    private String newPass;

    public ChangePasswordRequest(String username, String oldPass, String newPass) {
        this.username = username;
        this.oldPass = oldPass;
        this.newPass = newPass;
    }
    public ChangePasswordRequest(){}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getOldPass() {
        return oldPass;
    }

    public void setOldPass(String oldPass) {
        this.oldPass = oldPass;
    }

    public String getNewPass() {
        return newPass;
    }

    public void setNewPass(String newPass) {
        this.newPass = newPass;
    }
}
