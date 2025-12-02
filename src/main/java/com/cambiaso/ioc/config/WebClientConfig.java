package com.cambiaso.ioc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration for WebClient used to make HTTP calls to external APIs
 * (e.g., Supabase Auth API)
 */
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}

