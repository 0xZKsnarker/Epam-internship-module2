package com.epam.service;

import com.epam.dao.TrainingTypeDao;
import com.epam.domain.Training;
import com.epam.dao.TrainingDao;
import com.epam.domain.TrainingType;
import com.epam.exception.ResourceNotFoundException;
import io.micrometer.core.instrument.MeterRegistry; 
import io.micrometer.core.instrument.Timer; 
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
    private MeterRegistry meterRegistry;

    @Autowired
    public TrainingServiceImpl(TrainingDao trainingDao, TrainingTypeDao trainingTypeDao, MeterRegistry meterRegistry) {
        this.trainingDao = trainingDao;
        this.trainingTypeDao = trainingTypeDao;
        this.meterRegistry = meterRegistry;
    }

    @Override
    @Transactional
    public Training create(Training training) {
        trainingDao.create(training);
        meterRegistry.counter("gym.trainings.created", "type", training.getTrainingType().getName()).increment(); 
        log.info("Scheduled training {} (id={})",
                training.getTrainingName(), training.getId());
        return training;
    }

    @Override
    public Optional<Training> findById(Long id) {
        Optional<Training> opt = trainingDao.findById(id);
        log.debug("findById({}) -> {}", id, opt.isPresent() ? "found" : "null");
        return opt;
    }

    @Override
    public List<Training> findAll() {
        List<Training> list = trainingDao.findAll();
        log.debug("findAll() -> {} trainings", list.size());
        return list;
    }

    @Override
    @Transactional
    public List<Training> getTraineeTrainingsByCriteria(String username, LocalDate fromDate, LocalDate toDate, String trainerName, String trainingTypeName) {
        Timer.Sample sample = Timer.start(meterRegistry); 
        try {
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
        } finally {
            sample.stop(meterRegistry.timer("gym.trainings.searches", "search_type", "trainee")); 
        }
    }

    @Override
    @Transactional
    public List<Training> getTrainerTrainingsByCriteria(String username, LocalDate fromDate, LocalDate toDate, String traineeName) {
        Timer.Sample sample = Timer.start(meterRegistry); 
        try {
            log.debug("Searching trainer trainings for user '{}' with criteria: from={}, to={}, traineeName={}",
                    username, fromDate, toDate, traineeName);
            List<Training> trainings = trainingDao.findForTrainerByCriteria(username, fromDate, toDate, traineeName);
            log.info("Found {} trainings for trainer '{}' matching criteria.", trainings.size(), username);
            return trainings;
        } finally {
            sample.stop(meterRegistry.timer("gym.trainings.searches", "search_type", "trainer")); 
        }
    }
}