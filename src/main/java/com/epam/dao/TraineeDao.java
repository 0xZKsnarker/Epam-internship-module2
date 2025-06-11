package com.epam.dao;

import com.epam.domain.Trainee;

import java.util.List;
import java.util.Optional;


public interface TraineeDao {
    void create(Trainee trainee);
    Optional<Trainee> findById(Long id);
    List<Trainee> findAll();
    void update(Trainee trainee);
    void delete(Long id);
    boolean usernameExists(String username);
    Optional<Trainee> findByUsername(String username);

}
