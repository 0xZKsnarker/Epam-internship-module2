package com.epam.dao;

import com.epam.domain.Trainer;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class TrainerDaoImplTest {

    // Build a DAO with its own storage map and a seeded AtomicLong
    private TrainerDaoImpl newDao(long startId) {
        Map<Long, Trainer> map = new HashMap<>();
        TrainerDaoImpl dao = new TrainerDaoImpl(map);
        try {
            Field f = TrainerDaoImpl.class.getDeclaredField("trainerIdGenerator");
            f.setAccessible(true);
            f.set(dao, new AtomicLong(startId));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        return dao;
    }

    // Convenience factory for Trainer objects
    private Trainer newTrainer(long id) {
        return new Trainer("John", "Smith", "john.smith", "pwd", true, "cardio", id);
    }

    // create() should assign an ID when userId == 0
    @Test
    void createGeneratesIdWhenMissing() {
        TrainerDaoImpl dao = newDao(1);
        Trainer t = newTrainer(0);
        dao.create(t);
        assertEquals(1L, t.getUserId());
        assertTrue(dao.findById(1L).isPresent());
    }

    // create() should respect an already-set ID
    @Test
    void createRespectsProvidedId() {
        TrainerDaoImpl dao = newDao(50);
        Trainer t = newTrainer(99);
        dao.create(t);
        assertEquals(99L, t.getUserId());
        assertTrue(dao.findById(99L).isPresent());
    }

    // findById() returns empty when record absent
    @Test
    void findByIdReturnsEmptyWhenAbsent() {
        TrainerDaoImpl dao = newDao(1);
        assertTrue(dao.findById(42L).isEmpty());
    }

    // findAll() returns every stored trainer
    @Test
    void findAllReturnsAllStored() {
        TrainerDaoImpl dao = newDao(1);
        dao.create(newTrainer(0));
        dao.create(newTrainer(0));
        List<Trainer> all = dao.findAll();
        assertEquals(2, all.size());
    }

    // update() should overwrite an existing record
    @Test
    void updateReplacesExisting() {
        TrainerDaoImpl dao = newDao(1);
        Trainer original = newTrainer(10);
        dao.create(original);
        Trainer updated = new Trainer("Jane", "Doe", "jane.doe", "pwd2", true, "yoga", 10);
        dao.update(updated);
        Trainer fromDao = dao.findById(10L).orElseThrow();
        assertEquals("Jane", fromDao.getFirstName());
        assertEquals("yoga", fromDao.getSpecialization());
    }
}
