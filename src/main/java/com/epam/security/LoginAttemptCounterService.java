package com.epam.security;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptCounterService {

    private final int maxAttempt = 3;
    private final LoadingCache<String, Integer> attemptsCache;


    public LoginAttemptCounterService(){
        super();
        attemptsCache = Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build(key -> 0);
    }

    public void loginSuccessful(String key){
        attemptsCache.invalidate(key);
    }

    public void loginFail(String key){
        int attempts = attemptsCache.get(key);
        attempts++;
        attemptsCache.put(key, attempts);
    }

    public boolean isBlocked(String key){
        return attemptsCache.get(key) >= maxAttempt;
    }

}
