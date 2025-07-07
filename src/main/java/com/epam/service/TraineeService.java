package com.epam.service;

import com.epam.domain.Trainee;

import java.util.List;
import java.util.Optional;

public interface TraineeService {

    Trainee create(Trainee trainee);
    Trainee update(Trainee trainee);
    void delete(Long id);
    Optional<Trainee> findById(Long id);
    List<Trainee> findAll();
    Optional<Trainee> findByUsername(String username);
    void deleteByUsername(String username);
    void changePassword(String username, String newPassword);
    void activateTrainee(String username, boolean isActive);
    Trainee updateTrainers(String traineeUsername, List<String> trainerUsernames);

}
