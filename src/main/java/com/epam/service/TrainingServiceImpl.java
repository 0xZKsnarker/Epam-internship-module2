package com.epam.service;

import com.epam.dao.TrainingTypeDao;
import com.epam.domain.Training;
import com.epam.dao.TrainingDao;
import com.epam.domain.TrainingType;
import com.epam.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TrainingServiceImpl implements TrainingService {

    private static final Logger log = LoggerFactory.getLogger(TrainingServiceImpl.class);
    private TrainingDao trainingDao;
    private TrainingTypeDao trainingTypeDao;


    @Autowired
    public void setTrainingDao(TrainingDao trainingDao) {
        this.trainingDao = trainingDao;
    }

    //create a new training
    @Override
    @Transactional
    public Training create(Training training) {
        trainingDao.create(training);
        log.info("Scheduled training {} (id={})",
                training.getTrainingName(), training.getId());
        return training;
    }

    //finds a training by its id
    @Override
    public Optional<Training> findById(Long id) {
        Optional<Training> opt = trainingDao.findById(id);
        log.debug("findById({}) -> {}", id, opt.isPresent() ? "found" : "null");
        return opt;
    }

    //returns a list of all trainings
    @Override
    public List<Training> findAll() {
        List<Training> list = trainingDao.findAll();
        log.debug("findAll() -> {} trainings", list.size());
        return list;
    }


    //gets trainee by criteria
    @Override
    @Transactional
    public List<Training> getTraineeTrainingsByCriteria(String username, LocalDate fromDate, LocalDate toDate, String trainerName, String trainingTypeName) {
        log.debug("Searching trainee trainings for user '{}' with criteria: from={}, to={}, trainerName={}, typeName={}", username, fromDate, toDate, trainerName, trainingTypeName);
        TrainingType trainingType = null;
        if (trainingTypeName != null && !trainingTypeName.isEmpty()) {
            trainingType = trainingTypeDao.findByName(trainingTypeName)
                    .orElseThrow(() -> new ResourceNotFoundException("TrainingType with name " + trainingTypeName + " not found."));
            log.debug("Found trainingType ID: {} for typeName: {}", trainingType.getId(), trainingTypeName);
        }

        List<Training> trainings = trainingDao.findForTraineeByCriteria(username, fromDate, toDate, trainerName, trainingType);
        log.info("Found {} trainings for trainee '{}' matching criteria.", trainings.size(), username);

        return trainings;
    }


    //returns trainings by criteria
    @Override
    @Transactional
    public List<Training> getTrainerTrainingsByCriteria(String username, LocalDate fromDate, LocalDate toDate, String traineeName) {

        log.debug("Searching trainer trainings for user '{}' with criteria: from={}, to={}, traineeName={}",
                username, fromDate, toDate, traineeName);
        List<Training> trainings = trainingDao.findForTrainerByCriteria(username, fromDate, toDate, traineeName);
        log.info("Found {} trainings for trainer '{}' matching criteria.", trainings.size(), username);
        return trainings;
    }
}