package com.cambiaso.ioc.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
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

    @Bean
    public Map<String, Bucket> rateLimitBuckets() {
        return buckets;
    }

    public Bucket createNewBucket(String key) {
        // Allow 10 requests per minute per user
        Bandwidth limit = Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1)));
        Bucket bucket = Bucket.builder()
                .addLimit(limit)
                .build();
        buckets.put(key, bucket);
        return bucket;
    }

    public Bucket resolveBucket(String key) {
        return buckets.computeIfAbsent(key, this::createNewBucket);
    }
}
