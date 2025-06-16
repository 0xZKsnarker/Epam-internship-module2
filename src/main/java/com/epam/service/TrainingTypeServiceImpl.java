package com.epam.service;

import com.epam.dao.TrainingTypeDao;
import com.epam.domain.TrainingType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TrainingTypeServiceImpl implements TrainingTypeService {

    private final TrainingTypeDao trainingTypeDao;

    public TrainingTypeServiceImpl(TrainingTypeDao trainingTypeDao) {
        this.trainingTypeDao = trainingTypeDao;
    }

    @Override
    public Optional<TrainingType> findById(Long id) {
        return trainingTypeDao.findById(id);
    }

    @Override
    public List<TrainingType> findAll() {
        return trainingTypeDao.findAll();
    }
}