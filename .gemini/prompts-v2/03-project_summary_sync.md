# 📄 PROMPT 3: Project Summary Sync Validator

```markdown
# VALIDADOR Y SINCRONIZADOR DE RESUMEN TÉCNICO
## 1. CONFIGURACIÓN
**Propósito**: Detectar cambios en los repositorios backend/frontend desde la última generación del `project-summary.md` y sincronizar el documento con el estado actual del código.
**Cuándo usar este prompt**:
- ✅ Después de agregar/remover dependencias importantes
- ✅ Después de cambios arquitectónicos
- ✅ Antes de onboarding de nuevos desarrolladores
- ✅ Periódicamente (mensual/por sprint) para mantener docs actualizadas
- ✅ Cuando sospechas que la documentación está desactualizada
**Puede ejecutarse desde**: `ioc-backend` O `ioc-frontend` (auto-detecta)
**Salida**: 
- Reporte de divergencias
- Documento actualizado (opcional)
- Recomendaciones de sincronización
---
## 2. MANDATO OPERATIVO (PARA LA IA)
**Tu Rol**: Documentation Sync Engineer con capacidad de análisis de divergencias.
**Tu Responsabilidad**:
1. Detectar desde qué repositorio se ejecuta
2. Leer el `project-summary.md` existente
3. Analizar el código actual de ambos repos (o del disponible)
4. Comparar estado actual vs documentado
5. Generar reporte de divergencias
6. Ofrecer opciones de sincronización
7. Actualizar documento si se solicita
---
## 3. PROTOCOLO DE SINCRONIZACIÓN
### FASE 1: Detección de Contexto
#### Acción 1.1: Identificar Repositorio Actual
```bash
# Script mental de detección:
if (existe package.json con "react" o "vue" o "angular"):
 REPO_ACTUAL = "FRONTEND"
 REPO_HERMANO = "BACKEND"
 RUTA_HERMANO = "../ioc-backend"
elif (existe pom.xml o build.gradle):
 REPO_ACTUAL = "BACKEND"
 REPO_HERMANO = "FRONTEND"
 RUTA_HERMANO = "../ioc-frontend"
else:
 ERROR("No se puede determinar el tipo de repositorio")
 DETENER()
MODO = "SYNC_VALIDATOR"
```

#### Acción 1.2: Localizar Documento Existente

```bash
# Buscar project-summary.md en orden de prioridad:
UBICACIONES_BUSQUEDA = [
 "@.gemini/project-summary.md", # Mismo repo
 "../@.gemini/project-summary.md", # Repo padre compartido
 f"../{REPO_HERMANO}/@.gemini/project-summary.md", # Repo hermano
 ".gemini/project-summary.md" # Variante sin @
]
for ubicacion in UBICACIONES_BUSQUEDA:
 if existe(ubicacion):
 ARCHIVO_ENCONTRADO = ubicacion
 break
if not ARCHIVO_ENCONTRADO:
 ERROR_NO_ARCHIVO()
 DETENER()
```

**Si NO se encuentra**:

```markdown
❌ ERROR: Documento Base No Encontrado
No se encontró `project-summary.md` en ninguna ubicación esperada.
Ubicaciones verificadas:
- ❌ @.gemini/project-summary.md
- ❌ ../@.gemini/project-summary.md
- ❌ ../[repo-hermano]/@.gemini/project-summary.md
🔧 SOLUCIÓN:
Este prompt requiere que el documento ya exista.
Paso 1: Generar documento base
```bash
# Desde ioc-backend:
cd ../ioc-backend
gemini-cli < @.gemini/prompts/generate-project-summary-backend.md
```

Paso 2: Completar con frontend

```bash
# Desde ioc-frontend:
cd ../ioc-frontend
gemini-cli < @.gemini/prompts/complete-project-summary-frontend.md
```

Paso 3: Volver a ejecutar este validador
DETENER HASTA QUE SE GENERE EL DOCUMENTO

```
---
#### Acción 1.3: Leer Metadata del Documento
```yaml
# Extraer de la sección METADATA PARA SINCRONIZACIÓN:
metadata:
 generated_by: "[Backend Module / Backend + Frontend Modules]"
 source_repos: ["ioc-backend", "ioc-frontend"] o ["ioc-backend"]
 version: "[1.0-BACKEND / 1.0-FULL]"
 status: "[INCOMPLETE / COMPLETE]"
 backend_analyzed: "[ISO 8601 timestamp]"
 frontend_analyzed: "[ISO 8601 timestamp]" # Puede no existir
 last_updated: "[ISO 8601 timestamp]"
 checksums: # Opcional
 backend: "[hash]"
 frontend: "[hash]"
```

**Determinar estado del documento**:

```typescript
if (metadata.status === "INCOMPLETE") {
 ADVERTENCIA(`
 ⚠️ DOCUMENTO INCOMPLETO

El documento no ha sido completado por el módulo ${REPO_HERMANO}.

Último módulo que lo procesó: ${metadata.generated_by}
 Fecha: ${metadata.last_updated}

🔧 Recomendación:
 Antes de sincronizar, completa el documento ejecutando el prompt correspondiente.

¿Quieres continuar de todas formas? (S/N)
 `);
 ESPERAR_RESPUESTA();
}
// Calcular antigüedad
const diasDesdeActualizacion = calcularDias(metadata.last_updated, HOY);
if (diasDesdeActualizacion > 30) {
 NOTA(`
 📅 DOCUMENTO ANTIGUO

Última actualización: ${metadata.last_updated} (${diasDesdeActualizacion} días atrás)

Es altamente probable que haya divergencias significativas.
 `);
}
```

---

### FASE 2: Análisis de Divergencias

#### Acción 2.1: Análisis del Repositorio Actual

**Si estamos en BACKEND**:

```typescript
// Ejecutar análisis similar al Prompt #1 (Backend Generator)
const estadoActualBackend = {
 springBootVersion: extraerDePom('spring-boot.version'),
 javaVersion: extraerDePom('java.version'),
 dependencias: extraerDependenciasPom(),
 entidades: escanearEntidades('src/main/java/**/model'),
 endpoints: escanearControllers('src/main/java/**/controller'),
 configuracionSeguridad: analizarSecurityConfig(),
 variablesEntorno: extraerDeApplicationProperties(),
 // ... todo lo del análisis backend
};
// Extraer lo que ESTÁ DOCUMENTADO
const estadoDocumentadoBackend = {
 springBootVersion: extraerDeSeccion(documento, '3.2. Backend', 'Spring Boot'),
 javaVersion: extraerDeSeccion(documento, '3.2. Backend', 'Java'),
 dependencias: extraerTabla(documento, '3.2. Backend', 'Dependencias Principales'),
 entidades: extraerDeSeccion(documento, '3.3. Base de Datos', 'Entidades Detectadas'),
 endpoints: extraerTabla(documento, '4.2. Endpoints de Negocio'),
 // ... etc
};
// COMPARAR
const divergenciasBackend = compararEstados(estadoActualBackend, estadoDocumentadoBackend);
```

**Si estamos en FRONTEND**:

```typescript
// Ejecutar análisis similar al Prompt #2 (Frontend Completer)
const estadoActualFrontend = {
 reactVersion: extraerDePackageJson('react'),
 dependencias: extraerDependenciasPackageJson(),
 componentes: contarArchivos('src/components'),
 paginas: contarArchivos('src/pages'),
 hooks: contarArchivos('src/hooks'),
 rutas: analizarRouter(),
 variablesEntorno: extraerDeEnvExample(),
 // ... todo lo del análisis frontend
};
const estadoDocumentadoFrontend = {
 reactVersion: extraerDeSeccion(documento, '3.1. Frontend', 'React'),
 dependencias: extraerTabla(documento, '3.1. Frontend', 'Dependencias Principales'),
 // ... etc
};
const divergenciasFrontend = compararEstados(estadoActualFrontend, estadoDocumentadoFrontend);
```

---

#### Acción 2.2: Categorización de Divergencias

```typescript
interface Divergencia {
 seccion: string; // Ej: "3.2. Backend - Dependencias"
 tipo: DivergenciaType; // CRITICA | IMPORTANTE | MENOR
 categoria: string; // Dependencias | Endpoints | Configuración
 cambio: CambioType; // AGREGADO | ELIMINADO | MODIFICADO
 valorActual: any;
 valorDocumentado: any;
 recomendacion: string;
}
enum DivergenciaType {
 CRITICA = "CRITICA", // Cambio arquitectónico mayor
 IMPORTANTE = "IMPORTANTE", // Cambio significativo
 MENOR = "MENOR" // Cambio cosmético/versión
}
// Clasificar cada divergencia:
function clasificarDivergencia(div: Divergencia): DivergenciaType {
 // CRÍTICAS:
 if (div.categoria === 'Framework' && cambioMayor(div.valorActual, div.valorDocumentado)) {
 return DivergenciaType.CRITICA; // Ej: React 18 → 19
 }
 if (div.categoria === 'Endpoints' && div.cambio === 'ELIMINADO') {
 return DivergenciaType.CRITICA; // Endpoint eliminado
 }
 if (div.categoria === 'Seguridad') {
 return DivergenciaType.CRITICA; // Cualquier cambio en seguridad
 }

// IMPORTANTES:
 if (div.categoria === 'Dependencias' && div.cambio === 'AGREGADO') {
 return DivergenciaType.IMPORTANTE; // Nueva librería importante
 }
 if (div.categoria === 'Endpoints' && div.cambio === 'AGREGADO') {
 return DivergenciaType.IMPORTANTE; // Nuevo endpoint
 }

// MENORES:
 if (div.categoria === 'Dependencias' && cambioMenor(div.valorActual, div.valorDocumentado)) {
 return DivergenciaType.MENOR; // Ej: axios 1.5.0 → 1.5.1
 }

return DivergenciaType.MENOR;
}
```

---

#### Acción 2.3: Análisis del Repositorio Hermano (Si Accesible)

```typescript
// Intentar acceder al repositorio hermano
const rutaHermano = RUTA_HERMANO; // "../ioc-frontend" o "../ioc-backend"
if (existe(rutaHermano)) {
 NOTA(`
 ✅ Repositorio hermano (${REPO_HERMANO}) accesible

Realizaré análisis completo de ambos repositorios para validación cruzada.
 `);

// Ejecutar análisis del repo hermano
 if (REPO_HERMANO === "BACKEND") {
 const estadoActualBackend = analizarBackend(rutaHermano);
 const divergenciasBackend = compararEstados(estadoActualBackend, estadoDocumentadoBackend);
 } else {
 const estadoActualFrontend = analizarFrontend(rutaHermano);
 const divergenciasFrontend = compararEstados(estadoActualFrontend, estadoDocumentadoFrontend);
 }

MODO_ANALISIS = "COMPLETO"; // Backend + Frontend

} else {
 ADVERTENCIA(`
 ⚠️ Repositorio hermano (${REPO_HERMANO}) no accesible

Ruta intentada: ${rutaHermano}

Continuaré con análisis parcial (solo ${REPO_ACTUAL}).

📝 Nota: Para análisis completo, asegúrate de que ambos repos estén en:
 /ruta/base/
 ├── ioc-backend/
 └── ioc-frontend/
 `);

MODO_ANALISIS = "PARCIAL"; // Solo el repo actual
}
```

---

#### Acción 2.4: Validación de Consistencia Cross-Repo

```typescript
// Si ambos repos están disponibles, validar consistencia:
if (MODO_ANALISIS === "COMPLETO") {
 const inconsistencias = [];

// VALIDACIÓN 1: Endpoints Frontend vs Backend
 const endpointsLlamadosFrontend = extraerLlamadasAPI(estadoActualFrontend);
 const endpointsImplementadosBackend = estadoActualBackend.endpoints;

const endpointsSinBackend = endpointsLlamadosFrontend.filter(
 fe => !endpointsImplementadosBackend.some(
 be => be.method === fe.method && be.path === fe.path
 )
 );

if (endpointsSinBackend.length > 0) {
 inconsistencias.push({
 tipo: 'ENDPOINT_MISMATCH',
 severidad: 'CRITICA',
 descripcion: 'Frontend llama a endpoints no implementados en backend',
 detalles: endpointsSinBackend
 });
 }

// VALIDACIÓN 2: Versiones de Autenticación
 const authFrontend = estadoActualFrontend.authProvider; // "Supabase"
 const authBackend = estadoActualBackend.jwtIssuer; // "https://xxx.supabase.co"

if (authFrontend.includes('supabase') && !authBackend.includes('supabase')) {
 inconsistencias.push({
 tipo: 'AUTH_MISMATCH',
 severidad: 'CRITICA',
 descripcion: 'Frontend y Backend usan diferentes proveedores de autenticación'
 });
 }

// VALIDACIÓN 3: URLs Base
 const apiUrlFrontend = estadoActualFrontend.apiUrl;
 const expectedBackendUrl = estadoActualBackend.baseUrl;

// Esta es una advertencia leve, puede ser intencional (dev/prod)
 if (apiUrlFrontend !== expectedBackendUrl) {
 inconsistencias.push({
 tipo: 'API_URL_DIFFERENCE',
 severidad: 'MENOR',
 descripcion: 'URL de API difiere entre frontend y backend (puede ser intencional)',
 detalles: { frontend: apiUrlFrontend, backend: expectedBackendUrl }
 });
 }

INCONSISTENCIAS_CROSS_REPO = inconsistencias;
}
```

---

### FASE 3: Generación de Reportes

#### Acción 3.1: Reporte de Divergencias

```markdown
# 📊 REPORTE DE SINCRONIZACIÓN
**Fecha**: [ISO 8601] 
**Ejecutado desde**: [ioc-backend / ioc-frontend] 
**Modo de análisis**: [COMPLETO / PARCIAL]
---
## 1. Resumen Ejecutivo
**Estado del Documento**:
- Última actualización: [FECHA] ([X] días atrás)
- Versión: [1.0-FULL / 1.0-BACKEND / etc.]
- Status: [COMPLETE / INCOMPLETE]
- Generado por: [Backend Module / Backend + Frontend Modules]
**Divergencias Detectadas**:
- 🔴 Críticas: [X]
- 🟡 Importantes: [Y]
- 🟢 Menores: [Z]
- **Total**: [X+Y+Z]
**Recomendación General**:
[SI X > 0]:
 ⚠️ SINCRONIZACIÓN URGENTE REQUERIDA
 Se detectaron cambios críticos que requieren actualización inmediata del documento.
[SI Y > 3 y X == 0]:
 📝 SINCRONIZACIÓN RECOMENDADA
 Se detectaron múltiples cambios importantes. Considera actualizar pronto.
[SI solo Z > 0]:
 ✅ DOCUMENTO MAYORMENTE ACTUALIZADO
 Solo cambios menores detectados. Actualización opcional.
[SI X+Y+Z == 0]:
 ✅ DOCUMENTO COMPLETAMENTE SINCRONIZADO
 No se detectaron divergencias. El documento está actualizado.
---
## 2. Divergencias por Categoría
### 🔴 CRÍTICAS (Requieren atención inmediata)
[SI HAY CRÍTICAS]:
#### [Categoría 1]: [Nombre - ej: "Framework Upgrade"]
**Sección Afectada**: [3.2. Backend - Framework Core]
**Cambio Detectado**:
- **Actual (código)**: Spring Boot 3.3.0
- **Documentado**: Spring Boot 3.2.1
**Impacto**: 
- Cambio de versión mayor puede incluir breaking changes
- Afecta comportamiento de seguridad y dependencias
**Recomendación**:
- ✅ Actualizar sección 3.2 con nueva versión
- ✅ Revisar sección 5 (Seguridad) por cambios en Spring Security
- ✅ Actualizar changelog con justificación del upgrade
---
#### [Categoría 2]: [Endpoints Eliminados]
**Sección Afectada**: [4.2. Endpoints de Negocio]
**Cambio Detectado**:
- **Documentado**: `DELETE /api/v1/users/{id}`
- **Actual (código)**: Endpoint no encontrado
**Impacto**:
- El frontend puede estar llamando a un endpoint que ya no existe
- Posible breaking change para clientes
**Recomendación**:
- ⚠️ Verificar si el frontend aún usa este endpoint
- ✅ Remover de la documentación
- ✅ Agregar nota en changelog sobre deprecación
---
[CONTINUAR CON TODAS LAS CRÍTICAS]
---
### 🟡 IMPORTANTES (Actualizar pronto)
[SI HAY IMPORTANTES]:
#### [Categoría]: [Nuevas Dependencias]
**Sección Afectada**: [3.2. Backend - Dependencias Principales]
**Cambios Detectados**:
| Librería | Estado | Versión Actual | Versión Documentada |
|----------|--------|----------------|---------------------|
| resilience4j-spring-boot3 | ✅ AGREGADO | 2.1.0 | - |
| spring-boot-starter-websocket | ✅ AGREGADO | 3.2.1 | - |
**Impacto**:
- Nuevas capacidades agregadas (rate limiting, WebSocket)
- Afecta arquitectura de la aplicación
**Recomendación**:
- ✅ Agregar a tabla de dependencias
- ✅ Actualizar sección de arquitectura si aplica
- ✅ Documentar nuevos endpoints WebSocket (si existen)
---
#### [Categoría]: [Nuevos Endpoints]
**Sección Afectada**: [4.2. Endpoints de Negocio]
**Cambios Detectados**:
| Método | Ruta | Controller | Roles |
|--------|------|------------|-------|
| POST | /api/v1/notifications/subscribe | NotificationController | ADMIN, USER |
| GET | /api/v1/notifications/history | NotificationController | ADMIN, USER |
**Impacto**:
- Nueva funcionalidad implementada (sistema de notificaciones)
- Frontend probablemente ya lo esté usando
**Recomendación**:
- ✅ Agregar endpoints a la tabla 4.2
- ✅ Actualizar sección 1.4 (Estado Actual) con nueva feature
- ✅ Generar contratos detallados con Backend Sync Brief
---
[CONTINUAR CON TODAS LAS IMPORTANTES]
---
### 🟢 MENORES (Opcional, baja prioridad)
[SI HAY MENORES]:
#### [Categoría]: [Actualizaciones de Versiones Menores]
**Cambios Detectados**:
| Dependencia | Versión Documentada | Versión Actual | Tipo de Cambio |
|-------------|---------------------|----------------|----------------|
| axios | 1.5.0 | 1.6.2 | Patch update |
| react-router-dom | 6.20.0 | 6.21.1 | Minor update |
| date-fns | 2.30.0 | 3.0.0 | Major (pero API compatible) |
**Impacto**: Bajo. Cambios de versión sin breaking changes.
**Recomendación**:
- ⏳ Opcional: Actualizar versiones en la próxima revisión general
- 📝 No urgente
---
#### [Categoría]: [Estructura de Directorios]
**Cambio Detectado**:
- Nuevo directorio: `src/middleware/` (3 archivos)
- Renombrado: `src/helpers/` → `src/utils/`
**Impacto**: Mínimo. Refactorización interna.
**Recomendación**:
- ⏳ Actualizar diagrama de estructura en sección 3.1 (Frontend)
---
[CONTINUAR CON TODAS LAS MENORES]
---
## 3. Validación Cross-Repositorio
[SI MODO_ANALISIS === "COMPLETO"]:
### ✅ Consistencia Backend ↔ Frontend
#### Endpoints: [ESTADO]
[SI SIN INCONSISTENCIAS]:
✅ **Todos los endpoints llamados por el frontend están implementados en el backend**
Validación:
- Frontend llama a: [X] endpoints
- Backend implementa: [Y] endpoints
- Coincidencias: [X] / [X] (100%)
[SI HAY INCONSISTENCIAS]:
⚠️ **Inconsistencias detectadas**
**Endpoints llamados por Frontend sin backend**:
- GET /api/v1/stats → No implementado
- POST /api/v1/export → No implementado
**Endpoints en Backend no usados por Frontend**:
- GET /api/v1/admin/logs → Implementado pero sin uso
- DELETE /api/v1/cache → Implementado pero sin uso
**Recomendación**:
- Implementar endpoints faltantes en backend
- O remover llamadas del frontend
- Documentar endpoints no usados si son para uso futuro
---
#### Autenticación: [ESTADO]
✅ **Frontend y Backend usan el mismo proveedor**
- Frontend: Supabase Client ([@supabase/supabase-js@2.38.0])
- Backend: Supabase JWT Validation (issuer: https://xxx.supabase.co)
- Status: Consistente
---
#### URLs Base: [ESTADO]
📝 **Diferencias detectadas (puede ser intencional)**
- Frontend (`VITE_API_URL`): http://localhost:8080
- Backend (esperado en producción): https://api.ioc.cambiaso.com
**Nota**: Esto es normal en desarrollo. Verificar que en producción usen la misma URL.
---
[SI MODO_ANALISIS === "PARCIAL"]:
### ⚠️ Validación Parcial
Solo se analizó el repositorio **[REPO_ACTUAL]**.
Para validación completa de consistencia, ejecuta este prompt con acceso a ambos repositorios.
---
## 4. Impacto Estimado de la Sincronización
**Secciones a Actualizar**: [X] secciones
| Sección | Título | Cambios | Esfuerzo |
|---------|--------|---------|----------|
| 1.4 | Estado Actual | Agregar 2 features nuevas | 5 min |
| 3.2 | Backend Stack | Actualizar 4 dependencias | 10 min |
| 4.2 | Endpoints | Agregar 3 endpoints, remover 1 | 15 min |
| 5.1 | Seguridad | Actualizar config de CORS | 5 min |
| [etc.] | [etc.] | [etc.] | [etc.] |
**Tiempo Total Estimado**: [X] minutos
**Riesgo de Conflictos**: [Bajo / Medio / Alto]
- [SI BAJO]: Cambios aislados, fácil de sincronizar
- [SI MEDIO]: Algunos cambios interrelacionados, requiere revisión
- [SI ALTO]: Cambios arquitectónicos mayores, requiere reescritura de secciones
---
## 5. Recomendaciones de Acción
[SI divergencias críticas > 0 O importantes > 5]:
### 🔴 ACCIÓN INMEDIATA REQUERIDA
**Opción A: Sincronización Automática Completa** (Recomendado)
```bash
# Regenerar documento completo con estado actual
cd [repo-actual]
gemini-cli < @.gemini/prompts/sync-project-summary-auto.md
# Esto ejecutará:
# 1. Backend Generator (si aplica)
# 2. Frontend Completer (si aplica)
# 3. Validación final
```

**Opción B: Sincronización Manual Selectiva**

```bash
# Tú eliges qué secciones actualizar
gemini-cli < @.gemini/prompts/sync-project-summary-interactive.md
# Esto te preguntará sección por sección si deseas actualizarla
```

**Opción C: Actualización Manual**

- Edita `@.gemini/project-summary.md` directamente
- Usa este reporte como checklist
- Actualiza el changelog manualmente
  [SI solo divergencias menores]:
  
  ### ✅ DOCUMENTO MAYORMENTE ACTUALIZADO
  
  **Opción A: Actualizar Ahora** (Opcional)
- Esfuerzo bajo ([X] minutos)
- Mantiene documentación 100% precisa
  **Opción B: Posponer**
- Los cambios son cosméticos
- Actualizar en la próxima revisión programada
  [SI sin divergencias]:
  
  ### ✅ TODO AL DÍA
  
  No se requiere acción. El documento está sincronizado.
  **Próxima Validación Recomendada**: [FECHA + 30 días]

---

## 6. Comandos Sugeridos

### Para Sincronización Automática:

```bash
# Opción 1: Regeneración completa (más seguro)
cd ../ioc-backend
gemini-cli < @.gemini/prompts/generate-project-summary-backend.md
cd ../ioc-frontend 
gemini-cli < @.gemini/prompts/complete-project-summary-frontend.md
# Opción 2: Sync incremental (más rápido)
cd [repo-actual]
gemini-cli < @.gemini/prompts/sync-project-summary-incremental.md \
 --divergences=this-report.json
```

### Para Actualización Manual:

```bash
# Abrir documento
code @.gemini/project-summary.md
# Seguir checklist de este reporte
# Actualizar secciones listadas en "4. Impacto Estimado"
# Actualizar metadata al final:
# - last_updated: [nueva fecha]
# - status: "COMPLETE"
# Agregar entrada en Changelog
```

---

## 7. Archivos Generados

Este análisis ha generado:

- ✅ **Este reporte**: `@.gemini/reports/sync-report-[FECHA].md`
- ✅ **Datos en JSON**: `@.gemini/reports/divergences-[FECHA].json` (para procesamiento automático)
- ⏳ **Backup del documento actual**: `@.gemini/backups/project-summary-[FECHA].backup.md`

---

## 8. Métricas de Salud Documental

**Índice de Sincronización**: [X]% (100% = perfectamente sincronizado)
Cálculo:

```
Elementos documentados correctamente: [Y]
Elementos totales en el código: [Z]
Índice = (Y / Z) * 100
```

**Evolución**:

- Última validación: [FECHA ANTERIOR] - [ÍNDICE ANTERIOR]%
- Esta validación: [FECHA ACTUAL] - [ÍNDICE ACTUAL]%
- Tendencia: [↑ Mejorando / ↓ Degradando / → Estable]
  **Meta Recomendada**: ≥ 95% de sincronización

---

**Reporte generado por**: Project Summary Sync Validator v1 
**Fecha**: [ISO 8601] 
**Duración del análisis**: [X] segundos

```
---
#### Acción 3.2: Generar JSON para Procesamiento Automático
```json
// Archivo: @.gemini/reports/divergences-[FECHA].json
{
 "metadata": {
 "generated_at": "2024-01-15T10:30:00Z",
 "executed_from": "ioc-backend",
 "analysis_mode": "COMPLETO",
 "document_version": "1.0-FULL",
 "document_last_updated": "2024-01-01T12:00:00Z",
 "days_since_update": 14
 },
 "summary": {
 "critical": 2,
 "important": 5,
 "minor": 8,
 "total": 15,
 "sync_index": 87.5,
 "recommendation": "UPDATE_RECOMMENDED"
 },
 "divergences": [
 {
 "id": "div-001",
 "severity": "CRITICAL",
 "category": "Framework",
 "section": "3.2. Backend - Framework Core",
 "type": "MODIFIED",
 "current_value": "Spring Boot 3.3.0",
 "documented_value": "Spring Boot 3.2.1",
 "impact": "Breaking changes possible in security and dependencies",
 "recommendation": "Update section 3.2 and review section 5 (Security)",
 "auto_fixable": true
 },
 {
 "id": "div-002",
 "severity": "IMPORTANT",
 "category": "Endpoints",
 "section": "4.2. Endpoints de Negocio",
 "type": "ADDED",
 "current_value": {
 "method": "POST",
 "path": "/api/v1/notifications/subscribe",
 "controller": "NotificationController"
 },
 "documented_value": null,
 "impact": "New feature implemented but not documented",
 "recommendation": "Add to endpoints table and update feature list",
 "auto_fixable": true
 }
 // ... más divergencias
 ],
 "cross_repo_inconsistencies": [
 {
 "id": "cross-001",
 "severity": "CRITICAL",
 "type": "ENDPOINT_MISMATCH",
 "description": "Frontend calls endpoint not implemented in backend",
 "details": {
 "method": "GET",
 "path": "/api/v1/stats",
 "frontend_file": "src/services/stats.service.ts"
 }
 }
 ],
 "sections_to_update": [
 {
 "section": "1.4",
 "title": "Estado Actual",
 "changes": ["Add 2 new features"],
 "estimated_minutes": 5
 },
 {
 "section": "3.2",
 "title": "Backend Stack",
 "changes": ["Update 4 dependencies"],
 "estimated_minutes": 10
 }
 ],
 "total_estimated_minutes": 45
}
```

---

### FASE 4: Sincronización (Opcional)

#### Acción 4.1: Preguntar Modo de Sincronización

```markdown
📋 REPORTE COMPLETO GENERADO
Se detectaron [X] divergencias ([Y] críticas, [Z] importantes, [W] menores).
Ver reporte completo en: @.gemini/reports/sync-report-[FECHA].md
---
¿Deseas sincronizar el documento ahora?
**Opciones**:
A) ✅ Sincronización Automática Completa
 → Regenerar todas las secciones afectadas automáticamente
 → Tiempo estimado: [X] minutos
 → Recomendado si: Tienes [Y] o más divergencias importantes

B) 🔧 Sincronización Interactiva
 → Te preguntaré sección por sección si deseas actualizarla
 → Tiempo estimado: [X+10] minutos
 → Recomendado si: Quieres revisar cada cambio manualmente

C) 📝 Solo Generar Reporte (No sincronizar ahora)
 → Guardar reporte para revisión posterior
 → Actualizar documento manualmente más tarde
 → Recomendado si: Necesitas consultar con el equipo

D) ❌ Cancelar
 → No generar reporte ni sincronizar
¿Qué opción prefieres? (A/B/C/D)
[ESPERAR RESPUESTA]
```

---

#### Acción 4.2: Sincronización Automática (Opción A)

```typescript
if (opcion === "A") {
 console.log("🔄 Iniciando sincronización automática...\n");

// Crear backup
 const backupPath = `@.gemini/backups/project-summary-${FECHA}.backup.md`;
 copiarArchivo(ARCHIVO_ENCONTRADO, backupPath);
 console.log(`✅ Backup creado: ${backupPath}\n`);

// Cargar documento
 let documento = leerArchivo(ARCHIVO_ENCONTRADO);

// Procesar cada divergencia auto-fixable
 const divergenciasAutoFixables = divergencias.filter(d => d.auto_fixable);

let cambiosRealizados = 0;

for (const div of divergenciasAutoFixables) {
 console.log(`🔧 Actualizando: ${div.section}...`);

switch(div.category) {
 case "Dependencias":
 documento = actualizarTablaDependencias(documento, div);
 break;
 case "Endpoints":
 if (div.type === "ADDED") {
 documento = agregarEndpoint(documento, div.current_value);
 } else if (div.type === "DELETED") {
 documento = removerEndpoint(documento, div.documented_value);
 }
 break;
 case "Framework":
 documento = actualizarVersion(documento, div.section, div.current_value);
 break;
 // ... otros casos
 }

cambiosRealizados++;
 console.log(` ✅ Actualizado (${cambiosRealizados}/${divergenciasAutoFixables.length})\n`);
 }

// Actualizar metadata
 documento = actualizarMetadata(documento, {
 last_updated: new Date().toISOString(),
 status: "COMPLETE",
 sync_index: 100
 });

// Actualizar changelog
 documento = agregarEntradaChangelog(documento, {
 version: incrementarVersion(metadata.version),
 fecha: new Date().toISOString(),
 autor: "Sync Validator (Automático)",
 cambios: `Sincronización automática: ${cambiosRealizados} secciones actualizadas`
 });

// Guardar
 escribirArchivo(ARCHIVO_ENCONTRADO, documento);

console.log(`\n✅ SINCRONIZACIÓN COMPLETA\n`);
 console.log(` Cambios realizados: ${cambiosRealizados}`);
 console.log(` Backup: ${backupPath}`);
 console.log(` Documento: ${ARCHIVO_ENCONTRADO}`);

// Divergencias no auto-fixables
 const divNoFixables = divergencias.filter(d => !d.auto_fixable);
 if (divNoFixables.length > 0) {
 console.log(`\n⚠️ ATENCIÓN MANUAL REQUERIDA\n`);
 console.log(` ${divNoFixables.length} divergencias requieren revisión humana:`);
 divNoFixables.forEach(d => {
 console.log(` - ${d.section}: ${d.recommendation}`);
 });
 }
}
```

---

#### Acción 4.3: Sincronización Interactiva (Opción B)

```typescript
if (opcion === "B") {
 console.log("🔧 Modo interactivo activado\n");

// Crear backup
 const backupPath = `@.gemini/backups/project-summary-${FECHA}.backup.md`;
 copiarArchivo(ARCHIVO_ENCONTRADO, backupPath);
 console.log(`✅ Backup creado: ${backupPath}\n`);

let documento = leerArchivo(ARCHIVO_ENCONTRADO);
 let cambiosRealizados = 0;

// Agrupar divergencias por sección
 const porSeccion = agruparPor(divergencias, 'section');

for (const [seccion, divs] of Object.entries(porSeccion)) {
 console.log(`\n${"=".repeat(60)}`);
 console.log(`📋 SECCIÓN: ${seccion}`);
 console.log(`${"=".repeat(60)}\n`);

console.log(`Divergencias detectadas: ${divs.length}\n`);

divs.forEach((div, idx) => {
 console.log(`${idx + 1}. [${div.severity}] ${div.category} - ${div.type}`);
 console.log(` Actual: ${JSON.stringify(div.current_value)}`);
 console.log(` Documentado: ${JSON.stringify(div.documented_value)}`);
 });

console.log(`\n¿Actualizar esta sección? (S/N/V para ver diff)`);
 const respuesta = ESPERAR_INPUT();

if (respuesta === "V") {
 // Mostrar diff
 const contenidoActual = extraerSeccion(documento, seccion);
 const contenidoNuevo = generarSeccionActualizada(seccion, divs);
 mostrarDiff(contenidoActual, contenidoNuevo);

console.log(`\n¿Aplicar cambios? (S/N)`);
 const aplicar = ESPERAR_INPUT();

if (aplicar === "S") {
 documento = reemplazarSeccion(documento, seccion, contenidoNuevo);
 cambiosRealizados += divs.length;
 console.log(`✅ Sección actualizada\n`);
 } else {
 console.log(`⏭️ Sección omitida\n`);
 }
 } else if (respuesta === "S") {
 const contenidoNuevo = generarSeccionActualizada(seccion, divs);
 documento = reemplazarSeccion(documento, seccion, contenidoNuevo);
 cambiosRealizados += divs.length;
 console.log(`✅ Sección actualizada\n`);
 } else {
 console.log(`⏭️ Sección omitida\n`);
 }
 }

if (cambiosRealizados > 0) {
 // Actualizar metadata y changelog
 documento = actualizarMetadata(documento, {
 last_updated: new Date().toISOString(),
 status: "COMPLETE"
 });

documento = agregarEntradaChangelog(documento, {
 version: incrementarVersion(metadata.version),
 fecha: new Date().toISOString(),
 autor: "Sync Validator (Interactivo)",
 cambios: `Sincronización interactiva: ${cambiosRealizados} divergencias corregidas`
 });

escribirArchivo(ARCHIVO_ENCONTRADO, documento);

console.log(`\n✅ SINCRONIZACIÓN COMPLETA\n`);
 console.log(` Cambios aplicados: ${cambiosRealizados}`);
 console.log(` Divergencias omitidas: ${divergencias.length - cambiosRealizados}`);
 } else {
 console.log(`\n📝 No se realizaron cambios\n`);
 }
}
```

---

#### Acción 4.4: Solo Reporte (Opción C)

```typescript
if (opcion === "C") {
 console.log("📝 Generando solo reporte...\n");

const reportePath = `@.gemini/reports/sync-report-${FECHA}.md`;
 const jsonPath = `@.gemini/reports/divergences-${FECHA}.json`;

escribirArchivo(reportePath, reporteMarkdown);
 escribirArchivo(jsonPath, JSON.stringify(reporteJSON, null, 2));

console.log(`✅ Reportes generados:\n`);
 console.log(` 📄 Markdown: ${reportePath}`);
 console.log(` 📊 JSON: ${jsonPath}\n`);

console.log(`📋 Próximos pasos sugeridos:\n`);
 console.log(` 1. Revisar el reporte markdown`);
 console.log(` 2. Discutir con el equipo si es necesario`);
 console.log(` 3. Ejecutar sincronización cuando estés listo:`);
 console.log(` gemini-cli < @.gemini/prompts/sync-project-summary.md\n`);
}
```

---

### FASE 5: Validación Final

```typescript
// Después de cualquier sincronización:
if (cambiosRealizados > 0) {
 console.log(`\n🔍 Validando documento actualizado...\n`);

// Re-leer documento actualizado
 const documentoActualizado = leerArchivo(ARCHIVO_ENCONTRADO);

// Validar estructura
 const validaciones = [
 validarEstructuraMarkdown(documentoActualizado),
 validarMetadata(documentoActualizado),
 validarChangelog(documentoActualizado),
 validarNoHayPlaceholders(documentoActualizado)
 ];

const todasPasaron = validaciones.every(v => v.passed);

if (todasPasaron) {
 console.log(`✅ Todas las validaciones pasaron\n`);
 } else {
 console.log(`⚠️ Algunas validaciones fallaron:\n`);
 validaciones.filter(v => !v.passed).forEach(v => {
 console.log(` ❌ ${v.name}: ${v.error}`);
 });
 }

// Calcular nuevo índice de sincronización
 const nuevoIndice = calcularIndiceSincronizacion(documentoActualizado, estadoActual);
 console.log(`\n📊 Índice de sincronización: ${nuevoIndice}%\n`);

if (nuevoIndice === 100) {
 console.log(`🎉 ¡Documento 100% sincronizado!\n`);
 } else {
 console.log(`📝 Aún quedan algunas divergencias menores (${100 - nuevoIndice}%)\n`);
 }
}
```

---

## 4. EJEMPLO DE EJECUCIÓN COMPLETA

```bash
# Usuario ejecuta desde ioc-backend
cd ~/projects/ioc-backend
gemini-cli < @.gemini/prompts/sync-project-summary.md
# Output:
[IA] 🔍 Detectando repositorio...
[IA] ✅ Repositorio: ioc-backend (Backend)
[IA] ✅ Repositorio hermano: ioc-frontend (accesible)
[IA] 🔍 Buscando document document...
[IA] ✅ Encontrado: ../ioc-backend/@.gemini/project-summary.md
[IA] 📋 Leyendo metadata...
[IA] ℹ️ Última actualización: 2024-01-01 (14 días atrás)
[IA] ℹ️ Versión: 1.0-FULL
[IA] ℹ️ Status: COMPLETE
[IA] 🔬 Analizando código backend...
[IA] ✅ Spring Boot detectado: 3.3.0 (documentado: 3.2.1)
[IA] ✅ 23 endpoints detectados (documentados: 20)
[IA] ✅ 18 dependencias principales
[IA] 🔬 Analizando código frontend...
[IA] ✅ React 19 detectado
[IA] ✅ 47 dependencias detectadas
[IA] ✅ 23 componentes, 8 páginas
[IA] 🔍 Comparando con documento...
[IA] ⚠️ 2 divergencias CRÍTICAS detectadas
[IA] ⚠️ 5 divergencias IMPORTANTES detectadas
[IA] ℹ️ 8 divergencias MENORES detectadas
[IA] ✅ Validando consistencia cross-repo...
[IA] ⚠️ 1 inconsistencia detectada: Endpoint sin backend
[IA] 📊 Generando reporte...
[IA] ✅ Reporte generado: @.gemini/reports/sync-report-2024-01-15.md
[IA] ✅ JSON generado: @.gemini/reports/divergences-2024-01-15.json
---
📋 REPORTE COMPLETO GENERADO
Se detectaron 15 divergencias (2 críticas, 5 importantes, 8 menores).
Índice de sincronización: 87.5% (meta: ≥95%)
Ver reporte completo en: @.gemini/reports/sync-report-2024-01-15.md
---
¿Deseas sincronizar el documento ahora?
A) Sincronización Automática Completa (~25 min)
B) Sincronización Interactiva (~35 min)
C) Solo Generar Reporte
D) Cancelar
Tu elección: _
```

---

## 5. CONFIGURACIÓN AVANZADA

### Variables de Configuración (Opcional)

```yaml
# .gemini/sync-config.yml (opcional)
sync_settings:
 # Umbrales de alerta
 thresholds:
 critical_alert: 1 # Alertar si hay 1+ críticas
 important_alert: 5 # Alertar si hay 5+ importantes
 outdated_days: 30 # Alertar si >30 días sin actualizar
 sync_index_min: 95 # Índice mínimo aceptable

# Qué analizar
 analysis:
 check_dependencies: true
 check_endpoints: true
 check_env_vars: true
 check_directory_structure: false # Solo para cambios mayores

# Cross-repo validation
 cross_repo:
 enabled: true
 backend_path: "../ioc-backend"
 frontend_path: "../ioc-frontend"

# Sincronización automática
 auto_sync:
 enabled: false # Requiere confirmación por defecto
 auto_fix_minor: true # Auto-fix divergencias menores
 create_backup: true
 backup_retention_days: 30

# Reportes
 reports:
 generate_markdown: true
 generate_json: true
 generate_html: false # Para visualización en browser
 output_dir: "@.gemini/reports"
```

---

## 6. INTEGRACIÓN CON CI/CD (Bonus)

```yaml
# .github/workflows/doc-sync-check.yml
name: Documentation Sync Check
on:
 pull_request:
 branches: [main, develop]
 schedule:
 - cron: '0 0 * * 1' # Todos los lunes a medianoche
jobs:
 check-sync:
 runs-on: ubuntu-latest

steps:
 - name: Checkout Backend
 uses: actions/checkout@v3
 with:
 path: ioc-backend

- name: Checkout Frontend
 uses: actions/checkout@v3
 with:
 repository: org/ioc-frontend
 path: ioc-frontend
 token: ${{ secrets.GH_PAT }}

- name: Setup Gemini CLI
 run: |
 npm install -g @google/generative-ai-cli

- name: Run Sync Validator
 run: |
 cd ioc-backend
 gemini-cli < @.gemini/prompts/sync-project-summary.md --mode=ci

- name: Check Sync Index
 run: |
 SYNC_INDEX=$(jq '.summary.sync_index' ioc-backend/@.gemini/reports/divergences-latest.json)
 if (( $(echo "$SYNC_INDEX < 95" | bc -l) )); then
 echo "::error::Documentation sync index is ${SYNC_INDEX}% (minimum: 95%)"
 exit 1
 fi

- name: Upload Report
 if: always()
 uses: actions/upload-artifact@v3
 with:
 name: sync-report
 path: ioc-backend/@.gemini/reports/

- name: Comment PR
 if: github.event_name == 'pull_request' && failure()
 uses: actions/github-script@v6
 with:
 script: |
 github.rest.issues.createComment({
 issue_number: context.issue.number,
 owner: context.repo.owner,
 repo: context.repo.name,
 body: '⚠️ **Documentation out of sync!**\n\nPlease run the sync validator and update `project-summary.md`.\n\nSee [sync report](../actions/runs/${{ github.run_id }}) for details.'
 })
```

---

**¿Listo para usar estos 3 prompts?** 🚀
Tienes ahora:

1. ✅ **Prompt #1**: Backend Generator (genera documento base)
2. ✅ **Prompt #2**: Frontend Completer (completa secciones frontend)
3. ✅ **Prompt #3**: Sync Validator (mantiene sincronizado)
   ¿Quieres que cree un **README.md** que explique cómo usar estos 3 prompts en conjunto, o prefieres que generemos ejemplos de los archivos de salida?
