package com.epam.service;

import com.epam.dao.TraineeDao;
import com.epam.dao.TrainerDao;
import com.epam.domain.Trainer;
import com.epam.domain.User;
import com.epam.exception.ResourceNotFoundException;
import com.epam.utils.AuthUtils;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TrainerServiceImpl implements TrainerService {

    private static final Logger log = LoggerFactory.getLogger(TrainerServiceImpl.class);
    private TrainerDao trainerDao;
    private TraineeDao traineeDao;
    private MeterRegistry meterRegistry;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public TrainerServiceImpl(TrainerDao trainerDao, TraineeDao traineeDao, MeterRegistry meterRegistry, PasswordEncoder passwordEncoder) {
        this.trainerDao = trainerDao;
        this.traineeDao = traineeDao;
        this.meterRegistry = meterRegistry;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    @Transactional
    public Trainer create(Trainer trainer) {
        log.info("Creating trainer");
        User user = trainer.getUser();

        boolean traineeExists = traineeDao.findAll().stream()
                .anyMatch(existingTrainee -> existingTrainee.getUser().getFirstName().equalsIgnoreCase(user.getFirstName()) &&
                        existingTrainee.getUser().getLastName().equalsIgnoreCase(user.getLastName()));

        if (traineeExists) {
            log.error("A user with this name already exists as a Trainee.");
            throw new IllegalStateException("A user with name " + user.getFirstName() + " " + user.getLastName() + " already exists as a Trainee.");
        }

        String password = AuthUtils.randomPassword(10);
        user.setPassword(passwordEncoder.encode(password));
        user.setActive(true);
        user.setUsername(AuthUtils.generateUsername(user.getFirstName(), user.getLastName(), this::UsernameExists));
        trainer.setUser(user);

        trainerDao.create(trainer);
        meterRegistry.counter("gym.users.created", "type", "trainer").increment();

        log.info("Created trainer with username {}", user.getUsername());

        trainer.getUser().setPassword(password);
        return trainer;
    }
    @Override
    public Trainer update(Trainer trainer) {
        Optional<Trainer> existingTrainer = trainerDao.findById(trainer.getId());
        if(!existingTrainer.isPresent()){
            throw new ResourceNotFoundException("Trainer with ID " + trainer.getId() + " not found for update.");
        }
        trainerDao.update(trainer);
        meterRegistry.counter("gym.users.updated", "type", "trainer").increment();
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
    public void changePassword(String username, String newPassword) {
        Trainer trainer = trainerDao.findByUsername(username).orElseThrow(() ->
                new ResourceNotFoundException("Trainer " + username + " not found for password change."));
        trainer.getUser().setPassword(passwordEncoder.encode(newPassword));
        trainerDao.update(trainer);
        log.info("Changed password for trainer {}", username);
    }

    @Override
    public void activateTrainer(String username, boolean isActive) {
        Trainer trainer = trainerDao.findByUsername(username).orElseThrow(() ->
                new ResourceNotFoundException("Trainer " + username + " not found for activation/deactivation."));
        trainer.getUser().setActive(isActive);
        trainerDao.update(trainer);
        meterRegistry.counter("gym.users.activation.change", "type", "trainer", "status", String.valueOf(isActive)).increment();
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

    private boolean UsernameExists(String username) {
        return trainerDao.findByUsername(username).isPresent();
    }
}

