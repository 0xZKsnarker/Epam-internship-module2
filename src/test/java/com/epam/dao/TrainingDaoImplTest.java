package com.epam.dao;

import com.epam.domain.Training;
import com.epam.domain.TrainingType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrainingDaoImplTest {

    @Mock
    private EntityManager entityManager;
    @Mock
    private TypedQuery<Training> typedQuery;

    @InjectMocks
    private TrainingDaoImpl trainingDao;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    @DisplayName("create() calls EntityManager.persist")
    void create_persistsTraining() {
        Training training = new Training();
        trainingDao.create(training);
        verify(entityManager).persist(training);
    }


    @Nested
    class FindById {

        @Test
        @DisplayName("findById() returns entity when present")
        void returnsEntity() {
            Training training = new Training();
            when(entityManager.find(Training.class, 1L)).thenReturn(training);

            Optional<Training> result = trainingDao.findById(1L);

            assertTrue(result.isPresent());
            assertSame(training, result.get());
        }

        @Test
        @DisplayName("findById() returns empty when absent")
        void returnsEmpty() {
            when(entityManager.find(Training.class, 42L)).thenReturn(null);
            assertTrue(trainingDao.findById(42L).isEmpty());
        }
    }


    @Test
    @DisplayName("findAll() returns query result")
    void findAll_returnsList() {
        @SuppressWarnings("unchecked")
        TypedQuery<Training> query = mock(TypedQuery.class);
        List<Training> expected = List.of(new Training(), new Training());

        when(entityManager.createQuery("FROM Training", Training.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(expected);

        assertEquals(expected, trainingDao.findAll());
    }


    private void mockCriteriaPipelineReturning(List<Training> expected) {
        CriteriaBuilder cb = mock(CriteriaBuilder.class, RETURNS_DEEP_STUBS);
        CriteriaQuery<Training> cq = mock(CriteriaQuery.class, RETURNS_DEEP_STUBS);
        Root<Training> root = mock(Root.class, RETURNS_DEEP_STUBS);

        when(entityManager.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(Training.class)).thenReturn(cq);
        when(cq.from(Training.class)).thenReturn(root);

        @SuppressWarnings("unchecked")
        TypedQuery<Training> typedQuery = mock(TypedQuery.class);
        when(entityManager.createQuery(cq)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expected);
    }

    @Nested
    class FindForTraineeByCriteria {

        @Test
        @DisplayName("Should build query with all criteria when all are provided")
        void returnsExpectedListWithAllCriteria() {
            String username = "john.doe";
            LocalDate fromDate = LocalDate.of(2025, 1, 1);
            LocalDate toDate = LocalDate.of(2025, 12, 31);
            String trainerName = "Alice";
            TrainingType type = new TrainingType("Cardio");
            List<Training> expected = Collections.singletonList(new Training());

            String expectedJpql = "SELECT t FROM Training t WHERE t.trainee.user.username = :username" +
                    " AND t.trainingDate >= :fromDate" +
                    " AND t.trainingDate <= :toDate" +
                    " AND t.trainer.user.firstName = :trainerName" +
                    " AND t.trainingType = :type";

            when(entityManager.createQuery(eq(expectedJpql), eq(Training.class))).thenReturn(typedQuery);
            when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery); // Allow chaining
            when(typedQuery.getResultList()).thenReturn(expected);

            List<Training> actual = trainingDao.findForTraineeByCriteria(username, fromDate, toDate, trainerName, type);


            assertEquals(expected, actual);

            ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<Object> valueCaptor = ArgumentCaptor.forClass(Object.class);
            verify(typedQuery, new org.mockito.internal.verification.Times(5)).setParameter(keyCaptor.capture(), valueCaptor.capture());

            Map<String, Object> params = new java.util.HashMap<>();
            for (int i=0; i < keyCaptor.getAllValues().size(); i++) {
                params.put(keyCaptor.getAllValues().get(i), valueCaptor.getAllValues().get(i));
            }

            assertEquals(username, params.get("username"));
            assertEquals(fromDate, params.get("fromDate"));
            assertEquals(toDate, params.get("toDate"));
            assertEquals(trainerName, params.get("trainerName"));
            assertEquals(type, params.get("type"));
        }

        @Test
        @DisplayName("Should build query with only username when other criteria are null")
        void returnsExpectedListWithOnlyUsername() {
            String username = "john.doe";
            String expectedJpql = "SELECT t FROM Training t WHERE t.trainee.user.username = :username";
            when(entityManager.createQuery(eq(expectedJpql), eq(Training.class))).thenReturn(typedQuery);
            when(typedQuery.setParameter("username", username)).thenReturn(typedQuery);
            when(typedQuery.getResultList()).thenReturn(Collections.emptyList());

            trainingDao.findForTraineeByCriteria(username, null, null, null, null);

            verify(typedQuery).setParameter("username", username);
        }
    }

    @Nested
    class FindForTrainerByCriteria {

        @Test
        @DisplayName("Should build query with all criteria when all are provided")
        void returnsExpectedListWithAllCriteria() {
            String username = "coach.jane";
            LocalDate fromDate = LocalDate.of(2025, 3, 1);
            LocalDate toDate = LocalDate.of(2025, 3, 31);
            String traineeName = "Bob";
            List<Training> expected = Collections.singletonList(new Training());

            String expectedJpql = "SELECT t FROM Training t WHERE t.trainer.user.username = :username" +
                    " AND t.trainingDate >= :fromDate" +
                    " AND t.trainingDate <= :toDate" +
                    " AND t.trainee.user.firstName = :traineeName";

            when(entityManager.createQuery(eq(expectedJpql), eq(Training.class))).thenReturn(typedQuery);
            when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
            when(typedQuery.getResultList()).thenReturn(expected);

            List<Training> actual = trainingDao.findForTrainerByCriteria(username, fromDate, toDate, traineeName);
            assertEquals(expected, actual);
            verify(typedQuery).setParameter("username", username);
            verify(typedQuery).setParameter("fromDate", fromDate);
            verify(typedQuery).setParameter("toDate", toDate);
            verify(typedQuery).setParameter("traineeName", traineeName);
            }
        }
    }

