package com.epam.service;

import com.epam.Trainee;
import com.epam.dao.TraineeDao;
import com.epam.utils.AuthUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TraineeServiceImpl implements TraineeService {

    private static final Logger log = LoggerFactory.getLogger(TraineeServiceImpl.class);
    private TraineeDao traineeDao;

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Override
    public Trainee create(Trainee trainee) {
        trainee.setUsername(AuthUtils.generateUsername(trainee.getFirstName(), trainee.getLastName(), this::usernameExists));
        trainee.setPassword(AuthUtils.randomPassword(10));
        traineeDao.create(trainee);
        log.info("Created trainee {}", trainee.getUsername());
        return trainee;
    }

    @Override public Trainee update(Trainee trainee){
        traineeDao.update(trainee);
        log.info("Updated trainee id={} username={}", trainee.getUserId(), trainee.getUsername());
        return trainee;
    }

    @Override
    public void delete(Long id) {
        traineeDao.delete(id);
        log.info("Deleted trainee id={}", id);
    }


    @Override
    public Optional<Trainee> findById(Long id) {
        Optional<Trainee> opt = traineeDao.findById(id);
        log.debug("findById({}) -> {}", id, opt.isPresent() ? "found" : "null");
        return opt;
    }

    @Override
    public List<Trainee> findAll() {
        List<Trainee> list = traineeDao.findAll();
        log.debug("findAll() -> {} trainees", list.size());
        return list;
    }

    private boolean usernameExists(String username) {
        for (Trainee tr : traineeDao.findAll()) {
            if (username.equals(tr.getUsername())) {
                return true;
            }
        }
        return false;
    }
}
