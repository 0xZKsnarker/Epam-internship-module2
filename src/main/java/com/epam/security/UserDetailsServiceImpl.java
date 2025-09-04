package com.epam.security;

import com.epam.dao.TraineeDao;
import com.epam.dao.TrainerDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;
    private final LoginAttemptCounterService loginAttemptCounterService;

    @Autowired
    public UserDetailsServiceImpl(TraineeDao traineeDao, TrainerDao trainerDao, LoginAttemptCounterService loginAttemptCounterService) {
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
        this.loginAttemptCounterService = loginAttemptCounterService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (loginAttemptCounterService.isBlocked(username)) {
            throw new RuntimeException("Blocked for too many attempts");
        }

        // Determine if the user is a trainee or a trainer
        return traineeDao.findByUsername(username)
                .map(trainee -> createUserDetails(trainee.getUser(), "ROLE_TRAINEE"))
                .orElseGet(() -> trainerDao.findByUsername(username)
                        .map(trainer -> createUserDetails(trainer.getUser(), "ROLE_TRAINER"))
                        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username))
                );
    }

    private UserDetails createUserDetails(com.epam.domain.User domainUser, String role) {
        return new org.springframework.security.core.userdetails.User(
                domainUser.getUsername(),
                domainUser.getPassword(),
                domainUser.isActive(),
                true, true, true,
                java.util.Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }
}