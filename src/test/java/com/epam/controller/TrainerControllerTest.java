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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrainerController.class)
class TrainerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GymFacade gymFacade;

    private TrainerService trainerService;
    private TrainingTypeService trainingTypeService;

    @BeforeEach
    void setUp() {
        trainerService      = Mockito.mock(TrainerService.class);
        trainingTypeService = Mockito.mock(TrainingTypeService.class);

        when(gymFacade.trainers()).thenReturn(trainerService);
        when(gymFacade.trainingTypes()).thenReturn(trainingTypeService);
    }

    @Test
    void registerTrainer_shouldReturnCreated() throws Exception {
        TrainerRegistrationRequest body = new TrainerRegistrationRequest();
        body.setFirstName("Jane");
        body.setLastName("Doe");
        body.setSpecializationId(1L);

        TrainingType spec = new TrainingType();
        when(trainingTypeService.findById(1L)).thenReturn(Optional.of(spec));

        User user = new User();
        user.setUsername("Jane.Doe");
        user.setPassword("pwd");

        Trainer trainer = new Trainer();
        trainer.setUser(user);
        trainer.setSpecialization(spec);

        when(trainerService.create(any(Trainer.class))).thenReturn(trainer);

        mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());
    }

    @Test
    void getTrainer_shouldReturnOk() throws Exception {
        User user = new User();
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setActive(true);

        TrainingType spec = new TrainingType();
        spec.setName("Yoga");

        Trainer trainer = new Trainer();
        trainer.setUser(user);
        trainer.setSpecialization(spec);

        when(trainerService.findByUsername("Jane.Doe")).thenReturn(Optional.of(trainer));

        mockMvc.perform(get("/api/trainers/{username}", "Jane.Doe"))
                .andExpect(status().isOk());
    }

    @Test
    void updateTrainerProfile_shouldReturnOk() throws Exception {
        UpdateTrainerProfileRequest body = new UpdateTrainerProfileRequest();
        body.setUsername("Jane.Doe");      // satisfies @NotBlank in DTO
        body.setFirstName("Jane");
        body.setLastName("Doe");
        body.setActive(true);

        Trainer existing = new Trainer();
        existing.setUser(new User());
        existing.setSpecialization(new TrainingType());

        Trainer updated = new Trainer();
        updated.setUser(new User());
        updated.getUser().setFirstName(body.getFirstName());
        updated.getUser().setLastName(body.getLastName());
        updated.getUser().setActive(body.isActive());
        TrainingType spec = new TrainingType();
        spec.setName("Yoga");
        updated.setSpecialization(spec);

        when(trainerService.findByUsername("Jane.Doe")).thenReturn(Optional.of(existing));
        when(trainerService.update(any(Trainer.class))).thenReturn(updated);

        mockMvc.perform(put("/api/trainers/{username}", "Jane.Doe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test
    void activateDeactivateTrainee_shouldReturnOk() throws Exception {
        UpdateActivationStatusRequest body = new UpdateActivationStatusRequest();
        body.setUsername("Jane.Doe");
        body.setActive(true);

        doNothing().when(trainerService).activateTrainer("Jane.Doe", true);

        mockMvc.perform(patch("/api/trainers/{username}/activation", "Jane.Doe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }
}
