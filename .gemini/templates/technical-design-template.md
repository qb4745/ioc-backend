# 📄 PROMPT 6: Technical Design Generator

Perfecto! Ahora vamos con el **Technical Design Generator** que convierte el QUÉ (Feature Plan) en el CÓMO técnico.

---

## PARTE 1: Plantilla del Technical Design

**Archivo**: `@.gemini/templates/technical-design-template.md`

```markdown
# Technical Design: [Nombre de la Feature]

## Metadata
- **ID**: TD-[XXX]
- **Feature Plan**: [FP-XXX-nombre.md]
- **Sprint**: Sprint [X]
- **Autor**: [Nombre del Tech Lead o IA]
- **Reviewers**: [Lista de revisores técnicos]
- **Estado**: 
  - [ ] Draft
  - [ ] En Revisión
  - [ ] Aprobado
  - [ ] Implementado
- **Fecha Creación**: [YYYY-MM-DD]
- **Última Actualización**: [YYYY-MM-DD]
- **Versión**: 1.0

---

## 1. Resumen Ejecutivo

### 1.1. Propósito de Este Documento

Este documento describe la **arquitectura técnica** y el **plan de implementación** detallado para la feature:

**[Nombre de la Feature]**

Basado en el Feature Plan: `[FP-XXX-nombre.md]`

### 1.2. Alcance Técnico

**Qué cubre este diseño**:
- Arquitectura de componentes frontend y backend
- Modelo de datos detallado (schemas, migraciones)
- Contratos de API completos (request/response)
- Estrategia de seguridad y validación
- Plan de testing
- Consideraciones de deployment

**Qué NO cubre**:
- Justificación de negocio (ver Feature Plan)
- Diseño visual/UX (ver mockups en FP)
- Roadmap de producto (ver Sprint Backlog)

### 1.3. Decisiones Arquitectónicas Clave

| Decisión | Opción Elegida | Alternativas Consideradas | Justificación |
|----------|----------------|---------------------------|---------------|
| [Decisión 1] | [Opción] | [Alt 1, Alt 2] | [Razón técnica] |
| [Decisión 2] | [Opción] | [Alt 1, Alt 2] | [Razón técnica] |

---

## 2. Arquitectura de la Solución

### 2.1. Diagrama de Arquitectura de Alto Nivel

```
┌─────────────────────────────────────────────────────────────┐
│                       FRONTEND                              │
│                                                             │
│  ┌──────────────────┐        ┌──────────────────────┐     │
│  │ [Componente A]   │◄───────┤ [Servicio API]       │     │
│  │ (UI Component)   │        │ (HTTP Client)        │     │
│  └──────────────────┘        └──────────────────────┘     │
│           │                            │                   │
│           ▼                            ▼                   │
│  ┌──────────────────┐        ┌──────────────────────┐     │
│  │ [Store/State]    │        │ [WebSocket Client]   │     │
│  │ (Zustand/Redux)  │        │ (si aplica)          │     │
│  └──────────────────┘        └──────────────────────┘     │
└────────────────────────────────┬────────────────────────────┘
                                 │ HTTPS/WSS
                                 │ JWT Bearer Token
┌────────────────────────────────▼────────────────────────────┐
│                       BACKEND                               │
│                                                             │
│  ┌──────────────────┐        ┌──────────────────────┐     │
│  │ [Controller]     │───────►│ [Service Layer]      │     │
│  │ (REST/WebSocket) │        │ (Business Logic)     │     │
│  └──────────────────┘        └──────────────────────┘     │
│           │                            │                   │
│           │ Security Filter            ▼                   │
│           ▼                   ┌──────────────────────┐     │
│  ┌──────────────────┐         │ [Repository]         │     │
│  │ JWT Validator    │         │ (Data Access)        │     │
│  └──────────────────┘         └──────────────────────┘     │
└────────────────────────────────┬────────────────────────────┘
                                 │
                      ┌──────────┴─────────┬──────────────┐
                      ▼                    ▼              ▼
              ┌──────────────┐    ┌──────────────┐  ┌─────────┐
              │ PostgreSQL   │    │ [Servicio    │  │ [Cache] │
              │ (Primary DB) │    │  Externo]    │  │ Redis   │
              └──────────────┘    └──────────────┘  └─────────┘
```

### 2.2. Flujo de Datos Detallado

#### Escenario 1: [Nombre del Flujo Principal]

```
[Usuario] → [Acción UI]
    ↓
[Frontend Component]
    ↓ (1) Validar input local
    ↓ (2) Dispatch action al store
    ↓
[API Service]
    ↓ (3) HTTP POST con JWT en header
    ↓
[Backend Controller]
    ↓ (4) Security Filter valida JWT
    ↓ (5) Extrae userId del token
    ↓
[Service Layer]
    ↓ (6) Valida datos (Bean Validation)
    ↓ (7) Ejecuta lógica de negocio
    ↓ (8) Llama a servicio externo (si aplica)
    ↓
[Repository]
    ↓ (9) Persiste en DB (transacción)
    ↓
[DB PostgreSQL]
    ↓ (10) Commit exitoso
    ↓
← [Service Layer]
    ↓ (11) Construye DTO de respuesta
    ↓
← [Controller]
    ↓ (12) Retorna 200 OK con DTO
    ↓
← [Frontend]
    ↓ (13) Actualiza store
    ↓ (14) Re-render componente
    ↓
[Usuario ve resultado]
```

**Tiempos Estimados**:
- Frontend (1-2): ~10ms
- HTTP Request (3): ~50ms
- Backend Processing (4-11): ~200ms
- DB Transaction (9-10): ~50ms
- HTTP Response (12): ~50ms
- Frontend Update (13-14): ~20ms
- **Total**: ~380ms (target: <500ms)

---

#### Escenario 2: [Flujo de Error]

```
[Usuario] → [Acción inválida]
    ↓
[Frontend Component]
    ↓ Validación local FALLA
    ↓
[Mostrar error en UI] → FIN (no llega al backend)

--- O ---

[Frontend] → [Request válido]
    ↓
[Backend Controller]
    ↓ JWT inválido
    ↓
[Security Filter] → 401 Unauthorized
    ↓
[Frontend]
    ↓ Interceptor detecta 401
    ↓ Redirige a /login
    ↓ Limpia tokens
```

---

### 2.3. Componentes y Responsabilidades

#### Frontend

| Componente | Tipo | Responsabilidad | Dependencias |
|------------|------|----------------|--------------|
| `[ComponenteA]` | Page Component | Orquestación del flujo | `[ServicioX]`, `[StoreY]` |
| `[ComponenteB]` | UI Component | Renderizado de datos | Ninguna (presentacional) |
| `[ServicioX]` | Service | Comunicación con API | `axios`, `[utils]` |
| `[StoreY]` | State Management | Estado global de [recurso] | `zustand` |
| `[HookZ]` | Custom Hook | Lógica reutilizable | `react`, `[ServicioX]` |

#### Backend

| Componente | Tipo | Responsabilidad | Dependencias |
|------------|------|----------------|--------------|
| `[Nombre]Controller` | REST Controller | Exponer endpoints HTTP | `[Nombre]Service` |
| `[Nombre]Service` | Service | Lógica de negocio | `[Nombre]Repository`, `[OtroService]` |
| `[Nombre]Repository` | JPA Repository | Acceso a datos | Spring Data JPA |
| `[Nombre]Entity` | Entity | Modelo de base de datos | JPA Annotations |
| `[Nombre]DTO` | DTO | Transferencia de datos | Ninguna |
| `[Nombre]Mapper` | Mapper | Conversión Entity ↔ DTO | MapStruct (opcional) |

---

## 3. Modelo de Datos

### 3.1. Diagrama Entidad-Relación

```
┌─────────────────────┐
│      users          │
├─────────────────────┤
│ id (PK)             │───┐
│ email               │   │
│ created_at          │   │ 1
└─────────────────────┘   │
                          │
                          │ N
                          │
                     ┌────▼──────────────────┐
                     │  [nueva_entidad]      │
                     ├───────────────────────┤
                     │ id (PK)               │
                     │ user_id (FK)          │───┐
                     │ campo_1               │   │
                     │ campo_2               │   │ 1
                     │ status                │   │
                     │ created_at            │   │
                     │ updated_at            │   │
                     └───────────────────────┘   │
                                                 │
                                                 │ N
                                                 │
                                          ┌──────▼──────────┐
                                          │ [otra_entidad]  │
                                          ├─────────────────┤
                                          │ id (PK)         │
                                          │ parent_id (FK)  │
                                          │ data            │
                                          └─────────────────┘
```

---

### 3.2. Especificación de Tablas

#### Tabla: `[nombre_tabla_nueva]`

**Propósito**: [Descripción de qué almacena esta tabla]

**Schema SQL**:

```sql
CREATE TABLE [nombre_tabla] (
    -- Identificación
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Relaciones
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    
    -- Datos de negocio
    campo_1 VARCHAR(255) NOT NULL,
    campo_2 INTEGER DEFAULT 0,
    campo_3 JSONB,  -- Para datos flexibles
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    
    -- Auditoría
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id),
    
    -- Constraints
    CONSTRAINT check_status CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED')),
    CONSTRAINT check_campo_2_positive CHECK (campo_2 >= 0)
);

-- Índices para performance
CREATE INDEX idx_[tabla]_user_id ON [nombre_tabla](user_id);
CREATE INDEX idx_[tabla]_status ON [nombre_tabla](status) WHERE status != 'COMPLETED';
CREATE INDEX idx_[tabla]_created_at ON [nombre_tabla](created_at DESC);

-- Índice compuesto para queries comunes
CREATE INDEX idx_[tabla]_user_status_created 
    ON [nombre_tabla](user_id, status, created_at DESC);
```

**Estimación de Tamaño**:
- Registros esperados año 1: [X]
- Crecimiento estimado: [Y] registros/mes
- Tamaño por registro: ~[Z] KB
- Tamaño total año 1: ~[X * Z] MB

**Estrategia de Archivado**:
- [SI APLICA] Archivar registros > 1 año a tabla `[nombre]_archive`
- [SI APLICA] Purgar registros con status 'FAILED' > 6 meses

---

#### Modificaciones a Tabla Existente: `users`

**Razón**: [Por qué necesitamos modificar esta tabla]

**Migration SQL**:

```sql
-- Agregar nuevos campos
ALTER TABLE users 
    ADD COLUMN last_notification_read_at TIMESTAMP,
    ADD COLUMN preferences JSONB DEFAULT '{}';

-- Crear índice si es necesario
CREATE INDEX idx_users_last_notification 
    ON users(last_notification_read_at) 
    WHERE last_notification_read_at IS NOT NULL;

-- Poblar datos iniciales (si es necesario)
UPDATE users 
SET preferences = '{"notifications_enabled": true}'::jsonb 
WHERE preferences IS NULL;
```

**Impacto**:
- ✅ Sin downtime (campos nullable o con defaults)
- ⚠️ Requiere reindex (índice nuevo)
- ✅ Backward compatible (columnas opcionales)

**Rollback**:

```sql
-- En caso de necesitar revertir
DROP INDEX IF EXISTS idx_users_last_notification;
ALTER TABLE users 
    DROP COLUMN IF EXISTS last_notification_read_at,
    DROP COLUMN IF EXISTS preferences;
```

---

### 3.3. Tipos TypeScript (Compartidos Frontend/Backend)

```typescript
// types/[nombre-feature].types.ts

/**
 * Estado de un [recurso]
 */
export enum [Recurso]Status {
  PENDING = 'PENDING',
  PROCESSING = 'PROCESSING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED'
}

/**
 * Modelo completo de [Recurso]
 */
export interface [Recurso] {
  id: string;                    // UUID
  userId: string;                // UUID
  campo1: string;
  campo2: number;
  campo3?: Record<string, any>;  // JSONB
  status: [Recurso]Status;
  createdAt: string;             // ISO 8601
  updatedAt: string;             // ISO 8601
  createdBy?: string;            // UUID
}

/**
 * DTO para crear un nuevo [Recurso]
 */
export interface Create[Recurso]Request {
  campo1: string;
  campo2: number;
  campo3?: Record<string, any>;
}

/**
 * DTO de respuesta
 */
export interface [Recurso]Response {
  id: string;
  campo1: string;
  campo2: number;
  status: [Recurso]Status;
  createdAt: string;
  // Campos calculados/derivados
  isProcessable: boolean;
  canRetry: boolean;
}

/**
 * DTO para actualización parcial
 */
export interface Update[Recurso]Request {
  campo1?: string;
  campo2?: number;
  status?: [Recurso]Status;
}

/**
 * Query params para listados
 */
export interface [Recurso]QueryParams {
  page?: number;
  size?: number;
  status?: [Recurso]Status;
  sortBy?: 'createdAt' | 'campo2';
  order?: 'asc' | 'desc';
  search?: string;
}

/**
 * Respuesta paginada
 */
export interface [Recurso]ListResponse {
  items: [Recurso]Response[];
  pagination: {
    totalElements: number;
    totalPages: number;
    currentPage: number;
    pageSize: number;
    hasNext: boolean;
    hasPrevious: boolean;
  };
}
```

---

### 3.4. Entidades JPA (Backend)

```java
// model/entity/[Recurso].java

package com.cambiaso.ioc.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "[nombre_tabla]",
    indexes = {
        @Index(name = "idx_[tabla]_user_id", columnList = "user_id"),
        @Index(name = "idx_[tabla]_status", columnList = "status"),
        @Index(name = "idx_[tabla]_created_at", columnList = "created_at")
    }
)
public class [Recurso] {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "campo_1", nullable = false, length = 255)
    @NotBlank(message = "Campo1 es requerido")
    @Size(max = 255, message = "Campo1 no puede exceder 255 caracteres")
    private String campo1;
    
    @Column(name = "campo_2", nullable = false)
    @Min(value = 0, message = "Campo2 debe ser positivo")
    private Integer campo2;
    
    @Column(name = "campo_3", columnDefinition = "jsonb")
    @Convert(converter = JsonbConverter.class)  // Converter personalizado
    private Map<String, Object> campo3;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private [Recurso]Status status = [Recurso]Status.PENDING;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
    
    // Constructors
    public [Recurso]() {}
    
    public [Recurso](User user, String campo1, Integer campo2) {
        this.user = user;
        this.campo1 = campo1;
        this.campo2 = campo2;
    }
    
    // Getters y Setters
    // ...
    
    // Business Logic Methods
    public boolean canRetry() {
        return this.status == [Recurso]Status.FAILED;
    }
    
    public void markAsProcessing() {
        this.status = [Recurso]Status.PROCESSING;
    }
    
    public void markAsCompleted() {
        this.status = [Recurso]Status.COMPLETED;
    }
    
    public void markAsFailed() {
        this.status = [Recurso]Status.FAILED;
    }
    
    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof [Recurso])) return false;
        [Recurso] that = ([Recurso]) o;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
```

**Enum**:

```java
// model/enums/[Recurso]Status.java

package com.cambiaso.ioc.model.enums;

public enum [Recurso]Status {
    PENDING("Pendiente"),
    PROCESSING("En Proceso"),
    COMPLETED("Completado"),
    FAILED("Fallido");
    
    private final String displayName;
    
    [Recurso]Status(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
```

---

## 4. Contratos de API

### 4.1. Endpoints REST

#### Endpoint 1: Crear [Recurso]

```typescript
POST /api/v1/[recursos]
```

**Propósito**: Crear un nuevo [recurso]

**Autenticación**: ✅ Requerida (JWT)  
**Roles Permitidos**: `ADMIN`, `USER`  
**Rate Limit**: 100 requests/minuto por usuario

---

**Request**:

```typescript
// Headers
Authorization: Bearer <jwt_token>
Content-Type: application/json

// Body
{
  "campo1": "valor ejemplo",
  "campo2": 42,
  "campo3": {
    "metadata": "opcional"
  }
}
```

**Validaciones**:
- `campo1`: Requerido, max 255 caracteres, no vacío
- `campo2`: Requerido, entero >= 0
- `campo3`: Opcional, objeto JSON válido

---

**Response (201 Created)**:

```typescript
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "campo1": "valor ejemplo",
  "campo2": 42,
  "status": "PENDING",
  "createdAt": "2024-01-15T10:30:00Z",
  "isProcessable": true,
  "canRetry": false
}
```

---

**Response (400 Bad Request)** - Validación fallida:

```typescript
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Errores de validación",
  "path": "/api/v1/[recursos]",
  "errors": [
    {
      "field": "campo1",
      "rejectedValue": "",
      "message": "Campo1 es requerido"
    },
    {
      "field": "campo2",
      "rejectedValue": -5,
      "message": "Campo2 debe ser positivo"
    }
  ]
}
```

---

**Response (401 Unauthorized)** - Token inválido:

```typescript
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Token JWT inválido o expirado",
  "path": "/api/v1/[recursos]"
}
```

---

**Response (429 Too Many Requests)** - Rate limit:

```typescript
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 429,
  "error": "Too Many Requests",
  "message": "Límite de peticiones excedido. Intenta de nuevo en 42 segundos",
  "path": "/api/v1/[recursos]",
  "retryAfter": 42
}
```

---

**Response (500 Internal Server Error)** - Error inesperado:

```typescript
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Error procesando la solicitud",
  "path": "/api/v1/[recursos]",
  "traceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

---

**Implementación Backend**:

```java
// controller/[Recurso]Controller.java

@RestController
@RequestMapping("/api/v1/[recursos]")
@RateLimiter(name = "[recursos]")
public class [Recurso]Controller {
    
    private final [Recurso]Service service;
    
    @Autowired
    public [Recurso]Controller([Recurso]Service service) {
        this.service = service;
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<[Recurso]Response> create(
        @Valid @RequestBody Create[Recurso]Request request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        [Recurso]Response response = service.create(userId, request);
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
    }
}
```

**Implementación Service**:

```java
// service/[Recurso]Service.java

@Service
@Transactional
public class [Recurso]Service {
    
    private final [Recurso]Repository repository;
    private final UserRepository userRepository;
    
    @Autowired
    public [Recurso]Service(
        [Recurso]Repository repository,
        UserRepository userRepository
    ) {
        this.repository = repository;
        this.userRepository = userRepository;
    }
    
    public [Recurso]Response create(UUID userId, Create[Recurso]Request request) {
        // 1. Buscar usuario
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        
        // 2. Crear entidad
        [Recurso] entity = new [Recurso]();
        entity.setUser(user);
        entity.setCampo1(request.getCampo1());
        entity.setCampo2(request.getCampo2());
        entity.setCampo3(request.getCampo3());
        entity.setStatus([Recurso]Status.PENDING);
        
        // 3. Validaciones de negocio adicionales
        validateBusinessRules(entity);
        
        // 4. Persistir
        [Recurso] saved = repository.save(entity);
        
        // 5. Lanzar procesamiento asíncrono (si aplica)
        // eventPublisher.publish(new [Recurso]CreatedEvent(saved.getId()));
        
        // 6. Convertir a DTO
        return toResponse(saved);
    }
    
    private void validateBusinessRules([Recurso] entity) {
        // Validaciones custom de negocio
        if (entity.getCampo2() > 1000) {
            throw new BusinessException("Campo2 no puede ser mayor a 1000");
        }
    }
    
    private [Recurso]Response toResponse([Recurso] entity) {
        [Recurso]Response response = new [Recurso]Response();
        response.setId(entity.getId().toString());
        response.setCampo1(entity.getCampo1());
        response.setCampo2(entity.getCampo2());
        response.setStatus(entity.getStatus());
        response.setCreatedAt(entity.getCreatedAt().toString());
        response.setIsProcessable(entity.getStatus() == [Recurso]Status.PENDING);
        response.setCanRetry(entity.canRetry());
        return response;
    }
}
```

---

#### Endpoint 2: Listar [Recursos]

```typescript
GET /api/v1/[recursos]
```

**Propósito**: Obtener lista paginada de [recursos] del usuario actual

**Autenticación**: ✅ Requerida  
**Roles**: `ADMIN`, `USER`  
**Rate Limit**: 300 requests/minuto

---

**Request**:

```typescript
// Headers
Authorization: Bearer <jwt_token>

// Query Params (todos opcionales)
?page=0               // Número de página (default: 0)
&size=20              // Tamaño de página (default: 20, max: 100)
&status=PENDING       // Filtrar por status
&sortBy=createdAt     // Campo para ordenar (createdAt | campo2)
&order=desc           // Orden (asc | desc, default: desc)
&search=texto         // Búsqueda en campo1
```

---

**Response (200 OK)**:

```typescript
{
  "items": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "campo1": "valor 1",
      "campo2": 42,
      "status": "COMPLETED",
      "createdAt": "2024-01-15T10:30:00Z",
      "isProcessable": false,
      "canRetry": false
    },
    {
      "id": "223e4567-e89b-12d3-a456-426614174001",
      "campo1": "valor 2",
      "campo2": 33,
      "status": "PENDING",
      "createdAt": "2024-01-15T09:15:00Z",
      "isProcessable": true,
      "canRetry": false
    }
  ],
  "pagination": {
    "totalElements": 47,
    "totalPages": 3,
    "currentPage": 0,
    "pageSize": 20,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

---

**Implementación Backend**:

```java
@GetMapping
@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
public ResponseEntity<[Recurso]ListResponse> list(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") @Max(100) int size,
    @RequestParam(required = false) [Recurso]Status status,
    @RequestParam(defaultValue = "createdAt") String sortBy,
    @RequestParam(defaultValue = "desc") String order,
    @RequestParam(required = false) String search,
    @AuthenticationPrincipal Jwt jwt
) {
    UUID userId = UUID.fromString(jwt.getSubject());
    
    [Recurso]QueryParams queryParams = [Recurso]QueryParams.builder()
        .page(page)
        .size(size)
        .status(status)
        .sortBy(sortBy)
        .order(order)
        .search(search)
        .build();
    
    [Recurso]ListResponse response = service.list(userId, queryParams);
    
    return ResponseEntity.ok(response);
}
```

**Implementación Service con Specifications**:

```java
public [Recurso]ListResponse list(UUID userId, [Recurso]QueryParams params) {
    // Construir paginación
    Sort sort = params.getOrder().equals("asc") 
        ? Sort.by(params.getSortBy()).ascending()
        : Sort.by(params.getSortBy()).descending();
    
    Pageable pageable = PageRequest.of(params.getPage(), params.getSize(), sort);
    
    // Construir specifications para filtros dinámicos
    Specification<[Recurso]> spec = Specification.where(
        [Recurso]Specifications.belongsToUser(userId)
    );
    
    if (params.getStatus() != null) {
        spec = spec.and([Recurso]Specifications.hasStatus(params.getStatus()));
    }
    
    if (params.getSearch() != null && !params.getSearch().isBlank()) {
        spec = spec.and([Recurso]Specifications.searchInCampo1(params.getSearch()));
    }
    
    // Ejecutar query
    Page<[Recurso]> page = repository.findAll(spec, pageable);
    
    // Convertir a DTO
    List<[Recurso]Response> items = page.getContent().stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
    
    // Construir respuesta
    return [Recurso]ListResponse.builder()
        .items(items)
        .pagination(PaginationDTO.fromPage(page))
        .build();
}
```

**Specifications**:

```java
// repository/specification/[Recurso]Specifications.java

public class [Recurso]Specifications {
    
    public static Specification<[Recurso]> belongsToUser(UUID userId) {
        return (root, query, cb) -> 
            cb.equal(root.get("user").get("id"), userId);
    }
    
    public static Specification<[Recurso]> hasStatus([Recurso]Status status) {
        return (root, query, cb) -> 
            cb.equal(root.get("status"), status);
    }
    
    public static Specification<[Recurso]> searchInCampo1(String search) {
        return (root, query, cb) -> 
            cb.like(
                cb.lower(root.get("campo1")), 
                "%" + search.toLowerCase() + "%"
            );
    }
}
```

---

#### Endpoint 3: Obtener [Recurso] por ID

```typescript
GET /api/v1/[recursos]/{id}
```

**Propósito**: Obtener detalles de un [recurso] específico

**Path Params**:
- `id`: UUID del recurso

**Autenticación**: ✅ Requerida  
**Roles**: `ADMIN`, `USER`

---

**Response (200 OK)**:

```typescript
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "campo1": "valor ejemplo",
  "campo2": 42,
  "campo3": {
    "metadata": "ejemplo"
  },
  "status": "COMPLETED",
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:35:00Z",
  "isProcessable": false,
  "canRetry": false
}
```

**Response (404 Not Found)**:

```typescript
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "[Recurso] no encontrado",
  "path": "/api/v1/[recursos]/123e4567-e89b-12d3-a456-426614174000"
}
```

**Response (403 Forbidden)** - Usuario intenta acceder a recurso de otro usuario:

```typescript
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 403,
  "error": "Forbidden",
  "message": "No tienes permiso para acceder a este recurso",
  "path": "/api/v1/[recursos]/123e4567-e89b-12d3-a456-426614174000"
}
```

---

**Implementación**:

```java
@GetMapping("/{id}")
@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
public ResponseEntity<[Recurso]Response> getById(
    @PathVariable UUID id,
    @AuthenticationPrincipal Jwt jwt
) {
    UUID userId = UUID.fromString(jwt.getSubject());
    [Recurso]Response response = service.getById(id, userId);
    return ResponseEntity.ok(response);
}
```

```java
// Service
public [Recurso]Response getById(UUID id, UUID userId) {
    [Recurso] entity = repository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("[Recurso] no encontrado"));
    
    // Verificar que el recurso pertenece al usuario
    // (Los ADMIN pueden ver todos)
    if (!entity.getUser().getId().equals(userId) && !isAdmin(userId)) {
        throw new ForbiddenException("No tienes permiso para acceder a este recurso");
    }
    
    return toResponse(entity);
}
```

---

#### Endpoint 4: Actualizar [Recurso]

```typescript
PATCH /api/v1/[recursos]/{id}
```

**Propósito**: Actualización parcial de un [recurso]

**Request**:

```typescript
// Headers
Authorization: Bearer <jwt_token>
Content-Type: application/json

// Body (todos los campos opcionales)
{
  "campo1": "nuevo valor",
  "campo2": 50,
  "status": "COMPLETED"
}
```

**Response (200 OK)**:

```typescript
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "campo1": "nuevo valor",
  "campo2": 50,
  "status": "COMPLETED",
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T11:00:00Z",  // Actualizado
  "isProcessable": false,
  "canRetry": false
}
```

---

#### Endpoint 5: Eliminar [Recurso]

```typescript
DELETE /api/v1/[recursos]/{id}
```

**Propósito**: Eliminar un [recurso] (soft delete o hard delete según lógica de negocio)

**Autenticación**: ✅ Requerida  
**Roles**: `ADMIN` (solo admins pueden eliminar)

**Response (204 No Content)**: Sin body, eliminación exitosa

**Response (404 Not Found)**: Recurso no existe

**Response (409 Conflict)**: No se puede eliminar (ej: tiene dependencias)

```typescript
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "No se puede eliminar [recurso] porque tiene [dependencias] asociadas",
  "path": "/api/v1/[recursos]/123e4567-e89b-12d3-a456-426614174000"
}
```

---

### 4.2. Endpoints WebSocket (Si Aplica)

#### WebSocket: Notificaciones en Tiempo Real

```typescript
WS /ws/[recursos]
```

**Propósito**: Recibir actualizaciones en tiempo real del estado de [recursos]

**Autenticación**: ✅ JWT en query param o header custom

**Conexión**:

```typescript
// Frontend
const socket = new WebSocket(
  'wss://api.ejemplo.com/ws/[recursos]?token=' + jwtToken
);

socket.onopen = () => {
  console.log('Conectado');
};

socket.onmessage = (event) => {
  const update = JSON.parse(event.data);
  // Procesar actualización
};

socket.onerror = (error) => {
  console.error('WebSocket error:', error);
};

socket.onclose = () => {
  console.log('Desconectado');
  // Reconectar con backoff
};
```

**Eventos del Servidor → Cliente**:

```typescript
// Evento: [recurso]:updated
{
  "event": "[recurso]:updated",
  "data": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "status": "COMPLETED",
    "campo2": 100,
    "updatedAt": "2024-01-15T11:00:00Z"
  },
  "timestamp": "2024-01-15T11:00:00.123Z"
}

// Evento: [recurso]:deleted
{
  "event": "[recurso]:deleted",
  "data": {
    "id": "123e4567-e89b-12d3-a456-426614174000"
  },
  "timestamp": "2024-01-15T11:05:00.456Z"
}

// Evento: error
{
  "event": "error",
  "data": {
    "message": "Error de autenticación",
    "code": "UNAUTHORIZED"
  },
  "timestamp": "2024-01-15T11:00:00.789Z"
}
```

**Implementación Backend (Spring WebSocket)**:

```java
// config/WebSocketConfig.java

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    
    private final [Recurso]WebSocketHandler handler;
    private final WebSocketAuthInterceptor authInterceptor;
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ws/[recursos]")
            .addInterceptors(authInterceptor)
            .setAllowedOrigins("*");  // Configurar CORS apropiadamente
    }
}
```

```java
// websocket/[Recurso]WebSocketHandler.java

@Component
public class [Recurso]WebSocketHandler extends TextWebSocketHandler {
    
    private final Map<UUID, WebSocketSession> sessions = new ConcurrentHashMap<>();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        UUID userId = extractUserIdFromSession(session);
        sessions.put(userId, session);
        logger.info("Usuario {} conectado via WebSocket", userId);
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        UUID userId = extractUserIdFromSession(session);
        sessions.remove(userId);
        logger.info("Usuario {} desconectado", userId);
    }
    
    public void sendUpdate(UUID userId, [Recurso] entity) {
        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                WebSocketMessage message = WebSocketMessage.builder()
                    .event("[recurso]:updated")
                    .data(toUpdateDTO(entity))
                    .timestamp(Instant.now())
                    .build();
                
                session.sendMessage(new TextMessage(
                    objectMapper.writeValueAsString(message)
                ));
            } catch (IOException e) {
                logger.error("Error enviando mensaje WebSocket", e);
            }
        }
    }
}
```

---

## 5. Seguridad

### 5.1. Autenticación

**Mecanismo**: JWT (JSON Web Tokens) emitidos por Supabase

**Flujo**:
1. Usuario se autentica en Supabase (frontend)
2. Supabase retorna `access_token` (JWT)
3. Frontend incluye token en todas las peticiones: `Authorization: Bearer <token>`
4. Backend valida token usando JWKS de Supabase
5. Backend extrae `userId` del claim `sub`

**Configuración Spring Security**:

```java
// config/SecurityConfig.java

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Value("${supabase.jwt.issuer}")
    private String jwtIssuer;
    
    @Value("${supabase.jwks-uri}")
    private String jwksUri;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())  // API REST stateless
            .cors(cors -> cors.configurationSource(corsConfig()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/api/v1/**").authenticated()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                )
            );
        
        return http.build();
    }
    
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder
            .withJwkSetUri(jwksUri)
            .build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfig() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173", "https://app.ejemplo.com"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
```

---

### 5.2. Autorización

**Modelo**: Role-Based Access Control (RBAC)

**Roles**:
- `ADMIN`: Acceso completo (CRUD sobre todos los recursos)
- `USER`: Acceso a sus propios recursos (CRUD sobre recursos propios)

**Almacenamiento de Roles**:
- En tabla `user_roles` de PostgreSQL
- Cargados en `GrantedAuthority` de Spring Security

**Verificación de Ownership**:

```java
// Verificar que el usuario solo acceda a sus propios recursos
public [Recurso] getById(UUID id, UUID userId) {
    [Recurso] entity = repository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException());
    
    // Los ADMIN pueden ver cualquier recurso
    if (hasRole(userId, "ADMIN")) {
        return entity;
    }
    
    // Los USER solo ven los suyos
    if (!entity.getUser().getId().equals(userId)) {
        throw new ForbiddenException("No tienes permiso");
    }
    
    return entity;
}
```

---

### 5.3. Validación de Datos

**Frontend (Primera Línea)**:

```typescript
// Validación con Zod
import { z } from 'zod';

const create[Recurso]Schema = z.object({
  campo1: z.string()
    .min(1, 'Campo1 es requerido')
    .max(255, 'Máximo 255 caracteres'),
  campo2: z.number()
    .int('Debe ser entero')
    .min(0, 'Debe ser positivo')
    .max(1000, 'Máximo 1000'),
  campo3: z.record(z.any()).optional()
});

// En el componente
const handleSubmit = async (data: unknown) => {
  try {
    const validated = create[Recurso]Schema.parse(data);
    await api.create[Recurso](validated);
  } catch (error) {
    if (error instanceof z.ZodError) {
      // Mostrar errores de validación
      showErrors(error.errors);
    }
  }
};
```

**Backend (Validación Definitiva)**:

```java
// dto/Create[Recurso]Request.java

public class Create[Recurso]Request {
    
    @NotBlank(message = "Campo1 es requerido")
    @Size(max = 255, message = "Máximo 255 caracteres")
    private String campo1;
    
    @NotNull(message = "Campo2 es requerido")
    @Min(value = 0, message = "Debe ser >= 0")
    @Max(value = 1000, message = "Debe ser <= 1000")
    private Integer campo2;
    
    private Map<String, Object> campo3;
    
    // Getters y Setters
}
```

**Validaciones Custom**:

```java
// validator/[Recurso]Validator.java

@Component
public class [Recurso]Validator {
    
    public void validateBusinessRules(Create[Recurso]Request request) {
        // Regla de negocio: campo1 no puede contener palabras prohibidas
        List<String> prohibitedWords = List.of("spam", "test");
        String campo1Lower = request.getCampo1().toLowerCase();
        
        for (String word : prohibitedWords) {
            if (campo1Lower.contains(word)) {
                throw new ValidationException(
                    "Campo1 contiene palabra prohibida: " + word
                );
            }
        }
        
        // Regla de negocio: si campo2 > 500, campo3 es obligatorio
        if (request.getCampo2() > 500 && 
            (request.getCampo3() == null || request.getCampo3().isEmpty())) {
            throw new ValidationException(
                "Campo3 es obligatorio cuando Campo2 > 500"
            );
        }
    }
}
```

---

### 5.4. Rate Limiting

**Implementación con Resilience4j**:

```yaml
# application.yml

resilience4j:
  ratelimiter:
    instances:
      [recursos]:
        limit-for-period: 100
        limit-refresh-period: 60s
        timeout-duration: 0s  # Fallar inmediatamente si se excede
```

```java
// Controller
@RateLimiter(name = "[recursos]", fallbackMethod = "rateLimitFallback")
@PostMapping
public ResponseEntity<[Recurso]Response> create(...) {
    // ...
}

public ResponseEntity<[Recurso]Response> rateLimitFallback(
    Exception e
) {
    return ResponseEntity
        .status(HttpStatus.TOO_MANY_REQUESTS)
        .body(ErrorResponse.builder()
            .message("Límite de peticiones excedido")
            .retryAfter(60)
            .build()
        );
}
```

---

### 5.5. Prevención de Ataques

**SQL Injection**:
- ✅ Protegido automáticamente por JPA (prepared statements)
- ✅ Nunca construir queries con concatenación de strings

**XSS (Cross-Site Scripting)**:
- ✅ Frontend sanitiza inputs antes de renderizar
- ✅ Backend escapa caracteres especiales en campos de texto

**CSRF (Cross-Site Request Forgery)**:
- ✅ No aplicable en API REST stateless (sin cookies de sesión)
- ✅ CSRF deshabilitado en Spring Security

**Mass Assignment**:
- ✅ Usar DTOs separados (no exponer entidades directamente)
- ✅ No permitir que el cliente envíe campos sensibles (`id`, `createdAt`, `createdBy`)

**Exposición de Información**:
- ✅ No incluir stack traces en respuestas de error en producción
- ✅ Usar `traceId` para correlación de logs sin exponer detalles

---

## 6. Testing

### 6.1. Estrategia de Testing

| Tipo de Test | Cobertura Objetivo | Herramientas | Responsable |
|--------------|-------------------|--------------|-------------|
| Unit Tests (Backend) | 80% de servicios y utils | JUnit 5, Mockito | Backend Dev |
| Integration Tests (Backend) | Endpoints críticos | Spring Boot Test, TestContainers | Backend Dev |
| Unit Tests (Frontend) | 70% de componentes | Vitest, Testing Library | Frontend Dev |
| Integration Tests (Frontend) | Flujos principales | Vitest + MSW | Frontend Dev |
| E2E Tests | Happy paths críticos | Playwright | QA / Frontend Lead |
| Performance Tests | Endpoints bajo carga | JMeter / k6 | DevOps |

---

### 6.2. Tests Unitarios Backend

```java
// service/[Recurso]ServiceTest.java

@ExtendWith(MockitoExtension.class)
class [Recurso]ServiceTest {
    
    @Mock
    private [Recurso]Repository repository;
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private [Recurso]Service service;
    
    @Test
    void create_ValidRequest_ReturnsResponse() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        
        Create[Recurso]Request request = new Create[Recurso]Request();
        request.setCampo1("test");
        request.setCampo2(42);
        
        [Recurso] entity = new [Recurso]();
        entity.setId(UUID.randomUUID());
        entity.setCampo1("test");
        entity.setCampo2(42);
        entity.setStatus([Recurso]Status.PENDING);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(repository.save(any([Recurso].class))).thenReturn(entity);
        
        // Act
        [Recurso]Response response = service.create(userId, request);
        
        // Assert
        assertNotNull(response);
        assertEquals("test", response.getCampo1());
        assertEquals(42, response.getCampo2());
        assertEquals([Recurso]Status.PENDING, response.getStatus());
        
        verify(userRepository).findById(userId);
        verify(repository).save(any([Recurso].class));
    }
    
    @Test
    void create_UserNotFound_ThrowsException() {
        // Arrange
        UUID userId = UUID.randomUUID();
        Create[Recurso]Request request = new Create[Recurso]Request();
        
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            service.create(userId, request);
        });
        
        verify(repository, never()).save(any());
    }
    
    @Test
    void create_InvalidBusinessRule_ThrowsException() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        
        Create[Recurso]Request request = new Create[Recurso]Request();
        request.setCampo1("test");
        request.setCampo2(2000);  // Excede límite de negocio (1000)
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        
        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            service.create(userId, request);
        });
    }
}
```

---

### 6.3. Tests de Integración Backend

```java
// controller/[Recurso]ControllerIntegrationTest.java

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
class [Recurso]ControllerIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private JwtDecoder jwtDecoder;  // Mock JWT validation
    
    @Test
    void create[Recurso]_ValidRequest_Returns201() throws Exception {
        // Arrange
        String userId = UUID.randomUUID().toString();
        mockJwtAuthentication(userId);
        
        Create[Recurso]Request request = Create[Recurso]Request.builder()
            .campo1("test")
            .campo2(42)
            .build();
        
        // Act & Assert
        mockMvc.perform(post("/api/v1/[recursos]")
                .header("Authorization", "Bearer mock-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.campo1").value("test"))
            .andExpect(jsonPath("$.campo2").value(42))
            .andExpect(jsonPath("$.status").value("PENDING"));
    }
    
    @Test
    void create[Recurso]_InvalidRequest_Returns400() throws Exception {
        // Arrange
        mockJwtAuthentication(UUID.randomUUID().toString());
        
        Create[Recurso]Request request = Create[Recurso]Request.builder()
            .campo1("")  // Inválido: vacío
            .campo2(-5)  // Inválido: negativo
            .build();
        
        // Act & Assert
        mockMvc.perform(post("/api/v1/[recursos]")
                .header("Authorization", "Bearer mock-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors").isArray())
            .andExpect(jsonPath("$.errors[0].field").exists());
    }
    
    @Test
    void create[Recurso]_NoAuth_Returns401() throws Exception {
        // Arrange
        Create[Recurso]Request request = Create[Recurso]Request.builder()
            .campo1("test")
            .campo2(42)
            .build();
        
        // Act & Assert
        mockMvc.perform(post("/api/v1/[recursos]")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }
    
    private void mockJwtAuthentication(String userId) {
        Jwt jwt = Jwt.withTokenValue("mock-token")
            .header("alg", "RS256")
            .subject(userId)
            .claim("email", "test@example.com")
            .build();
        
        when(jwtDecoder.decode(anyString())).thenReturn(jwt);
    }
}
```

---

### 6.4. Tests Unitarios Frontend

```typescript
// components/[Componente].test.tsx

import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { [Componente] } from './[Componente]';

describe('[Componente]', () => {
  it('renderiza correctamente con props', () => {
    render(<[Componente] campo1="test" campo2={42} />);
    
    expect(screen.getByText('test')).toBeInTheDocument();
    expect(screen.getByText('42')).toBeInTheDocument();
  });
  
  it('llama a onSubmit cuando se envía el formulario', async () => {
    const mockOnSubmit = vi.fn();
    
    render(<[Componente] onSubmit={mockOnSubmit} />);
    
    const input = screen.getByLabelText('Campo 1');
    fireEvent.change(input, { target: { value: 'nuevo valor' } });
    
    const submitButton = screen.getByRole('button', { name: /enviar/i });
    fireEvent.click(submitButton);
    
    await waitFor(() => {
      expect(mockOnSubmit).toHaveBeenCalledWith({
        campo1: 'nuevo valor',
        campo2: expect.any(Number)
      });
    });
  });
  
  it('muestra error de validación si campo1 está vacío', async () => {
    render(<[Componente] />);
    
    const submitButton = screen.getByRole('button', { name: /enviar/i });
    fireEvent.click(submitButton);
    
    await waitFor(() => {
      expect(screen.getByText('Campo1 es requerido')).toBeInTheDocument();
    });
  });
});
```

---

### 6.5. Tests de Integración Frontend (con MSW)

```typescript
// services/[recurso].service.test.ts

import { describe, it, expect, beforeAll, afterAll, afterEach } from 'vitest';
import { setupServer } from 'msw/node';
import { http, HttpResponse } from 'msw';
import { [Recurso]Service } from './[recurso].service';

const server = setupServer();

beforeAll(() => server.listen());
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

describe('[Recurso]Service', () => {
  it('create() envía request correcto y procesa respuesta', async () => {
    // Mock del endpoint
    server.use(
      http.post('/api/v1/[recursos]', async ({ request }) => {
        const body = await request.json();
        
        expect(body).toEqual({
          campo1: 'test',
          campo2: 42
        });
        
        return HttpResponse.json({
          id: '123e4567-e89b-12d3-a456-426614174000',
          campo1: 'test',
          campo2: 42,
          status: 'PENDING',
          createdAt: '2024-01-15T10:30:00Z'
        }, { status: 201 });
      })
    );
    
    // Ejecutar
    const service = new [Recurso]Service();
    const result = await service.create({
      campo1: 'test',
      campo2: 42
    });
    
    // Verificar
    expect(result.id).toBe('123e4567-e89b-12d3-a456-426614174000');
    expect(result.status).toBe('PENDING');
  });
  
  it('create() maneja error 400 correctamente', async () => {
    server.use(
      http.post('/api/v1/[recursos]', () => {
        return HttpResponse.json({
          status: 400,
          error: 'Bad Request',
          errors: [
            { field: 'campo1', message: 'Campo1 es requerido' }
          ]
        }, { status: 400 });
      })
    );
    
    const service = new [Recurso]Service();
    
    await expect(service.create({ campo1: '', campo2: 42 }))
      .rejects
      .toThrow('Campo1 es requerido');
  });
});
```

---

### 6.6. Tests E2E (Playwright)

```typescript
// e2e/[feature].spec.ts

import { test, expect } from '@playwright/test';

test.describe('[Feature] - Flujo completo', () => {
  test.beforeEach(async ({ page }) => {
    // Login
    await page.goto('/login');
    await page.fill('[name="email"]', 'test@example.com');
    await page.fill('[name="password"]', 'password123');
    await page.click('button[type="submit"]');
    await expect(page).toHaveURL('/dashboard');
  });
  
  test('usuario puede crear, ver y eliminar un [recurso]', async ({ page }) => {
    // Navegar a la página
    await page.goto('/[recursos]');
    
    // Crear nuevo recurso
    await page.click('button:has-text("Crear Nuevo")');
    await page.fill('[name="campo1"]', 'Test E2E');
    await page.fill('[name="campo2"]', '99');
    await page.click('button:has-text("Guardar")');
    
    // Verificar que aparece en la lista
    await expect(page.locator('text=Test E2E')).toBeVisible();
    await expect(page.locator('text=99')).toBeVisible();
    await expect(page.locator('text=PENDING')).toBeVisible();
    
    // Abrir detalles
    await page.click('text=Test E2E');
    await expect(page).toHaveURL(/\/[recursos]\/[a-f0-9-]+/);
    
    // Eliminar
    await page.click('button:has-text("Eliminar")');
    await page.click('button:has-text("Confirmar")');
    
    // Verificar que ya no está
    await expect(page).toHaveURL('/[recursos]');
    await expect(page.locator('text=Test E2E')).not.toBeVisible();
  });
  
  test('validación muestra errores correctamente', async ({ page }) => {
    await page.goto('/[recursos]');
    await page.click('button:has-text("Crear Nuevo")');
    
    // Intentar enviar sin llenar
    await page.click('button:has-text("Guardar")');
    
    // Verificar errores de validación
    await expect(page.locator('text=Campo1 es requerido')).toBeVisible();
    await expect(page.locator('text=Campo2 es requerido')).toBeVisible();
  });
});
```

---

## 7. Performance y Optimización

### 7.1. Objetivos de Performance

| Métrica | Objetivo | Crítico |
|---------|----------|---------|
| Tiempo de respuesta API (P95) | < 500ms | < 1s |
| Tiempo de carga inicial (FCP) | < 1.5s | < 3s |
| Throughput | > 100 req/s | > 50 req/s |
| Tamaño bundle JS | < 500 KB | < 1 MB |
| Queries a DB | < 5 por endpoint | < 10 |

---

### 7.2. Optimizaciones Backend

#### Caching

```java
// Cachear resultados de queries costosas
@Cacheable(value = "[recursos]", key = "#id")
public [Recurso]Response getById(UUID id, UUID userId) {
    // ... lógica
}

@CacheEvict(value = "[recursos]", key = "#id")
public void update(UUID id, Update[Recurso]Request request) {
    // ... lógica
}
```

**Configuración Redis (opcional)**:

```yaml
spring:
  cache:
    type: redis
  redis:
    host: localhost
    port: 6379
    time-to-live: 600000  # 10 minutos
```

---

#### N+1 Query Problem

```java
// ❌ MAL: N+1 queries
public List<[Recurso]Response> list(UUID userId) {
    List<[Recurso]> recursos = repository.findByUserId(userId);
    
    return recursos.stream()
        .map(r -> {
            User user = r.getUser();  // SELECT adicional por cada recurso!
            return toResponse(r);
        })
        .collect(Collectors.toList());
}

// ✅ BIEN: 1 query con JOIN FETCH
@Query("SELECT r FROM [Recurso] r JOIN FETCH r.user WHERE r.user.id = :userId")
List<[Recurso]> findByUserIdWithUser(@Param("userId") UUID userId);
```

---

#### Paginación Eficiente

```java
// Usar paginación siempre en listados
public Page<[Recurso]> list(Pageable pageable) {
    return repository.findAll(pageable);  // LIMIT/OFFSET en SQL
}

// Evitar count() costoso si no es necesario
@Query(value = "SELECT r FROM [Recurso] r WHERE r.user.id = :userId",
       countQuery = "SELECT COUNT(r) FROM [Recurso] r WHERE r.user.id = :userId")
Page<[Recurso]> findByUserIdOptimized(@Param("userId") UUID userId, Pageable pageable);
```

---

### 7.3. Optimizaciones Frontend

#### Code Splitting

```typescript
// Lazy loading de rutas
import { lazy, Suspense } from 'react';

const [Recurso]Page = lazy(() => import('./pages/[Recurso]Page'));

function App() {
  return (
    <Routes>
      <Route 
        path="/[recursos]" 
        element={
          <Suspense fallback={<Loading />}>
            <[Recurso]Page />
          </Suspense>
        } 
      />
    </Routes>
  );
}
```

---

#### Memoization

```typescript
// Evitar re-renders innecesarios
import { memo, useMemo, useCallback } from 'react';

export const [Componente] = memo(({ data, onUpdate }) => {
  // Memoizar cálculos costosos
  const processedData = useMemo(() => {
    return data.map(item => ({
      ...item,
      computed: expensiveCalculation(item)
    }));
  }, [data]);
  
  // Memoizar callbacks
  const handleClick = useCallback((id: string) => {
    onUpdate(id);
  }, [onUpdate]);
  
  return (
    <div>
      {processedData.map(item => (
        <Item key={item.id} data={item} onClick={handleClick} />
      ))}
    </div>
  );
});
```

---

#### Debouncing de Búsquedas

```typescript
// Evitar búsquedas en cada keystroke
import { useDebouncedValue } from '@/hooks/useDebouncedValue';

export function [SearchComponent]() {
  const [search, setSearch] = useState('');
  const debouncedSearch = useDebouncedValue(search, 300);  // 300ms delay
  
  useEffect(() => {
    if (debouncedSearch) {
      fetchResults(debouncedSearch);
    }
  }, [debouncedSearch]);
  
  return (
    <input 
      value={search} 
      onChange={(e) => setSearch(e.target.value)} 
    />
  );
}
```

---

### 7.4. Monitoreo de Performance

**Backend - Actuator Metrics**:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

**Frontend - Web Vitals**:

```typescript
// Reportar métricas a analytics
import { onCLS, onFID, onLCP } from 'web-vitals';

onCLS(console.log);  // Cumulative Layout Shift
onFID(console.log);  // First Input Delay
onLCP(console.log);  // Largest Contentful Paint
```

---

## 8. Deployment

### 8.1. Pipeline de CI/CD

```yaml
# .github/workflows/deploy-[feature].yml

name: Deploy [Feature]

on:
  push:
    branches: [main]
    paths:
      - 'src/**'
      - 'pom.xml'

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '21'
      - name: Run Tests
        run: mvn test
      - name: Upload Coverage
        uses: codecov/codecov-action@v3
  
  deploy-staging:
    needs: test
    runs-on: ubuntu-latest
    steps:
      - name: Deploy to Staging
        run: |
          # Deploy logic
          
  deploy-production:
    needs: deploy-staging
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    steps:
      - name: Deploy to Production
        run: |
          # Deploy logic
```

---

### 8.2. Migraciones de Base de Datos

**Flyway Migration**:

```sql
-- V003__add_[nombre_tabla].sql

-- Crear tabla
CREATE TABLE [nombre_tabla] (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    campo_1 VARCHAR(255) NOT NULL,
    campo_2 INTEGER DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Índices
CREATE INDEX idx_[tabla]_user_id ON [nombre_tabla](user_id);
CREATE INDEX idx_[tabla]_status ON [nombre_tabla](status);

-- Datos iniciales (si es necesario)
-- INSERT INTO ...
```

**Estrategia de Rollback**:

```sql
-- R003__rollback_[nombre_tabla].sql (manual)

DROP TABLE IF EXISTS [nombre_tabla] CASCADE;
```

---

### 8.3. Feature Flags (Opcional)

```typescript
// Habilitar feature gradualmente
const FEATURE_ENABLED = import.meta.env.VITE_ENABLE_[FEATURE] === 'true';

export function App() {
  return (
    <Routes>
      {FEATURE_ENABLED && (
        <Route path="/[recursos]" element={<[Recurso]Page />} />
      )}
    </Routes>
  );
}
```

---

## 9. Monitoreo y Observabilidad

### 9.1. Logging

**Backend**:

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class [Recurso]Service {
    private static final Logger logger = LoggerFactory.getLogger([Recurso]Service.class);
    
    public [Recurso]Response create(UUID userId, Create[Recurso]Request request) {
        logger.info("Creando [recurso] para usuario: {}", userId);
        
        try {
            // ...lógica
            logger.debug("[Recurso] creado exitosamente: {}", saved.getId());
            return toResponse(saved);
        } catch (Exception e) {
            logger.error("Error creando [recurso] para usuario {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }
}
```

---

### 9.2. Alertas

**Configurar alertas para**:
- ❌ Tasa de errores > 5%
- ⏱️ Latencia P95 > 1s
- 💾 Uso de DB > 80%
- 🔄 Tasa de retry > 20%

---

## 10. Documentación

### 10.1. Swagger/OpenAPI

```java
// Generar documentación automática
@OpenAPIDefinition(
    info = @Info(
        title = "[Feature] API",
        version = "1.0",
        description = "API para gestión de [recursos]"
    )
)
```

**Acceso**: `http://localhost:8080/swagger-ui.html`

---

### 10.2. README Técnico

Crear `docs/[feature]-README.md` con:
- Setup local
- Cómo ejecutar tests
- Cómo hacer deploy
- Troubleshooting común

---

## 11. Checklist de Implementación

### Backend
- [ ] Crear entidad JPA
- [ ] Crear repository
- [ ] Crear service con lógica de negocio
- [ ] Crear controller con endpoints
- [ ] Implementar validaciones
- [ ] Implementar seguridad (autenticación/autorización)
- [ ] Agregar rate limiting
- [ ] Escribir tests unitarios (service)
- [ ] Escribir tests de integración (controller)
- [ ] Crear migración de base de datos
- [ ] Agregar logging
- [ ] Documentar en Swagger

### Frontend
- [ ] Crear tipos TypeScript
- [ ] Crear servicio API
- [ ] Crear componentes UI
- [ ] Implementar validación de formularios
- [ ] Agregar manejo de errores
- [ ] Implementar estados de carga
- [ ] Escribir tests de componentes
- [ ] Escribir tests de integración (MSW)
- [ ] Optimizar performance (memoization, lazy loading)
- [ ] Agregar accesibilidad (ARIA labels)

### Integración
- [ ] Conectar frontend con backend real
- [ ] Testing E2E de flujo completo
- [ ] Probar en staging
- [ ] Validar performance end-to-end
- [ ] Code review
- [ ] Deploy a producción

---

## 12. Decisiones Pendientes

| ID | Decisión | Opciones | Deadline | Responsable |
|----|----------|----------|----------|-------------|
| [ID] | [Decisión técnica pendiente] | [Opciones] | [Fecha] | [Persona] |

---

## 13. Referencias

**Feature Plan**: `@.gemini/sprints/feature-plans/FP-[XXX]-[nombre].md`  
**Project Summary**: `@.gemini/project-summary.md`  
**Blueprints**: Pendiente (siguiente paso)  
**Backend Sync Brief**: Pendiente (generado después de blueprints)

---

## 14. Aprobaciones

| Rol | Nombre | Aprobado | Fecha | Comentarios |
|-----|--------|----------|-------|-------------|
| Tech Lead | [Nombre] | ⏳ Pendiente | - | - |
| Backend Lead | [Nombre] | ⏳ Pendiente | - | - |
| Frontend Lead | [Nombre] | ⏳ Pendiente | - | - |

---

## 15. Changelog

| Versión | Fecha | Autor | Cambios |
|---------|-------|-------|---------|
| 1.0 | [YYYY-MM-DD] | [Nombre/IA] | Creación inicial |

---

**Technical Design creado por**: [Nombre o "IA Technical Design Generator"]  
**Fecha**: [YYYY-MM-DD]  
**Última actualización**: [YYYY-MM-DD]
```

---
