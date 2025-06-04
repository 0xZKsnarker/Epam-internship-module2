package com.epam.dao;

import com.epam.domain.Trainer;
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
public class TrainerDaoImpl implements TrainerDao {
    private static final Logger log = LoggerFactory.getLogger(TrainerDaoImpl.class);

    private final Map<Long, Trainer> trainerMap;

    @Autowired
    private AtomicLong trainerIdGenerator;

    @Autowired
    public TrainerDaoImpl(@Qualifier("trainerStorage") Map<Long, Trainer> trainerMap) {
        this.trainerMap = trainerMap;
    }

    @Override
    //method for create
    public void create(Trainer trainer) {
        if (trainer.getUserId() == 0) {
            trainer.setUserId(trainerIdGenerator.getAndIncrement());
        }
        trainerMap.put(trainer.getUserId(), trainer);
        log.debug("DAO stored trainer {}", trainer.getUserId());
    }

    @Override
    //method to find trainer by id
    public Optional<Trainer> findById(Long id) {
        Optional<Trainer> result = Optional.ofNullable(trainerMap.get(id));
        log.debug("DAO read trainer {} -> {}", id, result.isPresent() ? "found" : "null");
        return result;
    }

    @Override
    //method to find all
    public List<Trainer> findAll() {
        List<Trainer> list = new ArrayList<>(trainerMap.values());
        log.debug("DAO read all trainers – returned {} record(s)", list.size());
        return list;
    }


    @Override
    //method to update trainer
    public void update(Trainer trainer) {
        trainerMap.put(trainer.getUserId(), trainer);
        log.debug("DAO updated trainer {}", trainer.getUserId());

    }
}
