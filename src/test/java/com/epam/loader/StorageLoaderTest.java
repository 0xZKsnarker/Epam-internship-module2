package com.epam.loader;

import com.epam.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.event.ContextRefreshedEvent;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class StorageLoaderTest {

    private StorageLoader loader;
    private Map<Long, Trainer>  trainerMap;
    private Map<Long, Trainee>  traineeMap;
    private Map<Long, Training> trainingMap;

    // Reflection helper
    private void inject(Object target, String field, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    void setUp() {
        loader      = new StorageLoader();
        trainerMap  = new HashMap<>();
        traineeMap  = new HashMap<>();
        trainingMap = new HashMap<>();
        inject(loader, "trainerMap",   trainerMap);
        inject(loader, "traineeMap",   traineeMap);
        inject(loader, "trainingMap",  trainingMap);
        inject(loader, "trainingIdGenerator", new AtomicLong(100));
    }

    @Test
    void loadsAllCsvsWhenPresent() {
        inject(loader, "trainerFile",  "trainers_test.csv");
        inject(loader, "traineeFile",  "trainees_test.csv");
        inject(loader, "trainingFile", "trainings_test.csv");

        loader.onApplicationEvent(Mockito.mock(ContextRefreshedEvent.class));

        assertEquals(1, trainerMap.size());
        assertEquals(1, traineeMap.size());
        assertEquals(1, trainingMap.size());
        assertEquals("John", trainerMap.get(1L).getFirstName());
        assertEquals(LocalDate.of(1990, 1, 1), traineeMap.get(2L).getDateOfBirth());
        assertEquals(100L, trainingMap.values().iterator().next().getId());
    }

    @Test
    void handlesMissingTrainerFileGracefully() {
        inject(loader, "trainerFile",  "no_file.csv");// file intentionally absent
        inject(loader, "traineeFile",  "trainees_test.csv");
        inject(loader, "trainingFile", "trainings_test.csv");

        assertDoesNotThrow(() ->
                loader.onApplicationEvent(Mockito.mock(ContextRefreshedEvent.class)));

        assertTrue(trainerMap.isEmpty());
        assertEquals(1, traineeMap.size());
        assertEquals(1, trainingMap.size());
    }

    @Test
    void incrementsTrainingIdsForMultipleRows() {
        inject(loader, "trainerFile",  "trainers_test.csv");
        inject(loader, "traineeFile",  "trainees_test.csv");
        inject(loader, "trainingFile", "trainings_two_rows_test.csv");

        loader.onApplicationEvent(Mockito.mock(ContextRefreshedEvent.class));

        assertEquals(Set.of(100L, 101L), trainingMap.keySet());
    }
}
