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

    /* ---------- GET /api/auth/login/{username}?password=... ---------- */

    @Test
    void login_withValidTrainee_shouldReturnOk() throws Exception {
        when(traineeService.checkCredentials("user", "password")).thenReturn(true);
        when(trainerService.checkCredentials("user", "password")).thenReturn(false);

        mockMvc.perform(get("/api/auth/login/{username}", "user")
                        .param("password", "password"))
                .andExpect(status().isOk());
    }

    @Test
    void login_withValidTrainer_shouldReturnOk() throws Exception {
        when(traineeService.checkCredentials("user", "password")).thenReturn(false);
        when(trainerService.checkCredentials("user", "password")).thenReturn(true);

        mockMvc.perform(get("/api/auth/login/{username}", "user")
                        .param("password", "password"))
                .andExpect(status().isOk());
    }

    @Test
    void login_withInvalidCredentials_shouldReturnUnauthorized() throws Exception {
        when(traineeService.checkCredentials("user", "wrong")).thenReturn(false);
        when(trainerService.checkCredentials("user", "wrong")).thenReturn(false);

        mockMvc.perform(get("/api/auth/login/{username}", "user")
                        .param("password", "wrong"))
                .andExpect(status().isUnauthorized());
    }

    /* ---------- PUT /api/auth/{username}/password ---------- */

    @Test
    void changePassword_forTrainee_shouldReturnOk() throws Exception {
        ChangePasswordRequest body = new ChangePasswordRequest();
        body.setUsername("user");          // @NotBlank field in DTO
        body.setOldPass("old_pass");
        body.setNewPass("new_pass");

        when(traineeService.checkCredentials("user", "old_pass")).thenReturn(true);

        mockMvc.perform(put("/api/auth/{username}/password", "user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        verify(traineeService).changePassword("user", "new_pass");
    }

    @Test
    void changePassword_forTrainer_shouldReturnOk() throws Exception {
        ChangePasswordRequest body = new ChangePasswordRequest();
        body.setUsername("user");
        body.setOldPass("old_pass");
        body.setNewPass("new_pass");

        when(traineeService.checkCredentials("user", "old_pass")).thenReturn(false);
        when(trainerService.checkCredentials("user", "old_pass")).thenReturn(true);

        mockMvc.perform(put("/api/auth/{username}/password", "user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        verify(trainerService).changePassword("user", "new_pass");
    }

    @Test
    void changePassword_withInvalidCredentials_shouldReturnUnauthorized() throws Exception {
        ChangePasswordRequest body = new ChangePasswordRequest();
        body.setUsername("user");
        body.setOldPass("wrong_old");
        body.setNewPass("new_pass");

        when(traineeService.checkCredentials("user", "wrong_old")).thenReturn(false);
        when(trainerService.checkCredentials("user", "wrong_old")).thenReturn(false);

        mockMvc.perform(put("/api/auth/{username}/password", "user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }
}
