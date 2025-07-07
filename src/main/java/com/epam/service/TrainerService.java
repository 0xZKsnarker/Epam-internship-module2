package com.epam.service;

import com.epam.domain.Trainer;

import java.util.List;
import java.util.Optional;

public interface TrainerService {
    Trainer create(Trainer trainer);
    Trainer update(Trainer trainer);
    Optional<Trainer> findById(Long id);
    List<Trainer>     findAll();
    Optional<Trainer> findByUsername(String username);
    void changePassword(String username, String newPassword);
    void activateTrainer(String username, boolean isActive);
    List<Trainer> getUnassignedTrainers(String traineeUsername);
}
