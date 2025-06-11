package com.epam.dao;

import com.epam.domain.TrainingType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class TrainingTypeDaoImpl implements TrainingTypeDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<TrainingType> findByName(String name) {
        try {
            TrainingType trainingType = entityManager.createQuery(
                            "SELECT t FROM TrainingType t WHERE t.name = :name", TrainingType.class)
                    .setParameter("name", name)
                    .getSingleResult();
            return Optional.of(trainingType);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public void create(TrainingType trainingType) {
        entityManager.persist(trainingType);
    }
}