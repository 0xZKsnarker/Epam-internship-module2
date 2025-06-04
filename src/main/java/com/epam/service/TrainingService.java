package com.epam.service;

import com.epam.domain.Training;

import java.util.List;
import java.util.Optional;

public interface TrainingService {

    Training create(Training training);
    Optional<Training> findById(Long id);
    List<Training> findAll();
}
