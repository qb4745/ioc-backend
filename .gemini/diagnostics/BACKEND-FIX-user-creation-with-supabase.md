# 🔧 Backend Fix: Automatizar Creación de Usuarios en Supabase

**Fecha:** 2025-10-29  
**Problema:** El backend actual requiere que el admin cree manualmente el usuario en Supabase y copie el UUID  
**Solución:** Automatizar la creación de cuenta en Supabase desde el backend

---

## 📋 Cambios Necesarios en el Backend (Java/Spring Boot)

### 1. Agregar Dependencia de Supabase (si no existe)

```xml
<!-- pom.xml -->
<dependency>
    <groupId>io.github.jan-tennert.supabase</groupId>
    <artifactId>supabase-kt-jvm</artifactId>
    <version>2.0.0</version>
</dependency>

<!-- O usar el cliente HTTP directamente -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

### 2. Crear Servicio para Supabase Auth

```java
package com.cambiaso.ioc.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SupabaseAuthService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service-role-key}")
    private String serviceRoleKey;

    private final WebClient.Builder webClientBuilder;

    /**
     * Crea un usuario en Supabase Auth usando la Admin API
     * @param email Email del usuario
     * @param password Contraseña temporal
     * @return UUID del usuario creado en Supabase
     */
    public UUID createSupabaseUser(String email, String password) {
        WebClient webClient = webClientBuilder
            .baseUrl(supabaseUrl)
            .defaultHeader("apikey", serviceRoleKey)
            .defaultHeader("Authorization", "Bearer " + serviceRoleKey)
            .build();

        Map<String, Object> requestBody = Map.of(
            "email", email,
            "password", password,
            "email_confirm", true,  // Auto-confirmar email
            "app_metadata", Map.of("role", "user")
        );

        try {
            Map<String, Object> response = webClient.post()
                .uri("/auth/v1/admin/users")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            if (response != null && response.containsKey("id")) {
                return UUID.fromString(response.get("id").toString());
            }
            throw new RuntimeException("Failed to create Supabase user: No ID returned");
        } catch (Exception e) {
            throw new RuntimeException("Error creating Supabase user: " + e.getMessage(), e);
        }
    }

    /**
     * Elimina un usuario de Supabase Auth (opcional, para rollback)
     */
    public void deleteSupabaseUser(UUID userId) {
        WebClient webClient = webClientBuilder
            .baseUrl(supabaseUrl)
            .defaultHeader("apikey", serviceRoleKey)
            .defaultHeader("Authorization", "Bearer " + serviceRoleKey)
            .build();

        try {
            webClient.delete()
                .uri("/auth/v1/admin/users/" + userId)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
        } catch (Exception e) {
            // Log pero no fallar - el usuario ya está en estado inconsistente
            System.err.println("Failed to delete Supabase user: " + e.getMessage());
        }
    }
}
```

### 3. Actualizar UsuarioCreateRequest DTO

```java
package com.cambiaso.ioc.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class UsuarioCreateRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    // Campo nuevo: contraseña temporal (reemplaza supabaseUserId)
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    
    // Mantener por compatibilidad temporal (deprecar)
    @Deprecated
    private UUID supabaseUserId;
    
    @NotBlank(message = "First name is required")
    private String primerNombre;
    
    private String segundoNombre;
    
    @NotBlank(message = "Last name is required")
    private String primerApellido;
    
    private String segundoApellido;
    
    private Integer plantaId;
    
    private String centroCosto;
    
    private LocalDate fechaContrato;
}
```

### 4. Actualizar UserAdminService

```java
package com.cambiaso.ioc.service;

import com.cambiaso.ioc.dto.request.UsuarioCreateRequest;
import com.cambiaso.ioc.dto.request.UsuarioUpdateRequest;
import com.cambiaso.ioc.exception.ResourceConflictException;
import com.cambiaso.ioc.exception.ResourceNotFoundException;
import com.cambiaso.ioc.mapper.UsuarioMapper;
import com.cambiaso.ioc.persistence.entity.AppUser;
import com.cambiaso.ioc.persistence.entity.Planta;
import com.cambiaso.ioc.persistence.repository.AppUserRepository;
import com.cambiaso.ioc.persistence.repository.AppUserSearchRepository;
import com.cambiaso.ioc.persistence.repository.PlantaRepository;
import com.cambiaso.ioc.persistence.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserAdminService {

    private final AppUserRepository appUserRepository;
    private final AppUserSearchRepository appUserSearchRepository;
    private final PlantaRepository plantaRepository;
    private final UserRoleRepository userRoleRepository;
    private final UsuarioMapper usuarioMapper;
    private final SupabaseAuthService supabaseAuthService; // NUEVO

    @Transactional(readOnly = true)
    public Page<com.cambiaso.ioc.dto.response.UsuarioResponse> search(
            String search, Integer plantaId, Boolean isActive, Pageable pageable) {
        Page<AppUser> page = appUserSearchRepository.search(
            (search == null || search.isBlank()) ? null : search.trim(), 
            plantaId, isActive, pageable
        );
        return page.map(u -> usuarioMapper.toResponse(
            u, userRoleRepository.findRoleNamesByUserId(u.getId()))
        );
    }

    @Transactional(readOnly = true)
    public com.cambiaso.ioc.dto.response.UsuarioResponse getById(Long id) {
        AppUser u = appUserRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        List<String> roles = userRoleRepository.findRoleNamesByUserId(id);
        return usuarioMapper.toResponse(u, roles);
    }

    public com.cambiaso.ioc.dto.response.UsuarioResponse create(UsuarioCreateRequest req) {
        String email = req.getEmail().trim();
        
        // Validar que el email no exista
        if (appUserRepository.existsByEmailIgnoreCase(email)) {
            throw new ResourceConflictException("Email already exists: " + email);
        }

        UUID supabaseId;
        
        // NUEVO FLUJO: Si viene password, crear en Supabase automáticamente
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            try {
                supabaseId = supabaseAuthService.createSupabaseUser(email, req.getPassword());
            } catch (Exception e) {
                throw new RuntimeException("Failed to create Supabase account: " + e.getMessage(), e);
            }
        } 
        // FLUJO LEGACY: Si viene supabaseUserId, usarlo (deprecado)
        else if (req.getSupabaseUserId() != null) {
            supabaseId = req.getSupabaseUserId();
            if (appUserRepository.existsBySupabaseUserId(supabaseId)) {
                throw new ResourceConflictException("Supabase user id already exists: " + supabaseId);
            }
        } 
        // ERROR: Debe venir uno de los dos
        else {
            throw new IllegalArgumentException("Either password or supabaseUserId must be provided");
        }

        // Crear usuario en la BD
        AppUser u = new AppUser();
        u.setEmail(email);
        u.setSupabaseUserId(supabaseId);
        u.setPrimerNombre(req.getPrimerNombre());
        u.setSegundoNombre(req.getSegundoNombre());
        u.setPrimerApellido(req.getPrimerApellido());
        u.setSegundoApellido(req.getSegundoApellido());
        u.setCentroCosto(req.getCentroCosto());
        u.setFechaContrato(req.getFechaContrato());
        u.setActive(true);
        u.setCreatedAt(OffsetDateTime.now());
        u.setUpdatedAt(OffsetDateTime.now());

        if (req.getPlantaId() != null) {
            Planta p = plantaRepository.findById(req.getPlantaId()).orElse(null);
            u.setPlanta(p);
        }

        try {
            AppUser saved = appUserRepository.save(u);
            return usuarioMapper.toResponse(saved, List.of());
        } catch (Exception e) {
            // Rollback: eliminar usuario de Supabase si falló la BD
            if (req.getPassword() != null) {
                try {
                    supabaseAuthService.deleteSupabaseUser(supabaseId);
                } catch (Exception rollbackError) {
                    // Log pero no fallar
                    System.err.println("Failed to rollback Supabase user: " + rollbackError.getMessage());
                }
            }
            throw e;
        }
    }

    public com.cambiaso.ioc.dto.response.UsuarioResponse update(Long id, UsuarioUpdateRequest req) {
        AppUser u = appUserRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));

        u.setPrimerNombre(req.getPrimerNombre());
        u.setSegundoNombre(req.getSegundoNombre());
        u.setPrimerApellido(req.getPrimerApellido());
        u.setSegundoApellido(req.getSegundoApellido());
        u.setCentroCosto(req.getCentroCosto());
        u.setFechaContrato(req.getFechaContrato());
        if (req.getIsActive() != null) u.setActive(req.getIsActive());
        u.setUpdatedAt(OffsetDateTime.now());

        if (req.getPlantaId() != null) {
            Planta p = plantaRepository.findById(req.getPlantaId()).orElse(null);
            u.setPlanta(p);
        } else {
            u.setPlanta(null);
        }

        AppUser saved = appUserRepository.save(u);
        List<String> roles = userRoleRepository.findRoleNamesByUserId(id);
        return usuarioMapper.toResponse(saved, roles);
    }

    public void delete(Long id) {
        AppUser u = appUserRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        // Soft delete
        u.setActive(false);
        u.setDeletedAt(OffsetDateTime.now());
        u.setUpdatedAt(OffsetDateTime.now());
        appUserRepository.save(u);
    }
}
```

### 5. Configuración en application.yml

```yaml
supabase:
  url: ${SUPABASE_URL:https://your-project.supabase.co}
  service-role-key: ${SUPABASE_SERVICE_ROLE_KEY:your-service-role-key}
```

---

## 🔄 Flujo Después de Implementar

1. **Admin completa formulario** con email, nombre, apellido y **contraseña temporal**
2. **Frontend envía** el request con el campo `password`
3. **Backend crea automáticamente** la cuenta en Supabase Auth
4. **Backend guarda** el usuario en la BD con el UUID retornado
5. **Usuario recibe** credenciales y puede iniciar sesión

---

## 📝 Actualización del Frontend (Después del Backend)

Una vez implementados estos cambios en el backend, ejecutar:

```bash
# Revertir el campo supabaseUserId a password
# Los archivos ya están preparados con TODOs marcados
```

Archivos a actualizar:
- `src/schemas/user.schema.ts` - Cambiar `supabaseUserId` por `password`
- `src/components/admin/user-management/UserFormModal.tsx` - Cambiar campo a password

---

## ✅ Ventajas de Esta Solución

1. ✅ **Mejor UX**: Admin no necesita ir a Supabase manualmente
2. ✅ **Más seguro**: Credenciales no expuestas en múltiples lugares
3. ✅ **Atómico**: Rollback automático si falla la creación
4. ✅ **Compatible**: Soporta ambos flujos durante la migración (legacy + nuevo)
5. ✅ **Escalable**: Fácil agregar validaciones adicionales (formato email, complejidad password, etc.)

---

## 🚨 Notas Importantes

- El `service-role-key` debe mantenerse **SECRETO** (solo backend)
- Agregar logs apropiados para troubleshooting
- Considerar agregar notificación por email al usuario con credenciales temporales
- El flag `email_confirm: true` auto-confirma el email (ajustar según política de seguridad)

