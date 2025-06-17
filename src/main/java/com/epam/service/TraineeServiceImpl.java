package com.epam.service;

import com.epam.dao.TraineeDao;
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

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Override
    public Trainee create(Trainee trainee) {
        String newUsername = AuthUtils.generateUsername(
                trainee.getUser().getFirstName(),
                trainee.getUser().getLastName(),
                traineeDao::usernameExists
        );
        trainee.getUser().setUsername(newUsername);
        trainee.getUser().setPassword(AuthUtils.randomPassword(10));
        trainee.getUser().setActive(true);

        traineeDao.create(trainee);
        log.info("Created trainee {}", newUsername);
        return trainee;
    }

    @Override
    public Trainee update(Trainee trainee) {
        Optional<Trainee> existingTrainee = traineeDao.findById(trainee.getId());
        if (!existingTrainee.isPresent()) {
            throw new ResourceNotFoundException("Trainee with ID " + trainee.getId() + " not found for update.");
        }
        traineeDao.update(trainee);
        log.info("Updated trainee id={}", trainee.getId());
        return trainee;
    }

    @Override
    public void delete(Long id) {
        traineeDao.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Trainee with ID " + id + " not found for deletion."));
        traineeDao.delete(id);
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
    @Transactional(readOnly = true)
    public boolean checkCredentials(String username, String password) {
        Optional<Trainee> optionalTrainee = traineeDao.findByUsername(username);
        if (optionalTrainee.isPresent()) {
            return optionalTrainee.get().getUser().getPassword().equals(password);
        }
        return false;
    }

    @Override
    public void changePassword(String username, String newPassword) {
        Trainee trainee = traineeDao.findByUsername(username).orElseThrow(() ->
                new ResourceNotFoundException("Trainee " + username + " not found for password change."));
        trainee.getUser().setPassword(newPassword);
        log.info("Changed password for trainee {}", username);
    }

    @Override
    public void activateTrainee(String username, boolean isActive) {
        Trainee trainee = traineeDao.findByUsername(username).orElseThrow(() ->
                new ResourceNotFoundException("Trainee " + username + " not found for activation/deactivation."));
        trainee.getUser().setActive(isActive);
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
        log.info("Updated trainer list for trainee {}", traineeUsername);
        return trainee;
    }


}