package com.epam.cucumber.config;

import com.epam.dao.TraineeDao;
import com.epam.dao.TrainerDao;
import com.epam.dao.TrainingTypeDao;
import com.epam.domain.Trainee;
import com.epam.domain.Trainer;
import com.epam.domain.TrainingType;
import com.epam.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;

@TestConfiguration
public class TestDataSetup {

    @Autowired
    private TrainerDao trainerDao;

    @Autowired
    private TraineeDao traineeDao;

    @Autowired
    private TrainingTypeDao trainingTypeDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void setupTestData() {
        // Create a training type first if needed
        TrainingType defaultType = trainingTypeDao.findAll().stream().findFirst()
            .orElseGet(() -> {
                TrainingType type = new TrainingType();
                type.setName("General");
                trainingTypeDao.create(type);
                return type;
            });

        String[] trainerUsernames = {"admin", "integration.trainer", "batch.trainer", "Jane.Doe"};
        
        for (String username : trainerUsernames) {
            if (!trainerDao.findByUsername(username).isPresent()) {
                User user = new User();
                user.setUsername(username);
                user.setPassword(passwordEncoder.encode("admin"));
                user.setFirstName(username.contains(".") ? username.split("\\.")[0] : username);
                user.setLastName(username.contains(".") ? username.split("\\.")[1] : "User");
                user.setActive(true);

                Trainer trainer = new Trainer();
                trainer.setUser(user);
                trainer.setSpecialization(defaultType);

                trainerDao.create(trainer);
            }
        }

        String[] traineeUsernames = {"integration.trainee", "batch.trainee"};
        
        for (String username : traineeUsernames) {
            if (!traineeDao.findByUsername(username).isPresent()) {
                // Create user
                User user = new User();
                user.setUsername(username);
                user.setPassword(passwordEncoder.encode("admin"));
                user.setFirstName(username.contains(".") ? username.split("\\.")[0] : username);
                user.setLastName(username.contains(".") ? username.split("\\.")[1] : "User");
                user.setActive(true);

                // Create trainee with user
                Trainee trainee = new Trainee();
                trainee.setUser(user);

                traineeDao.create(trainee);
            }
        }
    }
}