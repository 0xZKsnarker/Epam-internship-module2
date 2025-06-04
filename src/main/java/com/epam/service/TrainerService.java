package com.epam.service;

import com.epam.Trainer;

import java.util.List;
import java.util.Optional;

public interface TrainerService {
    Trainer create(Trainer trainer);
    Trainer update(Trainer trainer);
    Optional<Trainer> findById(Long id);
    List<Trainer>     findAll();
}
