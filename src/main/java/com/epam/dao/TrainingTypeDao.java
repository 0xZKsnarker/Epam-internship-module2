package com.epam.dao;

import com.epam.domain.TrainingType;
import java.util.Optional;

public interface TrainingTypeDao {

    Optional<TrainingType> findByName(String name);
    void create(TrainingType trainingType);
}