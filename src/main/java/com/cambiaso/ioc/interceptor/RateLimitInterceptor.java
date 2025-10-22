package com.cambiaso.ioc.interceptor;

import com.cambiaso.ioc.config.RateLimitingConfig;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    // make the config optional for test slices by using ObjectProvider
    private final ObjectProvider<RateLimitingConfig> rateLimitingConfigProvider;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        // Only apply rate limiting to ETL endpoints
        if (!request.getRequestURI().startsWith("/api/etl/")) {
            return true;
        }

        // obtain the actual config bean if present
        RateLimitingConfig rateLimitingConfig = rateLimitingConfigProvider.getIfAvailable();
        if (rateLimitingConfig == null) {
            // No rate limiting configured in this context (e.g. lightweight test slice) - allow through
            log.debug("RateLimitingConfig bean not present; skipping rate limit checks");
            return true;
        }

        String userId = getUserId();
        if (userId == null) {
            userId = request.getRemoteAddr(); // Fallback to IP address
        }

        Bucket bucket = rateLimitingConfig.resolveBucket(userId);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            // RFC 9231 standard rate limit headers
            response.setHeader("RateLimit-Limit", "10"); // align with your bucket config
            response.setHeader("RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));
            log.debug("Rate limit check passed for user: {} (remaining: {})", userId, probe.getRemainingTokens());
            return true;
        } else {
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;

            // Set standard HTTP headers
            response.setHeader("Retry-After", String.valueOf(waitForRefill));
            response.setHeader("RateLimit-Limit", "10");
            response.setHeader("RateLimit-Remaining", "0");
            response.setHeader("RateLimit-Reset", String.valueOf(waitForRefill));
            response.setHeader("Content-Type", "application/json");

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());

            // Write JSON response
            String jsonResponse = String.format(
                "{\"error\":\"Rate limit exceeded\",\"message\":\"Try again in %d seconds\",\"retryAfter\":%d}",
                waitForRefill, waitForRefill
            );
            response.getWriter().write(jsonResponse);
            response.getWriter().flush();

            log.warn("Rate limit exceeded for user: {} (retry after: {} seconds)", userId, waitForRefill);
            return false;
        }
    }

    private String getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return null;
    }
}
