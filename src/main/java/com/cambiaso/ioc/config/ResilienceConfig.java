package com.cambiaso.ioc.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class ResilienceConfig {
    // Circuit breaker configuration will be handled by application.properties
    // and @CircuitBreaker annotations on methods
}
