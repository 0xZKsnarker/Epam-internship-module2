package com.epam.config;

import com.epam.Trainee;
import com.epam.Trainer;
import com.epam.Training;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class AppConfig {

    @Bean(name = "traineeStorage")
    public Map<Long, Trainee> traineeStorage() {
        return new HashMap<>();
    }

    @Bean(name = "trainingStorage")
    public Map<Long, Training> trainingStorage() {
        return new HashMap<>();
    }

    @Bean(name = "trainerStorage")
    public Map<Long, Trainer> trainerStorage() {
        return new HashMap<>();
    }


}
