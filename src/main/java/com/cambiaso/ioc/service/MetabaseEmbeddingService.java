package com.cambiaso.ioc.service;

import com.cambiaso.ioc.config.MetabaseProperties;
import com.cambiaso.ioc.exception.DashboardAccessDeniedException;
import com.cambiaso.ioc.exception.DashboardNotFoundException;
import com.cambiaso.ioc.security.CustomUserDetails;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class MetabaseEmbeddingService {

    private final MetabaseProperties properties;
    private final SecretKey key;
    private final DashboardAuditService auditService;
    private final MeterRegistry meterRegistry;
    private final CacheManager cacheManager;

    public MetabaseEmbeddingService(
            MetabaseProperties properties,
            DashboardAuditService auditService,
            MeterRegistry meterRegistry,
            CacheManager cacheManager
    ) {
        this.properties = properties;
        this.auditService = auditService;
        this.meterRegistry = meterRegistry;
        this.cacheManager = cacheManager;

        validateSecretKey(properties.getSecretKey());
        
        this.key = Keys.hmacShaKeyFor(
            properties.getSecretKey().getBytes(StandardCharsets.UTF_8)
        );
        
        log.info("MetabaseEmbeddingService initialized successfully with {} configured dashboards", 
            properties.getDashboards().size());
    }

    private void validateSecretKey(String secretKey) {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException(
                "Metabase secret key is required. Set METABASE_SECRET_KEY environment variable."
            );
        }
        if (secretKey.length() < 64) {
            throw new IllegalStateException(
                String.format("Metabase secret key is too short (%d chars). Must be at least 64 characters.", 
                    secretKey.length())
            );
        }
        if (!secretKey.matches("^[A-Fa-f0-9]+$")) {
            throw new IllegalStateException(
                "Metabase secret key must be hexadecimal (0-9, A-F). Check your METABASE_SECRET_KEY variable."
            );
        }
    }

    @CircuitBreaker(name = "metabaseService", fallbackMethod = "getSignedDashboardUrlFallback")
    @Cacheable(value = "dashboardTokens", key = "#authentication.name + '_' + #dashboardId")
    public String getSignedDashboardUrl(int dashboardId, Authentication authentication) {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            MetabaseProperties.DashboardConfig config = findDashboardConfig(dashboardId);
            checkAuthorization(config, authentication);
            
            auditService.logDashboardAccess(authentication.getName(), dashboardId, config.getName(), true);
            
            Map<String, Object> params = buildParams(config, authentication);
            String token = generateToken(dashboardId, params);
            String url = String.format("%s/embed/dashboard/%s#bordered=true&titled=true", 
                properties.getSiteUrl(), token);
            
            meterRegistry.counter("metabase.dashboard.access", "dashboard", String.valueOf(dashboardId), "user", authentication.getName(), "status", "success").increment();
            log.debug("Generated new signed URL for dashboard {} for user {} (cache miss or expired)", dashboardId, authentication.getName());

            return url;
            
        } catch (DashboardAccessDeniedException | DashboardNotFoundException e) {
            String username = (authentication != null && authentication.getName() != null) ? authentication.getName() : "unknown";
            auditService.logDashboardAccess(username, dashboardId, "UNKNOWN", false);
            meterRegistry.counter("metabase.dashboard.access", "dashboard", String.valueOf(dashboardId), "user", username, "status", "denied").increment();
            throw e;
        } finally {
            sample.stop(Timer.builder("metabase.dashboard.request.duration").tag("dashboard", String.valueOf(dashboardId)).register(meterRegistry));
        }
    }

    /**
     * Invalida todos los tokens en cache de un usuario específico.
     * Útil cuando el usuario cierra sesión o sus permisos cambian.
     *
     * @param username nombre del usuario cuyo cache se debe invalidar
     */
    public void invalidateUserTokens(String username) {
        Cache cache = cacheManager.getCache("dashboardTokens");
        if (cache != null) {
            // Nota: Caffeine no soporta invalidación por patrón directamente
            // Esta es una implementación básica que invalida todo el cache
            // Para invalidación selectiva, considerar usar cache keys con prefijos
            cache.invalidate();
            log.info("Invalidated dashboard tokens cache for all users (logout/permission change)");
            meterRegistry.counter("metabase.cache.invalidation", "reason", "user_logout", "user", username).increment();
        }
    }

    /**
     * Invalida el token en cache para un usuario y dashboard específicos.
     *
     * @param username nombre del usuario
     * @param dashboardId ID del dashboard
     */
    @CacheEvict(value = "dashboardTokens", key = "#username + '_' + #dashboardId")
    public void invalidateUserDashboardToken(String username, int dashboardId) {
        log.debug("Invalidated dashboard token cache for user {} and dashboard {}", username, dashboardId);
        meterRegistry.counter("metabase.cache.invalidation", "reason", "manual", "user", username, "dashboard", String.valueOf(dashboardId)).increment();
    }

    @SuppressWarnings("unused")
    private String getSignedDashboardUrlFallback(int dashboardId, Authentication authentication, Exception ex) {
        log.error("Circuit breaker activated for dashboard {}. Metabase may be down.", dashboardId, ex);
        String username = (authentication != null && authentication.getName() != null) ? authentication.getName() : "unknown";
        if (meterRegistry != null) {
            meterRegistry.counter("metabase.dashboard.access", "dashboard", String.valueOf(dashboardId), "user", username, "status", "circuit_open").increment();
        }
        throw new RuntimeException("Dashboard service is temporarily unavailable. Please try again in a few moments.", ex);
    }

    private MetabaseProperties.DashboardConfig findDashboardConfig(int dashboardId) {
        return properties.getDashboards().stream()
            .filter(d -> d.getId() == dashboardId)
            .findFirst()
            .orElseThrow(() -> new DashboardNotFoundException(String.format("Dashboard with ID %d is not configured or does not exist.", dashboardId)));
    }

    private void checkAuthorization(MetabaseProperties.DashboardConfig config, Authentication authentication) {
        // --- LÓGICA DE AUTORIZACIÓN TEMPORAL (PERMITE A CUALQUIER AUTENTICADO) ---
        // Se verifica únicamente que el usuario esté autenticado.
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Unauthenticated user attempted to access dashboard {}", config.getName());
            throw new DashboardAccessDeniedException("You must be authenticated to view this dashboard.");
        }

        /*
        // --- LÓGICA DE AUTORIZACIÓN BASADA EN ROLES (DESACTIVADA TEMPORALMENTE) ---
        // TODO: Reactivar esta sección cuando los roles se gestionen y se incluyan en el JWT.
        boolean isAuthorized = authentication.getAuthorities().stream()
            .anyMatch(grantedAuthority -> config.getAllowedRoles().contains(grantedAuthority.getAuthority()));
        
        if (!isAuthorized) {
            log.warn("User {} attempted to access dashboard {} without proper roles. Required: {}, Has: {}", 
                authentication.getName(), config.getName(), config.getAllowedRoles(), authentication.getAuthorities());
            throw new DashboardAccessDeniedException(String.format("You do not have permission to view '%s'. Required roles: %s", config.getName(), config.getAllowedRoles()));
        }
        */
    }

    private Map<String, Object> buildParams(MetabaseProperties.DashboardConfig config, Authentication authentication) {
        Map<String, Object> params = new HashMap<>();
        if (config.getFilters() == null || config.getFilters().isEmpty()) return params;
        
        if (!(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            log.warn("Authentication principal is not CustomUserDetails. Attribute-based filters will not be applied.");
            return params;
        }
        
        config.getFilters().forEach(filter -> {
            if ("USER_ATTRIBUTE".equals(filter.getType())) {
                Object value = extractUserAttribute(userDetails, filter.getSource());
                if (value != null) {
                    params.put(filter.getName(), value);
                    log.debug("Applied filter: {} = {}", filter.getName(), value);
                } else {
                    log.warn("Could not extract user attribute '{}' for filter '{}'", filter.getSource(), filter.getName());
                }
            }
        });
        return params;
    }

    private Object extractUserAttribute(CustomUserDetails userDetails, String attributeName) {
        return switch (attributeName) {
            case "userId" -> userDetails.getUserId();
            case "username" -> userDetails.getUsername();
            case "email" -> userDetails.getEmail();
            case "department" -> userDetails.getDepartment();
            case "region" -> userDetails.getRegion();
            case "fullName" -> userDetails.getFullName();
            default -> {
                log.warn("Unknown user attribute requested: {}", attributeName);
                yield null;
            }
        };
    }

    private String generateToken(int dashboardId, Map<String, Object> params) {
        long expirationMillis = TimeUnit.MINUTES.toMillis(properties.getTokenExpirationMinutes());
        
        return Jwts.builder()
            .claim("resource", Map.of("dashboard", dashboardId))
            .claim("params", params)
            .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
            .setIssuedAt(new Date())
            .signWith(key, Jwts.SIG.HS256)  // CRÍTICO: Metabase requiere HS256, no HS512
            .compact();
    }
}
