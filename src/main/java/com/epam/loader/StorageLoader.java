package com.epam.loader;


import com.epam.dao.TraineeDao;
import com.epam.dao.TrainerDao;
import com.epam.dao.TrainingDao;
import com.epam.domain.Trainee;
import com.epam.domain.Trainer;
import com.epam.domain.Training;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.beans.factory.annotation.Qualifier; // No longer needed for maps
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDate;
// import java.util.Map; // No longer directly using Maps here
import java.util.concurrent.atomic.AtomicLong;

@Component
public class StorageLoader implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger log = LoggerFactory.getLogger(StorageLoader.class);

    @Value("${storage.init.trainer}")
    private String trainerFile;
    @Value("${storage.init.trainee}")
    private String traineeFile;
    @Value("${storage.init.training}")
    private String trainingFile;

    // updated, using DAOs instead of map beans directly
    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;
    private final TrainingDao trainingDao;

    @Autowired
    private AtomicLong trainingIdGenerator;

    @Autowired
    public StorageLoader(TraineeDao traineeDao, TrainerDao trainerDao, TrainingDao trainingDao) {
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
        this.trainingDao = trainingDao;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        loadTrainers();
        loadTrainees();
        loadTrainings();
    }


    private void loadTrainers() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(trainerFile)))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank() || line.startsWith("#")) continue;

                String[] p = line.split(",");
                Trainer t = new Trainer(
                        p[0].trim(),// first name
                        p[1].trim(),// last  name
                        p[2].trim(),// username
                        p[3].trim(),// password
                        Boolean.parseBoolean(p[4].trim()),// isActive
                        p[5].trim(),// specialization
                        Long.parseLong(p[6].trim()) // userId
                );

                trainerDao.create(t);
            }
            log.info("Loaded {}", trainerFile);
        } catch (Exception e) {
            log.error("Could not load {}", trainerFile, e);
        }
    }

    private void loadTrainees() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(traineeFile)))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank() || line.startsWith("#")) continue;

                String[] p = line.split(",");
                Trainee t = new Trainee(
                        p[0].trim(), // first
                        p[1].trim(), // last
                        p[2].trim(), // username
                        p[3].trim(), // password
                        Boolean.parseBoolean(p[4].trim()), // isActive
                        LocalDate.parse(p[5].trim()), // dob
                        p[6].trim(), // address
                        Long.parseLong(p[7].trim()) // userId
                );
                traineeDao.create(t);
            }
            log.info("Loaded {}", traineeFile);
        } catch (Exception e) {
            log.error("Could not load {}", traineeFile, e);
        }
    }


    private void loadTrainings() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(trainingFile)))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank() || line.startsWith("#")) continue;

                String[] p = line.split(",");
                Training tr = new Training(
                        Long.parseLong(p[0].trim()), // traineeId
                        Long.parseLong(p[1].trim()), // trainerId
                        p[2].trim(), // training name
                        p[3].trim(), // type
                        LocalDate.parse(p[4].trim()), // date
                        Integer.parseInt(p[5].trim()) // duration
                );
                tr.setId(trainingIdGenerator.getAndIncrement());
                trainingDao.create(tr);
            }
            log.info("Loaded {}", trainingFile);
        } catch (Exception e) {
            log.error("Could not load {}", trainingFile, e);
        }
    }
}