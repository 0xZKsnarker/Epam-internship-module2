package com.epam.controller;

import com.epam.domain.Trainer;
import com.epam.domain.TrainingType;
import com.epam.domain.User;
import com.epam.dto.trainer.TrainerRegistrationRequest;
import com.epam.dto.trainer.UpdateTrainerProfileRequest;
import com.epam.dto.user.UpdateActivationStatusRequest;
import com.epam.facade.GymFacade;
import com.epam.service.TrainerService;
import com.epam.service.TrainingTypeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrainerController.class)
public class TrainerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GymFacade gymFacade;

    @Autowired
    private ObjectMapper objectMapper;

    private TrainerService trainerService;
    private TrainingTypeService trainingTypeService;

    @BeforeEach
    void setUp() {
        trainerService = Mockito.mock(TrainerService.class);
        trainingTypeService = Mockito.mock(TrainingTypeService.class);

        when(gymFacade.trainers()).thenReturn(trainerService);
        when(gymFacade.trainingTypes()).thenReturn(trainingTypeService);
    }

    @Test
    void registerTrainer_shouldReturnCreated() throws Exception {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest();
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setSpecializationId(1L);

        User user = new User();
        user.setUsername("Jane.Doe");
        user.setPassword("password");

        Trainer trainer = new Trainer();
        trainer.setUser(user);
        trainer.setSpecialization(new TrainingType());

        when(trainingTypeService.findById(1L)).thenReturn(Optional.of(new TrainingType()));
        when(trainerService.create(any(Trainer.class))).thenReturn(trainer);

        mockMvc.perform(post("/api/trainers/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void getTrainer_shouldReturnOk() throws Exception {
        User user = new User();
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setActive(true);

        TrainingType specialization = new TrainingType();
        specialization.setName("Yoga");

        Trainer trainer = new Trainer();
        trainer.setUser(user);
        trainer.setSpecialization(specialization);

        when(trainerService.findByUsername("Jane.Doe")).thenReturn(Optional.of(trainer));

        mockMvc.perform(get("/api/trainers/profile")
                        .param("username", "Jane.Doe"))
                .andExpect(status().isOk());
    }

    @Test
    void updateTrainerProfile_shouldReturnOk() throws Exception {
        UpdateTrainerProfileRequest request = new UpdateTrainerProfileRequest();
        request.setUsername("Jane.Doe");
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setActive(true);

        Trainer existingTrainer = new Trainer();
        existingTrainer.setUser(new User());
        existingTrainer.setSpecialization(new TrainingType());

        User updatedUser = new User();
        updatedUser.setFirstName(request.getFirstName());
        updatedUser.setLastName(request.getLastName());
        updatedUser.setActive(request.isActive());

        TrainingType specialization = new TrainingType();
        specialization.setName("Yoga");

        Trainer updatedTrainer = new Trainer();
        updatedTrainer.setUser(updatedUser);
        updatedTrainer.setSpecialization(specialization);


        when(trainerService.findByUsername("Jane.Doe")).thenReturn(Optional.of(existingTrainer));
        when(trainerService.update(any(Trainer.class))).thenReturn(updatedTrainer);

        mockMvc.perform(put("/api/trainers/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void activateDeactivateTrainee_shouldReturnOk() throws Exception {
        UpdateActivationStatusRequest request = new UpdateActivationStatusRequest();
        request.setUsername("Jane.Doe");
        request.setActive(true);

        doNothing().when(trainerService).activateTrainer(eq("Jane.Doe"), eq(true));

        mockMvc.perform(patch("/api/trainers/activation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}