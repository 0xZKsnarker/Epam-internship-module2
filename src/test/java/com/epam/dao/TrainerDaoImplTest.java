package com.epam.dao;

import com.epam.domain.Trainer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class TrainerDaoImplTest {

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private TrainerDaoImpl trainerDao;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    @DisplayName("create() calls EntityManager.persist")
    void create_persistsTrainer() {
        Trainer trainer = new Trainer();
        trainerDao.create(trainer);
        verify(entityManager).persist(trainer);
    }


    @Nested
    class FindById {

        @Test
        @DisplayName("findById() returns entity when present")
        void returnsEntity() {
            Trainer trainer = new Trainer();
            when(entityManager.find(Trainer.class, 1L)).thenReturn(trainer);

            Optional<Trainer> result = trainerDao.findById(1L);

            assertTrue(result.isPresent());
            assertSame(trainer, result.get());
        }

        @Test
        @DisplayName("findById() returns empty when missing")
        void returnsEmpty() {
            when(entityManager.find(Trainer.class, 99L)).thenReturn(null);
            assertTrue(trainerDao.findById(99L).isEmpty());
        }
    }


    @Test
    @DisplayName("findAll() delegates to query result")
    void findAll_returnsQueryList() {
        @SuppressWarnings("unchecked")
        TypedQuery<Trainer> query = mock(TypedQuery.class);
        List<Trainer> expected = List.of(new Trainer(), new Trainer());

        when(entityManager.createQuery("FROM Trainer", Trainer.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(expected);

        assertEquals(expected, trainerDao.findAll());
    }


    @Test
    @DisplayName("update() calls EntityManager.merge")
    void update_mergesTrainer() {
        Trainer trainer = new Trainer();
        trainerDao.update(trainer);
        verify(entityManager).merge(trainer);
    }

    @Nested
    class UsernameExists {

        @Test
        @DisplayName("usernameExists() is true when count > 0")
        void returnsTrue() {
            @SuppressWarnings("unchecked")
            TypedQuery<Long> query = mock(TypedQuery.class);

            when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(query);
            when(query.setParameter("username", "john")).thenReturn(query);
            when(query.getSingleResult()).thenReturn(5L);

            assertTrue(trainerDao.usernameExists("john"));
        }

        @Test
        @DisplayName("usernameExists() is false when count == 0")
        void returnsFalse() {
            @SuppressWarnings("unchecked")
            TypedQuery<Long> query = mock(TypedQuery.class);

            when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(query);
            when(query.setParameter("username", "john")).thenReturn(query);
            when(query.getSingleResult()).thenReturn(0L);

            assertFalse(trainerDao.usernameExists("john"));
        }
    }


    @Nested
    class FindByUsername {

        @Test
        @DisplayName("findByUsername() returns entity when found")
        void returnsEntity() {
            @SuppressWarnings("unchecked")
            TypedQuery<Trainer> query = mock(TypedQuery.class);
            Trainer trainer = new Trainer();

            when(entityManager.createQuery(anyString(), eq(Trainer.class))).thenReturn(query);
            when(query.setParameter("username", "john")).thenReturn(query);
            when(query.getSingleResult()).thenReturn(trainer);

            Optional<Trainer> result = trainerDao.findByUsername("john");

            assertTrue(result.isPresent());
            assertSame(trainer, result.get());
        }

        @Test
        @DisplayName("findByUsername() returns empty on NoResultException")
        void returnsEmpty() {
            @SuppressWarnings("unchecked")
            TypedQuery<Trainer> query = mock(TypedQuery.class);

            when(entityManager.createQuery(anyString(), eq(Trainer.class))).thenReturn(query);
            when(query.setParameter("username", "john")).thenReturn(query);
            when(query.getSingleResult()).thenThrow(NoResultException.class);

            assertTrue(trainerDao.findByUsername("john").isEmpty());
        }
    }


    @Test
    @DisplayName("findUnassignedTrainers() passes traineeUsername and returns list")
    void unassignedTrainersQuery() {
        @SuppressWarnings("unchecked")
        TypedQuery<Trainer> query = mock(TypedQuery.class);
        List<Trainer> expected = List.of(new Trainer());

        when(entityManager.createQuery(startsWith("SELECT t FROM Trainer"), eq(Trainer.class)))
                .thenReturn(query);
        when(query.setParameter("traineeUsername", "alice")).thenReturn(query);
        when(query.getResultList()).thenReturn(expected);

        assertEquals(expected, trainerDao.findUnassignedTrainers("alice"));
    }
}
