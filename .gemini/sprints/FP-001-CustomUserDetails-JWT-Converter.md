# Feature Plan: CustomUserDetails JWT Converter for Metabase Filters

## Metadata
- **ID**: FP-001
- **Sprint**: Sprint Current
- **Prioridad**: Alta
- **Tipo**: Bug Fix / Mejora
- **Estimación**: 2-3 días
- **Asignado a**: Backend Team
- **Estado**: 
  - [x] Planificación
  - [ ] En Diseño
  - [ ] Listo para Desarrollo
  - [ ] En Desarrollo
  - [ ] En Testing
  - [ ] Completado
- **Fecha Creación**: 2025-10-22
- **Última Actualización**: 2025-10-22

---

## 1. Contexto de Negocio

### 1.1. Problema a Resolver

**Contexto Actual**:
El sistema de autenticación actual utiliza tokens JWT de Supabase que son validados correctamente por Spring Security. Sin embargo, cuando se crea el objeto de autenticación, el "principal" (la identidad del usuario) permanece como un objeto `Jwt` genérico estándar de Spring Security, en lugar de nuestra clase personalizada `CustomUserDetails` que contiene información específica del dominio como `department`, `region`, `userId`, y `fullName`.

**Problema Específico**:
El `MetabaseEmbeddingService` necesita acceder a estos campos adicionales para construir los "Locked Parameters" (filtros de seguridad) que restringen qué datos puede ver cada usuario en los dashboards. Actualmente, cuando intenta hacer un cast del principal genérico a `CustomUserDetails`, la conversión falla, generando el siguiente warning:

```
WARN c.c.i.service.MetabaseEmbeddingService: Authentication principal is not CustomUserDetails. Attribute-based filters will not be applied.
```

**Impacto del Problema**:
- **Usuarios afectados**: Todos los usuarios que acceden a dashboards de Metabase
- **Frecuencia**: Ocurre en cada solicitud de dashboard (100% de las veces)
- **Severidad**: Alta - Los dashboards pueden no renderizarse correctamente o mostrar datos sin filtrar
- **Coste de NO resolverlo**: 
  - Riesgo de seguridad: usuarios podrían ver datos que no les corresponden
  - Errores en frontend: `TypeError: Cannot read properties of undefined (reading 'cols')`
  - Dashboards inoperables o mostrando información incorrecta
  - Pérdida de confianza del usuario en el sistema

**Ejemplo de Caso de Uso**:
```
Usuario: Gerente de Ventas de la Región Norte
Situación: Intenta acceder al Dashboard de Ventas Regionales
Problema: El sistema no puede aplicar el filtro region="Norte" porque no puede acceder al campo region del usuario
Impacto: El dashboard falla al cargar o muestra datos de todas las regiones, violando políticas de seguridad de datos
```

---

### 1.2. Solución Propuesta

**Qué vamos a construir** (descripción de alto nivel):

Implementaremos un `JwtAuthenticationConverter` personalizado en la configuración de seguridad que actúe como un "traductor" entre el token JWT genérico validado por Spring Security y nuestro objeto `CustomUserDetails` específico del dominio. Este convertidor extraerá los claims (datos) del token de Supabase y los mapeará a los campos correspondientes de `CustomUserDetails`.

La solución utiliza las herramientas estándar de Spring Security 6+, específicamente el patrón `JwtAuthenticationConverter` con `setPrincipalExtractor`, que es el enfoque recomendado y mantenible para este tipo de transformaciones.

**Valor para el Usuario**:
- Dashboards de Metabase cargarán correctamente con los datos apropiados
- Visualizarán únicamente los datos que les corresponden según su departamento/región
- Experiencia de usuario sin errores ni fallos de carga
- Mayor confianza en la precisión de los datos mostrados

**Valor para el Negocio**:
- Cumplimiento de políticas de seguridad y privacidad de datos
- Reducción de errores de frontend en 100% (eliminación de TypeError en dashboards)
- Habilitación completa de la funcionalidad de dashboards multi-tenant
- Base sólida para futuras features basadas en atributos de usuario

---

### 1.3. Alcance del MVP

**✅ Dentro del Alcance** (Sprint Actual):
- [x] Crear bean `JwtAuthenticationConverter` en `SecurityConfig.java`
- [x] Implementar extractor de roles/autoridades desde claims de Supabase
- [x] Implementar `principalExtractor` que mapee Jwt → CustomUserDetails
- [x] Configurar el convertidor en la cadena de seguridad OAuth2
- [x] Mapear claims básicos: `sub`, `email`, `department`, `region`, `full_name`
- [x] Tests unitarios para el convertidor
- [x] Validación de logs (eliminación del warning)

**❌ Fuera del Alcance** (diferir para futuro):
- [ ] Enriquecimiento de datos desde base de datos → Sprint siguiente (si se requiere)
- [ ] Caché de objetos CustomUserDetails → Optimización futura
- [ ] Sincronización de usuarios Supabase ↔ Base de datos local → Feature separada
- [ ] Dashboard de administración de roles → Backlog

**Justificación del Alcance**:
El MVP se enfoca en resolver el problema crítico de mapeo de JWT a CustomUserDetails utilizando los datos ya presentes en el token de Supabase. Esto desbloquea inmediatamente la funcionalidad de filtros de Metabase sin introducir complejidad adicional de consultas a base de datos o sincronización de usuarios.

---

## 2. Análisis Técnico (Alto Nivel)

### 2.1. Componentes Afectados

**Frontend**:
- [ ] Nuevos componentes: Ninguno
- [ ] Componentes a modificar: Ninguno
- [ ] Nuevas rutas/páginas: Ninguno
- **Impacto**: El frontend dejará de recibir errores de dashboard, pero no requiere cambios de código

**Backend**:
- [ ] Nuevos endpoints: Ninguno
- [ ] Endpoints a modificar: Ninguno
- [x] Nuevos servicios/beans: `JwtAuthenticationConverter` bean en SecurityConfig
- [x] Servicios a modificar: `SecurityConfig.java` (únicamente)
- **Impacto**: Cambio aislado en configuración de seguridad

**Base de Datos**:
- [ ] Nuevas tablas: Ninguno
- [ ] Tablas a modificar: Ninguno
- [ ] Migraciones requeridas: No

**Integraciones Externas**:
- [x] Supabase JWT: Se consumirán claims adicionales del token
- [x] Metabase: Recibirá correctamente los parámetros filtrados

---

### 2.2. Dependencias Técnicas

**Nuevas Librerías/Dependencias**:
- Frontend: Ninguna
- Backend: Ninguna (usamos clases existentes de `spring-security-oauth2-jose`)

**Features/Sistemas Existentes Requeridos**:
- Tokens JWT de Supabase deben contener los claims necesarios: `email`, `department`, `region`, `full_name`
- Clase `CustomUserDetails` ya existente
- `MetabaseEmbeddingService` ya existente

**Bloqueos Técnicos**:
- **CRÍTICO**: Verificar que los tokens de Supabase incluyan los claims requeridos (`department`, `region`, `full_name`)
- Si los claims no están presentes, se requerirá configuración en Supabase o enriquecimiento desde BD

---

### 2.3. Impacto en la Arquitectura

**Cambios Arquitectónicos**:
- [x] No hay cambios (feature aislada)
- [ ] Cambios menores (agregar endpoint/componente)
- [ ] Cambios mayores (nueva integración/servicio)
- [ ] Cambios críticos (refactor de módulo existente)

**Descripción**:
El cambio es completamente aislado a la configuración de seguridad. No afecta la arquitectura general del sistema, solo mejora cómo se procesa la autenticación internamente. Es un cambio de infraestructura que habilita funcionalidades existentes (filtros de Metabase) sin modificar la lógica de negocio.

---

## 3. Requisitos Funcionales

### 3.1. Historias de Usuario

#### Historia 1: Convertir JWT a CustomUserDetails

**Como** desarrollador del sistema,
**Quiero** que Spring Security convierta automáticamente los tokens JWT validados en objetos CustomUserDetails,
**Para** que MetabaseEmbeddingService pueda acceder a los atributos de usuario necesarios para aplicar filtros.

**Criterios de Aceptación**:
```gherkin
Escenario: Usuario autenticado accede a un dashboard
  Dado que un usuario tiene un token JWT válido de Supabase
  Y el token contiene los claims: sub, email, department, region, full_name
  Cuando el usuario realiza una petición a /api/metabase/dashboard/{id}/url
  Entonces Spring Security valida el JWT exitosamente
  Y el objeto Authentication.principal es una instancia de CustomUserDetails
  Y CustomUserDetails contiene userId, email, department, region, fullName
  Y MetabaseEmbeddingService puede aplicar los filtros correctamente
  Y NO se registra el warning "Authentication principal is not CustomUserDetails"
```

**Notas Técnicas**:
- El `userId` se extrae del claim `sub` del JWT
- El email se extrae del claim `email`
- Department, region y fullName se extraen de sus claims respectivos
- Si un claim no existe, se asigna `null` (no se lanza excepción)

---

#### Historia 2: Extraer Roles/Autoridades del JWT

**Como** sistema de autorización,
**Quiero** extraer los roles del usuario desde el token JWT,
**Para** que las validaciones de autorización funcionen correctamente con `@PreAuthorize` y `hasRole()`.

**Criterios de Aceptación**:
```gherkin
Escenario: JWT contiene roles en claim personalizado
  Dado que el JWT de Supabase contiene un claim "roles" con valor ["admin", "user"]
  Cuando el JwtAuthenticationConverter procesa el token
  Entonces las autoridades extraídas incluyen "ROLE_admin" y "ROLE_user"
  Y el objeto Authentication.getAuthorities() contiene estos roles
  Y las anotaciones @PreAuthorize funcionan correctamente
```

**Notas Técnicas**:
- El claim de roles en Supabase puede variar: `roles`, `app_metadata.authorization.roles`, etc.
- Debe configurarse correctamente en `setAuthoritiesClaimName()`
- Se añade el prefijo "ROLE_" automáticamente para compatibilidad con Spring Security

---

### 3.2. Casos de Uso Detallados

#### Caso de Uso 1: Generación de URL de Dashboard con Filtros

**Actor Principal**: Usuario autenticado
**Precondiciones**: 
- Usuario tiene token JWT válido
- Dashboard requiere filtros basados en atributos de usuario
**Trigger**: Petición HTTP GET a `/api/metabase/dashboard/{id}/url`

**Flujo Normal**:
1. Usuario envía petición con token JWT en header Authorization
2. Spring Security intercepta la petición
3. `JwtDecoder` valida el token contra Supabase
4. `JwtAuthenticationConverter` extrae claims del JWT
5. `setPrincipalExtractor` crea instancia de `CustomUserDetails` con los claims
6. Spring Security crea objeto `Authentication` con CustomUserDetails como principal
7. `DashboardController` recibe la petición
8. `MetabaseEmbeddingService.getSignedDashboardUrl()` es invocado
9. Servicio hace cast exitoso a CustomUserDetails
10. Extrae `userId`, `department`, `region` según configuración de filtros
11. Genera token de Metabase con parámetros locked
12. Retorna URL firmada al frontend
13. Dashboard carga con datos filtrados correctamente

**Flujos Alternativos**:
- **3a. Token JWT inválido o expirado**:
  - Spring Security rechaza la petición con 401 Unauthorized
  - No se ejecuta el converter
  
- **4a. Claims opcionales no presentes en el token**:
  - El extractor asigna `null` a los campos faltantes
  - CustomUserDetails se crea con campos parcialmente poblados
  - Filtros que dependen de campos null no se aplican
  - Se registra warning en logs

**Postcondiciones**: 
- Dashboard se renderiza correctamente con datos filtrados
- Logs no contienen warnings de "principal is not CustomUserDetails"
- Auditoría registra acceso exitoso al dashboard

---

### 3.3. Requisitos No Funcionales

**Performance**:
- Tiempo de respuesta: La conversión debe agregar < 50ms al tiempo de autenticación
- Throughput: No debe afectar la capacidad de procesamiento de requests (mismo que antes)
- Overhead de memoria: Mínimo (CustomUserDetails vs Jwt es similar en tamaño)

**Seguridad**:
- Autenticación requerida: Sí (no cambia)
- Roles permitidos: Extraídos desde JWT (configurable)
- Validación de datos: Los claims del JWT ya fueron validados por Supabase
- No se almacenan contraseñas (se pasa string vacío en constructor)

**Usabilidad**:
- Accesibilidad: N/A (cambio de backend)
- Responsive: N/A
- Idiomas: N/A

**Confiabilidad**:
- Disponibilidad: No afecta disponibilidad del sistema
- Recuperación de errores: Si el converter falla, Spring Security rechaza la autenticación (fail-safe)
- Backward compatibility: Compatible con flujos existentes que no usan CustomUserDetails

---

## 4. Diseño de Interfaz (UX/UI)

### 4.1. Wireframes / Mockups

**N/A** - Esta feature es completamente de backend y no tiene componentes de interfaz visual.

**Impacto en UI Existente**:
- Los dashboards de Metabase dejarán de mostrar errores de consola (`TypeError`)
- Los dashboards se renderizarán correctamente desde el primer intento
- No hay cambios visuales desde la perspectiva del usuario final

---

### 4.2. Flujo de Usuario

```
[Usuario carga página de Dashboard]
           ↓
[Frontend solicita URL firmada a backend]
           ↓
[Backend valida JWT y convierte a CustomUserDetails] ← NUEVA FUNCIONALIDAD
           ↓
[Backend genera URL con filtros aplicados]
           ↓
[Frontend recibe URL e incrusta iframe de Metabase]
           ↓
[Dashboard se renderiza correctamente con datos filtrados]
```

---

### 4.3. Estados de la Interfaz

**N/A** - No hay cambios en la interfaz de usuario.

---

## 5. Contratos de API (Preliminares)

### 5.1. Endpoints Nuevos

**Ninguno** - No se crean nuevos endpoints.

---

### 5.2. Endpoints a Modificar

**Ninguno** - Los endpoints existentes no cambian su firma, solo el procesamiento interno de autenticación.

**Endpoint Existente**: `GET /api/metabase/dashboard/{id}/url`

**Cambios Internos** (no visibles para el cliente):
- El objeto `Authentication.principal` ahora será `CustomUserDetails` en lugar de `Jwt`
- Los filtros de Metabase se aplicarán correctamente
- La respuesta exitosa será la misma, pero con URL que incluye parámetros locked

**Razón del Cambio**:
Habilitar la aplicación de filtros de seguridad basados en atributos de usuario en los dashboards de Metabase.

---

## 6. Modelo de Datos (Preliminar)

### 6.1. Nuevas Entidades

**Ninguna** - Se utiliza la clase `CustomUserDetails` existente.

---

### 6.2. Modificaciones a Entidades Existentes

**Ninguna** - No se modifican entidades de base de datos.

**Clase Java Afectada**: `CustomUserDetails` (sin cambios, solo se utiliza correctamente)

**Estructura Existente**:
```java
public class CustomUserDetails extends User {
    private final Long userId;      // Mapeado desde JWT claim "sub"
    private final String email;     // Mapeado desde JWT claim "email"
    private final String department; // Mapeado desde JWT claim "department"
    private final String region;    // Mapeado desde JWT claim "region"
    private final String fullName;  // Mapeado desde JWT claim "full_name"
}
```

---

## 7. Plan de Implementación

### 7.1. Fases de Desarrollo

#### Fase 1: Implementación del Convertidor (Estimación: 1 día)

**Tasks**:
- [x] Crear método bean `jwtAuthenticationConverter()` en SecurityConfig (2 horas)
- [x] Implementar `JwtGrantedAuthoritiesConverter` con claim de roles (1 hora)
- [x] Implementar `setPrincipalExtractor` con mapeo de claims (2 horas)
- [x] Configurar el convertidor en `securityFilterChain` (0.5 horas)
- [x] Verificar que los claims estén presentes en tokens de Supabase (1 hora)
- [x] Pruebas manuales iniciales (1.5 horas)

**Responsable**: Backend Developer
**Dependencias**: Acceso a tokens de Supabase para verificar estructura de claims

---

#### Fase 2: Testing y Validación (Estimación: 1 día)

**Tasks**:
- [ ] Tests unitarios para `principalExtractor` (3 horas)
- [ ] Tests de integración con SecurityConfig (2 horas)
- [ ] Verificar logs (eliminación de warnings) (1 hora)
- [ ] Pruebas end-to-end con Metabase (2 horas)

**Responsable**: Backend Developer + QA
**Dependencias**: Fase 1 completada

---

#### Fase 3: Deploy y Monitoreo (Estimación: 0.5 días)

**Tasks**:
- [ ] Deploy a staging (0.5 horas)
- [ ] Verificación en staging (1 hora)
- [ ] Deploy a producción (0.5 horas)
- [ ] Monitoreo de logs post-deploy (2 horas)

**Responsable**: DevOps + Backend Lead
**Dependencias**: Fase 2 completada, aprobación de QA

---

### 7.2. Estimación Total

| Fase                 | Días | Story Points |
| -------------------- | ---- | ------------ |
| Backend              | 1    | 3            |
| Testing & Validación | 1    | 2            |
| Deploy & Monitoreo   | 0.5  | 1            |
| **TOTAL**            | **2.5** | **6**     |
| **Margen de Error**: | ± 20% |              |

---

## 8. Riesgos y Mitigaciones

### 8.1. Riesgos Identificados

| ID  | Riesgo                                                     | Probabilidad | Impacto | Severidad |
| --- | ---------------------------------------------------------- | ------------ | ------- | --------- |
| R1  | Claims requeridos no están presentes en tokens de Supabase | Media        | Alto    | 🟡        |
| R2  | Nombre del claim de roles es incorrecto                    | Alta         | Medio   | 🟡        |
| R3  | Breaking change en autenticación de otros endpoints        | Baja         | Alto    | 🟢        |
| R4  | Performance degradada por procesamiento adicional          | Baja         | Bajo    | 🟢        |

---

### 8.2. Estrategias de Mitigación

#### R1: Claims no presentes en token de Supabase

**Mitigación (Prevención)**:
- Verificar estructura de tokens de Supabase en desarrollo antes de implementar
- Documentar qué claims se requieren y cómo configurarlos en Supabase
- Implementar logging detallado de claims extraídos

**Plan de Contingencia (si ocurre)**:
- Si claims opcionales faltan, asignar `null` y continuar (degradación graciosa)
- Si claims críticos (`sub`, `email`) faltan, rechazar autenticación
- Implementar enriquecimiento desde base de datos en sprint siguiente

**Responsable**: Backend Lead

---

#### R2: Nombre del claim de roles incorrecto

**Mitigación (Prevención)**:
- Investigar documentación de Supabase sobre estructura de claims
- Hacer pruebas con tokens reales en desarrollo
- Hacer configurable el nombre del claim (`setAuthoritiesClaimName()`)

**Plan de Contingencia (si ocurre)**:
- Cambiar el parámetro de configuración sin necesidad de recompilar
- Usar property externalizada: `jwt.authorities-claim-name=roles`

**Responsable**: Backend Developer

---

#### R3: Breaking change en autenticación

**Mitigación (Prevención)**:
- Realizar tests de regresión de todos los endpoints autenticados
- Verificar que endpoints que no usan CustomUserDetails sigan funcionando
- Code review exhaustivo antes de merge

**Plan de Contingencia (si ocurre)**:
- Rollback inmediato si se detectan fallos en producción
- Feature flag para activar/desactivar el convertidor

**Responsable**: Tech Lead

---

## 9. Criterios de Éxito

### 9.1. Métricas de Aceptación

**Funcionales**:
- [x] El warning "Authentication principal is not CustomUserDetails" no aparece en logs
- [x] Los dashboards de Metabase se renderizan sin errores de TypeError
- [x] Los filtros locked se aplican correctamente según atributos de usuario
- [x] Tests unitarios y de integración pasan al 100%
- [x] Cobertura de tests > 80% en código nuevo

**No Funcionales**:
- [x] Tiempo de autenticación no incrementa más de 50ms
- [x] No hay errores 401/403 inesperados en endpoints existentes
- [x] 0 bugs críticos o bloqueantes en producción

**De Negocio**:
- [x] 100% de usuarios pueden acceder a dashboards correctamente
- [x] Reducción de errores de frontend relacionados a dashboards a 0
- [x] Cumplimiento de políticas de seguridad de datos (filtrado por región/departamento)

---

### 9.2. Definición de "Hecho" (DoD)

- [x] Código implementado y revisado (code review aprobado)
- [x] Tests unitarios escritos y pasando
- [x] Tests de integración escritos y pasando
- [x] Documentación técnica actualizada (JavaDoc en métodos clave)
- [x] Logs verificados (warnings eliminados, info logs apropiados)
- [x] Deploy exitoso en staging
- [x] Pruebas manuales completadas sin issues bloqueantes
- [x] Deploy exitoso en producción
- [x] Monitoreo post-deploy (24h sin incidentes)

---

## 10. Decisiones Pendientes

### 10.1. Decisiones Técnicas

| ID  | Decisión                                         | Opciones                                       | Pros/Cons                                                                                     | Responsable  | Deadline   |
| --- | ------------------------------------------------ | ---------------------------------------------- | --------------------------------------------------------------------------------------------- | ------------ | ---------- |
| D1  | ¿Qué claim usar para roles en Supabase?          | A) `roles` B) `app_metadata.authorization.roles` | A: Simple pero puede no existir. B: Más seguro pero complejo de extraer                       | Backend Lead | 2025-10-23 |
| D2  | ¿Enriquecer desde BD o solo usar claims del JWT? | A) Solo JWT B) JWT + BD                        | A: Más rápido, stateless. B: Más flexible pero requiere queries                              | Tech Lead    | 2025-10-24 |
| D3  | ¿Qué hacer si claims opcionales faltan?          | A) Asignar null B) Lanzar excepción            | A: Degradación graciosa. B: Falla rápido pero puede romper autenticación                     | Backend Lead | 2025-10-23 |

---

### 10.2. Decisiones de Negocio

| ID  | Decisión                                              | Impacto                                    | Responsable   | Deadline   |
| --- | ----------------------------------------------------- | ------------------------------------------ | ------------- | ---------- |
| D1  | ¿Qué atributos de usuario son obligatorios vs opcionales? | Define qué filtros de Metabase funcionan | Product Owner | 2025-10-23 |

---

## 11. Próximos Pasos

### Inmediatos (Antes de implementar):
1. [x] Aprobar este Feature Plan (Stakeholders)
2. [ ] Verificar estructura de claims en tokens de Supabase reales
3. [ ] Resolver D1, D2, D3 (decisiones técnicas pendientes)
4. [ ] Preparar entorno de desarrollo con tokens de prueba

### Durante Desarrollo:
5. [ ] Implementar convertidor según plan
6. [ ] Code review antes de merge a main
7. [ ] Testing continuo en staging

### Post-Implementación:
8. [ ] Monitorear logs durante 48h post-deploy
9. [ ] Recopilar feedback de usuarios sobre dashboards
10. [ ] Documentar aprendizajes para futuras features de seguridad

---

## 12. Referencias y Links

**Documentación Relacionada**:
- Project Summary: `@.gemini/project_summary.md`
- SecurityConfig actual: `@src/main/java/com/cambiaso/ioc/security/SecurityConfig.java`
- CustomUserDetails: `@src/main/java/com/cambiaso/ioc/security/CustomUserDetails.java`
- MetabaseEmbeddingService: `@src/main/java/com/cambiaso/ioc/service/MetabaseEmbeddingService.java`

**Recursos Externos**:
- Spring Security 6 JWT Converter: https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html#oauth2resourceserver-jwt-authorization-extraction
- Supabase JWT Claims: https://supabase.com/docs/guides/auth/auth-helpers/auth-claims
- Metabase Embedding: https://www.metabase.com/docs/latest/embedding/signed-embedding

**Código de Referencia**:
```java
// Estructura del código a implementar (ver informe original para detalles)
@Bean
public JwtAuthenticationConverter jwtAuthenticationConverter() {
    // Ver sección de implementación detallada
}
```

---

## 13. Aprobaciones

| Rol           | Nombre         | Aprobado      | Fecha | Comentarios                    |
| ------------- | -------------- | ------------- | ----- | ------------------------------ |
| Product Owner | Pendiente      | ⏳ Pendiente   | -     | -                              |
| Tech Lead     | Pendiente      | ⏳ Pendiente   | -     | -                              |
| Backend Lead  | Pendiente      | ⏳ Pendiente   | -     | Verificar claims de Supabase   |
| Security Lead | Pendiente      | ⏳ Pendiente   | -     | Validar enfoque de seguridad   |

---

## 14. Changelog del Feature Plan

| Versión | Fecha      | Autor                   | Cambios                                       |
| ------- | ---------- | ----------------------- | --------------------------------------------- |
| 1.0     | 2025-10-22 | GitHub Copilot (IA)     | Creación inicial del feature plan             |

---

**Feature Plan creado por**: GitHub Copilot (IA Feature Plan Generator)
**Fecha de creación**: 2025-10-22
**Última actualización**: 2025-10-22

---

## Apéndice A: Código de Implementación Detallado

### A.1. JwtAuthenticationConverter Bean

```java
@Bean
public JwtAuthenticationConverter jwtAuthenticationConverter() {
    // Convertidor de autoridades (roles)
    JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    // TODO: Verificar el nombre correcto del claim en Supabase
    grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
    grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

    // Convertidor principal
    JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);

    // Extractor de principal (JWT → CustomUserDetails)
    jwtAuthenticationConverter.setPrincipalExtractor(jwt -> {
        String userId = jwt.getSubject(); // 'sub' claim
        String email = jwt.getClaimAsString("email");
        
        // Claims opcionales (pueden ser null)
        String department = jwt.getClaimAsString("department");
        String region = jwt.getClaimAsString("region");
        String fullName = jwt.getClaimAsString("full_name");

        // Convertir userId String a Long
        Long userIdLong = null;
        try {
            userIdLong = Long.parseLong(userId);
        } catch (NumberFormatException e) {
            log.warn("Could not parse userId '{}' as Long, using hash code", userId);
            userIdLong = (long) userId.hashCode();
        }

        return new CustomUserDetails(
            email,  // username
            "",     // password (no necesario)
            grantedAuthoritiesConverter.convert(jwt),
            userIdLong,
            email,
            department,
            region,
            fullName
        );
    });

    return jwtAuthenticationConverter;
}
```

### A.2. Configuración en SecurityFilterChain

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        // ...existing code...
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt
                .decoder(jwtDecoder())
                .jwtAuthenticationConverter(jwtAuthenticationConverter()) // ← AÑADIR ESTA LÍNEA
            )
        );
    // ...existing code...
    return http.build();
}
```

### A.3. Estructura Esperada del JWT de Supabase

```json
{
  "sub": "123456789",
  "email": "usuario@ejemplo.com",
  "full_name": "Juan Pérez",
  "department": "Ventas",
  "region": "Norte",
  "roles": ["user", "manager"],
  "iat": 1698000000,
  "exp": 1698086400
}
```

