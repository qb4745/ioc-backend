package com.cambiaso.ioc.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${metabase.site-url}")
    private String metabaseUrl;

    // Puedes inyectar la URL de tu frontend desde application.properties también
    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // Aplicar a toda la API para consistencia
            .allowedOrigins(frontendUrl, metabaseUrl) // Permitir peticiones del frontend y Metabase
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }
}
