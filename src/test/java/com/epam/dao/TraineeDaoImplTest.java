package com.epam.dao;

import com.epam.domain.Trainee;
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


class TraineeDaoImplTest {

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private TraineeDaoImpl traineeDao;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    @DisplayName("create() should delegate to EntityManager.persist")
    void create_persistsTrainee() {
        Trainee trainee = new Trainee();
        traineeDao.create(trainee);
        verify(entityManager).persist(trainee);
    }


    @Nested
    class FindById {

        @Test
        @DisplayName("findById() returns Optional with entity when found")
        void returnsEntityWhenPresent() {
            Trainee trainee = new Trainee();
            when(entityManager.find(Trainee.class, 1L)).thenReturn(trainee);

            Optional<Trainee> result = traineeDao.findById(1L);

            assertTrue(result.isPresent());
            assertSame(trainee, result.get());
        }

        @Test
        @DisplayName("findById() returns Optional.empty when entity missing")
        void returnsEmptyWhenMissing() {
            when(entityManager.find(Trainee.class, 99L)).thenReturn(null);

            assertTrue(traineeDao.findById(99L).isEmpty());
        }
    }


    @Test
    @DisplayName("findAll() returns the list yielded by the query")
    void findAll_returnsQueryResult() {
        @SuppressWarnings("unchecked")
        TypedQuery<Trainee> query = mock(TypedQuery.class);
        List<Trainee> expected = List.of(new Trainee(), new Trainee());

        when(entityManager.createQuery("FROM Trainee", Trainee.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(expected);

        List<Trainee> all = traineeDao.findAll();

        assertEquals(expected, all);
    }


    @Test
    @DisplayName("update() delegates to EntityManager.merge")
    void update_callsMerge() {
        Trainee trainee = new Trainee();
        traineeDao.update(trainee);
        verify(entityManager).merge(trainee);
    }


    @Nested
    class Delete {

        @Test
        @DisplayName("delete() removes entity when it exists")
        void removesWhenFound() {
            Trainee trainee = new Trainee();
            when(entityManager.find(Trainee.class, 7L)).thenReturn(trainee);

            traineeDao.delete(7L);

            verify(entityManager).remove(trainee);
        }

        @Test
        @DisplayName("delete() is a no-op when entity missing")
        void noOpWhenMissing() {
            when(entityManager.find(Trainee.class, 7L)).thenReturn(null);

            traineeDao.delete(7L);

            verify(entityManager, never()).remove(any());
        }
    }


    @Nested
    class UsernameExists {

        @Test
        @DisplayName("usernameExists() returns true when count>0")
        void returnsTrueWhenFound() {
            @SuppressWarnings("unchecked")
            TypedQuery<Long> query = mock(TypedQuery.class);
            when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(query);
            when(query.setParameter("username", "john")).thenReturn(query);
            when(query.getSingleResult()).thenReturn(3L);

            assertTrue(traineeDao.usernameExists("john"));
        }

        @Test
        @DisplayName("usernameExists() returns false when count==0")
        void returnsFalseWhenNotFound() {
            @SuppressWarnings("unchecked")
            TypedQuery<Long> query = mock(TypedQuery.class);
            when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(query);
            when(query.setParameter("username", "john")).thenReturn(query);
            when(query.getSingleResult()).thenReturn(0L);

            assertFalse(traineeDao.usernameExists("john"));
        }
    }


    @Nested
    class FindByUsername {

        @Test
        @DisplayName("findByUsername() returns entity when query succeeds")
        void returnsEntity() {
            @SuppressWarnings("unchecked")
            TypedQuery<Trainee> query = mock(TypedQuery.class);
            Trainee trainee = new Trainee();

            when(entityManager.createQuery(anyString(), eq(Trainee.class))).thenReturn(query);
            when(query.setParameter("username", "john")).thenReturn(query);
            when(query.getSingleResult()).thenReturn(trainee);

            Optional<Trainee> result = traineeDao.findByUsername("john");

            assertTrue(result.isPresent());
            assertSame(trainee, result.get());
        }

        @Test
        @DisplayName("findByUsername() returns Optional.empty on NoResultException")
        void returnsEmptyWhenNotFound() {
            @SuppressWarnings("unchecked")
            TypedQuery<Trainee> query = mock(TypedQuery.class);

            when(entityManager.createQuery(anyString(), eq(Trainee.class))).thenReturn(query);
            when(query.setParameter("username", "john")).thenReturn(query);
            when(query.getSingleResult()).thenThrow(NoResultException.class);

            assertTrue(traineeDao.findByUsername("john").isEmpty());
        }
    }
}
