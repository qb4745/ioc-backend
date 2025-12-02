package com.cambiaso.ioc.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * CacheManager con configuración de múltiples cachés.
     * - aiExplanationsHistorical: 24 horas (datos históricos)
     * - aiExplanationsCurrent: 30 minutos (datos actuales/futuros)
     * - dashboardAccess: 60 segundos (decisiones de autorización)
     * - dashboardTokens: 8 minutos (tokens JWT de Metabase)
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // Cache para datos históricos (24 horas)
        cacheManager.registerCustomCache("aiExplanationsHistorical",
            Caffeine.newBuilder()
                .maximumSize(5000)
                .expireAfterWrite(24, TimeUnit.HOURS)
                .recordStats()
                .build());

        // Cache para datos actuales (30 minutos)
        cacheManager.registerCustomCache("aiExplanationsCurrent",
            Caffeine.newBuilder()
                .maximumSize(5000)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .recordStats()
                .build());

        // Cache para verificación de acceso a dashboards (60 segundos)
        // R4: Cache breve para decisiones de autorización
        cacheManager.registerCustomCache("dashboardAccess",
            Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(60, TimeUnit.SECONDS)
                .recordStats()
                .build());

        // Cache para tokens JWT de Metabase (8 minutos)
        // TTL menor que la expiración del JWT (10 min) para evitar servir tokens expirados
        // Política: Regenerar token 2 minutos antes de la expiración para evitar race conditions
        cacheManager.registerCustomCache("dashboardTokens",
            Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(8, TimeUnit.MINUTES)
                .recordStats()
                .build());

        return cacheManager;
    }
}
