package com.epam.dao;

import com.epam.domain.Trainee;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TraineeDaoImpl implements TraineeDao {

    @PersistenceContext
    private EntityManager entityManager;


    @Override
    public void create(Trainee trainee) {
        entityManager.persist(trainee);
    }

    @Override
    public Optional<Trainee> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Trainee.class, id));
    }

    @Override
    public List<Trainee> findAll() {
        return entityManager.createQuery("FROM Trainee", Trainee.class).getResultList();
    }

    @Override
    public void update(Trainee trainee) {
        entityManager.merge(trainee);
    }

    @Override
    public void delete(Long id) {
        Optional<Trainee> optionalTrainee = findById(id);
        if (optionalTrainee.isPresent()) {
            Trainee trainee = optionalTrainee.get();
            entityManager.remove(trainee);
        }
    }

    @Override
    public boolean usernameExists(String username) {
        long count = entityManager.createQuery("SELECT COUNT(t) FROM Trainee t WHERE t.user.username = :username", Long.class)
                .setParameter("username", username)
                .getSingleResult();
        return count > 0;
    }

    @Override
    //method to find a trainee by username
    public Optional<Trainee> findByUsername(String username) {
        try {
            Trainee trainee =
                    entityManager.createQuery("SELECT t FROM Trainee t WHERE t.user.username = :username", Trainee.class)
                    .setParameter("username", username)
                    .getSingleResult();
            return Optional.of(trainee);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}