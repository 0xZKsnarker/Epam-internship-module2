// src/main/java/com/epam/config/TransactionLoggingFilter.java

package com.epam.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(1)
public class TransactionLoggingFilter implements Filter {

    private static final String TRANSACTION_ID_KEY = "transactionId";
    private static final Logger log = LoggerFactory.getLogger(TransactionLoggingFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        try {
            String transactionId = UUID.randomUUID().toString();
            MDC.put(TRANSACTION_ID_KEY, transactionId);

            if (request instanceof HttpServletRequest) {
                HttpServletRequest httpRequest = (HttpServletRequest) request;
                log.info("Request Started: {} {}", httpRequest.getMethod(), httpRequest.getRequestURI());
            }

            filterChain.doFilter(request, response);

            if (response instanceof HttpServletResponse) {
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                log.info("Request Finished: Status {}", httpResponse.getStatus());
            }

        } finally {
            MDC.remove(TRANSACTION_ID_KEY);
        }
    }
}