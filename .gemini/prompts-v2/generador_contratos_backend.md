# Prompt: Backend API Brief Generator - IOC Platform

## 1. CONFIGURACIÓN (PARA EL HUMANO)

**Propósito**: Este prompt escanea recursivamente el código backend del proyecto IOC, extrae información de endpoints y genera un brief técnico para que el equipo Frontend implemente los contratos de API.

**Acción Requerida**: El agente preguntará sobre el alcance del análisis y ubicación de documentación si es necesario.

**Modo de Uso**: Copia y pega en tu herramienta de IA preferida.

***

## 2. CONTEXTO DEL PROYECTO (MAPA MENTAL DE LA IA)

### Stack Tecnológico Target

**Frontend**: React 19 + TypeScript + Vite + Tailwind CSS  
**Backend**: Spring Boot 3 + Java 21 + PostgreSQL  
**Auth**: Supabase (JWT Provider)  
**Analytics**: Metabase (AWS EC2)

### Arquitectura de Información

```

@.gemini/project-summary      ← Fuente Summary del Proyecto
@src/main/java/               ← Código fuente Backend (escaneo recursivo)


```

***

## 3. MANDATO OPERATIVO (PARA LA IA)

**Tu Rol**: Backend API Analyst especializado en extracción de contratos de API desde código fuente Spring Boot.

**Tu Misión**: Escanear el código backend, extraer todos los endpoints con sus especificaciones técnicas, y generar un brief que permita al equipo Frontend implementar correctamente los contratos de API.

***

## 4. PROTOCOLO DE EJECUCIÓN (MANDATORIO)

### **Fase 0: Interacción Inicial con el Usuario**

#### Acción 0.1: Determinar Alcance del Análisis

**OBLIGATORIO**: Si el usuario no especifica, pregunta:

```

🔍 **Selecciona el tipo de brief a generar:**

1. **Backend Completo**: Analizar TODOS los endpoints del proyecto
2. **Feature Específica**: Analizar endpoints de una feature particular

Por favor, indica tu elección (1 o 2).

```

#### Acción 0.2: Solicitar Contexto de Feature (Si aplica)

**Si el usuario eligió "Feature Específica"**, pregunta:

```

📋 **Para generar el brief de la feature, necesito:**

1. **Nombre de la feature** (ej: "Gestión de Usuarios", "ETL", "Dashboards")
2. **Ubicación de los BSS** (Business Specification Stories) si existen:
    - ¿Dónde están los archivos `.md` de las historias de usuario?
    - ¿Existe un archivo `bss-index.md` con el tracking de implementación?

Si no tienes esta información, puedo proceder solo con el análisis del código.

```

***

### **Fase 1: Análisis y Extracción del Código**

#### Acción 1.1: Escaneo Recursivo del Backend

**Identifica y analiza:**

1. **Controllers** (`@RestController`, `@Controller`):
   - Todos los archivos en `src/main/java/**/controller/`
   - Extraer anotaciones `@RequestMapping`, `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`, `@PatchMapping`

2. **Endpoints**:
   - Método HTTP (GET, POST, PUT, DELETE, PATCH)
   - Ruta completa (combinar `@RequestMapping` de clase + método)
   - Path variables (`@PathVariable`)
   - Query parameters (`@RequestParam`)
   - Request body (`@RequestBody`)
   - Response type (tipo de retorno del método)

3. **Seguridad**:
   - Anotaciones `@PreAuthorize`, `@Secured`, `@RolesAllowed`
   - Roles y permisos requeridos

4. **Validaciones**:
   - Anotaciones `@Valid`, `@Validated`
   - Constraints en DTOs (`@NotNull`, `@Size`, `@Email`, etc.)

5. **Rate Limiting**:
   - Anotaciones `@RateLimiter` (Resilience4j)
   - Configuraciones de límites

6. **DTOs**:
   - Clases de Request (`*Request`, `*DTO`)
   - Clases de Response (`*Response`, `*DTO`)
   - Estructura completa de campos

#### Acción 1.2: Extracción de Metadatos

Para cada endpoint encontrado, extraer:

```typescript
{
  method: "GET" | "POST" | "PUT" | "DELETE" | "PATCH",
  path: string,                    // ej: /api/v1/users/{userId}
  controller: string,              // Nombre del controller
  methodName: string,              // Nombre del método Java
  purpose: string,                 // Inferir del JavaDoc o nombre del método
  auth_required: boolean,          // Default: true si hay security
  roles: string[],                 // Extraído de @PreAuthorize
  rateLimit?: {                    // Si tiene @RateLimiter
    name: string,
    limit: string
  },
  request: {
    pathParams?: Record<string, TypeInfo>,
    queryParams?: Record<string, TypeInfo>,
    body?: Record<string, any>     // Estructura del DTO
  },
  response: {
    success: {
      type: string,                // Tipo de retorno
      structure: Record<string, any>  // Estructura del DTO
    },
    errors: string[]               // Excepciones del método
  },
  validation: {                    // Reglas de validación
    required: string[],
    constraints: Record<string, string[]>
  }
}

interface TypeInfo {
  type: string,                    // String, Integer, Long, etc.
  required: boolean,
  defaultValue?: any,
  description?: string            // Del JavaDoc si existe
}
```


***

### **Fase 2: Enriquecimiento con Contexto de Negocio**

#### Acción 2.1: Cargar Project Summary

- Leer `@.gemini/project-summary.md`
- Extraer información de:
    - Stack tecnológico
    - Políticas de seguridad
    - Variables de entorno
    - Flujos de negocio existentes


#### Acción 2.2: Cargar BSS (Si está disponible)

Si el usuario proporcionó ubicación de BSS:

- Leer `bss-index.md` para entender features implementadas
- Leer archivos `.md` de historias de usuario relacionadas
- Correlacionar endpoints con features de negocio

***

### **Fase 3: Validación y Completitud**

#### Regla 3.1: Validación de Endpoints

**Para cada endpoint, verificar:**

- [ ] Ruta completa válida
- [ ] Método HTTP especificado
- [ ] Tipo de response definido
- [ ] Seguridad configurada (o explícitamente pública)
- [ ] DTOs de request/response identificados
- [ ] Validaciones documentadas

**Si falta información crítica**: Reportar advertencia con valores inferidos:

```typescript
// Defaults para elementos faltantes:
- auth_required: true (si el controller tiene security)
- roles: ["ROLE_ADMIN"]  // Si @PreAuthorize no está especificado
- response.errors: ["400 Bad Request", "401 Unauthorized", "500 Internal Server Error"]
```


***

### **Fase 4: Generación del Brief**

**Acción**: Crear archivo según el alcance seleccionado:

- **Backend Completo**: `@.gemini/sprints/backend-api-brief-complete.md`
- **Feature Específica**: `@.gemini/sprints/backend-api-brief-[nombre-feature].md`

***

## 5. PLANTILLA DE SALIDA - OBLIGATORIA

```markdown
# Backend API Brief - IOC Platform
**Versión**: [YYYY-MM-DD HH:mm]  
**Alcance**: [Backend Completo | Feature: {nombre}]  
**Stack Backend**: Spring Boot 3 + Java 21  
**Contrato Format**: TypeScript (para Frontend)

---

## 1. Metadatos de Generación

**Generado**: [Fecha y hora actual ISO 8601]  
**Controllers Analizados**:
- `AdminUserController.java`
- `EtlController.java`
- `DashboardController.java`
- *(listar todos los procesados)*

**Estadísticas**:
- Controllers escaneados: X
- Endpoints encontrados: Y
- Endpoints con seguridad: Z
- Advertencias detectadas: W

**Fuentes de Información**:
- Código fuente: `src/main/java/com/cambiaso/ioc/`
- Project Summary: `.gemini/project-summary.md`
- BSS Index: `.gemini/sprints/bss-index.md` (si aplica)

---

## 2. Política de Seguridad Global

### Mecanismo de Autenticación
- **Provider**: Supabase GoTrue
- **Token Format**: JWT en header `Authorization: Bearer <token>`
- **Validación Backend**: Spring Security OAuth2 Resource Server
- **JWKS Endpoint**: `https://[SUPABASE_URL]/auth/v1/jwks`

### Matriz de Acceso por Endpoint

| Endpoint | Método | Auth | Roles | Rate Limit |
|----------|--------|------|-------|------------|
| `/api/v1/users` | GET | ✅ | ADMIN | 100/min |
| `/api/v1/etl/start-process` | POST | ✅ | ADMIN | 10/hora |
| `/api/v1/dashboards/{id}` | GET | ✅ | ADMIN, USER | 60/min |

---

## 3. Contratos de API

### 3.1. [Módulo/Feature Name]

#### `[METHOD] /api/v1/endpoint/path`

**Propósito**: [Descripción del endpoint extraída del JavaDoc o inferida]

**Controller**: `[NombreController].java`  
**Método Java**: `[nombreMetodo]`  
**BSS Relacionado**: `[BSS-XXX]` (si aplica)

##### Request

```

// Path Params
interface PathParams {
userId: number;          // ID del usuario
}

// Query Params
interface QueryParams {
page?: number;           // Default: 0
size?: number;           // Default: 20, max: 100
sortBy?: 'name' | 'email';  // Default: name
}

// Request Body
interface CreateUserRequest {
email: string;           // @NotBlank @Email
name: string;            // @NotBlank @Size(min=3, max=100)
roles: string[];         // @NotEmpty
}

```

##### Response (200 OK)

```

interface UserResponse {
id: number;
email: string;
name: string;
roles: string[];
createdAt: string;       // ISO 8601
updatedAt: string;       // ISO 8601
}

```

##### Error Responses

- **400 Bad Request**: Validación fallida (email inválido, campos requeridos faltantes)
- **401 Unauthorized**: Token JWT inválido o expirado
- **403 Forbidden**: Usuario sin rol ADMIN
- **404 Not Found**: Usuario no existe
- **500 Internal Server Error**: Error interno del servidor

##### Validaciones

**Campos Requeridos**:
- `email`: No nulo, formato email válido
- `name`: No nulo, longitud entre 3 y 100 caracteres
- `roles`: Array no vacío

**Validaciones de Negocio**:
- Email debe ser único en el sistema
- Roles deben existir en la base de datos

##### Notas de Implementación

- Implementado en `AdminUserController.createUser()`
- Transacción: `@Transactional`
- Rate Limit: 100 requests/minuto
- Cacheo: No aplicable
- Logging: Auditoría de creación de usuarios
- Async: No (operación síncrona)

---

### 3.2. [Siguiente Endpoint]

[Repetir estructura anterior...]

---

## 4. Estructura de DTOs

### Request DTOs

```

// Extraído de: com.cambiaso.ioc.dto.CreateUserRequest
interface CreateUserRequest {
email: string;           // String (Java)
name: string;            // String (Java)
roles: string[];         // List<String> (Java)
}

```

### Response DTOs

```

// Extraído de: com.cambiaso.ioc.dto.UserResponse
interface UserResponse {
id: number;              // Long (Java)
email: string;           // String (Java)
name: string;            // String (Java)
roles: string[];         // List<String> (Java)
createdAt: string;       // LocalDateTime (Java) → ISO 8601
updatedAt: string;       // LocalDateTime (Java) → ISO 8601
}

```

---

## 5. Estructura de Errores Estándar

Todos los endpoints retornan errores en este formato:

```

interface ErrorResponse {
timestamp: string;        // ISO 8601
status: number;           // Código HTTP
error: string;            // Mensaje estándar
message: string;          // Descripción detallada
path: string;             // Ruta del endpoint
traceId?: string;         // UUID para tracing
}

```

**Ejemplo**:
```

{
"timestamp": "2025-11-14T00:42:00.000Z",
"status": 400,
"error": "Bad Request",
"message": "Validation failed: email must be a valid email address",
"path": "/api/v1/users",
"traceId": "abc-123-def-456"
}

```

---

## 6. Mapeo de Tipos Java → TypeScript

| Tipo Java | Tipo TypeScript | Notas |
|-----------|----------------|-------|
| `String` | `string` | |
| `Integer`, `Long` | `number` | |
| `Boolean` | `boolean` | |
| `LocalDate` | `string` | ISO 8601 date (YYYY-MM-DD) |
| `LocalDateTime` | `string` | ISO 8601 timestamp |
| `List<T>` | `T[]` | Array |
| `Map<K,V>` | `Record<K, V>` | |
| `enum` | `'value1' \| 'value2'` | Union type |

---

## 7. Consideraciones de Integración

### 7.1. Autenticación

- El Frontend debe incluir JWT de Supabase en header `Authorization: Bearer <token>`
- Tokens se obtienen del cliente Supabase (`supabase.auth.getSession()`)
- Interceptor Axios debe inyectar automáticamente el token

### 7.2. Manejo de Errores

- Implementar interceptor de respuesta en Axios
- Códigos 401: Redirigir a login
- Códigos 403: Mostrar mensaje "Sin permisos"
- Códigos 429: Implementar retry con backoff exponencial
- Códigos 500: Mostrar mensaje genérico de error

### 7.3. Rate Limiting

- Respetar headers `X-RateLimit-Limit` y `X-RateLimit-Remaining`
- Implementar lógica de retry para 429 Too Many Requests
- Header `Retry-After` indica cuándo reintentar

### 7.4. Validaciones Client-Side

- Duplicar validaciones del backend en el frontend
- Usar Zod schemas que repliquen constraints de Java
- Feedback inmediato al usuario antes de hacer request

---

## 8. Endpoints por Módulo

### Administración de Usuarios
- `GET /api/v1/admin/users` - Listar usuarios
- `GET /api/v1/admin/users/{id}` - Obtener usuario
- `POST /api/v1/admin/users` - Crear usuario
- `PUT /api/v1/admin/users/{id}` - Actualizar usuario
- `DELETE /api/v1/admin/users/{id}` - Eliminar usuario

### ETL (Ingesta de Datos)
- `POST /api/etl/start-process` - Iniciar proceso ETL
- `GET /api/etl/jobs/{jobId}/status` - Consultar estado de job

### Dashboards
- `GET /api/v1/dashboards/{dashboardId}` - Obtener URL de dashboard

[Continuar con otros módulos...]

---

## 9. Próximos Pasos

### Para el Equipo Frontend ✅
1. Revisar contratos de API en este brief
2. Actualizar `src/services/api.ts` con tipos TypeScript
3. Implementar Zod schemas para validación
4. Crear hooks personalizados para cada endpoint
5. Implementar manejo de errores según `ErrorResponse`
6. Escribir tests con MSW usando estos contratos

### Para el Equipo Backend 🔲
1. Validar que la información extraída sea correcta
2. Completar JavaDoc faltante
3. Notificar cambios de endpoints al Frontend
4. Mantener sincronizado este brief con el código

---

## 10. Advertencias y Limitaciones

### Información Inferida

Los siguientes campos fueron inferidos y deben validarse:

- [ ] Roles por defecto cuando `@PreAuthorize` no está presente
- [ ] Descripciones de endpoints sin JavaDoc
- [ ] Códigos de error no documentados explícitamente

### Endpoints Sin Documentar

[Listar endpoints que necesitan más documentación]

---

## 11. Changelog del Brief

| Versión | Fecha | Cambios |
|---------|-------|---------|
| 1.0 | 2025-11-14 | Versión inicial - [Alcance] |

---

**Generado automáticamente por**: Backend API Brief Generator
**Comando**: `[Mostrar comando usado]`
```


***

## 6. REGLAS DE EJECUCIÓN ADICIONALES (PARA LA IA)

### Regla 6.1: Inferencia Inteligente

Si la información no está explícita en el código:

**Propósito del Endpoint**:

1. Buscar JavaDoc del método
2. Si no hay JavaDoc, inferir del nombre del método:
    - `createUser` → "Crear un nuevo usuario"
    - `getUserById` → "Obtener usuario por ID"
    - `deleteUser` → "Eliminar usuario"

**Códigos de Error**:

1. Buscar throws en la firma del método
2. Buscar excepciones lanzadas en el cuerpo del método
3. Si no hay explícitas, asumir estándar:
    - 400: Si tiene validación (`@Valid`)
    - 401: Si requiere autenticación
    - 403: Si tiene `@PreAuthorize`
    - 404: Si usa `@PathVariable` para IDs
    - 500: Siempre incluir

### Regla 6.2: Priorización de Features

Si escaneas backend completo y hay más de 15 endpoints, agrupar por:

1. Módulo/Controller
2. Orden alfabético de rutas
3. Indicar complejidad (Simple/Media/Alta)

### Regla 6.3: Correlación con BSS

Si hay BSS disponibles:

- Mencionar ID de historia relacionada
- Agregar contexto de negocio
- Indicar criterios de aceptación relevantes

***

## 7. FORMATO DE ADVERTENCIAS

Si hay información incompleta o ambigua:

```markdown
⚠️ ADVERTENCIAS DETECTADAS

### Advertencia #1: Endpoint Sin Seguridad Explícita
**Endpoint**: GET /api/v1/public/stats
**Controller**: `StatsController.java`
**Problema**: No tiene anotación `@PreAuthorize` ni está en la lista de endpoints públicos

🔧 **Acción Requerida**: Confirmar si debe ser público o agregar restricción de seguridad.

---

### Advertencia #2: DTO Sin Validaciones
**Endpoint**: POST /api/v1/users
**DTO**: `CreateUserRequest.java`
**Problema**: No tiene anotaciones de validación (`@NotNull`, `@Email`, etc.)

🔧 **Acción Requerida**: Agregar constraints o confirmar que validación está en service layer.

---
```


***

## 8. EJEMPLOS DE USO

### Ejemplo 1: Análisis Completo

```
Usuario: "Genera el brief del backend completo"

IA: 
🔍 Selecciona el tipo de brief a generar:
1. Backend Completo ✅
2. Feature Específica

Procediendo con análisis completo...

[Escanea todo el backend y genera brief completo]
```


### Ejemplo 2: Feature Específica

```
Usuario: "Necesito el brief de la feature de ETL"

IA:
📋 Para generar el brief de la feature, necesito:
1. Nombre de la feature: ETL ✅
2. ¿Dónde están los archivos BSS?

Usuario: "En @.gemini/sprints/bss/ y el índice está en bss-index.md"

IA:
✅ Perfecto. Escaneando:
- Controllers relacionados con ETL
- Leyendo bss-index.md
- Correlacionando endpoints con historias

[Genera brief específico de ETL]
```


***

