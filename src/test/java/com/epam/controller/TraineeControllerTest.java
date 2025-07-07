package com.epam.controller;

import com.epam.domain.Trainee;
import com.epam.domain.User;
import com.epam.dto.trainee.TraineeRegistrationRequest;
import com.epam.dto.trainee.UpdateTraineeProfileRequest;
import com.epam.dto.trainee.UpdateTraineeTrainersRequest;
import com.epam.dto.user.UpdateActivationStatusRequest;
import com.epam.facade.GymFacade;
import com.epam.security.Jwtutil;
import com.epam.service.TraineeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TraineeController.class)
@AutoConfigureMockMvc(addFilters = false)
class TraineeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GymFacade gymFacade;

    @MockBean
    private UserDetailsService userDetailsService;
    @MockBean
    private Jwtutil jwtUtil;

    private TraineeService traineeService;

    @BeforeEach
    void setUp() {
        traineeService = Mockito.mock(TraineeService.class);
        when(gymFacade.trainees()).thenReturn(traineeService);
    }

    @Test
    void registerTrainee_shouldReturnCreated() throws Exception {
        TraineeRegistrationRequest body = new TraineeRegistrationRequest();
        body.setFirstName("John");
        body.setLastName("Doe");

        Trainee createdTrainee = new Trainee();
        createdTrainee.setUser(new User("John", "Doe", "John.Doe", "pwd", true));
        when(traineeService.create(any(Trainee.class))).thenReturn(createdTrainee);

        mockMvc.perform(post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());
    }

    @Test
    void getTrainee_shouldReturnOk() throws Exception {
        Trainee trainee = new Trainee();
        trainee.setUser(new User("John", "Doe", "John.Doe", "pwd", true));
        when(traineeService.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));

        mockMvc.perform(get("/api/trainees/{username}", "John.Doe"))
                .andExpect(status().isOk());
    }

    @Test
    void updateTraineeProfile_shouldReturnOk() throws Exception {
        UpdateTraineeProfileRequest body = new UpdateTraineeProfileRequest();
        body.setUsername("John.Doe");
        body.setFirstName("John");
        body.setLastName("Doe");
        body.setActive(true);

        Trainee existing = new Trainee();
        existing.setUser(new User());
        when(traineeService.findByUsername("John.Doe")).thenReturn(Optional.of(existing));

        Trainee updatedTraineeWithUser = new Trainee();
        updatedTraineeWithUser.setUser(new User());
        when(traineeService.update(any(Trainee.class))).thenReturn(updatedTraineeWithUser);

        mockMvc.perform(put("/api/trainees/{username}", "John.Doe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteTraineeProfile_shouldReturnOk() throws Exception {
        doNothing().when(traineeService).deleteByUsername("John.Doe");

        mockMvc.perform(delete("/api/trainees/{username}", "John.Doe"))
                .andExpect(status().isOk());
    }

    @Test
    void activateDeactivateTrainee_shouldReturnOk() throws Exception {
        UpdateActivationStatusRequest body = new UpdateActivationStatusRequest();
        body.setUsername("John.Doe");
        body.setActive(true);
        doNothing().when(traineeService).activateTrainee("John.Doe", true);

        mockMvc.perform(patch("/api/trainees/{username}/activation", "John.Doe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test
    void updateTraineeTrainers_shouldReturnOk() throws Exception {
        UpdateTraineeTrainersRequest body = new UpdateTraineeTrainersRequest();
        body.setTraineeUsername("John.Doe");
        body.setTrainers(List.of("trainer1"));

        when(traineeService.updateTrainers(anyString(), anyList())).thenReturn(new Trainee());

        mockMvc.perform(put("/api/trainees/{username}/trainers", "John.Doe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }
}