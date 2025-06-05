package com.epam.service;

import com.epam.domain.Trainee;
import com.epam.dao.TraineeDao;
import com.epam.exception.ResourceNotFoundException;
import com.epam.utils.AuthUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TraineeServiceImpl implements TraineeService {

    private static final Logger log = LoggerFactory.getLogger(TraineeServiceImpl.class);
    private TraineeDao traineeDao;

    //Sets the TraineeDao dependency via setter-based autowiring.
    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    //Creates a new Trainee profile generates a unique username and a random password
    @Override
    public Trainee create(Trainee trainee) {
        trainee.setUsername(AuthUtils.generateUsername(trainee.getFirstName(), trainee.getLastName(), traineeDao::usernameExists));
        trainee.setPassword(AuthUtils.randomPassword(10));
        traineeDao.create(trainee);
        log.info("Created trainee {}", trainee.getUsername());
        return trainee;
    }

    //Updates an existing Trainee profile throws an exception if not found
    @Override
    public Trainee update(Trainee trainee){
        Optional<Trainee> existingTrainee = traineeDao.findById(trainee.getUserId());
        if (existingTrainee.isEmpty()) {
            log.warn("Attempted to update non-existent trainee id={}", trainee.getUserId());
            throw new ResourceNotFoundException("Trainee with ID " + trainee.getUserId() + " not found for update.");
        }
        traineeDao.update(trainee);
        log.info("Updated trainee id={} username={}", trainee.getUserId(), trainee.getUsername());
        return trainee;
    }

    //deletes a Trainee profile by their id throws an exception if not found
    @Override
    public void delete(Long id) {
        Optional<Trainee> opt = traineeDao.findById(id);
        if (opt.isEmpty()) {
            log.warn("Attempted to delete non-existent trainee id={}", id);
            throw new ResourceNotFoundException("Trainee with ID " + id + " not found for deletion.");
        }
        traineeDao.delete(id);
        log.info("Deleted trainee id={}", id);
    }

    //finds a Trainee profile by their id
    @Override
    public Optional<Trainee> findById(Long id) {
        Optional<Trainee> opt = traineeDao.findById(id);
        log.debug("findById({}) -> {}", id, opt.isPresent() ? "found" : "null");
        return opt;
    }

    //retrieves a list of all trainee profiles.
    @Override
    public List<Trainee> findAll() {
        List<Trainee> list = traineeDao.findAll();
        log.debug("findAll() -> {} trainees", list.size());
        return list;
    }
}