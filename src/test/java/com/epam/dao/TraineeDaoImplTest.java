package com.epam.dao;

import com.epam.domain.Trainee;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class TraineeDaoImplTest {


    //Build a DAO with its own empty Map and a fresh AtomicLong.
    private TraineeDaoImpl newDao(long startingId) {
        Map<Long, Trainee> backingMap = new HashMap<>();
        TraineeDaoImpl dao = new TraineeDaoImpl(backingMap);

        //inject atomic long into the private field
        try {
            Field f = TraineeDaoImpl.class.getDeclaredField("traineeIdGenerator");
            f.setAccessible(true);
            f.set(dao, new AtomicLong(startingId));
        } catch (ReflectiveOperationException roe) {
            throw new RuntimeException("Failed to inject AtomicLong", roe);
        }
        return dao;
    }

    private Trainee newTrainee(long id) {
        return new Trainee(
                "John",
                "Doe",
                "john.doe",
                "pwd",
                true,
                LocalDate.of(1990, 1, 1),
                "Main St",
                id
        );
    }


    // create method test
    @Test
    void create_generatesIdWhenMissing() {
        TraineeDaoImpl dao = newDao(1);
        Trainee t = newTrainee(0);

        dao.create(t);

        assertEquals(1L, t.getUserId());
        assertTrue(dao.findById(1L).isPresent());
    }

    @Test
    void create_respectsProvidedId() {
        TraineeDaoImpl dao = newDao(50);
        Trainee t = newTrainee(99);

        dao.create(t);

        assertEquals(99L, t.getUserId());
        assertTrue(dao.findById(99L).isPresent());
    }

    //find by id method test
    @Test
    void findById_returnsEmptyWhenMissing() {
        TraineeDaoImpl dao = newDao(1);

        assertTrue(dao.findById(42L).isEmpty());
    }

    //find all method test
    @Test
    void findAll_returnsAllStoredTrainees() {
        TraineeDaoImpl dao = newDao(1);
        dao.create(newTrainee(0));
        dao.create(newTrainee(0));

        List<Trainee> all = dao.findAll();

        assertEquals(2, all.size());
    }

    //update method test
    @Test
    void update_replacesExistingRecord() {
        TraineeDaoImpl dao = newDao(1);
        Trainee original = newTrainee(10);
        dao.create(original);

        Trainee changed = new Trainee(
                "Jane", "Doe", "jane.doe", "pwd2", true,
                LocalDate.of(1992, 2, 2), "Second St", 10
        );
        dao.update(changed);

        Trainee fromDao = dao.findById(10L).orElseThrow();
        assertEquals("Jane", fromDao.getFirstName());
        assertEquals("jane.doe", fromDao.getUsername());
    }

    //delete method test
    @Test
    void delete_removesRecord() {
        TraineeDaoImpl dao = newDao(1);
        dao.create(newTrainee(20));

        dao.delete(20L);

        assertTrue(dao.findById(20L).isEmpty());
        assertEquals(0, dao.findAll().size());
    }
}
