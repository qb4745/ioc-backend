# 📄 PROMPT 5: Feature Plan Generator

Perfecto, vamos paso a paso. Empecemos con el **Feature Plan Generator**.
---

## PARTE 1: Plantilla del Feature Plan

Primero, creemos la plantilla que el prompt usará como guía.
**Archivo**: `@.gemini/templates/feature-plan-template.md`

```markdown
# Feature Plan: [Nombre de la Feature]
## Metadata
- **ID**: FP-[XXX]
- **Sprint**: Sprint [X]
- **Prioridad**: [Alta | Media | Baja]
- **Tipo**: [Nueva Feature | Mejora | Bug Fix | Refactor]
- **Estimación**: [Story Points o Días]
- **Asignado a**: [Equipo/Persona o Pendiente]
- **Estado**: 
- [ ] Planificación
 - [ ] En Diseño
 - [ ] Listo para Desarrollo
 - [ ] En Desarrollo
 - [ ] En Testing
 - [ ] Completado
- **Fecha Creación**: [YYYY-MM-DD]
- **Última Actualización**: [YYYY-MM-DD]
---
## 1. Contexto de Negocio
### 1.1. Problema a Resolver
**Contexto Actual**:
[Descripción del estado actual del sistema y por qué no es suficiente]
**Problema Específico**:
[Descripción concreta del problema que esta feature resuelve]
**Impacto del Problema**:
- **Usuarios afectados**: [Quiénes sufren este problema]
- **Frecuencia**: [Con qué frecuencia ocurre]
- **Severidad**: [Qué tan crítico es]
- **Coste de NO resolverlo**: [Qué pasa si no se hace]
**Ejemplo de Caso de Uso**:
```

Usuario: [Tipo de usuario]
Situación: [Contexto específico]
Problema: [Qué no puede hacer o qué es ineficiente]
Impacto: [Consecuencia negativa]

```
---
### 1.2. Solución Propuesta
**Qué vamos a construir** (descripción de alto nivel):
[Explicación de la solución en 2-3 párrafos]
**Valor para el Usuario**:
- [Beneficio 1]
- [Beneficio 2]
- [Beneficio 3]
**Valor para el Negocio**:
- [Métrica 1 que mejorará]
- [Métrica 2 que mejorará]
- [ROI estimado si aplica]
---
### 1.3. Alcance del MVP
**✅ Dentro del Alcance** (Sprint [X]):
- [ ] [Feature core 1]
- [ ] [Feature core 2]
- [ ] [Feature core 3]
**❌ Fuera del Alcance** (diferir para futuro):
- [ ] [Feature nice-to-have 1] → Sprint [X+1]
- [ ] [Feature nice-to-have 2] → Backlog
- [ ] [Feature compleja 3] → Requiere investigación
**Justificación del Alcance**:
[Por qué incluimos X y excluimos Y - principio de MVP]
---
## 2. Análisis Técnico (Alto Nivel)
### 2.1. Componentes Afectados
**Frontend**:
- [ ] Nuevos componentes: [Listar]
- [ ] Componentes a modificar: [Listar]
- [ ] Nuevas rutas/páginas: [Listar]
**Backend**:
- [ ] Nuevos endpoints: [Listar]
- [ ] Endpoints a modificar: [Listar]
- [ ] Nuevos servicios: [Listar]
**Base de Datos**:
- [ ] Nuevas tablas: [Listar]
- [ ] Tablas a modificar: [Listar]
- [ ] Migraciones requeridas: [Sí/No]
**Integraciones Externas**:
- [ ] Nuevos servicios externos: [Listar]
- [ ] APIs de terceros: [Listar]
---
### 2.2. Dependencias Técnicas
**Nuevas Librerías/Dependencias**:
- Frontend: [Listar si aplica]
- Backend: [Listar si aplica]
**Features/Sistemas Existentes Requeridos**:
- [Dependencia 1]
- [Dependencia 2]
**Bloqueos Técnicos**:
- [Si hay algo que debe completarse primero]
---
### 2.3. Impacto en la Arquitectura
**Cambios Arquitectónicos**:
- [ ] No hay cambios (feature aislada)
- [ ] Cambios menores (agregar endpoint/componente)
- [ ] Cambios mayores (nueva integración/servicio)
- [ ] Cambios críticos (refactor de módulo existente)
**Si hay cambios, describir**:
[Qué partes de la arquitectura se verán afectadas y por qué]
---
## 3. Requisitos Funcionales
### 3.1. Historias de Usuario
#### Historia 1: [Título]
**Como** [tipo de usuario], 
**Quiero** [acción/funcionalidad], 
**Para** [beneficio/resultado].
**Criterios de Aceptación**:
```gherkin
Escenario: [Nombre del escenario]
 Dado que [precondición]
 Cuando [acción del usuario]
 Entonces [resultado esperado]
 Y [resultado adicional]
```

**Notas Técnicas**:

- [Consideración técnica 1]
- [Consideración técnica 2]

---

#### Historia 2: [Título]

[Repetir estructura...]
---

### 3.2. Casos de Uso Detallados

#### Caso de Uso 1: [Flujo Principal]

**Actor Principal**: [Usuario/Sistema] 
**Precondiciones**: [Estado inicial requerido] 
**Trigger**: [Qué inicia este flujo]
**Flujo Normal**:

1. Usuario hace [acción]

2. Sistema valida [datos]

3. Sistema [procesa/guarda/envía]

4. Sistema muestra [resultado]

5. Usuario ve [confirmación]
   **Flujos Alternativos**:
- **3a. Error de validación**:
  
  - Sistema muestra mensaje de error
  - Usuario corrige y reintenta

- **4a. Timeout del servicio externo**:
  
  - Sistema muestra mensaje de retry
  - Sistema encola para procesamiento posterior
    **Postcondiciones**: [Estado final del sistema]

---

### 3.3. Requisitos No Funcionales

**Performance**:

- Tiempo de respuesta: < [X] segundos
- Throughput: [Y] requests/segundo
- [Otras métricas]
  **Seguridad**:
- Autenticación requerida: [Sí/No]
- Roles permitidos: [Listar]
- Validación de datos: [Qué se valida]
- [Otras consideraciones]
  **Usabilidad**:
- Accesibilidad: [WCAG 2.1 AA / Otro estándar]
- Responsive: [Sí/No - dispositivos soportados]
- Idiomas: [es / en / multi]
  **Confiabilidad**:
- Disponibilidad: [99.9% / otro]
- Recuperación de errores: [Estrategia]
- Backup de datos: [Si aplica]

---

## 4. Diseño de Interfaz (UX/UI)

### 4.1. Wireframes / Mockups

**Referencias de Diseño**:

- Figma: [URL si existe]
- Sketch: [URL si existe]
- Capturas de pantalla: [Ubicación]
  **Descripción Visual** (si no hay mockups):
  ```
  [Pantalla 1: Login]
- Header con logo
- Formulario centrado con:
  - Input email
  - Input password
  - Botón "Iniciar Sesión"
  - Link "¿Olvidaste tu contraseña?"
- Footer con links legales
  
  ```
  
  ```

---

### 4.2. Flujo de Usuario

```
[Pantalla Inicial]
 ↓
 [Acción 1]
 ↓
 [Validación]
 ↙ ↘
[Error] [Éxito]
 ↓ ↓
[Retry] [Resultado]
```

**Descripción Paso a Paso**:

1. Usuario inicia en [pantalla X]
2. Usuario hace clic en [botón/link]
3. Sistema muestra [modal/página]
4. [Continuar...]

---

### 4.3. Estados de la Interfaz

**Estados a Considerar**:

- [ ] Estado inicial (cargando datos)
- [ ] Estado con datos (happy path)
- [ ] Estado vacío (sin datos)
- [ ] Estado de error (fallo de carga)
- [ ] Estado de procesando (loading)
- [ ] Estado de éxito (confirmación)

---

## 5. Contratos de API (Preliminares)

### 5.1. Endpoints Nuevos

#### Endpoint 1: [Nombre descriptivo]

```typescript
// Método y Ruta
POST /api/v1/[recurso]
// Request
interface RequestBody {
 campo1: string;
 campo2: number;
 // ...
}
// Response (200 OK)
interface SuccessResponse {
 id: string;
 mensaje: string;
 data: {
 // ...
 };
}
// Errores
// 400: Validación fallida
// 401: No autenticado
// 403: Sin permisos
// 500: Error interno
```

**Notas**:

- [Consideraciones especiales]

---

### 5.2. Endpoints a Modificar

#### Endpoint Existente: [GET /api/v1/users]

**Cambios Propuestos**:

- Agregar query param: `?filter=[nuevo filtro]`
- Agregar campo en response: `lastLogin`
- Cambiar comportamiento: [describir]
  **Razón del Cambio**:
  [Por qué es necesario modificar este endpoint]

---

## 6. Modelo de Datos (Preliminar)

### 6.1. Nuevas Entidades

#### Entidad: [Nombre]

```typescript
interface [NombreEntidad] {
 id: UUID;
 campo1: string;
 campo2: number;
 createdAt: DateTime;
 updatedAt: DateTime;
}
```

**Tabla SQL**:

```sql
CREATE TABLE [nombre_tabla] (
 id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
 campo1 VARCHAR(255) NOT NULL,
 campo2 INTEGER,
 created_at TIMESTAMP DEFAULT NOW(),
 updated_at TIMESTAMP DEFAULT NOW()
);
```

**Relaciones**:

- [nombre_tabla] → [otra_tabla] (FK: [campo])

---

### 6.2. Modificaciones a Entidades Existentes

#### Tabla: [users]

**Campos a Agregar**:

```sql
ALTER TABLE users ADD COLUMN last_login TIMESTAMP;
ALTER TABLE users ADD COLUMN preferences JSONB;
```

**Impacto**:

- [Cuántos registros se verán afectados]
- [Si requiere migración de datos]
- [Estrategia de rollback]

---

## 7. Plan de Implementación

### 7.1. Fases de Desarrollo

#### Fase 1: Backend (Estimación: [X] días)

**Tasks**:

- [ ] Crear modelo de datos ([Y] horas)
- [ ] Crear endpoints API ([Z] horas)
- [ ] Implementar lógica de negocio ([W] horas)
- [ ] Tests unitarios ([V] horas)
- [ ] Tests de integración ([U] horas)
  **Responsable**: [Nombre/Equipo] 
  **Dependencias**: [Si las hay]

---

#### Fase 2: Frontend (Estimación: [X] días)

**Tasks**:

- [ ] Crear componentes UI ([Y] horas)
- [ ] Integrar con API ([Z] horas)
- [ ] Manejo de estados y errores ([W] horas)
- [ ] Tests de componentes ([V] horas)
- [ ] Testing manual ([U] horas)
  **Responsable**: [Nombre/Equipo] 
  **Dependencias**: Fase 1 completada (mocks mientras tanto)

---

#### Fase 3: Testing & Deploy (Estimación: [X] días)

**Tasks**:

- [ ] Testing end-to-end ([Y] horas)
- [ ] Fix de bugs ([Z] horas)
- [ ] Deploy a staging ([W] horas)
- [ ] QA/UAT ([V] horas)
- [ ] Deploy a producción ([U] horas)
  **Responsable**: [Nombre/Equipo]

---

### 7.2. Estimación Total

| Fase                         | Días    | Story Points |
| ---------------------------- | ------- | ------------ |
| Backend                      | [X]     | [Y]          |
| Frontend                     | [X]     | [Y]          |
| Testing & Deploy             | [X]     | [Y]          |
| **TOTAL**                    | **[X]** | **[Y]**      |
| **Margen de Error**: ± [20%] |         |              |

---

## 8. Riesgos y Mitigaciones

### 8.1. Riesgos Identificados

| ID  | Riesgo                                          | Probabilidad    | Impacto         | Severidad |
| --- | ----------------------------------------------- | --------------- | --------------- | --------- |
| R1  | [Descripción del riesgo]                        | Alta/Media/Baja | Alto/Medio/Bajo | 🔴/🟡/🟢  |
| R2  | Integración con [servicio externo] puede fallar | Media           | Alto            | 🟡        |
| R3  | Estimación insuficiente para [tarea compleja]   | Alta            | Medio           | 🟡        |

---

### 8.2. Estrategias de Mitigación

#### R1: [Nombre del Riesgo]

**Mitigación (Prevención)**:

- [Acción preventiva 1]
- [Acción preventiva 2]
  **Plan de Contingencia (si ocurre)**:
- [Acción reactiva 1]
- [Acción reactiva 2]
  **Responsable**: [Nombre]

---

## 9. Criterios de Éxito

### 9.1. Métricas de Aceptación

**Funcionales**:

- [ ] Todos los criterios de aceptación de las historias se cumplen
- [ ] [X]% de cobertura de tests
- [ ] 0 bugs críticos o bloqueantes
  **No Funcionales**:
- [ ] Tiempo de respuesta < [X] segundos
- [ ] [Y]% de disponibilidad en la primera semana
- [ ] Accesibilidad WCAG 2.1 AA
  **De Negocio** (si aplica):
- [ ] [Métrica de adopción]: [X]% de usuarios la usan en el primer mes
- [ ] [Métrica de satisfacción]: NPS > [Y]
- [ ] [Métrica de eficiencia]: Reduce tiempo de [tarea] en [Z]%

---

### 9.2. Definición de "Hecho" (DoD)

- [ ] Código implementado y revisado (code review)
- [ ] Tests unitarios escritos y pasando
- [ ] Tests de integración escritos y pasando
- [ ] Documentación técnica actualizada
- [ ] API contracts documentados en Backend Sync Brief
- [ ] FTVs creadas para todos los componentes UI
- [ ] Deploy exitoso en staging
- [ ] QA/UAT completado sin issues bloqueantes
- [ ] Performance validado según requisitos
- [ ] Deploy exitoso en producción
- [ ] Monitoreo configurado (logs, alerts)

---

## 10. Decisiones Pendientes

### 10.1. Decisiones Técnicas

| ID  | Decisión                     | Opciones            | Pros/Cons     | Responsable | Deadline |
| --- | ---------------------------- | ------------------- | ------------- | ----------- | -------- |
| D1  | ¿Qué librería usar para [X]? | A) [Lib1] B) [Lib2] | [Comparación] | [Nombre]    | [Fecha]  |

---

### 10.2. Decisiones de Negocio

| ID  | Decisión                      | Impacto             | Responsable   | Deadline |
| --- | ----------------------------- | ------------------- | ------------- | -------- |
| D1  | ¿Qué plan de pricing ofrecer? | Define monetización | Product Owner | [Fecha]  |

---

## 11. Próximos Pasos

### Inmediatos (Antes de implementar):

1. [ ] Aprobar este Feature Plan (Stakeholders)
2. [ ] Resolver decisiones pendientes
3. [ ] Crear Technical Design (TD-[XXX])
4. [ ] Crear Blueprints/FTVs
5. [ ] Generar Backend Sync Brief
   
   ### Durante Desarrollo:
6. [ ] Daily standups para tracking
7. [ ] Code reviews obligatorias
8. [ ] Testing continuo
   
   ### Post-Implementación:
9. [ ] Monitorear métricas de éxito
10. [ ] Recopilar feedback de usuarios
11. [ ] Iterar basado en aprendizajes

---

## 12. Referencias y Links

**Documentación Relacionada**:

- Project Summary: `@.gemini/project-summary.md`
- Sprint Backlog: `@.gemini/sprints/Sprint-[X]-Backlog.md`
- Technical Design: `@.gemini/sprints/technical-designs/TD-[XXX]-[nombre].md` (pendiente)
- Blueprints: `@.gemini/blueprints/ftv-*.md` (pendiente)
  **Recursos Externos**:
- Figma: [URL]
- Jira/Linear: [URL del ticket]
- Slack Thread: [URL de discusión]
- Research: [Links a investigación previa]

---

## 13. Aprobaciones

| Rol           | Nombre   | Aprobado    | Fecha | Comentarios |
| ------------- | -------- | ----------- | ----- | ----------- |
| Product Owner | [Nombre] | ⏳ Pendiente | -     | -           |
| Tech Lead     | [Nombre] | ⏳ Pendiente | -     | -           |
| Frontend Lead | [Nombre] | ⏳ Pendiente | -     | -           |
| Backend Lead  | [Nombre] | ⏳ Pendiente | -     | -           |
| UX Designer   | [Nombre] | ⏳ Pendiente | -     | -           |

---

## 14. Changelog del Feature Plan

| Versión | Fecha        | Autor       | Cambios                  |
| ------- | ------------ | ----------- | ------------------------ |
| 1.0     | [YYYY-MM-DD] | [Nombre/IA] | Creación inicial         |
| 1.1     | [YYYY-MM-DD] | [Nombre]    | [Descripción de cambios] |

---

**Feature Plan creado por**: [Nombre o "IA Feature Plan Generator"] 
**Fecha de creación**: [YYYY-MM-DD] 
**Última actualización**: [YYYY-MM-DD]

```


