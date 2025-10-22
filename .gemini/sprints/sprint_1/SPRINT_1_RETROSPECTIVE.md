# 🔄 Sprint Retrospective - Sprint 1

**Proyecto:** IOC (Indicadores Operacionales Cambiaso)  
**Sprint:** Sprint 1 - El Ciclo de Valor Completo  
**Período:** 8 Septiembre - 5 Octubre 2025 (4 semanas + 2 días extra)  
**Equipo:** Boris Rojas (Developer), Jaime Arancibia (Developer)

---

## 📊 DATOS DEL SPRINT

### Completado
- ✅ **IOC-021:** Como usuario, quiero iniciar sesión en la plataforma (5 SP)
- ✅ **IOC-022:** Como usuario, quiero cerrar sesión de forma segura (2 SP)
- ✅ **IOC-023:** Como usuario, quiero navegar entre secciones principales (8 SP)
- ✅ **IOC-001:** Como administrador, quiero cargar y validar archivos de producción (13 SP)
- ✅ **IOC-006:** Como gerente, quiero visualizar dashboard con KPIs actualizados (13 SP)

### Impedimentos Resueltos
- **Total:** 16 impedimentos (4 críticos, 6 altos, 6 medios)
- **Tasa de resolución:** 100% - Tiempo promedio: 1.2 días
- **Impedimentos destacados:**
  - IMP-009: Rendimiento ETL (4+ min → <30 seg) - Mejora de 800%
  - IMP-016: Agotamiento de conexiones BD - DataSource dual
  - IMP-007: Tests masivamente rotos - Perfil test con H2
  - IMP-013: Bucle de renderizado infinito - Optimización con useCallback

### Métricas
- **Planificado:** 41 pts | **Completado:** 41 pts (100%)
- **Días hábiles planeados:** 18 días | **Días realmente trabajados:** 20 días
- **⚠️ Horas extras:** 2 días de fin de semana (5 Octubre)
- **Impedimentos resueltos:** 16/16 (100%)
- **Velocidad nominal:** 10.25 SP/semana ❌ (incluye overtime)
- **Velocidad sostenible:** 9.2 SP/semana ✅ (sin overtime)
- **Tareas técnicas completadas:** 40/40 (100%)

---

## 1. ✅ ¿Qué salió bien?

### 1.1 Resolución excepcional de impedimentos críticos
**Evidencia:** 16 impedimentos resueltos en promedio de 1.2 días, incluyendo 4 críticos (IMP-007, IMP-009, IMP-013, IMP-016) que fueron resueltos en 1-2 días cada uno. Destacó IMP-009 que mejoró el rendimiento del ETL en 800% (de 4+ min a <30 seg).

**Impacto:** La capacidad del equipo para identificar, analizar y resolver rápidamente impedimentos complejos (configuración BD, optimización de rendimiento, bucles infinitos) demostró madurez técnica y evitó desvíos significativos del Sprint Goal.

### 1.2 Cumplimiento del 100% del Sprint Goal sin comprometer calidad
**Evidencia:** 5/5 historias completadas (IOC-021, IOC-022, IOC-023, IOC-001, IOC-006) con 41/41 SP, cumpliendo todos los criterios de aceptación. El ciclo completo (Auth → Ingesta → Visualización) quedó funcional y demostrable.

**Impacto:** El Sprint Goal se logró sin deuda técnica significativa, validando que el alcance inicial fue ambicioso pero alcanzable con el esfuerzo adecuado.

### 1.3 Arquitectura técnica robusta que previno problemas futuros
**Evidencia:** Implementación de Advisory Locks (BE-TASK-10), Circuit Breaker con Resilience4j (BE-TASK-23), caché con Caffeine (BE-TASK-24), DataSource dual (IMP-016), y reintentos con backoff exponencial (BE-TASK-11). Estos patrones no eran requeridos en los criterios de aceptación pero se implementaron proactivamente.

**Impacto:** Estas decisiones arquitectónicas anticiparon problemas de concurrencia, disponibilidad de servicios externos y escalabilidad, reduciendo significativamente el riesgo técnico futuro.

### 1.4 Optimización de rendimiento que hizo viable el producto
**Evidencia:** IMP-009 documentó el problema: el ETL tardaba 4+ minutos para 17k registros. La solución (caché en memoria de dimensiones + batching JDBC + estrategia SEQUENCE) redujo el tiempo a <30 segundos.

**Impacto:** Sin esta optimización, el producto habría sido inviable en producción. La mejora de 800% demuestra que el equipo no solo "cumplió" la historia IOC-001, sino que entregó un sistema production-ready.

### 1.5 Comunicación efectiva en Daily Scrums
**Evidencia:** 18 Daily Scrums ejecutadas consistentemente, con identificación temprana de blockers. Los impedimentos IMP-001, IMP-004, IMP-007, IMP-008, IMP-009, IMP-011, IMP-013, IMP-015 fueron reportados el mismo día que surgieron.

**Impacto:** La transparencia diaria permitió actuar rápidamente sobre impedimentos antes de que bloquearan trabajo por días, manteniendo el flujo continuo del Sprint.

### 1.6 Cobertura completa de instrumentación y observabilidad
**Evidencia:** Implementación de métricas con Micrometer (BE-TASK-13), Health Indicator personalizado (BE-TASK-14), DashboardAuditService (BE-TASK-20) y verificadores de integridad al arranque (BE-TASK-16).

**Impacto:** El sistema quedó con visibilidad operacional desde el día 1, facilitando debugging y demostrando profesionalismo en el enfoque de producción.

### 1.7 Gestión proactiva de calidad y seguridad
**Evidencia:** Refactorización de tipado laxo (IMP-002), eliminación de estilos inline para cumplir CSP (IMP-014), ajuste de SecurityConfig para CSP (BE-TASK-21), y adopción de react-hot-toast como estándar (IMP-003).

**Impacto:** El equipo no solo entregó funcionalidad, sino que elevó estándares de calidad y seguridad más allá de los requisitos mínimos, reduciendo vulnerabilidades y mejorando mantenibilidad.

---

## 2. ❌ ¿Qué no salió bien?

### 2.1 Sobre-compromiso que requirió trabajo en fin de semana
**Evidencia:** El Sprint estaba planeado para 18 días hábiles (8 Sept - 3 Oct), pero se extendió hasta el 5 de Octubre (domingo), sumando 2 días extra de overtime. El burndown chart muestra que en la Semana 4 solo se completaron 5 SP vs. los 11 planeados, requiriendo la extensión.

**Causa Raíz:** Subestimación de la complejidad de IOC-006 (integración Metabase) y del impacto acumulado de los impedimentos críticos (IMP-013, IMP-015, IMP-016) que surgieron en la última semana. La capacidad sostenible real fue 9.2 SP/semana, no los 10.25 comprometidos.

**Consecuencia:** Riesgo de burnout del equipo y velocidad insostenible para futuros sprints.

### 2.2 Testing relegado a etapa tardía del Sprint
**Evidencia:** IMP-007 (fallo masivo de tests) no se detectó hasta el Día 9 (20 Sept), dejando la suite de tests rota por casi 2 semanas. La configuración de entorno de testing (perfil test, H2 in-memory) debió estar lista desde el Día 1.

**Causa Raíz:** El DoD no incluía "tests pasando" como criterio obligatorio para marcar tareas como completadas. La ausencia de CI/CD automatizado permitió que el problema se acumulara sin visibilidad.

**Consecuencia:** Pérdida de confianza en la suite de tests y deuda técnica que requirió esfuerzo adicional (2 días) para remediar.

### 2.3 Múltiples impedimentos relacionados con configuración de entorno
**Evidencia:** 4 impedimentos (25% del total) estuvieron relacionados con setup de entorno: IMP-007 (tests con BD), IMP-011 (proxy Vite), IMP-015 (perfil Spring dev), IMP-016 (agotamiento de conexiones BD). Todos eran prevenibles con un "Environment Setup Checklist" completo.

**Causa Raíz:** No existe documentación estandarizada de setup de desarrollo, y los blueprints de arquitectura no incluyen secciones de "Configuración de Entorno" ni "Runbook de Troubleshooting".

**Consecuencia:** Pérdida de ~5 días-persona en troubleshooting de problemas de configuración que pudieron evitarse con documentación adecuada.

### 2.4 Flujos end-to-end incompletos en blueprints iniciales
**Evidencia:** IMP-001 documentó que la página `/update-password` no existía, causando error 404 al resetear contraseña. El blueprint de autenticación no contempló la vista de destino del enlace de reseteo.

**Causa Raíz:** Los blueprints de historias de usuario se enfocaron en los "happy paths" pero no mapearon completamente todos los flujos secundarios (reseteo de contraseña, errores de validación, timeouts, etc.).

**Consecuencia:** Descubrimiento tardío de funcionalidad faltante que debió ser evidente en el diseño inicial.

### 2.5 Deuda técnica por desarrollo acelerado sin linters
**Evidencia:** IMP-002 identificó "uso de `any` y falta de tipos específicos" en componentes de autenticación. IMP-013 documentó funciones no memoizadas causando bucles infinitos. IMP-014 mostró violaciones de CSP por estilos inline.

**Causa Raíz:** Ausencia de linters/formatters integrados en el flujo de desarrollo (pre-commit hooks, CI/CD) que validen automáticamente calidad de código. El DoD no incluye criterios técnicos específicos (tipado estricto, memoización, CSP compliance).

**Consecuencia:** Múltiples ciclos de refactorización para corregir problemas de calidad que debieron prevenirse en la escritura inicial del código.

### 2.6 Dependencia crítica de servicios externos sin Plan B
**Evidencia:** Metabase (IOC-006) y Supabase Auth (IOC-021/022/023) son dependencias críticas. Aunque se implementó Circuit Breaker y caché, no existe un "modo degradado" o funcionalidad offline básica si estos servicios fallan completamente.

**Causa Raíz:** El Register de Riesgos identificó R-002 (dependencia de Metabase) pero las mitigaciones se enfocaron en resiliencia, no en alternativas funcionales.

**Consecuencia:** En caso de caída prolongada de Metabase o Supabase, la aplicación quedaría completamente inoperativa, sin capacidad de funcionar de manera limitada.

### 2.7 Estimación inicial insuficientemente refinada
**Evidencia:** IMP-010 documentó "Rediseño de la Planificación de Sprints" donde se reconoció que "la complejidad y las dependencias de las historias de usuario no se evaluaron con suficiente profundidad". Esto llevó 5 días de re-análisis a mitad del Sprint.

**Causa Raíz:** Ausencia de sesiones de Backlog Refinement previas al Sprint Planning. Las historias fueron estimadas sin descomposición completa en tareas técnicas ni análisis de dependencias entre ellas.

**Consecuencia:** Re-planificación a mitad del Sprint que generó incertidumbre y afectó el compromiso del equipo con el Sprint Goal original.

---

## 3. 🚀 ¿Qué mejoras implementaremos?

### 3.1 Adoptar velocidad sostenible de 37 SP por sprint de 4 semanas
**Responsable:** Boris (PO)  
**Acción:** En el Sprint Planning del Sprint 2, comprometerse a **33-37 SP** (no 41 SP), basado en la velocidad sostenible real de 9.2 SP/semana. Incluir buffer de seguridad del 10% para absorber impedimentos imprevistos.  
**Criterio de Éxito:** Sprint 2 se completa en los 18 días hábiles planeados, sin requerir trabajo en fin de semana. La diferencia entre SP planeados y completados es ≤ 10%.  
**Plazo:** Implementar en Sprint Planning del Sprint 2 (7 Octubre 2025).

### 3.2 Integrar testing en el flujo continuo con suite ejecutable desde Día 1
**Responsable:** Jaime (SM)  
**Acción:** 
1. Actualizar DoD: añadir criterio "Todos los tests relacionados con la historia/tarea pasan exitosamente antes de marcar como Done".
2. Configurar GitHub Actions para ejecutar tests automáticamente en cada PR (CI/CD básico).
3. En Sprint 2, dedicar 1 tarea técnica a "Setup de entorno de testing" antes de iniciar desarrollo.  
**Criterio de Éxito:** La suite de tests se ejecuta automáticamente en CI y nunca permanece rota por más de 1 día. Cobertura de tests unitarios ≥ 60% para código nuevo.  
**Plazo:** CI/CD configurado antes del Día 3 del Sprint 2 (10 Octubre 2025).

### 3.3 Crear y validar "Environment Setup Checklist" para desarrollo local
**Responsable:** Jaime (SM)  
**Acción:** 
1. Documentar en `.gemini/runbooks/ENVIRONMENT_SETUP.md` un checklist paso a paso de configuración de entorno (BD, variables, perfiles Spring, proxy Vite, conexiones, etc.).
2. Incluir sección de "Troubleshooting común" con los aprendizajes de IMP-007, IMP-011, IMP-015, IMP-016.
3. Validar que un desarrollador nuevo pueda levantar el entorno en <30 min siguiendo la guía.  
**Criterio de Éxito:** Reducción de impedimentos relacionados con configuración de entorno a ≤ 1 por sprint. Tiempo de onboarding de nuevo desarrollador ≤ 30 min.  
**Plazo:** Documentación completa y validada antes del Sprint 2 Día 1 (8 Octubre 2025).

### 3.4 Actualizar DoD con criterios técnicos específicos de calidad
**Responsable:** Equipo (Boris + Jaime)  
**Acción:** Actualizar el Definition of Done en `.gemini/process/DEFINITION_OF_DONE.md` para incluir:
- ✅ Cero usos explícitos de `any` en TypeScript (usar tipos específicos o `unknown`).
- ✅ Todas las funciones pasadas como props están memoizadas con `useCallback` si son usadas en `useEffect`.
- ✅ No se introducen estilos inline (`style="..."`) - usar clases Tailwind o CSS Modules.
- ✅ Todos los procesos asíncronos tienen indicador de carga visual y manejo de errores.
- ✅ Tests unitarios para toda lógica de negocio nueva pasan exitosamente.  
**Criterio de Éxito:** Reducción de impedimentos relacionados con calidad de código (tipo IMP-002, IMP-013, IMP-014) a 0 en Sprint 2.  
**Plazo:** DoD actualizado y comunicado antes del Sprint 2 Planning (7 Octubre 2025).

### 3.5 Implementar sesión de Backlog Refinement a mitad de cada sprint
**Responsable:** Boris (PO)  
**Acción:** 
1. Agendar sesión de Refinement de 2 horas en la mitad del Sprint 2 (Día 9 - 18 Octubre).
2. En la sesión, descomponer las historias del Sprint 3 en tareas técnicas, identificar dependencias, re-estimar con Planning Poker.
3. Actualizar el backlog con las estimaciones refinadas antes del siguiente Sprint Planning.  
**Criterio de Éxito:** Las historias del Sprint 3 llegan al Planning con estimaciones validadas y descomposición técnica completa. Reducción de re-planificaciones a mitad de sprint a 0.  
**Plazo:** Primera sesión ejecutada el 18 Octubre 2025 (Sprint 2 Día 9).

### 3.6 Integrar ESLint + Prettier con pre-commit hooks
**Responsable:** Jaime (SM)  
**Acción:** 
1. Configurar ESLint con reglas estrictas (`@typescript-eslint/no-explicit-any: error`, `react-hooks/exhaustive-deps: error`).
2. Configurar Prettier para formateo automático.
3. Instalar Husky para ejecutar linters automáticamente en pre-commit.
4. Documentar en README.md cómo configurar los hooks en desarrollo local.  
**Criterio de Éxito:** Código pushed a Git nunca contiene violaciones de linter. Reducción de PRs con feedback de "problemas de tipado/formateo" a 0.  
**Plazo:** Configuración completa antes del Día 5 del Sprint 2 (14 Octubre 2025).

### 3.7 Diseñar modo degradado básico para dependencias críticas
**Responsable:** Boris (PO) + Jaime (Dev)  
**Acción:** 
1. Para Metabase: implementar mensaje de "Dashboard temporalmente no disponible" cuando el Circuit Breaker se abre, con opción de "Ver última captura de pantalla" (caché de imagen estática).
2. Para Supabase Auth: documentar procedimiento de rollback a autenticación local con JWT si Supabase falla por >1 hora (no implementar aún, solo diseñar).
3. Actualizar Risk Register con estas mitigaciones.  
**Criterio de Éxito:** En caso de caída de Metabase, el usuario recibe feedback claro en lugar de error genérico. Existe un plan documentado de contingencia para fallo de Supabase.  
**Plazo:** Modo degradado de Metabase implementado en Sprint 2. Plan de contingencia Supabase documentado en Risk Register antes del Sprint 3 (4 Noviembre 2025).

---

## 📈 Resumen de Acciones

| # | Acción | Responsable | Plazo | Tipo |
|---|--------|-------------|-------|------|
| 3.1 | Adoptar velocidad sostenible de 37 SP | Boris | Sprint 2 Planning (7-Oct) | Proceso |
| 3.2 | Integrar testing con CI/CD | Jaime | Sprint 2 Día 3 (10-Oct) | Técnico |
| 3.3 | Crear Environment Setup Checklist | Jaime | Sprint 2 Día 1 (8-Oct) | Documentación |
| 3.4 | Actualizar DoD con criterios técnicos | Equipo | Sprint 2 Planning (7-Oct) | Proceso |
| 3.5 | Implementar Backlog Refinement | Boris | Sprint 2 Día 9 (18-Oct) | Proceso |
| 3.6 | Integrar ESLint + Prettier + Husky | Jaime | Sprint 2 Día 5 (14-Oct) | Técnico |
| 3.7 | Diseñar modo degradado para Metabase | Boris + Jaime | Sprint 2 (completo) | Técnico |

---

## 🎯 Compromiso del Equipo

**Boris (PO/Dev):**
> "Me comprometo a reducir el alcance del Sprint 2 a 37 SP máximo y liderar la sesión de Backlog Refinement para prevenir sobre-estimación. También diseñaré el modo degradado de Metabase para mejorar resiliencia."

**Jaime (SM/Dev):**
> "Me comprometo a ser más estricto y cuidadoso en las revisiones de código. Si veo algo que es confuso, desordenado o que podría darnos problemas en
el futuro, lo señalaré y ayudaré a solucionarlo en el momento, aunque nos tome un poco más de tiempo."

---

## 📊 Métricas de Seguimiento para Sprint 2

Para validar que las mejoras están funcionando, mediremos:

1. **Overtime:** 0 días de trabajo en fin de semana
2. **Impedimentos de configuración:** ≤ 1 (vs. 4 en Sprint 1)
3. **Impedimentos de calidad de código:** 0 (vs. 3 en Sprint 1)
4. **Tests rotos por >1 día:** 0 (vs. 1 episodio de 2 semanas en Sprint 1)
5. **Precisión de estimación:** Diferencia entre planeado y completado ≤ 10%
6. **PRs con feedback de linting:** 0 (nuevo)

---

**Fecha de Retrospective:** 3 Octubre 2025  
**Facilitador:** Jaime Arancibia (Scrum Master)  
**Próxima Retrospective:** Sprint 2 - 31 Octubre 2025

---

**Documentos relacionados:**
- Sprint Backlog: `.gemini/sprints/Sprint-1-Backlog.md`
- Impediment Log: `.gemini/sprints/IMPEDIMENT_LOG_SPRINT_1_v3.md`
- Daily Scrum Summary: `.gemini/sprints/DAILY_SCRUM_SUMMARY_SPRINT_1.md`
- Risk Register: `.gemini/evidencias/RISK_REGISTER_SCRUM.md`

