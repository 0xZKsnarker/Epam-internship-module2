package com.epam.security;

import com.epam.dao.TraineeDao;
import com.epam.dao.TrainerDao;
import org.springframework.beans.factory.annotation.Autowired;
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

        // CORRECTED: Declare the variable with the correct type: com.epam.domain.User
        com.epam.domain.User domainUser = traineeDao.findByUsername(username)
                .map(t -> t.getUser())
                .orElseGet(() ->
                        trainerDao.findByUsername(username)
                                .map(t -> t.getUser())
                                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username))
                );

        // Now, use the 'domainUser' object to build and return the Spring Security User.
        return new org.springframework.security.core.userdetails.User(
                domainUser.getUsername(),
                domainUser.getPassword(),
                domainUser.isActive(),
                true, true, true,
                java.util.Collections.emptyList()
        );
    }
}