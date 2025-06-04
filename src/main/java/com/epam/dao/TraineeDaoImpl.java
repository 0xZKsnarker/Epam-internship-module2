package com.epam.dao;

import com.epam.Trainee;
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
public class TraineeDaoImpl implements TraineeDao {

    private final Map<Long, Trainee> traineeMap;
    private static final Logger log = LoggerFactory.getLogger(TraineeDaoImpl.class);



    @Autowired
    public TraineeDaoImpl(@Qualifier("traineeStorage") Map<Long, Trainee> traineeMap) {
        this.traineeMap = traineeMap;
    }

    @Override
    public void create(Trainee trainee) {
        traineeMap.put(trainee.getUserId(), trainee);
        log.debug("DAO stored trainee {}", trainee.getUserId());

    }
    @Override
    public Optional<Trainee> findById(Long id) {
        Optional<Trainee> result = Optional.ofNullable(traineeMap.get(id));
        log.debug("DAO read trainee {} -> {}", id, result.isPresent() ? "found" : "null");
        return result;
    }

    @Override
    public List<Trainee> findAll() {
        List<Trainee> list = new ArrayList<>(traineeMap.values());
        log.debug("DAO read all trainees - returned {} record(s)", list.size());
        return list;
    }

    @Override
    public void update(Trainee trainee) {
        traineeMap.put(trainee.getUserId(), trainee);
        log.debug("DAO updated trainee {}", trainee.getUserId());
    }

    @Override
    public void delete(Long id) {
        traineeMap.remove(id);
        log.debug("DAO deleted trainee {}", id);
    }
}
