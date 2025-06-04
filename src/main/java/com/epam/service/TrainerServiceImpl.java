package com.epam.service;

import com.epam.domain.Trainer;
import com.epam.dao.TrainerDao;
import com.epam.exception.ResourceNotFoundException; // Import the new exception
import com.epam.utils.AuthUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TrainerServiceImpl implements TrainerService {

    private static final Logger log = LoggerFactory.getLogger(TrainerServiceImpl.class);
    private TrainerDao trainerDao;

    // Sets the TrainerDao dependency via setter-based autowiring
    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    // Creates a new Trainer profile generates a unique username and a random password
    @Override
    public Trainer create(Trainer trainer) {
        trainer.setUsername(AuthUtils.generateUsername(trainer.getFirstName(), trainer.getLastName(), this::usernameExists));
        trainer.setPassword(AuthUtils.randomPassword(10));
        trainerDao.create(trainer);
        log.info("Created trainer {}", trainer.getUsername());
        return trainer;
    }

    // Updates an existing trainer profile throws an exception if not found
    @Override
    public Trainer update(Trainer trainer){
        // Check if trainer exists before updating
        Optional<Trainer> existingTrainer = trainerDao.findById(trainer.getUserId());
        if (existingTrainer.isEmpty()) {
            log.warn("Attempted to update non-existent trainer id={}", trainer.getUserId());
            throw new ResourceNotFoundException("Trainer with ID " + trainer.getUserId() + " not found for update.");
        }
        trainerDao.update(trainer);
        log.info("Updated trainer id={} username={}", trainer.getUserId(), trainer.getUsername());
        return trainer;
    }

    // Finds a Trainer profile by their id
    @Override
    public Optional<Trainer> findById(Long id) {
        Optional<Trainer> opt = trainerDao.findById(id);
        log.debug("findById({}) -> {}", id, opt.isPresent() ? "found" : "null");
        return opt;
    }

    // Retrieves a list of all Trainer profiles
    @Override
    public List<Trainer> findAll() {
        List<Trainer> list = trainerDao.findAll();
        log.debug("findAll() -> {} trainers", list.size());
        return list;
    }

    // Checks if a username already exists in the system
    private boolean usernameExists(String username) {
        for (Trainer tr : trainerDao.findAll()) {
            if (username.equals(tr.getUsername())) {
                return true;
            }
        }
        return false;
    }
}