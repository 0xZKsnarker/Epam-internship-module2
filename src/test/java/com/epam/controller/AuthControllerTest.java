package com.epam.controller;

import com.epam.dto.auth.ChangePasswordRequest;
import com.epam.facade.GymFacade;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class) // Specify that we are testing the AuthController
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc; // For performing fake HTTP requests

    @Autowired
    private ObjectMapper objectMapper; // For converting objects to JSON

    @MockBean
    private GymFacade gymFacade; // Provide a mock of the GymFacade

    @Test
    void testLogin_whenTraineeCredentialsAreValid_shouldReturnOk() throws Exception {
        // Arrange: Set up mock behavior
        when(gymFacade.trainees().checkCredentials("user", "password")).thenReturn(true);
        when(gymFacade.trainers().checkCredentials("user", "password")).thenReturn(false);

        // Act & Assert: Perform the request and check the status
        mockMvc.perform(get("/api/auth/login")
                        .param("username", "user")
                        .param("password", "password"))
                .andExpect(status().isOk()); // Expect HTTP 200 OK
    }

    @Test
    void testLogin_whenTrainerCredentialsAreValid_shouldReturnOk() throws Exception {
        // Arrange
        when(gymFacade.trainees().checkCredentials("user", "password")).thenReturn(false);
        when(gymFacade.trainers().checkCredentials("user", "password")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/auth/login")
                        .param("username", "user")
                        .param("password", "password"))
                .andExpect(status().isOk());
    }

    @Test
    void testLogin_whenCredentialsAreInvalid_shouldReturnUnauthorized() throws Exception {
        // Arrange
        when(gymFacade.trainees().checkCredentials("user", "wrong_password")).thenReturn(false);
        when(gymFacade.trainers().checkCredentials("user", "wrong_password")).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/auth/login")
                        .param("username", "user")
                        .param("password", "wrong_password"))
                .andExpect(status().isUnauthorized()); // Expect HTTP 401 Unauthorized
    }

    @Test
    void testChangePassword_whenTraineeIsValid_shouldReturnOk() throws Exception {
        // Arrange
        ChangePasswordRequest request = new ChangePasswordRequest("user", "old_pass", "new_pass");
        when(gymFacade.trainees().checkCredentials("user", "old_pass")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(put("/api/auth/change-pass")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Verify that the changePassword method was called on the trainee service
        verify(gymFacade.trainees()).changePassword("user", "new_pass");
    }

    @Test
    void testChangePassword_whenTrainerIsValid_shouldReturnOk() throws Exception {
        // Arrange
        ChangePasswordRequest request = new ChangePasswordRequest("user", "old_pass", "new_pass");
        when(gymFacade.trainees().checkCredentials("user", "old_pass")).thenReturn(false);
        when(gymFacade.trainers().checkCredentials("user", "old_pass")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(put("/api/auth/change-pass")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Verify that the changePassword method was called on the trainer service
        verify(gymFacade.trainers()).changePassword("user", "new_pass");
    }

    @Test
    void testChangePassword_whenCredentialsAreInvalid_shouldReturnUnauthorized() throws Exception {
        // Arrange
        ChangePasswordRequest request = new ChangePasswordRequest("user", "wrong_old_pass", "new_pass");
        when(gymFacade.trainees().checkCredentials("user", "wrong_old_pass")).thenReturn(false);
        when(gymFacade.trainers().checkCredentials("user", "wrong_old_pass")).thenReturn(false);

        // Act & Assert
        mockMvc.perform(put("/api/auth/change-pass")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}