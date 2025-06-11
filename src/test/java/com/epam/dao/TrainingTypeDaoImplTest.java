package com.epam.dao;

import com.epam.domain.TrainingType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class TrainingTypeDaoImplTest {

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private TrainingTypeDaoImpl trainingTypeDao;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    @DisplayName("create() delegates to EntityManager.persist")
    void createPersistsEntity() {
        TrainingType type = new TrainingType();
        trainingTypeDao.create(type);
        verify(entityManager).persist(type);
    }


    @Nested
    class FindByName {

        @Test
        @DisplayName("findByName() returns entity when query succeeds")
        void returnsEntity() {
            @SuppressWarnings("unchecked")
            TypedQuery<TrainingType> query = mock(TypedQuery.class);
            TrainingType resultType = new TrainingType();

            when(entityManager.createQuery(anyString(), eq(TrainingType.class))).thenReturn(query);
            when(query.setParameter("name", "Cardio")).thenReturn(query);
            when(query.getSingleResult()).thenReturn(resultType);

            Optional<TrainingType> result = trainingTypeDao.findByName("Cardio");

            assertTrue(result.isPresent());
            assertSame(resultType, result.get());
        }

        @Test
        @DisplayName("findByName() returns Optional.empty on NoResultException")
        void returnsEmpty() {
            TypedQuery<TrainingType> query = mock(TypedQuery.class);

            when(entityManager.createQuery(anyString(), eq(TrainingType.class))).thenReturn(query);
            when(query.setParameter("name", "Strength")).thenReturn(query);
            when(query.getSingleResult()).thenThrow(NoResultException.class);

            assertTrue(trainingTypeDao.findByName("Strength").isEmpty());
        }
    }
}
