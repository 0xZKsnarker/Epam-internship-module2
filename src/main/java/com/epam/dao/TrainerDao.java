
package com.epam.dao;
import com.epam.domain.Trainer;

import java.util.List;
import java.util.Optional;


public interface TrainerDao {

    void create(Trainer trainer);
    Optional<Trainer> findById(Long id);
    List<Trainer> findAll();
    void update(Trainer trainer);
    boolean usernameExists(String username);
    Optional<Trainer> findByUsername(String username);
    List<Trainer> findUnassignedTrainers(String traineeUsername);
}
