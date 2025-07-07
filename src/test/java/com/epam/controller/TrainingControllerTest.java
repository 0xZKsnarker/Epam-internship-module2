package com.epam.controller;

import com.epam.domain.*;
import com.epam.dto.training.AddTrainingRequest;
import com.epam.facade.GymFacade;
import com.epam.security.Jwtutil; // Corrected import
import com.epam.service.TraineeService;
import com.epam.service.TrainerService;
import com.epam.service.TrainingService;
import com.epam.service.TrainingTypeService;
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

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrainingController.class)
@AutoConfigureMockMvc(addFilters = false)
class TrainingControllerTest {

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
    private TrainerService trainerService;
    private TrainingService trainingService;
    private TrainingTypeService trainingTypeService;

    @BeforeEach
    void setUp() {
        traineeService      = Mockito.mock(TraineeService.class);
        trainerService      = Mockito.mock(TrainerService.class);
        trainingService     = Mockito.mock(TrainingService.class);
        trainingTypeService = Mockito.mock(TrainingTypeService.class);

        when(gymFacade.trainees()).thenReturn(traineeService);
        when(gymFacade.trainers()).thenReturn(trainerService);
        when(gymFacade.trainings()).thenReturn(trainingService);
        when(gymFacade.trainingTypes()).thenReturn(trainingTypeService);
    }

    @Test
    void createNewTraining_shouldReturnCreated() throws Exception {
        AddTrainingRequest request = new AddTrainingRequest();
        request.setTraineeUsername("John.Doe");
        request.setTrainerUsername("Jane.Doe");
        request.setTrainingName("Morning Yoga");
        request.setTrainingDate(LocalDate.of(2025, 6, 23));
        request.setDurationOfTraining(60);

        Trainee trainee = new Trainee();
        Trainer trainer = new Trainer();
        trainer.setSpecialization(new TrainingType());


        when(traineeService.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));
        when(trainerService.findByUsername("Jane.Doe")).thenReturn(Optional.of(trainer));
        when(trainingService.create(any(Training.class))).thenReturn(new Training());

        mockMvc.perform(post("/api/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void getTrainingTypes_shouldReturnOk() throws Exception {
        when(trainingTypeService.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/trainings/types"))
                .andExpect(status().isOk());
    }

    @Test
    void getTraineeTrainings_shouldReturnOk() throws Exception {
        when(trainingService.getTraineeTrainingsByCriteria(any(), any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/trainings/trainees/{username}", "John.Doe"))
                .andExpect(status().isOk());
    }

    @Test
    void getTrainerTrainings_shouldReturnOk() throws Exception {
        when(trainingService.getTrainerTrainingsByCriteria(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/trainings/trainers/{username}", "Jane.Doe"))
                .andExpect(status().isOk());
    }

    @Test
    void getNotAssignedTrainers_shouldReturnOk() throws Exception {
        Trainer mockTrainer = new Trainer();
        mockTrainer.setUser(new User());
        mockTrainer.setSpecialization(new TrainingType());
        when(trainerService.getUnassignedTrainers(any(String.class)))
                .thenReturn(Collections.singletonList(mockTrainer));

        mockMvc.perform(get("/api/trainings/trainees/{username}/unassigned-trainers", "John.Doe"))
                .andExpect(status().isOk());
    }
}