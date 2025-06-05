package com.epam.dao;

import com.epam.domain.Trainee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class TraineeDaoImpl implements TraineeDao {

    private final Map<Long, Trainee> traineeMap;
    private static final Logger log = LoggerFactory.getLogger(TraineeDaoImpl.class);

    // Inject the new ID generator
    @Autowired
    private AtomicLong traineeIdGenerator;

    @Autowired
    public TraineeDaoImpl(@Qualifier("traineeStorage") Map<Long, Trainee> traineeMap) {
        this.traineeMap = traineeMap;
    }

    @Override
    //method to create a new trainee
    public void create(Trainee trainee) {
        if (trainee.getUserId() == 0) { // Generate ID if not provided (e.g., 0L)
            trainee.setUserId(traineeIdGenerator.getAndIncrement());
        }
        traineeMap.put(trainee.getUserId(), trainee);
        log.debug("DAO stored trainee {}", trainee.getUserId());

    }
    @Override
    //method to find by id
    public Optional<Trainee> findById(Long id) {
        Optional<Trainee> result = Optional.ofNullable(traineeMap.get(id));
        log.debug("DAO read trainee {} -> {}", id, result.isPresent() ? "found" : "null");
        return result;
    }

    @Override
    //method to find all
    public List<Trainee> findAll() {
        List<Trainee> list = new ArrayList<>(traineeMap.values());
        log.debug("DAO read all trainees - returned {} record(s)", list.size());
        return list;
    }

    @Override
    //update method
    public void update(Trainee trainee) {
        traineeMap.put(trainee.getUserId(), trainee);
        log.debug("DAO updated trainee {}", trainee.getUserId());
    }

    @Override
    //delete method
    public void delete(Long id) {
        traineeMap.remove(id);
        log.debug("DAO deleted trainee {}", id);
    }

    @Override
    public boolean usernameExists(String username) {
        for (Trainee trainee : traineeMap.values()) {
            if (trainee.getUsername() != null && trainee.getUsername().equals(username)) {
                log.debug("DAO usernameExists check for '{}': true", username);
                return true;
            }
        }
        log.debug("DAO usernameExists check for '{}': false", username);
        return false;
    }
}