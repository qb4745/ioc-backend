package com.cambiaso.ioc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.Set;

@Data
@Configuration
@ConfigurationProperties(prefix = "metabase")
@Validated
public class MetabaseProperties {

    @NotBlank(message = "Metabase site URL is required")
    private String siteUrl;

    @NotBlank(message = "Metabase secret key is required")
    @Pattern(regexp = "^[A-Fa-f0-9]{64,}$", message = "Secret key must be hexadecimal with at least 64 characters")
    private String secretKey;

    @NotBlank(message = "Metabase username is required for API access")
    private String username;

    @NotBlank(message = "Metabase password is required for API access")
    private String password;

    @Min(value = 1, message = "Token expiration must be at least 1 minute")
    private int tokenExpirationMinutes = 10;

    @NotEmpty(message = "At least one dashboard must be configured")
    @Valid
    private List<DashboardConfig> dashboards;

    @Data
    public static class DashboardConfig {
        @Min(value = 1, message = "Dashboard ID must be positive")
        private int id;

        @NotBlank(message = "Dashboard name is required")
        private String name;

        private String description;

        @NotEmpty(message = "At least one role must be configured")
        private Set<String> allowedRoles;

        private List<FilterConfig> filters;
    }

    @Data
    public static class FilterConfig {
        @NotBlank(message = "Filter name is required")
        private String name;

        @NotBlank(message = "Filter type is required")
        private String type;

        @NotBlank(message = "Filter source is required")
        private String source;
    }
}
