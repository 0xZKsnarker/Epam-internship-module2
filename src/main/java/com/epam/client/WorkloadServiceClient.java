package com.epam.client;

import com.epam.dto.client.WorkloadRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "trainer-workload-service", fallback = WorkloadServiceClient.WorkloadServiceFallback.class)
public interface WorkloadServiceClient {

    @PostMapping("/api/workload")
    @CircuitBreaker(name = "workloadService")
    void updateWorkload(@RequestBody WorkloadRequest request);

    @Component
    class WorkloadServiceFallback implements WorkloadServiceClient {
        private static final Logger log = LoggerFactory.getLogger(WorkloadServiceFallback.class);

        @Override
        public void updateWorkload(WorkloadRequest request) {
            log.error("Circuit breaker opened for trainer workload service. Action '{}' for trainer {} was not sent.",
                    request.getActionType(), request.getTrainerUsername());
        }
    }
}