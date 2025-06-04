package com.epam.service;

import com.epam.domain.Training;
import com.epam.dao.TrainingDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;

@Service
public class TrainingServiceImpl implements TrainingService {

    private static final Logger log = LoggerFactory.getLogger(TrainingServiceImpl.class);

    private TrainingDao trainingDao;

    // Sets the TrainingDao dependency via setter-based autowiring
    @Autowired
    public void setTrainingDao(TrainingDao trainingDao) {
        this.trainingDao = trainingDao;
    }

    // Creates a new Training record
    @Override
    public Training create(Training training) {
        trainingDao.create(training);
        log.info("Scheduled training {} (id={})",
                training.getTrainingName(), training.getId());
        return training;
    }

    // Finds a Training record by its id
    @Override
    public Optional<Training> findById(Long id) {
        Optional<Training> opt = trainingDao.findById(id);
        log.debug("findById({}) -> {}", id, opt.isPresent() ? "found" : "null");
        return opt;
    }

    // Retrieves a list of all Training records
    @Override
    public List<Training> findAll() {
        List<Training> list = trainingDao.findAll();
        log.debug("findAll() -> {} trainings", list.size());
        return list;
    }

}