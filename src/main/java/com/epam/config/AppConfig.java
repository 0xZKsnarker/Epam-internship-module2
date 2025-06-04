package com.epam.config;

import com.epam.Trainee;
import com.epam.Trainer;
import com.epam.Training;
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
    public static PropertySourcesPlaceholderConfigurer propertyConfigurer() {
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        // Crucial: Set the location for your properties file
        configurer.setLocation(new ClassPathResource("application.properties"));
        configurer.setIgnoreResourceNotFound(false); // Helps in diagnosing if file is missing
        return configurer;
    }

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

    @Bean
    public AtomicLong trainingIdGenerator() {
        return new AtomicLong(1);
    }
}