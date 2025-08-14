package com.epam.service;

import com.epam.dao.TraineeDao;
import com.epam.dao.TrainerDao;
import com.epam.domain.Trainee;
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class TraineeServiceImpl implements TraineeService {

    private static final Logger log = LoggerFactory.getLogger(TraineeServiceImpl.class);
    private TraineeDao traineeDao;
    private TrainerDao trainerDao;
    private MeterRegistry meterRegistry;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public TraineeServiceImpl(TraineeDao traineeDao, TrainerDao trainerDao, MeterRegistry meterRegistry, PasswordEncoder passwordEncoder) {
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
        this.meterRegistry = meterRegistry;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Trainee create(Trainee trainee) {
        log.info("Creating trainee");
        User user = trainee.getUser();

        boolean trainerExists = trainerDao.findAll().stream()
                .anyMatch(existingTrainer -> existingTrainer.getUser().getFirstName().equalsIgnoreCase(user.getFirstName()) &&
                        existingTrainer.getUser().getLastName().equalsIgnoreCase(user.getLastName()));

        if (trainerExists) {
            log.error("A user with this name already exists as a Trainer.");
            throw new IllegalStateException("A user with name " + user.getFirstName() + " " + user.getLastName() + " already exists as a Trainer.");
        }

        String rawPassword = AuthUtils.randomPassword(10);  // Keep the raw password
        String generatedUsername = AuthUtils.generateUsername(user.getFirstName(), user.getLastName(), this::UsernameExists);

        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setActive(true);
        user.setUsername(generatedUsername);
        trainee.setUser(user);

        traineeDao.create(trainee);
        meterRegistry.counter("gym.users.created", "type", "trainee").increment();

        log.info("Created trainee with username {}", user.getUsername());

        // Create a detached User object with raw password for response
        User responseUser = new User();
        responseUser.setUsername(generatedUsername);
        responseUser.setPassword(rawPassword);  // Raw password for response
        responseUser.setFirstName(user.getFirstName());
        responseUser.setLastName(user.getLastName());

        Trainee responseTrainee = new Trainee();
        responseTrainee.setUser(responseUser);
        responseTrainee.setId(trainee.getId());

        return responseTrainee;
    }

    @Override
    public Trainee update(Trainee trainee) {
        Optional<Trainee> existingTrainee = traineeDao.findById(trainee.getId());
        if (!existingTrainee.isPresent()) {
            throw new ResourceNotFoundException("Trainee with ID " + trainee.getId() + " not found for update.");
        }
        traineeDao.update(trainee);
        meterRegistry.counter("gym.users.updated", "type", "trainee").increment();  
        log.info("Updated trainee id={}", trainee.getId());
        return trainee;
    }

    @Override
    public void delete(Long id) {
        traineeDao.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Trainee with ID " + id + " not found for deletion."));
        traineeDao.delete(id);
        meterRegistry.counter("gym.users.deleted", "type", "trainee").increment();  
        log.info("Deleted trainee id={}", id);
    }

    @Override
    public void deleteByUsername(String username) {
        Trainee trainee = findByUsername(username).orElseThrow(() ->
                new ResourceNotFoundException("Trainee with username " + username + " not found for deletion."));
        delete(trainee.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Trainee> findById(Long id) {
        return traineeDao.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Trainee> findAll() {
        return traineeDao.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Trainee> findByUsername(String username) {
        return traineeDao.findByUsername(username);
    }


    @Override
    public void changePassword(String username, String newPassword) {
        Trainee trainee = traineeDao.findByUsername(username).orElseThrow(() ->
                new ResourceNotFoundException("Trainee " + username + " not found for password change."));
        trainee.getUser().setPassword(passwordEncoder.encode(newPassword));
        traineeDao.update(trainee);
        log.info("Changed password for trainee {}", username);
    }

    @Override
    public void activateTrainee(String username, boolean isActive) {
        Trainee trainee = traineeDao.findByUsername(username).orElseThrow(() ->
                new ResourceNotFoundException("Trainee " + username + " not found for activation/deactivation."));
        trainee.getUser().setActive(isActive);
        traineeDao.update(trainee);
        meterRegistry.counter("gym.users.activation.change", "type", "trainee", "status", String.valueOf(isActive)).increment();  
        log.info("Set active status to {} for trainee {}", isActive, username);
    }

    @Override
    public Trainee updateTrainers(String traineeUsername, List<String> trainerUsernames) {
        Trainee trainee = traineeDao.findByUsername(traineeUsername).orElseThrow(() ->
                new ResourceNotFoundException("Trainee " + traineeUsername + " not found."));

        Set<Trainer> newTrainers = new HashSet<>();
        for (String trainerUsername : trainerUsernames) {
            Trainer trainer = trainerDao.findByUsername(trainerUsername).orElseThrow(() ->
                    new ResourceNotFoundException("Trainer " + trainerUsername + " not found while updating trainee's trainers."));
            newTrainers.add(trainer);
        }

        trainee.setTrainers(newTrainers);
        traineeDao.update(trainee);
        meterRegistry.counter("gym.trainee.trainers.updated", "trainee", traineeUsername).increment();
        log.info("Updated trainer list for trainee {}", traineeUsername);
        return trainee;
    }

    private boolean UsernameExists(String username) {
        return traineeDao.findByUsername(username).isPresent();
    }
}