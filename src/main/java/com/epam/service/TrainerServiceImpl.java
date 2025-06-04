package com.epam.service;

import com.epam.Trainer;
import com.epam.dao.TrainerDao;
import com.epam.utils.AuthUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TrainerServiceImpl implements TrainerService {

    private static final Logger log = LoggerFactory.getLogger(TrainerServiceImpl.class);
    private TrainerDao trainerDao;

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Override
    public Trainer create(Trainer trainer) {
        trainer.setUsername(generateUniqueUsername(trainer.getFirstName(), trainer.getLastName()));
        trainer.setPassword(AuthUtils.randomPassword(10));
        trainerDao.create(trainer);
        log.info("Created trainer {}", trainer.getUsername());
        return trainer;
    }

    @Override 
    public Trainer update(Trainer trainer){
        trainerDao.update(trainer);
        log.info("Updated trainer id={} username={}", trainer.getUserId(), trainer.getUsername());
        return trainer;
    }

    @Override
    public Optional<Trainer> findById(Long id) {
            Optional<Trainer> opt = trainerDao.findById(id);
            log.debug("findById({}) -> {}", id, opt.isPresent() ? "found" : "null");
            return opt;
    }

    @Override
    public List<Trainer> findAll() {
            List<Trainer> list = trainerDao.findAll();
            log.debug("findAll() -> {} trainers", list.size());
            return list;
    }

    private String generateUniqueUsername(String first, String last) {
        String base = (first + "." + last).toLowerCase();
        String candidate = base;
        int suffix = 1;

        while (usernameExists(candidate)) {
            candidate = base + "." + suffix++;
        }
        return candidate;
    }

    private boolean usernameExists(String username) {
        for (Trainer tr : trainerDao.findAll()) {
            if (username.equals(tr.getUsername())) {
                return true;
            }
        }
        return false;
    }
}
