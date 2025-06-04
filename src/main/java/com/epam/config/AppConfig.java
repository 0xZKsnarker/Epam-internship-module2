package com.epam.config;

import com.epam.domain.Trainee;
import com.epam.domain.Trainer;
import com.epam.domain.Training;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource; // Required for ClassPathResource

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong; // Required for AtomicLong

@Configuration
@ComponentScan(basePackages = "com.epam")
@PropertySource("classpath:application.properties")
public class AppConfig {



    @Bean
    // Configures Spring to load properties from "application.properties" and resolve placeholders
    public static PropertySourcesPlaceholderConfigurer propertyConfigurer() {
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        configurer.setLocation(new ClassPathResource("application.properties"));
        configurer.setIgnoreResourceNotFound(false);
        return configurer;
    }

    //bean for trainee storage
    @Bean(name = "traineeStorage")
    public Map<Long, Trainee> traineeStorage() {
        return new HashMap<>();
    }

    //bean for training storage
    @Bean(name = "trainingStorage")
    public Map<Long, Training> trainingStorage() {
        return new HashMap<>();
    }

    //bean for trainer storage
    @Bean(name = "trainerStorage")
    public Map<Long, Trainer> trainerStorage() {
        return new HashMap<>();
    }

    //global bean for training id
    @Bean
    public AtomicLong trainingIdGenerator() {
        return new AtomicLong(1);
    }

    //global bean for trainee id
    @Bean
    public AtomicLong traineeIdGenerator() {
        return new AtomicLong(4);
    }

    //global bean for trainer id
    @Bean
    public AtomicLong trainerIdGenerator() {
        return new AtomicLong(4);
    }
}