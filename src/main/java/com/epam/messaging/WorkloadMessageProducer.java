package com.epam.messaging;

import com.epam.config.JmsConfig;
import com.epam.dto.client.WorkloadRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@Component
public class WorkloadMessageProducer {

    @Autowired
    private JmsTemplate jmsTemplate;

    public void sendWorkloadUpdate(WorkloadRequest request) {
        String transactionId = MDC.get("transactionId");
        String authToken = extractAuthToken();

        log.info("Sending workload update to queue for trainer: {} with transaction ID: {}",
                request.getTrainerUsername(), transactionId);

        jmsTemplate.convertAndSend(JmsConfig.WORKLOAD_QUEUE, request, message -> {
            if (transactionId != null) {
                message.setStringProperty("transactionId", transactionId);
            }
            if (authToken != null) {
                message.setStringProperty("Authorization", authToken);
            }
            message.setStringProperty("actionType", request.getActionType());
            return message;
        });

        log.info("Message sent successfully to queue");
    }

    private String extractAuthToken() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return request.getHeader("Authorization");
        }
        return null;
    }
}