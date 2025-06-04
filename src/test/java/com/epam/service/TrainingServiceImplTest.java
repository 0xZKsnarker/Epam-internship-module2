package com.epam.service;

import com.epam.dao.TrainingDao;
import com.epam.domain.Training;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceImplTest {

    @Mock
    private TrainingDao trainingDao;

    @InjectMocks
    private TrainingServiceImpl service;

    // Helper to build a Training instance
    private Training training(long id, String name) {
        Training t = new Training(
                1L,                    // traineeId
                2L,                    // trainerId
                name,                  // trainingName
                "Strength",            // trainingType
                LocalDate.now(),       // trainingDate
                60                     // duration
        );
        t.setId(id);
        return t;
    }

    // create() should persist via DAO and return the same object
    @Test
    void createPersistsAndReturns() {
        Training t = training(0, "Bench Press");
        Training out = service.create(t);
        verify(trainingDao).create(t);
        assertSame(t, out);
    }

    // findById() should delegate to DAO
    @Test
    void findByIdDelegates() {
        Training t = training(5, "Squats");
        when(trainingDao.findById(5L)).thenReturn(Optional.of(t));
        Optional<Training> res = service.findById(5L);
        assertTrue(res.isPresent());
        assertSame(t, res.get());
    }

    // findAll() should delegate to DAO
    @Test
    void findAllDelegates() {
        List<Training> list = List.of(training(1, "Deadlift"));
        when(trainingDao.findAll()).thenReturn(list);
        List<Training> out = service.findAll();
        assertEquals(list, out);
    }
}
