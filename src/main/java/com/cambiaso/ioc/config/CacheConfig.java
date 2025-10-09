package com.cambiaso.ioc.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {
    // La configuración específica de Caffeine se gestiona en application.properties.
    // Esta clase solo sirve para activar el framework de caché de Spring.
}
