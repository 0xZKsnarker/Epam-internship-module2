package com.epam.dao;

import com.epam.Trainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Repository
public class TrainerDaoImpl implements TrainerDao {
    private static final Logger log = LoggerFactory.getLogger(TrainerDaoImpl.class);

    private final Map<Long, Trainer> trainerMap;

    @Autowired
    public TrainerDaoImpl(@Qualifier("trainerStorage") Map<Long, Trainer> trainerMap) {
        this.trainerMap = trainerMap;
    }

    @Override
    public void create(Trainer trainer) {
        trainerMap.put(trainer.getUserId(), trainer);
        log.debug("DAO stored trainer {}", trainer.getUserId());
    }

    @Override
    public Optional<Trainer> findById(Long id) {
        Optional<Trainer> result = Optional.ofNullable(trainerMap.get(id));
        log.debug("DAO read trainer {} -> {}", id, result.isPresent() ? "found" : "null");
        return result;
    }

    @Override
    public List<Trainer> findAll() {
        List<Trainer> list = new ArrayList<>(trainerMap.values());
        log.debug("DAO read all trainers – returned {} record(s)", list.size());
        return list;
    }


    @Override
    public void update(Trainer trainer) {
        trainerMap.put(trainer.getUserId(), trainer);
        log.debug("DAO updated trainer {}", trainer.getUserId());

    }
}
