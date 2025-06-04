package com.epam.dao;

import com.epam.Training;

import java.util.List;
import java.util.Optional;

public interface TrainingDao {
    void create(Training training);
    Optional<Training> findById(Long id);
    List<Training> findAll();
}
