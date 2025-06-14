package com.epam.service;

import com.epam.dao.TrainerDao;
import com.epam.domain.Trainee;
import com.epam.domain.Trainer;
import com.epam.exception.ResourceNotFoundException;
import com.epam.utils.AuthUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TrainerServiceImpl implements TrainerService {

    private static final Logger log = LoggerFactory.getLogger(TrainerServiceImpl.class);
    private TrainerDao trainerDao;

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Override
    public Trainer create(Trainer trainer) {
        String newUsername = AuthUtils.generateUsername(trainer.getUser().getFirstName(), trainer.getUser().getLastName(), trainerDao::usernameExists);
        trainer.getUser().setUsername(newUsername);
        trainer.getUser().setPassword(AuthUtils.randomPassword(10));
        trainer.getUser().setActive(true);

        trainerDao.create(trainer);
        log.info("Created trainer {}", newUsername);
        return trainer;
    }

    @Override
    public Trainer update(Trainer trainer) {
        Optional<Trainer> existingTrainer = trainerDao.findById(trainer.getId());
        if(!existingTrainer.isPresent()){
            throw new ResourceNotFoundException("Trainer with ID " + trainer.getId() + " not found for update.");
        }
        trainerDao.update(trainer);
        log.info("Updated trainer id={}", trainer.getId());
        return trainer;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Trainer> findById(Long id) {
        log.debug("Finding trainer by id: {}", id);
        return trainerDao.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Trainer> findAll() {
        log.debug("Finding all trainers");
        List<Trainer> trainers = trainerDao.findAll();
        log.info("Found {} trainers.", trainers.size());
        return trainers;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Trainer> findByUsername(String username) {
        log.debug("Finding trainer by username: {}", username);
        return trainerDao.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkCredentials(String username, String password) {
        Optional<Trainer> optionalTrainer = trainerDao.findByUsername(username);
        if (optionalTrainer.isPresent()) {
            return optionalTrainer.get().getUser().getPassword().equals(password);
        }
        return false;
    }

    @Override
    public void changePassword(String username, String newPassword) {
        Trainer trainer = trainerDao.findByUsername(username).orElseThrow(() ->
                new ResourceNotFoundException("Trainer " + username + " not found for password change."));
        trainer.getUser().setPassword(newPassword);
        log.info("Changed password for trainer {}", username);
    }

    @Override
    public void activateTrainer(String username, boolean isActive) {
        Trainer trainer = trainerDao.findByUsername(username).orElseThrow(() ->
                new ResourceNotFoundException("Trainer " + username + " not found for activation/deactivation."));
        trainer.getUser().setActive(isActive);
        log.info("Set active status to {} for trainer {}", isActive, username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Trainer> getUnassignedTrainers(String traineeUsername) {
        log.debug("Finding unassigned trainers for trainee: {}", traineeUsername);
        List<Trainer> trainers = trainerDao.findUnassignedTrainers(traineeUsername);
        log.info("Found {} unassigned trainers for trainee '{}'.", trainers.size(), traineeUsername);
        return trainers;
    }
}