package com.epam.controller;

import com.epam.dto.auth.ChangePasswordRequest;
import com.epam.facade.GymFacade;
import com.epam.service.TraineeService;
import com.epam.service.TrainerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GymFacade gymFacade;

    private TraineeService traineeService;
    private TrainerService trainerService;

    @BeforeEach
    void setUp() {
        traineeService = Mockito.mock(TraineeService.class);
        trainerService = Mockito.mock(TrainerService.class);
        when(gymFacade.trainees()).thenReturn(traineeService);
        when(gymFacade.trainers()).thenReturn(trainerService);
    }

    @Test
    void testLogin_whenTraineeCredentialsAreValid_shouldReturnOk() throws Exception {
        when(traineeService.checkCredentials("user", "password")).thenReturn(true);
        when(trainerService.checkCredentials("user", "password")).thenReturn(false);

        mockMvc.perform(get("/api/auth/login")
                        .param("username", "user")
                        .param("password", "password"))
                .andExpect(status().isOk());
    }

    @Test
    void testLogin_whenTrainerCredentialsAreValid_shouldReturnOk() throws Exception {
        when(traineeService.checkCredentials("user", "password")).thenReturn(false);
        when(trainerService.checkCredentials("user", "password")).thenReturn(true);

        mockMvc.perform(get("/api/auth/login")
                        .param("username", "user")
                        .param("password", "password"))
                .andExpect(status().isOk());
    }

    @Test
    void testLogin_whenCredentialsAreInvalid_shouldReturnUnauthorized() throws Exception {
        when(traineeService.checkCredentials("user", "wrong_password")).thenReturn(false);
        when(trainerService.checkCredentials("user", "wrong_password")).thenReturn(false);

        mockMvc.perform(get("/api/auth/login")
                        .param("username", "user")
                        .param("password", "wrong_password"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testChangePassword_whenTraineeIsValid_shouldReturnOk() throws Exception {

        ChangePasswordRequest request = new ChangePasswordRequest(null, null, null); // Call empty constructor
        request.setUsername("user");
        request.setOldPass("old_pass");
        request.setNewPass("new_pass");

        when(traineeService.checkCredentials("user", "old_pass")).thenReturn(true);

        mockMvc.perform(put("/api/auth/change-pass")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(traineeService).changePassword("user", "new_pass");
    }

    @Test
    void testChangePassword_whenTrainerIsValid_shouldReturnOk() throws Exception {

        ChangePasswordRequest request = new ChangePasswordRequest(null, null, null); // Call empty constructor
        request.setUsername("user");
        request.setOldPass("old_pass");
        request.setNewPass("new_pass");

        when(traineeService.checkCredentials("user", "old_pass")).thenReturn(false);
        when(trainerService.checkCredentials("user", "old_pass")).thenReturn(true);

        mockMvc.perform(put("/api/auth/change-pass")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(trainerService).changePassword("user", "new_pass");
    }

    @Test
    void testChangePassword_whenCredentialsAreInvalid_shouldReturnUnauthorized() throws Exception {

        ChangePasswordRequest request = new ChangePasswordRequest(null, null, null); // Call empty constructor
        request.setUsername("user");
        request.setOldPass("wrong_old_pass");
        request.setNewPass("new_pass");

        when(traineeService.checkCredentials("user", "wrong_old_pass")).thenReturn(false);
        when(trainerService.checkCredentials("user", "wrong_old_pass")).thenReturn(false);

        mockMvc.perform(put("/api/auth/change-pass")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}