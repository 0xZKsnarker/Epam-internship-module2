package com.epam.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class DatabaseHealth implements HealthIndicator {


    private final DataSource dataSource;

    @Autowired
    public DatabaseHealth(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(1)) {
                return Health.up().withDetail("database", "Service is running").build();
            } else {
                return Health.down().withDetail("database", "Connection validation failed").build();
            }
        } catch (SQLException e) {
            return Health.down(e).withDetail("database", "Service is unavailable").build();
        }
    }
}