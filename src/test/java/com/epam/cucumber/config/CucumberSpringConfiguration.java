package com.epam.cucumber.config;

import com.epam.Main;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

@CucumberContextConfiguration
@SpringBootTest(classes = { Main.class, TestJmsOverrides.class, TestDataSetup.class }, // add our test configurations
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public class CucumberSpringConfiguration {

    @LocalServerPort
    protected int port;


    @Container
    static GenericContainer<?> activeMQ = new GenericContainer<>("symptoma/activemq:5.18.0")
            .withExposedPorts(61616, 8161)
            .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(90)));

    static {
        activeMQ.start();
    }

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.activemq.broker-url",
                () -> "tcp://" + activeMQ.getHost() + ":" + activeMQ.getMappedPort(61616));

        registry.add("eureka.client.enabled", () -> "false");
    }
}
