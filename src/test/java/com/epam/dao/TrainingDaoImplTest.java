package com.epam.dao;

import com.epam.domain.Training;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class TrainingDaoImplTest {

    // Build DAO with its own storage map and a seeded AtomicLong
    private TrainingDaoImpl newDao(long startId) {
        Map<Long, Training> map = new HashMap<>();
        TrainingDaoImpl dao = new TrainingDaoImpl(map);
        try {
            Field f = TrainingDaoImpl.class.getDeclaredField("trainingIdGenerator");
            f.setAccessible(true);
            f.set(dao, new AtomicLong(startId));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        return dao;
    }

    // Helper to create a Training instance with sensible defaults
    private Training newTraining(long id) {
        Training t = new Training(
                1L,                       // traineeId
                2L,                       // trainerId
                "Morning Cardio",         // trainingName
                "Cardio",                 // trainingType
                LocalDate.now(),          // trainingDate
                45                        // trainingDuration
        );
        t.setId(id);
        return t;
    }

    // create() assigns an ID when id == 0
    @Test
    void createGeneratesIdWhenMissing() {
        TrainingDaoImpl dao = newDao(1);
        Training tr = newTraining(0);
        dao.create(tr);
        assertEquals(1L, tr.getId());
        assertTrue(dao.findById(1L).isPresent());
    }

    // create() respects an already-set ID
    @Test
    void createRespectsProvidedId() {
        TrainingDaoImpl dao = newDao(50);
        Training tr = newTraining(99);
        dao.create(tr);
        assertEquals(99L, tr.getId());
        assertTrue(dao.findById(99L).isPresent());
    }

    // findById() returns empty when record absent
    @Test
    void findByIdReturnsEmptyWhenAbsent() {
        TrainingDaoImpl dao = newDao(1);
        assertTrue(dao.findById(42L).isEmpty());
    }

    // findAll() returns every stored training
    @Test
    void findAllReturnsAllStored() {
        TrainingDaoImpl dao = newDao(1);
        dao.create(newTraining(0));
        dao.create(newTraining(0));
        List<Training> all = dao.findAll();
        assertEquals(2, all.size());
    }
}
