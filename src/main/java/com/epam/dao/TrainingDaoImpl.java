package com.epam.dao;

import com.epam.domain.Training;

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
public class TrainingDaoImpl implements TrainingDao {
    private static final Logger log = LoggerFactory.getLogger(TrainingDaoImpl.class);

    private final Map<Long, Training> trainingMap;

    @Autowired
    private AtomicLong trainingIdGenerator;

    @Autowired
    public TrainingDaoImpl(@Qualifier("trainingStorage") Map<Long, Training> trainingMap) {
        this.trainingMap = trainingMap;
    }

    @Override
    //method to create a new training
    public void create(Training training) {
        if (training.getId() == 0) {
            training.setId(trainingIdGenerator.getAndIncrement());
        }
        trainingMap.put(training.getId(), training);
        log.debug("DAO stored training {}", training.getId());

    }
    @Override
    //method to find training by id
    public Optional<Training> findById(Long id) {
        Optional<Training> result = Optional.ofNullable(trainingMap.get(id));
        log.debug("DAO read training {} -> {}", id, result.isPresent() ? "found" : "null");
        return result;
    }

    @Override
    //method to find all
    public List<Training> findAll() {
        List<Training> list = new ArrayList<>(trainingMap.values());
        log.debug("DAO read all trainings – returned {} record(s)", list.size());
        return list;
    }
}