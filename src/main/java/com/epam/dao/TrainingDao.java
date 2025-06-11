package com.epam.dao;

import com.epam.domain.Training;
import com.epam.domain.TrainingType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TrainingDao {
    void create(Training training);
    Optional<Training> findById(Long id);
    List<Training> findAll();
    List<Training> findForTraineeByCriteria(String username, LocalDate fromDate, LocalDate toDate, String trainerName, TrainingType type);
    List<Training> findForTrainerByCriteria(String username, LocalDate fromDate, LocalDate toDate, String traineeName);
}
