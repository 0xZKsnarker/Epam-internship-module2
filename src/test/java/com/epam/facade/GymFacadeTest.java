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

    private GymFacade facade;

    @BeforeEach
    void setUp() {
        facade = new GymFacade(traineeService, trainerService, trainingService);
    }

    // trainees() should return the same instance that was injected
    @Test
    void traineesReturnsInjectedService() {
        assertSame(traineeService, facade.trainees());
    }

    // trainers() should return the same instance that was injected
    @Test
    void trainersReturnsInjectedService() {
        assertSame(trainerService, facade.trainers());
    }

    // trainings() should return the same instance that was injected
    @Test
    void trainingsReturnsInjectedService() {
        assertSame(trainingService, facade.trainings());
    }

    // facade should let callers invoke underlying service methods transparently
    @Test
    void facadeDelegatesCalls() {
        facade.trainees().findAll();
        verify(traineeService).findAll();
    }
}
