package com.cambiaso.ioc.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class StartupLogger {
    private static final Logger log = LoggerFactory.getLogger(StartupLogger.class);

    @Autowired
    private Environment env;

    @PostConstruct
    public void logStartupInfo() {
        String[] profiles = env.getActiveProfiles();
        if (profiles == null || profiles.length == 0) {
            log.info("No active Spring profiles detected");
        } else {
            log.info("Active Spring profiles: {}", (Object) profiles);
        }

        // Mask the password in the JDBC URL if present
        String url = env.getProperty("spring.datasource.url");
        if (url != null) {
            String masked = maskJdbcUrl(url);
            log.info("JDBC URL (masked): {}", masked);
        } else {
            log.info("spring.datasource.url is not set");
        }
    }

    private String maskJdbcUrl(String url) {
        // Common pattern: jdbc:postgresql://host:port/db?params
        // We'll mask any password query param if present, otherwise return url unchanged.
        try {
            if (url.contains("password=")) {
                return url.replaceAll("(password=)([^&]+)", "$1****");
            }
        } catch (Exception e) {
            // ignore masking failures
        }
        return url;
    }
}

