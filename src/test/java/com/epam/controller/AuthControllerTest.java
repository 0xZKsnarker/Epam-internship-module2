package com.epam.controller;

import com.epam.domain.Trainee;
import com.epam.domain.Trainer;
import com.epam.domain.User;
import com.epam.dto.auth.ChangePasswordRequest;
import com.epam.facade.GymFacade;
import com.epam.service.TraineeService;
import com.epam.service.TrainerService;
import com.epam.utils.CredentialsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.*;
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

    @MockBean
    private CredentialsService credentialsService;

    @MockBean
    private MeterRegistry meterRegistry;

    @MockBean
    private Counter counter;

    private TraineeService traineeService;
    private TrainerService trainerService;

    @BeforeEach
    void setUp() {
        traineeService = Mockito.mock(TraineeService.class);
        trainerService = Mockito.mock(TrainerService.class);
        when(gymFacade.trainees()).thenReturn(traineeService);
        when(gymFacade.trainers()).thenReturn(trainerService);

        when(meterRegistry.counter(anyString(), any(String[].class))).thenReturn(counter);
        doNothing().when(counter).increment();
    }

    private User createUser(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        return user;
    }

    private Trainee createTrainee(String username, String password) {
        Trainee trainee = new Trainee();
        trainee.setUser(createUser(username, password));
        return trainee;
    }

    private Trainer createTrainer(String username, String password) {
        Trainer trainer = new Trainer();
        trainer.setUser(createUser(username, password));
        return trainer;
    }


    @Test
    void login_withValidTrainee_shouldReturnOk() throws Exception {
        Trainee trainee = createTrainee("user", "password");
        when(traineeService.findByUsername("user")).thenReturn(Optional.of(trainee));
        when(trainerService.findByUsername("user")).thenReturn(Optional.empty());
        when(credentialsService.checkCredentials(trainee.getUser(), "password")).thenReturn(true);

        mockMvc.perform(get("/api/auth/login/{username}", "user")
                        .param("password", "password"))
                .andExpect(status().isOk());

        verify(meterRegistry).counter("auth.logins", "status", "success", "username", "user");
    }

    @Test
    void login_withValidTrainer_shouldReturnOk() throws Exception {
        Trainer trainer = createTrainer("user", "password");
        when(traineeService.findByUsername("user")).thenReturn(Optional.empty());
        when(trainerService.findByUsername("user")).thenReturn(Optional.of(trainer));
        when(credentialsService.checkCredentials(trainer.getUser(), "password")).thenReturn(true);

        mockMvc.perform(get("/api/auth/login/{username}", "user")
                        .param("password", "password"))
                .andExpect(status().isOk());

        verify(meterRegistry).counter("auth.logins", "status", "success", "username", "user");
    }

    @Test
    void login_withInvalidCredentials_shouldReturnUnauthorized() throws Exception {
        Trainee trainee = createTrainee("user", "password");
        when(traineeService.findByUsername("user")).thenReturn(Optional.of(trainee));
        when(trainerService.findByUsername("user")).thenReturn(Optional.empty());
        when(credentialsService.checkCredentials(trainee.getUser(), "wrong")).thenReturn(false);

        mockMvc.perform(get("/api/auth/login/{username}", "user")
                        .param("password", "wrong"))
                .andExpect(status().isUnauthorized());

        verify(meterRegistry).counter("auth.logins", "status", "failure", "username", "user");
    }

    @Test
    void login_withNonExistentUser_shouldReturnUnauthorized() throws Exception {
        when(traineeService.findByUsername("nonexistent")).thenReturn(Optional.empty());
        when(trainerService.findByUsername("nonexistent")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/auth/login/{username}", "nonexistent")
                        .param("password", "password"))
                .andExpect(status().isUnauthorized());

        verify(meterRegistry).counter("auth.logins", "status", "failure", "username", "nonexistent");
    }


    @Test
    void changePassword_forTrainee_shouldReturnOk() throws Exception {
        ChangePasswordRequest body = new ChangePasswordRequest();
        body.setUsername("user");
        body.setOldPass("old_pass");
        body.setNewPass("new_pass");

        Trainee trainee = createTrainee("user", "old_pass");
        when(traineeService.findByUsername("user")).thenReturn(Optional.of(trainee));
        when(credentialsService.checkCredentials(trainee.getUser(), "old_pass")).thenReturn(true);

        mockMvc.perform(put("/api/auth/{username}/password", "user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        verify(traineeService).changePassword("user", "new_pass");
        verify(meterRegistry).counter("auth.password.changes", "username", "user");
    }

    @Test
    void changePassword_forTrainer_shouldReturnOk() throws Exception {
        ChangePasswordRequest body = new ChangePasswordRequest();
        body.setUsername("user");
        body.setOldPass("old_pass");
        body.setNewPass("new_pass");

        Trainer trainer = createTrainer("user", "old_pass");
        when(traineeService.findByUsername("user")).thenReturn(Optional.empty());
        when(trainerService.findByUsername("user")).thenReturn(Optional.of(trainer));
        when(credentialsService.checkCredentials(trainer.getUser(), "old_pass")).thenReturn(true);

        mockMvc.perform(put("/api/auth/{username}/password", "user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        verify(trainerService).changePassword("user", "new_pass");
        verify(meterRegistry).counter("auth.password.changes", "username", "user");
    }

    @Test
    void changePassword_withInvalidCredentials_shouldReturnUnauthorized() throws Exception {
        ChangePasswordRequest body = new ChangePasswordRequest();
        body.setUsername("user");
        body.setOldPass("wrong_old");
        body.setNewPass("new_pass");

        Trainee trainee = createTrainee("user", "correct_pass");
        when(traineeService.findByUsername("user")).thenReturn(Optional.of(trainee));
        when(trainerService.findByUsername("user")).thenReturn(Optional.empty());
        when(credentialsService.checkCredentials(trainee.getUser(), "wrong_old")).thenReturn(false);

        mockMvc.perform(put("/api/auth/{username}/password", "user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());

        verify(traineeService, never()).changePassword(anyString(), anyString());
        verify(trainerService, never()).changePassword(anyString(), anyString());
    }

    @Test
    void changePassword_withNonExistentUser_shouldReturnUnauthorized() throws Exception {
        ChangePasswordRequest body = new ChangePasswordRequest();
        body.setUsername("nonexistent");
        body.setOldPass("old_pass");
        body.setNewPass("new_pass");

        when(traineeService.findByUsername("nonexistent")).thenReturn(Optional.empty());
        when(trainerService.findByUsername("nonexistent")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/auth/{username}/password", "nonexistent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());

        verify(traineeService, never()).changePassword(anyString(), anyString());
        verify(trainerService, never()).changePassword(anyString(), anyString());
    }
}