package com.epam.facade;

import com.epam.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GymFacadeTest {

    @Mock
    TraineeService traineeService;
    @Mock
    TrainerService trainerService;
    @Mock
    TrainingService trainingService;
    private TrainingTypeService trainingTypeService;

    private GymFacade facade;

    @BeforeEach
    void setUp() {
        facade = new GymFacade(traineeService, trainerService, trainingService, trainingTypeService);
    }

    @Test
    void traineesReturnsInjectedService() {
        assertSame(traineeService, facade.trainees());
    }

    @Test
    void trainersReturnsInjectedService() {
        assertSame(trainerService, facade.trainers());
    }

    @Test
    void trainingsReturnsInjectedService() {
        assertSame(trainingService, facade.trainings());
    }

    @Test
    void facadeDelegatesCalls() {
        facade.trainees().findAll();
        verify(traineeService).findAll();
    }
}
