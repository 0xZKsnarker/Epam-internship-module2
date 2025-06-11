package com.epam.service;

import com.epam.domain.Training;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TrainingService {

    Training create(Training training);
    Optional<Training> findById(Long id);
    List<Training> findAll();
    List<Training> getTraineeTrainingsByCriteria(String username, LocalDate fromDate, LocalDate toDate, String trainerName, String trainingTypeName);
    List<Training> getTrainerTrainingsByCriteria(String username, LocalDate fromDate, LocalDate toDate, String traineeName);
}
