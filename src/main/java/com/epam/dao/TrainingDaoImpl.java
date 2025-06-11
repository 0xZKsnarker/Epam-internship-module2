package com.epam.dao;

import com.epam.domain.Trainee;
import com.epam.domain.Trainer;
import com.epam.domain.Training;
import com.epam.domain.TrainingType;
import com.epam.domain.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.*;

@Repository
public class TrainingDaoImpl implements TrainingDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void create(Training training) {
        entityManager.persist(training);
    }

    @Override
    public Optional<Training> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Training.class, id));
    }

    @Override
    public List<Training> findAll() {
        return entityManager.createQuery("FROM Training", Training.class).getResultList();
    }

    @Override
    public List<Training> findForTraineeByCriteria(String username, LocalDate fromDate, LocalDate toDate, String trainerName, TrainingType type) {
        StringBuilder jpql = new StringBuilder("SELECT t FROM Training t WHERE t.trainee.user.username = :username");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("username", username);

        if (fromDate != null) {
            jpql.append(" AND t.trainingDate >= :fromDate");
            parameters.put("fromDate", fromDate);
        }
        if (toDate != null) {
            jpql.append(" AND t.trainingDate <= :toDate");
            parameters.put("toDate", toDate);
        }
        if (trainerName != null && !trainerName.isEmpty()) {
            jpql.append(" AND t.trainer.user.firstName = :trainerName");
            parameters.put("trainerName", trainerName);
        }
        if (type != null) {
            jpql.append(" AND t.trainingType = :type");
            parameters.put("type", type);
        }

        // 3. Create and execute the final query
        TypedQuery<Training> query = entityManager.createQuery(jpql.toString(), Training.class);
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        return query.getResultList();
    }

    @Override
    public List<Training> findForTrainerByCriteria(String username, LocalDate fromDate, LocalDate toDate, String traineeName) {
        StringBuilder jpql = new StringBuilder("SELECT t FROM Training t WHERE t.trainer.user.username = :username");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("username", username);

        if (fromDate != null) {
            jpql.append(" AND t.trainingDate >= :fromDate");
            parameters.put("fromDate", fromDate);
        }
        if (toDate != null) {
            jpql.append(" AND t.trainingDate <= :toDate");
            parameters.put("toDate", toDate);
        }
        if (traineeName != null && !traineeName.isEmpty()) {
            jpql.append(" AND t.trainee.user.firstName = :traineeName");
            parameters.put("traineeName", traineeName);
        }
        TypedQuery<Training> query = entityManager.createQuery(jpql.toString(), Training.class);
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        return query.getResultList();
    }
}