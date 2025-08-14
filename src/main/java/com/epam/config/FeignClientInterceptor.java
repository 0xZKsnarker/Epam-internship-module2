package com.epam.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class FeignClientInterceptor implements RequestInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String TRANSACTION_ID_HEADER = "X-Transaction-ID";
    private static final String TRANSACTION_ID_KEY = "transactionId";

    @Override
    public void apply(RequestTemplate requestTemplate) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                requestTemplate.header(AUTHORIZATION_HEADER, authorizationHeader);
            }
        }

        String transactionId = MDC.get(TRANSACTION_ID_KEY);
        if (transactionId != null) {
            requestTemplate.header(TRANSACTION_ID_HEADER, transactionId);
        }
    }
}