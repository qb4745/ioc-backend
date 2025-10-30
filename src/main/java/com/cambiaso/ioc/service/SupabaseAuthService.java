package com.cambiaso.ioc.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;
import java.util.UUID;

/**
 * Service for interacting with Supabase Auth Admin API
 * This allows the backend to create users automatically without requiring
 * the admin to manually create accounts in Supabase dashboard
 */
@Service
@Slf4j
public class SupabaseAuthService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service-role-key}")
    private String serviceRoleKey;

    private final WebClient.Builder webClientBuilder;

    // Explicit constructor to ensure Spring-based injection is recognized by IDE and runtime
    public SupabaseAuthService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    /**
     * Creates a user in Supabase Auth using the Admin API
     *
     * @param email Email of the user
     * @param password Temporary password for the user
     * @return UUID of the created user in Supabase Auth
     * @throws RuntimeException if user creation fails
     */
    public UUID createSupabaseUser(String email, String password) {
        log.info("Creating Supabase user for email: {}", email);

        WebClient webClient = webClientBuilder
            .baseUrl(supabaseUrl)
            .defaultHeader("apikey", serviceRoleKey)
            .defaultHeader("Authorization", "Bearer " + serviceRoleKey)
            .defaultHeader("Content-Type", "application/json")
            .build();

        Map<String, Object> requestBody = Map.of(
            "email", email,
            "password", password,
            "email_confirm", true,  // Auto-confirm email
            "app_metadata", Map.of("role", "user")
        );

        try {
            Map<String, Object> response = webClient.post()
                .uri("/auth/v1/admin/users")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            if (response != null && response.containsKey("id")) {
                UUID userId = UUID.fromString(response.get("id").toString());
                log.info("Successfully created Supabase user with ID: {}", userId);
                return userId;
            }

            log.error("Failed to create Supabase user: No ID returned in response");
            throw new RuntimeException("Failed to create Supabase user: No ID returned");
        } catch (WebClientResponseException e) {
            log.error("Error creating Supabase user: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Error creating Supabase user: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error creating Supabase user", e);
            throw new RuntimeException("Error creating Supabase user: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a user from Supabase Auth (for rollback purposes)
     *
     * @param userId UUID of the user to delete
     */
    public void deleteSupabaseUser(UUID userId) {
        log.info("Attempting to delete Supabase user: {}", userId);

        WebClient webClient = webClientBuilder
            .baseUrl(supabaseUrl)
            .defaultHeader("apikey", serviceRoleKey)
            .defaultHeader("Authorization", "Bearer " + serviceRoleKey)
            .build();

        try {
            webClient.delete()
                .uri("/auth/v1/admin/users/" + userId)
                .retrieve()
                .bodyToMono(Void.class)
                .block();

            log.info("Successfully deleted Supabase user: {}", userId);
        } catch (Exception e) {
            // Log but don't fail - the user is already in an inconsistent state
            log.error("Failed to delete Supabase user {} during rollback: {}", userId, e.getMessage());
        }
    }
}
