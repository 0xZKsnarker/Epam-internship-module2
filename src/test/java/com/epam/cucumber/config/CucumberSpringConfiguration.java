package com.epam.cucumber.config;

import com.epam.Main;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

@CucumberContextConfiguration
@SpringBootTest(classes = Main.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public class CucumberSpringConfiguration {

    @LocalServerPort
    protected int port;

    @Container
    static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("gym_test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> activeMQContainer = new GenericContainer<>("symptoma/activemq:5.18.0")
            .withExposedPorts(61616, 8161)
            .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(60)));

    static {
        // Start containers before Spring context loads
        mysqlContainer.start();
        activeMQContainer.start();

        // Set system properties for ActiveMQ
        System.setProperty("spring.activemq.broker-url",
                "tcp://" + activeMQContainer.getHost() + ":" + activeMQContainer.getMappedPort(61616));
        System.setProperty("spring.activemq.user", "admin");
        System.setProperty("spring.activemq.password", "admin");
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        // MySQL
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);

        // ActiveMQ - also set here for redundancy
        String activeMqUrl = "tcp://" + activeMQContainer.getHost() + ":" + activeMQContainer.getMappedPort(52280);
        registry.add("spring.activemq.broker-url", () -> activeMqUrl);
        registry.add("spring.activemq.user", () -> "admin");
        registry.add("spring.activemq.password", () -> "admin");

        // Disable connection pooling for tests
        registry.add("spring.activemq.pool.enabled", () -> "false");
        registry.add("spring.jms.cache.enabled", () -> "false");

        // Other properties
        registry.add("eureka.client.enabled", () -> "false");
        registry.add("jwt.secret", () -> "testSecretKeyForJWTTokenGenerationAndValidation123456789012345678901234567890");
    }
}