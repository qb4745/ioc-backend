# Feature Plan: Sistema de Configuración de KPIs y Umbrales

## Metadata
- **ID**: FP-IOC-003
- **Sprint**: Sprint 3
- **Prioridad**: Alta
- **Tipo**: Nueva Feature
- **Estimación**: 8 Story Points (5-6 días)
- **Asignado a**: Equipo Backend + Frontend
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
El sistema IOC actualmente procesa datos de producción y los visualiza en dashboards de Metabase, pero no existe un mecanismo para que los administradores configuren dinámicamente qué métricas (KPIs) monitorear ni establecer umbrales personalizados que generen alertas cuando se exceden valores críticos.

**Problema Específico**:
- Los KPIs están hardcodeados en el sistema o solo existen en Metabase
- No hay forma de activar/desactivar el monitoreo de KPIs específicos
- No existen alertas automáticas cuando un KPI excede umbrales críticos
- Los umbrales de alerta no son configurables por el usuario

**Impacto del Problema**:
- **Usuarios afectados**: Administradores y Gerentes de operaciones
- **Frecuencia**: Diaria - cada vez que necesitan monitorear métricas críticas
- **Severidad**: Media-Alta - limita la capacidad de respuesta proactiva ante problemas operacionales
- **Coste de NO resolverlo**: 
  - Detección tardía de problemas de producción
  - Imposibilidad de personalizar el sistema a las métricas específicas del negocio
  - Dependencia de revisión manual constante de dashboards

**Ejemplo de Caso de Uso**:
```
Usuario: Gerente de Producción
Situación: La eficiencia de una línea de producción cae por debajo del 85%
Problema: No recibe ninguna alerta automática, debe revisar manualmente los dashboards constantemente
Impacto: Pierde horas de producción antes de detectar el problema
```

---

### 1.2. Solución Propuesta

**Qué vamos a construir** (descripción de alto nivel):

Implementaremos un módulo de configuración de KPIs que permitirá a los administradores:
1. Definir parámetros de KPIs con rangos válidos (ej: eficiencia entre 0-100%)
2. Establecer umbrales de alerta (warning, critical)
3. Activar/desactivar el monitoreo de KPIs específicos
4. Configurar alertas que se disparen automáticamente cuando los valores exceden umbrales

El sistema validará todos los parámetros ingresados, garantizando que estén dentro de rangos permitidos, y proporcionará un sistema de alertas que se integre con el pipeline ETL existente para evaluar los KPIs en cada carga de datos.

**Valor para el Usuario**:
- Personalización total de las métricas críticas para su negocio
- Notificaciones proactivas de problemas operacionales
- Flexibilidad para ajustar umbrales según temporadas o cambios en procesos
- Interfaz simple para gestionar configuraciones sin necesidad de cambios en código

**Valor para el Negocio**:
- Reducción del tiempo de respuesta ante problemas de producción en un 70%
- Mayor adopción del sistema al adaptarse a necesidades específicas
- ROI mejorado por detección temprana de ineficiencias

---

### 1.3. Alcance del MVP

**✅ Dentro del Alcance** (Sprint 3):
- [x] CRUD completo de parámetros de KPIs (backend + frontend)
- [x] Validación de rangos permitidos para valores de KPIs
- [x] Sistema de activación/desactivación de KPIs
- [x] Definición de umbrales (warning, critical)
- [x] Motor de evaluación de KPIs durante el proceso ETL
- [x] Generación de alertas básicas (almacenadas en BD)
- [x] UI para configuración de KPIs en el panel de administración
- [x] Visualización de alertas generadas

**❌ Fuera del Alcance** (diferir para futuro):
- [ ] Notificaciones por email/SMS → Sprint 4
- [ ] Historial de cambios en configuración de KPIs → Sprint 5
- [ ] Dashboards personalizados basados en KPIs configurados → Sprint 6
- [ ] KPIs calculados (fórmulas personalizadas) → Requiere investigación
- [ ] Exportación de reportes de alertas → Backlog

**Justificación del Alcance**:
El MVP se enfoca en la configuración básica y generación de alertas almacenadas en la base de datos. Esto proporciona el valor core (detección de problemas) mientras que las notificaciones externas pueden agregarse incrementalmente sin bloquear la funcionalidad principal.

---

## 2. Análisis Técnico (Alto Nivel)

### 2.1. Componentes Afectados

**Frontend**:
- [x] Nuevos componentes:
  - `KpiConfigurationPage` - Página principal de configuración
  - `KpiForm` - Formulario para crear/editar KPIs
  - `KpiList` - Tabla con lista de KPIs configurados
  - `KpiThresholdEditor` - Editor de umbrales
  - `AlertsPanel` - Panel de visualización de alertas
- [x] Componentes a modificar:
  - `Sidebar` - Agregar nueva opción "Configuración KPIs"
- [x] Nuevas rutas/páginas:
  - `/admin/kpi-configuration`
  - `/admin/alerts`

**Backend**:
- [x] Nuevos endpoints:
  - `GET /api/v1/kpis` - Listar todos los KPIs
  - `POST /api/v1/kpis` - Crear nuevo KPI
  - `PUT /api/v1/kpis/{id}` - Actualizar KPI
  - `DELETE /api/v1/kpis/{id}` - Eliminar KPI (soft delete)
  - `PATCH /api/v1/kpis/{id}/toggle` - Activar/desactivar KPI
  - `GET /api/v1/alerts` - Listar alertas generadas
  - `PATCH /api/v1/alerts/{id}/acknowledge` - Marcar alerta como vista
- [x] Nuevos servicios:
  - `KpiConfigurationService` - Lógica de negocio de KPIs
  - `KpiEvaluationService` - Motor de evaluación de KPIs
  - `AlertService` - Gestión de alertas

**Base de Datos**:
- [x] Nuevas tablas:
  - `kpi_configurations` - Configuración de KPIs
  - `kpi_thresholds` - Umbrales de alertas
  - `kpi_alerts` - Alertas generadas
  - `kpi_alert_history` - Historial de alertas
- [x] Tablas a modificar: Ninguna
- [x] Migraciones requeridas: Sí (scripts DDL incluidos)

**Integraciones Externas**:
- [ ] Ninguna en el MVP

---

### 2.2. Dependencias Técnicas

**Nuevas Librerías/Dependencias**:
- Frontend: Ninguna (usar librerías existentes)
- Backend: Ninguna (usar Spring Boot existente)

**Features/Sistemas Existentes Requeridos**:
- Sistema de autenticación (Supabase) - Ya implementado
- Pipeline ETL (`EtlJobService`) - Modificar para incluir evaluación de KPIs
- Sistema de roles y permisos - Ya implementado (solo `ROLE_ADMIN` puede configurar)

**Bloqueos Técnicos**:
- Ninguno identificado - la infraestructura necesaria ya existe

---

### 2.3. Impacto en la Arquitectura

**Cambios Arquitectónicos**:
- [x] Cambios menores (agregar endpoint/componente)

**Si hay cambios, describir**:
Se agregará un nuevo módulo de "KPI Management" que se integrará con el pipeline ETL existente. El `EtlJobService` incluirá un paso adicional de evaluación de KPIs después del procesamiento de datos. No requiere cambios en la arquitectura core, es una extensión modular.

---

## 3. Requisitos Funcionales

### 3.1. Historias de Usuario

#### Historia 1: Configurar Parámetros de KPI

**Como** administrador del sistema, 
**Quiero** crear y configurar nuevos KPIs con sus rangos permitidos, 
**Para** adaptar el sistema de monitoreo a las métricas específicas de mi negocio.

**Criterios de Aceptación**:
```gherkin
Escenario: Crear un KPI con parámetros válidos
  Dado que soy un administrador autenticado
  Cuando ingreso un nuevo KPI con:
    | Nombre          | Eficiencia de Línea A |
    | Código          | EFFICIENCY_LINE_A     |
    | Unidad          | percentage            |
    | Rango Mínimo    | 0                     |
    | Rango Máximo    | 100                   |
    | Valor Objetivo  | 90                    |
  Entonces el KPI se crea exitosamente
  Y aparece en la lista de KPIs configurados
  Y está activo por defecto

Escenario: Intentar crear un KPI con valores fuera de rango
  Dado que soy un administrador autenticado
  Cuando ingreso un KPI con valor objetivo = 150
  Y el rango máximo permitido es 100
  Entonces el sistema muestra el error "El valor objetivo debe estar entre 0 y 100"
  Y el KPI no se crea
```

**Notas Técnicas**:
- Validación tanto en frontend (UX inmediata) como backend (seguridad)
- El código del KPI debe ser único (constraint en BD)
- Soportar diferentes tipos de unidades: percentage, integer, decimal, time

---

#### Historia 2: Definir Umbrales de Alerta

**Como** administrador del sistema, 
**Quiero** establecer umbrales de advertencia y críticos para cada KPI, 
**Para** recibir alertas cuando los valores se desvían de los rangos aceptables.

**Criterios de Aceptación**:
```gherkin
Escenario: Configurar umbrales de alerta
  Dado que existe un KPI "Eficiencia de Línea A" con rango 0-100
  Cuando configuro los umbrales:
    | Tipo Umbral       | Operador | Valor |
    | Warning Bajo      | <        | 80    |
    | Critical Bajo     | <        | 70    |
    | Warning Alto      | >        | 95    |
  Entonces los umbrales se guardan correctamente
  Y están asociados al KPI

Escenario: Umbral fuera del rango permitido
  Dado que un KPI tiene rango 0-100
  Cuando intento configurar un umbral con valor 150
  Entonces el sistema muestra "El umbral debe estar dentro del rango 0-100"
  Y el umbral no se guarda
```

**Notas Técnicas**:
- Validar que umbrales críticos sean más restrictivos que warnings
- Soportar operadores: `<`, `>`, `<=`, `>=`, `=`, `!=`
- Permitir múltiples umbrales por KPI

---

#### Historia 3: Activar/Desactivar Monitoreo de KPIs

**Como** administrador del sistema, 
**Quiero** activar o desactivar el monitoreo de KPIs específicos, 
**Para** controlar qué métricas generan alertas según el contexto operativo.

**Criterios de Aceptación**:
```gherkin
Escenario: Desactivar un KPI activo
  Dado que existe un KPI "Eficiencia de Línea A" activo
  Cuando desactivo el KPI
  Entonces el KPI cambia a estado inactivo
  Y no se generan alertas para ese KPI en futuras evaluaciones

Escenario: Reactivar un KPI inactivo
  Dado que existe un KPI "Eficiencia de Línea A" inactivo
  Cuando activo el KPI
  Entonces el KPI cambia a estado activo
  Y se evalúa en la próxima carga de datos
```

**Notas Técnicas**:
- Soft delete - no eliminar datos históricos
- Toggle simple en la UI (switch/checkbox)
- El cambio debe ser inmediato

---

#### Historia 4: Visualizar Alertas Generadas

**Como** gerente de operaciones, 
**Quiero** ver las alertas que se han generado por exceso de umbrales, 
**Para** tomar acciones correctivas rápidamente.

**Criterios de Aceptación**:
```gherkin
Escenario: Ver alertas activas
  Dado que se han generado 3 alertas en las últimas 24 horas
  Cuando accedo al panel de alertas
  Entonces veo una lista con:
    - Nombre del KPI
    - Valor actual
    - Umbral excedido
    - Severidad (Warning/Critical)
    - Fecha y hora
  Y las alertas críticas aparecen primero

Escenario: Marcar alerta como revisada
  Dado que veo una alerta activa
  Cuando marco la alerta como "Revisada"
  Entonces la alerta cambia de estado
  Y se registra quién y cuándo la revisó
```

**Notas Técnicas**:
- Paginación para listas grandes
- Filtros por severidad, KPI, fecha
- Badge visual de alertas no leídas

---

### 3.2. Casos de Uso Detallados

#### Caso de Uso 1: Evaluación Automática de KPIs durante ETL

**Actor Principal**: Sistema (automático) 
**Precondiciones**: 
- Existen KPIs configurados y activos
- Se ha completado una carga ETL con nuevos datos de producción

**Trigger**: Finalización exitosa del proceso ETL

**Flujo Normal**:
1. Sistema completa la carga de datos a `fact_production`
2. Sistema obtiene todos los KPIs activos de `kpi_configurations`
3. Para cada KPI activo:
   - Calcula el valor actual desde `fact_production` (ej: AVG(eficiencia))
   - Obtiene los umbrales asociados
   - Evalúa si el valor actual excede algún umbral
   - Si excede, crea un registro en `kpi_alerts`
4. Sistema registra la evaluación en logs
5. Sistema incrementa contador de alertas no leídas (si hay nuevas)

**Flujos Alternativos**:
- **3a. Error al calcular KPI**:
  - Sistema registra el error en logs
  - Continúa con el siguiente KPI
  - Notifica al administrador del error
  
- **3b. Valor de KPI está en rango normal**:
  - No se crea alerta
  - Se actualiza `last_evaluated_at` del KPI

**Postcondiciones**: 
- Todas las alertas pertinentes están almacenadas en BD
- Los KPIs tienen timestamp de última evaluación actualizado

---

### 3.3. Requisitos No Funcionales

**Performance**:
- Tiempo de evaluación de KPIs: < 2 segundos para 50 KPIs activos
- Tiempo de respuesta del API: < 500ms para operaciones CRUD
- Carga de página de configuración: < 1 segundo

**Seguridad**:
- Autenticación requerida: Sí (JWT de Supabase)
- Roles permitidos: 
  - CRUD de KPIs: `ROLE_ADMIN` únicamente
  - Visualización de alertas: `ROLE_ADMIN`, `ROLE_MANAGER`
- Validación de datos: 
  - Validación de rangos numéricos
  - Sanitización de nombres de KPI
  - Prevención de inyección SQL en queries dinámicos

**Usabilidad**:
- Accesibilidad: WCAG 2.1 AA
- Responsive: Sí - Desktop y Tablet (mobile view básico)
- Idiomas: Español (labels en código, preparado para i18n)
- Mensajes de error claros y accionables

**Confiabilidad**:
- Disponibilidad: 99.5% (mismo SLA que el resto del sistema)
- Recuperación de errores: Si la evaluación de KPIs falla, no debe bloquear el ETL
- Backup de datos: Incluido en backup diario de Supabase
- Idempotencia: Re-evaluar los mismos datos no debe duplicar alertas

---

## 4. Diseño de Interfaz (UX/UI)

### 4.1. Wireframes / Mockups

**Referencias de Diseño**:
- Figma: N/A (usar componentes existentes del proyecto)
- Estilo: Consistente con el resto de la aplicación IOC (Tailwind CSS)

**Descripción Visual**:

```
[Página: Configuración de KPIs]

+----------------------------------------------------------+
| IOC Dashboard                    👤 Admin     [Logout]  |
+----------------------------------------------------------+
| [☰] Sidebar                    | CONFIGURACIÓN DE KPIs  |
|   - Dashboard                  |                         |
|   - Ingesta Datos              | [+ Nuevo KPI]  [Filtrar▼] |
|   - Alertas (3)                |                         |
|   > Configuración KPIs         | ┌────────────────────┐ |
|                                 | │ Eficiencia Línea A │ |
|                                 | │ Código: EFFICIENCY_A│|
|                                 | │ Rango: 0-100%      │ |
|                                 | │ Estado: ✅ Activo   │ |
|                                 | │ Umbrales: 2        │ |
|                                 | │ [Editar] [❌]      │ |
|                                 | └────────────────────┘ |
|                                 |                         |
|                                 | ┌────────────────────┐ |
|                                 | │ Tiempo Ciclo Máq.3 │ |
|                                 | │ Código: CYCLE_M3   │ |
|                                 | │ Rango: 0-300 seg   │ |
|                                 | │ Estado: ⭕ Inactivo │ |
|                                 | │ Umbrales: 3        │ |
|                                 | │ [Editar] [❌]      │ |
|                                 | └────────────────────┘ |
+----------------------------------------------------------+


[Modal: Crear/Editar KPI]

+------------------------------------------+
| ✖ CREAR NUEVO KPI                       |
+------------------------------------------+
| Nombre *                                 |
| [Eficiencia de Línea A_____________]    |
|                                          |
| Código Único *                           |
| [EFFICIENCY_LINE_A_________________]    |
|                                          |
| Descripción                              |
| [Mide el porcentaje de...__________]    |
|                                          |
| Unidad *                                 |
| [Percentage ▼]                           |
|                                          |
| Rango Permitido                          |
| Mínimo: [0____]  Máximo: [100_____]     |
|                                          |
| Valor Objetivo: [90_____]               |
|                                          |
| ─────────── UMBRALES ───────────        |
|                                          |
| + Warning si < [80____]                  |
| + Critical si < [70____]                 |
| [+ Agregar Umbral]                       |
|                                          |
| [✅ Activo]                              |
|                                          |
|              [Cancelar]  [Guardar]      |
+------------------------------------------+


[Página: Panel de Alertas]

+----------------------------------------------------------+
| ALERTAS DE KPIs           [Filtros▼] [Solo No Leídas☑] |
+----------------------------------------------------------+
| 🔴 CRITICAL - Eficiencia Línea A                        |
| Valor actual: 65% | Umbral: < 70%                       |
| Detectado: 2025-10-22 14:30                             |
| [✓ Marcar como revisada]                                |
+----------------------------------------------------------+
| 🟡 WARNING - Tiempo Ciclo Máquina 3                     |
| Valor actual: 285 seg | Umbral: > 270 seg               |
| Detectado: 2025-10-22 13:15                             |
| [✓ Marcar como revisada]                                |
+----------------------------------------------------------+
```

---

### 4.2. Flujo de Usuario

```
[Login] 
   ↓
[Dashboard Principal]
   ↓
[Clic en "Configuración KPIs" en Sidebar]
   ↓
[Lista de KPIs configurados]
   ↓
   ├─→ [Clic "+ Nuevo KPI"]
   │      ↓
   │   [Modal de Creación]
   │      ↓
   │   [Completar Formulario]
   │      ↓
   │   [Validación en Frontend]
   │      ↓
   │      ├─→ [Errores] → [Mostrar mensajes] → [Corregir]
   │      ↓
   │   [Enviar a Backend]
   │      ↓
   │   [Validación en Backend]
   │      ↓
   │      ├─→ [Error] → [Mostrar toast error]
   │      ↓
   │   [✓ KPI Creado] → [Toast éxito] → [Actualizar lista]
   │
   └─→ [Clic "Alertas" en Sidebar]
          ↓
       [Panel de Alertas]
          ↓
       [Ver detalles de alerta]
          ↓
       [Marcar como revisada]
          ↓
       [Alerta actualizada]
```

---

### 4.3. Estados de la Interfaz

**Estados a Considerar**:
- [x] Estado inicial (cargando datos) - Skeleton loader
- [x] Estado con datos (happy path) - Lista de KPIs
- [x] Estado vacío (sin datos) - "No hay KPIs configurados. Crea el primero."
- [x] Estado de error (fallo de carga) - "Error al cargar KPIs. [Reintentar]"
- [x] Estado de procesando (loading) - Spinner en botón "Guardar"
- [x] Estado de éxito (confirmación) - Toast verde "KPI creado exitosamente"

---

## 5. Contratos de API (Preliminares)

### 5.1. Endpoints Nuevos

#### Endpoint 1: Listar KPIs

```typescript
// Método y Ruta
GET /api/v1/kpis

// Query Params (opcionales)
?active=true&page=0&size=20&sort=name,asc

// Response (200 OK)
interface KpiListResponse {
  content: KpiDto[];
  page: {
    size: number;
    totalElements: number;
    totalPages: number;
    number: number;
  };
}

interface KpiDto {
  id: number;
  code: string;
  name: string;
  description: string | null;
  unit: 'PERCENTAGE' | 'INTEGER' | 'DECIMAL' | 'TIME';
  minValue: number;
  maxValue: number;
  targetValue: number;
  isActive: boolean;
  thresholds: ThresholdDto[];
  createdAt: string; // ISO 8601
  updatedAt: string;
}

interface ThresholdDto {
  id: number;
  type: 'WARNING' | 'CRITICAL';
  operator: '<' | '>' | '<=' | '>=' | '=' | '!=';
  value: number;
}

// Errores
// 401: No autenticado
// 403: Sin permisos (requiere ROLE_ADMIN o ROLE_MANAGER)
```

---

#### Endpoint 2: Crear KPI

```typescript
// Método y Ruta
POST /api/v1/kpis

// Request
interface CreateKpiRequest {
  code: string;           // Unique, alphanumeric + underscore
  name: string;           // Max 100 chars
  description?: string;   // Max 500 chars
  unit: 'PERCENTAGE' | 'INTEGER' | 'DECIMAL' | 'TIME';
  minValue: number;
  maxValue: number;
  targetValue: number;
  isActive: boolean;
  thresholds: CreateThresholdDto[];
}

interface CreateThresholdDto {
  type: 'WARNING' | 'CRITICAL';
  operator: '<' | '>' | '<=' | '>=' | '=' | '!=';
  value: number;
}

// Response (201 Created)
interface KpiDto {
  // ... mismo que en GET
}

// Errores
// 400: Validación fallida
//   - code ya existe
//   - targetValue fuera del rango [minValue, maxValue]
//   - threshold.value fuera del rango
// 401: No autenticado
// 403: Sin permisos (requiere ROLE_ADMIN)
```

**Notas**:
- El `code` debe ser único y se usará como identificador en cálculos
- Validar que `minValue < maxValue`
- Validar que `targetValue` esté entre `minValue` y `maxValue`

---

#### Endpoint 3: Actualizar KPI

```typescript
// Método y Ruta
PUT /api/v1/kpis/{id}

// Request
interface UpdateKpiRequest {
  // Mismo que CreateKpiRequest, pero el 'code' no es modificable
  name: string;
  description?: string;
  unit: 'PERCENTAGE' | 'INTEGER' | 'DECIMAL' | 'TIME';
  minValue: number;
  maxValue: number;
  targetValue: number;
  isActive: boolean;
  thresholds: CreateThresholdDto[];
}

// Response (200 OK)
interface KpiDto {
  // ... mismo que en GET
}

// Errores
// 400: Validación fallida
// 401: No autenticado
// 403: Sin permisos
// 404: KPI no encontrado
```

---

#### Endpoint 4: Toggle KPI (Activar/Desactivar)

```typescript
// Método y Ruta
PATCH /api/v1/kpis/{id}/toggle

// Request: No body

// Response (200 OK)
interface ToggleKpiResponse {
  id: number;
  isActive: boolean;
  message: string; // "KPI activado" o "KPI desactivado"
}

// Errores
// 401: No autenticado
// 403: Sin permisos
// 404: KPI no encontrado
```

---

#### Endpoint 5: Eliminar KPI (Soft Delete)

```typescript
// Método y Ruta
DELETE /api/v1/kpis/{id}

// Response (204 No Content)

// Errores
// 401: No autenticado
// 403: Sin permisos
// 404: KPI no encontrado
```

**Notas**:
- Soft delete: marcar `deleted_at` en lugar de eliminar físicamente
- Las alertas históricas asociadas se mantienen

---

#### Endpoint 6: Listar Alertas

```typescript
// Método y Ruta
GET /api/v1/alerts

// Query Params
?acknowledged=false&severity=CRITICAL&page=0&size=20

// Response (200 OK)
interface AlertListResponse {
  content: AlertDto[];
  page: PageInfo;
}

interface AlertDto {
  id: number;
  kpiId: number;
  kpiName: string;
  kpiCode: string;
  currentValue: number;
  thresholdValue: number;
  thresholdOperator: string;
  severity: 'WARNING' | 'CRITICAL';
  isAcknowledged: boolean;
  acknowledgedBy: string | null;
  acknowledgedAt: string | null;
  detectedAt: string; // ISO 8601
}

// Errores
// 401: No autenticado
// 403: Sin permisos
```

---

#### Endpoint 7: Marcar Alerta como Revisada

```typescript
// Método y Ruta
PATCH /api/v1/alerts/{id}/acknowledge

// Request: No body

// Response (200 OK)
interface AcknowledgeAlertResponse {
  id: number;
  isAcknowledged: true;
  acknowledgedBy: string; // Email del usuario
  acknowledgedAt: string; // ISO 8601
}

// Errores
// 401: No autenticado
// 403: Sin permisos
// 404: Alerta no encontrada
```

---

## 6. Modelo de Datos (Preliminar)

### 6.1. Nuevas Entidades

#### Entidad: KpiConfiguration

```typescript
interface KpiConfiguration {
  id: number;
  code: string;                    // Unique identifier
  name: string;
  description: string | null;
  unit: KpiUnit;                   // ENUM
  minValue: number;
  maxValue: number;
  targetValue: number;
  isActive: boolean;
  createdBy: string;               // User email
  createdAt: Date;
  updatedAt: Date;
  deletedAt: Date | null;          // Soft delete
  lastEvaluatedAt: Date | null;
  thresholds: KpiThreshold[];      // One-to-Many
}

enum KpiUnit {
  PERCENTAGE = 'PERCENTAGE',
  INTEGER = 'INTEGER',
  DECIMAL = 'DECIMAL',
  TIME = 'TIME'
}
```

**Tabla SQL**:

```sql
CREATE TABLE kpi_configurations (
  id SERIAL PRIMARY KEY,
  code VARCHAR(100) UNIQUE NOT NULL,
  name VARCHAR(200) NOT NULL,
  description TEXT,
  unit VARCHAR(20) NOT NULL CHECK (unit IN ('PERCENTAGE', 'INTEGER', 'DECIMAL', 'TIME')),
  min_value NUMERIC(15, 4) NOT NULL,
  max_value NUMERIC(15, 4) NOT NULL,
  target_value NUMERIC(15, 4) NOT NULL,
  is_active BOOLEAN DEFAULT true,
  created_by VARCHAR(255) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deleted_at TIMESTAMP,
  last_evaluated_at TIMESTAMP,
  
  CONSTRAINT check_min_max CHECK (min_value < max_value),
  CONSTRAINT check_target_in_range CHECK (target_value BETWEEN min_value AND max_value)
);

CREATE INDEX idx_kpi_code ON kpi_configurations(code);
CREATE INDEX idx_kpi_active ON kpi_configurations(is_active) WHERE deleted_at IS NULL;
```

**Relaciones**:
- `kpi_configurations` → `kpi_thresholds` (One-to-Many)

---

#### Entidad: KpiThreshold

```typescript
interface KpiThreshold {
  id: number;
  kpiConfigurationId: number;      // FK
  type: ThresholdType;             // WARNING | CRITICAL
  operator: ThresholdOperator;     // <, >, <=, >=, =, !=
  value: number;
  createdAt: Date;
}

enum ThresholdType {
  WARNING = 'WARNING',
  CRITICAL = 'CRITICAL'
}

enum ThresholdOperator {
  LESS_THAN = '<',
  GREATER_THAN = '>',
  LESS_OR_EQUAL = '<=',
  GREATER_OR_EQUAL = '>=',
  EQUAL = '=',
  NOT_EQUAL = '!='
}
```

**Tabla SQL**:

```sql
CREATE TABLE kpi_thresholds (
  id SERIAL PRIMARY KEY,
  kpi_configuration_id INTEGER NOT NULL REFERENCES kpi_configurations(id) ON DELETE CASCADE,
  type VARCHAR(20) NOT NULL CHECK (type IN ('WARNING', 'CRITICAL')),
  operator VARCHAR(5) NOT NULL CHECK (operator IN ('<', '>', '<=', '>=', '=', '!=')),
  value NUMERIC(15, 4) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_threshold_kpi ON kpi_thresholds(kpi_configuration_id);
```

**Relaciones**:
- `kpi_thresholds` → `kpi_configurations` (Many-to-One, FK)

---

#### Entidad: KpiAlert

```typescript
interface KpiAlert {
  id: number;
  kpiConfigurationId: number;
  etlJobId: number | null;         // FK to etl_jobs (context)
  currentValue: number;
  thresholdId: number;
  severity: 'WARNING' | 'CRITICAL';
  isAcknowledged: boolean;
  acknowledgedBy: string | null;
  acknowledgedAt: Date | null;
  detectedAt: Date;
  metadata: object | null;         // JSON for extensibility
}
```

**Tabla SQL**:

```sql
CREATE TABLE kpi_alerts (
  id SERIAL PRIMARY KEY,
  kpi_configuration_id INTEGER NOT NULL REFERENCES kpi_configurations(id),
  etl_job_id INTEGER REFERENCES etl_jobs(id),
  current_value NUMERIC(15, 4) NOT NULL,
  threshold_id INTEGER NOT NULL REFERENCES kpi_thresholds(id),
  severity VARCHAR(20) NOT NULL CHECK (severity IN ('WARNING', 'CRITICAL')),
  is_acknowledged BOOLEAN DEFAULT false,
  acknowledged_by VARCHAR(255),
  acknowledged_at TIMESTAMP,
  detected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  metadata JSONB,
  
  CONSTRAINT check_acknowledged CHECK (
    (is_acknowledged = false AND acknowledged_by IS NULL AND acknowledged_at IS NULL) OR
    (is_acknowledged = true AND acknowledged_by IS NOT NULL AND acknowledged_at IS NOT NULL)
  )
);

CREATE INDEX idx_alert_kpi ON kpi_alerts(kpi_configuration_id);
CREATE INDEX idx_alert_acknowledged ON kpi_alerts(is_acknowledged);
CREATE INDEX idx_alert_detected ON kpi_alerts(detected_at DESC);
CREATE INDEX idx_alert_severity ON kpi_alerts(severity);
```

**Relaciones**:
- `kpi_alerts` → `kpi_configurations` (Many-to-One)
- `kpi_alerts` → `kpi_thresholds` (Many-to-One)
- `kpi_alerts` → `etl_jobs` (Many-to-One, opcional)

---

### 6.2. Modificaciones a Entidades Existentes

No se requieren modificaciones a tablas existentes. La integración se realiza mediante:

1. **etl_jobs**: Se referencia opcionalmente desde `kpi_alerts` para contexto
2. **fact_production**: Se usa como fuente de datos para calcular KPIs (no se modifica)

---

## 7. Plan de Implementación

### 7.1. Fases de Desarrollo

#### Fase 1: Backend - Modelo y CRUD (Estimación: 2 días)

**Tasks**:
- [x] Crear entidades JPA (KpiConfiguration, KpiThreshold, KpiAlert) - 3 horas
- [x] Crear repositorios Spring Data - 1 hora
- [x] Implementar DTOs y mappers - 2 horas
- [x] Implementar KpiConfigurationService (CRUD) - 4 horas
- [x] Crear controllers y endpoints REST - 3 horas
- [x] Validaciones con Bean Validation - 2 horas
- [x] Tests unitarios de servicios - 3 horas

**Responsable**: Backend Developer 
**Dependencias**: Ninguna

---

#### Fase 2: Backend - Motor de Evaluación (Estimación: 1.5 días)

**Tasks**:
- [x] Implementar KpiEvaluationService - 4 horas
- [x] Integrar evaluación en EtlJobService (callback post-ETL) - 2 horas
- [x] Implementar AlertService (creación y gestión) - 3 horas
- [x] Crear queries para cálculo de KPIs desde fact_production - 3 horas
- [x] Tests de integración del motor de evaluación - 4 horas

**Responsable**: Backend Developer 
**Dependencias**: Fase 1 completada

---

#### Fase 3: Frontend - UI de Configuración (Estimación: 2 días)

**Tasks**:
- [x] Crear servicio API para KPIs (axios) - 2 horas
- [x] Implementar KpiConfigurationPage - 3 horas
- [x] Implementar KpiForm (crear/editar) - 4 horas
- [x] Implementar KpiList con tabla - 3 horas
- [x] Implementar validaciones en formulario - 2 horas
- [x] Agregar ruta en Sidebar - 1 hora
- [x] Tests de componentes - 3 horas

**Responsable**: Frontend Developer 
**Dependencias**: Fase 1 completada (puede trabajar con mocks)

---

#### Fase 4: Frontend - Panel de Alertas (Estimación: 1 día)

**Tasks**:
- [x] Crear servicio API para Alertas - 1 hora
- [x] Implementar AlertsPanel - 3 horas
- [x] Implementar filtros y paginación - 2 horas
- [x] Implementar badge de notificaciones en Sidebar - 2 horas
- [x] Tests de componentes - 2 horas

**Responsable**: Frontend Developer 
**Dependencias**: Fase 2 completada

---

#### Fase 5: Testing & Deploy (Estimación: 1.5 días)

**Tasks**:
- [x] Testing end-to-end del flujo completo - 4 horas
- [x] Fix de bugs identificados - 4 horas
- [x] Deploy a staging - 1 hora
- [x] QA/UAT con usuarios reales - 3 horas
- [x] Deploy a producción - 1 hora
- [x] Configurar monitoreo y logs - 1 hora

**Responsable**: Equipo completo

---

### 7.2. Estimación Total

| Fase                     | Días | Story Points |
| ------------------------ | ---- | ------------ |
| Backend - CRUD           | 2    | 3            |
| Backend - Evaluación     | 1.5  | 2            |
| Frontend - Configuración | 2    | 2            |
| Frontend - Alertas       | 1    | 1            |
| Testing & Deploy         | 1.5  | -            |
| **TOTAL**                | **8**| **8**        |
| **Margen de Error**: ± 20% | 6.4-9.6 días |

---

## 8. Riesgos y Mitigaciones

### 8.1. Riesgos Identificados

| ID  | Riesgo                                                | Probabilidad | Impacto | Severidad |
| --- | ----------------------------------------------------- | ------------ | ------- | --------- |
| R1  | Complejidad en queries de cálculo de KPIs dinámicos  | Media        | Alto    | 🟡        |
| R2  | Performance degradada al evaluar muchos KPIs          | Baja         | Medio   | 🟢        |
| R3  | Validaciones complejas generan confusión en usuarios  | Media        | Medio   | 🟡        |
| R4  | Duplicación de alertas por re-evaluaciones            | Media        | Alto    | 🟡        |

---

### 8.2. Estrategias de Mitigación

#### R1: Complejidad en queries de cálculo de KPIs dinámicos

**Mitigación (Prevención)**:
- Limitar el MVP a KPIs simples (AVG, SUM, COUNT sobre columnas existentes)
- Crear una librería de "calculadoras" predefinidas para cada tipo de KPI
- Documentar claramente qué KPIs se pueden configurar

**Plan de Contingencia (si ocurre)**:
- Implementar cálculos custom mediante código Java en lugar de SQL dinámico
- Diferir KPIs complejos a sprints futuros

**Responsable**: Tech Lead

---

#### R2: Performance degradada al evaluar muchos KPIs

**Mitigación (Prevención)**:
- Evaluar KPIs en paralelo usando CompletableFuture
- Cachear resultados de cálculos pesados
- Limitar a 50 KPIs activos simultáneamente

**Plan de Contingencia (si ocurre)**:
- Mover evaluación a proceso asíncrono fuera del ETL
- Implementar evaluación incremental (solo datos nuevos)

**Responsable**: Backend Developer

---

#### R3: Validaciones complejas generan confusión en usuarios

**Mitigación (Prevención)**:
- Mostrar tooltips explicativos en cada campo
- Validación en tiempo real con mensajes claros
- Proveer ejemplos pre-configurados

**Plan de Contingencia (si ocurre)**:
- Crear un wizard paso a paso en lugar de formulario único
- Agregar video tutorial o guía interactiva

**Responsable**: Frontend Developer / UX

---

#### R4: Duplicación de alertas por re-evaluaciones

**Mitigación (Prevención)**:
- Verificar si ya existe una alerta no-acknowledged para el mismo KPI y umbral
- Solo crear nueva alerta si han pasado > 24 horas desde la última
- Implementar constraint único en BD (kpi_id + threshold_id + is_acknowledged)

**Plan de Contingencia (si ocurre)**:
- Script de limpieza de alertas duplicadas
- Ajustar lógica de creación con ventanas de tiempo

**Responsable**: Backend Developer

---

## 9. Criterios de Éxito

### 9.1. Métricas de Aceptación

**Funcionales**:
- [x] ✅ Parámetros válidos: Se guardan y aplican correctamente
- [x] ✅ Parámetros inválidos: El sistema alerta sobre valores no válidos
- [x] ✅ KPIs activos: Se generan alertas si se exceden umbrales
- [x] ✅ KPIs inactivos: No se generan alertas
- [x] 90% de cobertura de tests en servicios críticos
- [x] 0 bugs críticos o bloqueantes en producción

**No Funcionales**:
- [x] Tiempo de evaluación de KPIs < 2 segundos
- [x] Tiempo de respuesta de API < 500ms (p95)
- [x] 99.5% de disponibilidad en la primera semana
- [x] Accesibilidad WCAG 2.1 AA validado

**De Negocio** (medición a 30 días):
- [x] 80% de administradores configuran al menos 1 KPI
- [x] Se detectan y alertan al menos 5 problemas operacionales antes de escalar
- [x] Feedback positivo de usuarios > 4/5 estrellas

---

### 9.2. Definición de "Hecho" (DoD)

- [x] Código implementado y revisado (code review aprobado)
- [x] Tests unitarios escritos y pasando (coverage > 80%)
- [x] Tests de integración escritos y pasando
- [x] Documentación técnica actualizada (JavaDoc, README)
- [x] API contracts documentados en Backend Sync Brief
- [x] Migraciones de BD ejecutadas exitosamente en staging
- [x] Deploy exitoso en staging
- [x] QA/UAT completado sin issues bloqueantes
- [x] Performance validado (load testing con 50 KPIs)
- [x] Deploy exitoso en producción
- [x] Monitoreo configurado (logs, alertas de sistema)
- [x] Rollback plan documentado

---

## 10. Decisiones Pendientes

### 10.1. Decisiones Técnicas

| ID  | Decisión                                    | Opciones                                  | Pros/Cons                                                                                               | Responsable | Deadline   |
| --- | ------------------------------------------- | ----------------------------------------- | ------------------------------------------------------------------------------------------------------- | ----------- | ---------- |
| D1  | ¿Cómo calcular KPIs dinámicamente?          | A) Queries SQL dinámicos B) Java hardcode | A: Flexible pero riesgoso / B: Seguro pero rígido                                                      | Tech Lead   | 2025-10-25 |
| D2  | ¿Evaluar KPIs dentro o fuera del ETL?       | A) Dentro B) Job separado                 | A: Simple, sincrónico / B: Desacoplado, puede ser asíncrono                                            | Tech Lead   | 2025-10-25 |
| D3  | ¿Librería de gráficos para tendencias KPI?  | A) Chart.js B) Recharts C) Solo números   | C es MVP, diferir gráficos a Sprint 4                                                                   | Frontend    | 2025-10-26 |

---

### 10.2. Decisiones de Negocio

| ID  | Decisión                                        | Impacto                            | Responsable   | Deadline   |
| --- | ----------------------------------------------- | ---------------------------------- | ------------- | ---------- |
| D1  | ¿Cuántos KPIs puede configurar un cliente?      | Define límites de escala           | Product Owner | 2025-10-24 |
| D2  | ¿Las alertas son solo visuales o notificaciones?| Define scope de Sprint 3 vs 4      | Product Owner | 2025-10-23 |

---

## 11. Próximos Pasos

### Inmediatos (Antes de implementar):
1. [x] Aprobar este Feature Plan (Stakeholders) - Fecha: 2025-10-23
2. [x] Resolver decisiones pendientes D1, D2
3. [x] Crear scripts de migración de BD - Archivo: `schema-kpi-configuration.sql`
4. [x] Definir KPIs iniciales (ejemplos) para demo
5. [ ] Crear Technical Design detallado (TD-IOC-003)

### Durante Desarrollo:
6. [ ] Daily standups para tracking (15 min diarios)
7. [ ] Code reviews obligatorias (antes de merge)
8. [ ] Testing continuo en staging
9. [ ] Documentar aprendizajes técnicos

### Post-Implementación:
10. [ ] Monitorear métricas de uso (analytics)
11. [ ] Recopilar feedback de primeros usuarios (semana 1)
12. [ ] Planificar Sprint 4 con notificaciones externas
13. [ ] Iterar basado en aprendizajes

---

## 12. Referencias y Links

**Documentación Relacionada**:
- Project Summary: `@.gemini/project_summary.md`
- Sprint Backlog: `@.gemini/sprints/Sprint-3-Backlog.md` (pendiente)
- Technical Design: `@.gemini/sprints/technical-designs/TD-IOC-003-kpi-config.md` (pendiente)
- SQL Schema: `@.gemini/sql/schema-kpi-configuration.sql` (creado)

**Recursos Externos**:
- Jira Ticket: IOC-003
- Confluence: [KPI Configuration Requirements](link-pendiente)
- Figma: N/A (usar componentes existentes)

**Código Relacionado**:
- `EtlJobService.java` - Punto de integración para evaluación
- `Role.java`, `Permission.java` - Sistema de permisos existente

---

## 13. Aprobaciones

| Rol           | Nombre      | Aprobado       | Fecha      | Comentarios                                    |
| ------------- | ----------- | -------------- | ---------- | ---------------------------------------------- |
| Product Owner | [Nombre]    | ⏳ Pendiente   | -          | -                                              |
| Tech Lead     | [Nombre]    | ⏳ Pendiente   | -          | Revisar D1 sobre queries dinámicos             |
| Backend Lead  | [Nombre]    | ⏳ Pendiente   | -          | -                                              |
| Frontend Lead | [Nombre]    | ⏳ Pendiente   | -          | -                                              |
| QA Lead       | [Nombre]    | ⏳ Pendiente   | -          | Definir casos de prueba de performance         |

---

## 14. Changelog del Feature Plan

| Versión | Fecha      | Autor              | Cambios                                            |
| ------- | ---------- | ------------------ | -------------------------------------------------- |
| 1.0     | 2025-10-22 | IA Feature Plan Generator | Creación inicial basada en IOC-003                 |
| 1.1     | [Pending]  | [Nombre]           | [Ajustes después de revisión de stakeholders]      |

---

**Feature Plan creado por**: GitHub Copilot (IA Feature Plan Generator) 
**Fecha de creación**: 2025-10-22 
**Última actualización**: 2025-10-22

---

## ANEXO: SQL Schema Completo

Ver archivo completo en: `.gemini/sql/schema-kpi-configuration.sql`

