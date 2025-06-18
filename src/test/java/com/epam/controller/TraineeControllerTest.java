package com.epam.controller;

import com.epam.domain.Trainee;
import com.epam.domain.User;
import com.epam.dto.trainee.TraineeRegistrationRequest;
import com.epam.dto.trainee.UpdateTraineeProfileRequest;
import com.epam.dto.trainee.UpdateTraineeTrainersRequest;
import com.epam.dto.user.UpdateActivationStatusRequest;
import com.epam.facade.GymFacade;
import com.epam.service.TraineeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TraineeController.class)
public class TraineeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GymFacade gymFacade;

    @Autowired
    private ObjectMapper objectMapper;

    private TraineeService traineeService;

    @BeforeEach
    void setUp() {
        traineeService = Mockito.mock(TraineeService.class);
        when(gymFacade.trainees()).thenReturn(traineeService);
    }

    @Test
    void registerTrainee_shouldReturnCreated() throws Exception {
        TraineeRegistrationRequest request = new TraineeRegistrationRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setDateOfBirth(LocalDate.of(2000, 1, 1));
        request.setAddress("123 Main St");

        User user = new User();
        user.setUsername("John.Doe");
        user.setPassword("password");

        Trainee trainee = new Trainee();
        trainee.setUser(user);

        when(traineeService.create(any(Trainee.class))).thenReturn(trainee);

        mockMvc.perform(post("/api/trainees/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void getTrainee_shouldReturnOk() throws Exception {
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setActive(true);

        Trainee trainee = new Trainee();
        trainee.setUser(user);
        trainee.setAddress("123 Main St");
        trainee.setDateOfBirth(LocalDate.of(2000, 1, 1));

        when(traineeService.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));

        mockMvc.perform(get("/api/trainees/profile")
                        .param("username", "John.Doe"))
                .andExpect(status().isOk());
    }

    @Test
    void updateTraineeProfile_shouldReturnOk() throws Exception {
        UpdateTraineeProfileRequest request = new UpdateTraineeProfileRequest();
        request.setUsername("John.Doe");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setDateOfBirth(LocalDate.of(2000, 1, 1));
        request.setAddress("456 Market St");
        request.setActive(true);

        Trainee existingTrainee = new Trainee();
        existingTrainee.setUser(new User());

        User updatedUser = new User();
        updatedUser.setFirstName(request.getFirstName());
        updatedUser.setLastName(request.getLastName());
        updatedUser.setActive(request.isActive());

        Trainee updatedTrainee = new Trainee();
        updatedTrainee.setUser(updatedUser);
        updatedTrainee.setAddress(request.getAddress());
        updatedTrainee.setDateOfBirth(request.getDateOfBirth());


        when(traineeService.findByUsername("John.Doe")).thenReturn(Optional.of(existingTrainee));
        when(traineeService.update(any(Trainee.class))).thenReturn(updatedTrainee);

        mockMvc.perform(put("/api/trainees/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
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
        UpdateActivationStatusRequest request = new UpdateActivationStatusRequest();
        request.setUsername("John.Doe");
        request.setActive(true);

        doNothing().when(traineeService).activateTrainee(eq("John.Doe"), eq(true));

        mockMvc.perform(patch("/api/trainees/activation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void updateTraineeTrainers_shouldReturnOk() throws Exception {
        List<String> trainerUsernames = List.of("trainer1", "trainer2");
        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest("John.Doe", trainerUsernames);

        when(traineeService.updateTrainers(eq("John.Doe"), eq(trainerUsernames))).thenReturn(new Trainee());

        mockMvc.perform(put("/api/trainees/trainee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}