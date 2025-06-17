package com.epam.dao;

import com.epam.domain.Training;
import com.epam.domain.TrainingType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TrainingTypeDao {

    Optional<TrainingType> findByName(String name);
    void create(TrainingType trainingType);
    Optional<TrainingType> findById(Long id);
    List<TrainingType> findAll();
}