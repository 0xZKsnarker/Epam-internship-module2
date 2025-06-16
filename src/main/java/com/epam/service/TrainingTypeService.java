package com.epam.service;

import com.epam.domain.TrainingType;

import java.util.List;
import java.util.Optional;

public interface TrainingTypeService {
    Optional<TrainingType> findById(Long id);
    List<TrainingType> findAll();
}