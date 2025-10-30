package com.cambiaso.ioc.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "IOC Backend API",
                version = "1.0.0",
                description = "API para la plataforma de Inteligencia Operacional Cambiaso. \n\n" +
                        "**Autenticación:** Usa JWT Bearer Token. Obtén tu token de Supabase Auth y pégalo en el botón 'Authorize' abajo. \n" +
                        "**Roles:** Los endpoints administrativos (`/api/admin/**`) requieren el rol `ADMIN`.",
                contact = @Contact(
                        name = "Equipo de Desarrollo IOC",
                        email = "dev@cambiaso.com"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Servidor Local"),
                @Server(url = "https://api.cambiaso.com", description = "Servidor de Producción")
        },
        security = {@SecurityRequirement(name = "bearerAuth")}
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer",
        description = "JWT Bearer Token (ej: eyJhbGci...)"
)
public class OpenApiConfig {
    // Esta clase no necesita métodos, las anotaciones hacen la configuración.
}
