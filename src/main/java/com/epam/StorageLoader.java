package com.epam;

import com.epam.Trainee;
import com.epam.Trainer;
import com.epam.Training;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class StorageLoader implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger log = LoggerFactory.getLogger(StorageLoader.class);

    @Value("${storage.init.trainer}")  private String trainerFile;
    @Value("${storage.init.trainee}")  private String traineeFile;
    @Value("${storage.init.training}") private String trainingFile;

    @Qualifier("trainerStorage")  @Autowired private Map<Long, Trainer>  trainerMap;
    @Qualifier("traineeStorage")  @Autowired private Map<Long, Trainee>  traineeMap;
    @Qualifier("trainingStorage") @Autowired private Map<Long, Training> trainingMap;

    private static final AtomicLong TRAINING_ID_GEN = new AtomicLong(1);

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        loadTrainers();
        loadTrainees();
        loadTrainings();
    }

    /* ---------------------------------------------------------
       Trainer CSV: first,last,username,password,isActive,specialization,userId
       --------------------------------------------------------- */
    private void loadTrainers() {
        try (BufferedReader br = Files.newBufferedReader(Paths.get(trainerFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank() || line.startsWith("#")) continue;

                String[] p = line.split(",");
                Trainer t = new Trainer(
                        p[0].trim(),                          // first name
                        p[1].trim(),                          // last  name
                        p[2].trim(),                          // username
                        p[3].trim(),                          // password
                        Boolean.parseBoolean(p[4].trim()),    // isActive
                        p[5].trim(),                          // specialization
                        Long.parseLong(p[6].trim())           // userId
                );
                trainerMap.putIfAbsent(t.getUserId(), t);
            }
            log.info("Loaded {}", trainerFile);
        } catch (Exception e) {
            log.error("Could not load {}", trainerFile, e);
        }
    }

    /* ---------------------------------------------------------
       Trainee CSV: first,last,username,password,isActive,dob,address,userId
       dob format: yyyy-MM-dd
       --------------------------------------------------------- */
    private void loadTrainees() {
        try (BufferedReader br = Files.newBufferedReader(Paths.get(traineeFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank() || line.startsWith("#")) continue;

                String[] p = line.split(",");
                Trainee t = new Trainee(
                        p[0].trim(),                          // first
                        p[1].trim(),                          // last
                        p[2].trim(),                          // username
                        p[3].trim(),                          // password
                        Boolean.parseBoolean(p[4].trim()),    // isActive
                        LocalDate.parse(p[5].trim()),         // dob
                        p[6].trim(),                          // address
                        Long.parseLong(p[7].trim())           // userId
                );
                traineeMap.putIfAbsent(t.getUserId(), t);
            }
            log.info("Loaded {}", traineeFile);
        } catch (Exception e) {
            log.error("Could not load {}", traineeFile, e);
        }
    }

    /* ---------------------------------------------------------
       Training CSV: traineeId,trainerId,name,type,date,duration
       date format: yyyy-MM-dd
       --------------------------------------------------------- */
    private void loadTrainings() {
        try (BufferedReader br = Files.newBufferedReader(Paths.get(trainingFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank() || line.startsWith("#")) continue;

                String[] p = line.split(",");
                Training tr = new Training(
                        Long.parseLong(p[0].trim()),          // traineeId
                        Long.parseLong(p[1].trim()),          // trainerId
                        p[2].trim(),                          // training name
                        p[3].trim(),                          // type
                        LocalDate.parse(p[4].trim()),         // date
                        Integer.parseInt(p[5].trim())         // duration
                );
                tr.setId(TRAINING_ID_GEN.getAndIncrement());
                trainingMap.put(tr.getId(), tr);
            }
            log.info("Loaded {}", trainingFile);
        } catch (Exception e) {
            log.error("Could not load {}", trainingFile, e);
        }
    }
}
