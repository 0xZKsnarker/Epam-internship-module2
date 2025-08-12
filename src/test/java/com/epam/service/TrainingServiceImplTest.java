package com.epam.service;

import com.epam.client.WorkloadServiceClient;
import com.epam.dao.TrainingDao;
import com.epam.dao.TrainingTypeDao;
import com.epam.domain.Training;
import com.epam.domain.TrainingType;
import com.epam.exception.ResourceNotFoundException;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class TrainingServiceImplTest {

    @Mock
    private TrainingDao trainingDao;
    @Mock
    private TrainingTypeDao trainingTypeDao;
    @Mock
    private WorkloadServiceClient workloadServiceClient;

    @InjectMocks
    private TrainingServiceImpl service;

    @BeforeEach
    void setUp() {
        MeterRegistry realMeterRegistry = new SimpleMeterRegistry();

        service = new TrainingServiceImpl(trainingDao, trainingTypeDao, realMeterRegistry, workloadServiceClient);
    }

    private static Training training(long id, String name) {
        Training t = new Training();
        t.setId(id);
        t.setTrainingName(name);
        TrainingType type = new TrainingType();
        type.setName("MockType");
        t.setTrainingType(type);
        return t;
    }

    private static TrainingType type(String name) {
        TrainingType tt = new TrainingType();
        tt.setName(name);
        return tt;
    }


    @Test
    @DisplayName("create() persists via DAO and returns same object")
    void create_persists() {
        Training t = training(0, "Bench-Press");
        Training result = service.create(t);
        verify(trainingDao).create(t);
        assertSame(t, result);
    }

    @Test
    @DisplayName("findById() delegates to DAO")
    void findById_delegates() {
        Training t = training(5, "Squats");
        when(trainingDao.findById(5L)).thenReturn(Optional.of(t));
        assertSame(t, service.findById(5L).orElseThrow());
    }

    @Test
    @DisplayName("findAll() delegates to DAO")
    void findAll_delegates() {
        List<Training> list = List.of(training(1, "Deadlift"));
        when(trainingDao.findAll()).thenReturn(list);
        assertEquals(list, service.findAll());
    }


    @Nested
    class GetTraineeTrainings {

        private final String username = "john";
        private final LocalDate fromDate = LocalDate.of(2025, 1, 1);
        private final LocalDate toDate = LocalDate.of(2025, 1, 31);
        private final String trainerName = "Alice";

        @Test
        @DisplayName("passes null type when trainingTypeName is null/blank")
        void nullTrainingTypeParam() {
            List<Training> expected = List.of(training(10, "Cardio"));
            when(trainingDao.findForTraineeByCriteria(username, fromDate, toDate, trainerName, null))
                    .thenReturn(expected);

            List<Training> result = service.getTraineeTrainingsByCriteria(
                    username, fromDate, toDate, trainerName, null);

            assertEquals(expected, result);
            verify(trainingTypeDao, never()).findByName(any());
        }

        @Test
        @DisplayName("converts trainingTypeName to entity and forwards it")
        void resolvesTrainingTypeName() {
            TrainingType cardio = type("Cardio");
            when(trainingTypeDao.findByName("Cardio")).thenReturn(Optional.of(cardio));

            List<Training> expected = List.of(training(11, "Treadmill"));
            when(trainingDao.findForTraineeByCriteria(username, null, null, null, cardio))
                    .thenReturn(expected);

            List<Training> result = service.getTraineeTrainingsByCriteria(
                    username, null, null, null, "Cardio");

            assertEquals(expected, result);
        }

        @Test
        @DisplayName("throws when trainingTypeName not found")
        void trainingTypeNotFound() {
            when(trainingTypeDao.findByName("Yoga")).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class,
                    () -> service.getTraineeTrainingsByCriteria(
                            username, null, null, null, "Yoga"));
        }
    }


    @Test
    @DisplayName("getTrainerTrainingsByCriteria() delegates to DAO")
    void getTrainerTrainings_delegates() {
        String username = "coach1";
        LocalDate fromDate = LocalDate.of(2025, 3, 1);
        LocalDate toDate = LocalDate.of(2025, 3, 31);
        String traineeName = "Bob";

        List<Training> expected = List.of(training(20, "Session"));
        when(trainingDao.findForTrainerByCriteria(username, fromDate, toDate, traineeName))
                .thenReturn(expected);

        assertEquals(expected,
                service.getTrainerTrainingsByCriteria(
                        username, fromDate, toDate, traineeName));
    }
}