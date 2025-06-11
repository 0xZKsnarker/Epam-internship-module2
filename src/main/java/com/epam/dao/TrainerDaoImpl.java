package com.epam.dao;

import com.epam.domain.Trainer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TrainerDaoImpl implements TrainerDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void create(Trainer trainer) {
        entityManager.persist(trainer);
    }

    @Override
    public Optional<Trainer> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Trainer.class, id));
    }

    @Override
    public List<Trainer> findAll() {
        return entityManager.createQuery("FROM Trainer", Trainer.class).getResultList();
    }

    @Override
    public void update(Trainer trainer) {
        entityManager.merge(trainer);
    }

    @Override
    //checks if username exists
    public boolean usernameExists(String username) {
        long count = entityManager.createQuery(
                        "SELECT COUNT(t) FROM Trainer t WHERE t.user.username = :username", Long.class)
                .setParameter("username", username)
                .getSingleResult();
        return count > 0;
    }

    @Override
    //finds by username
    public Optional<Trainer> findByUsername(String username) {
        try {
            Trainer trainer = entityManager.createQuery(
                            "SELECT t FROM Trainer t WHERE t.user.username = :username", Trainer.class)
                    .setParameter("username", username)
                    .getSingleResult();
            return Optional.of(trainer);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Trainer> findUnassignedTrainers(String traineeUsername) {
        String jpql = "SELECT t FROM Trainer t WHERE t.id NOT IN (" +
                "  SELECT tr.id FROM Trainee te JOIN te.trainers tr " +
                "  WHERE te.user.username = :traineeUsername" +
                ")";
        return entityManager.createQuery(jpql, Trainer.class)
                .setParameter("traineeUsername", traineeUsername)
                .getResultList();
    }
}