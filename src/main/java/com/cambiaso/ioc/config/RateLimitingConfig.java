package com.cambiaso.ioc.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@EnableCaching
public class RateLimitingConfig {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Value("${rate-limit.requests-per-minute:60}")
    private int requestsPerMinute;

    @Bean
    public Map<String, Bucket> rateLimitBuckets() {
        return buckets;
    }

    // ✅ CORRECCIÓN: Solo crear el bucket, NO hacer put() - evita recursión
    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.classic(
            requestsPerMinute,
            Refill.intervally(requestsPerMinute, Duration.ofMinutes(1))
        );

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    // ✅ CORRECCIÓN: Usar lambda en lugar de method reference para evitar recursión
    public Bucket resolveBucket(String key) {
        return buckets.computeIfAbsent(key, k -> createNewBucket());
    }
}
