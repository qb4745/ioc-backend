# Registro de Impedimentos del Sprint 1 (v3.0)

**Proyecto:** IOC (Indicadores Operacionales Cambiaso)  
**Sprint:** Sprint 1 - El Ciclo de Valor Completo  
**Período:** 8 Septiembre - 3 Octubre 2025 (19 días laborables, excluyendo festivos 18-19 Sept)  
**Equipo:**
- Boris Rojas (Product Owner + Developer)
- Jaime Arancibia (Scrum Master + Developer)

---

## 📊 Estado Actual

| Métrica | Valor |
|---------|-------|
| **Total de Impedimentos** | 16 |
| **Resueltos** | 16 (100%) |
| **Pendientes** | 0 |
| **Tiempo Promedio de Resolución** | 1.2 días |
| **Impedimentos Críticos** | 4 (IMP-007, IMP-009, IMP-013, IMP-016) |
| **Impedimentos de Alto Impacto** | 6 |
| **Impedimentos de Impacto Medio** | 6 |

---

## Proceso de Gestión de Impedimentos

1. **Identificación:** Cualquier miembro del equipo reporta el impedimento en la Daily Scrum.
2. **Clasificación:** Se asigna ID, impacto (🔴 Crítico / 🟡 Alto / 🟢 Medio) y responsable.
3. **Análisis:** Se define plan de acción, causa raíz y acción preventiva.
4. **Seguimiento:** El estado se actualiza diariamente hasta su resolución.
5. **Cierre:** Se documenta la solución y se calcula el tiempo de resolución.

---

## 📋 Registro de Impedimentos Resueltos

| ID | Impacto | Impedimento | Responsable | Detectado | Resuelto | Días | Solución Aplicada | Causa Raíz | Acción Preventiva |
| :--- | :---: | :--- | :--- | :---: | :---: | :-: | :--- | :--- | :--- |
| **IMP-001** | 🟡 Alto | **Flujo de usuario incompleto (Error 404):** La página `/update-password` no existía, causando error 404 al resetear contraseña. | Jaime | 11-Sep | 12-Sep | 1 | Implementar la vista UpdatePasswordPage.tsx y agregar la ruta en App.tsx según el blueprint de autenticación. | **Planificación Incompleta:** El blueprint inicial no contempló la página de destino del enlace de reseteo. | **Mejora de Blueprints:** Actualizar todos los futuros blueprints para incluir explícitamente todas las vistas y rutas implicadas en un flujo end-to-end. |
| **IMP-002** | 🟢 Medio | **Deuda técnica por tipado laxo:** El uso de `any` y la falta de tipos específicos generaron advertencias de calidad en los componentes de autenticación. | Jaime | 11-Sep | 12-Sep | 1 | Refactorizar los componentes SignIn, SignUp y ForgotPassword para usar tipos estrictos e interfaces específicas (User, Session, AuthError). | **Desarrollo Acelerado:** Se priorizó la funcionalidad sobre la calidad de tipos en la implementación inicial. | **Definición de Hecho (DoD):** Añadir criterio al DoD: "Cero advertencias de `any` explícito en código nuevo. Todas las props y estados deben tener tipos definidos." |
| **IMP-003** | 🟢 Medio | **UX Inconsistente en notificaciones:** El uso de `alert()` nativo rompía con el sistema de diseño y la experiencia de usuario. | Jaime | 12-Sep | 12-Sep | 1 | Instalar `react-hot-toast` y refactorizar todos los formularios de autenticación para usar notificaciones emergentes toast. | **Falta de un Sistema de Notificaciones:** El proyecto no contaba con una solución estandarizada para notificaciones. | **Decisión de Arquitectura:** Adoptar `react-hot-toast` como la librería oficial para todas las notificaciones emergentes del proyecto. |
| **IMP-004** | 🟡 Alto | **Cierre de sesión inseguro:** El logout desde el menú de usuario (UserDropdown) no invalidaba la sesión correctamente, permitiendo acceso con el botón "atrás". | Jaime | 16-Sep | 17-Sep | 1 | Implementar la lógica `handleSignOut` en UserDropdown.tsx usando `supabase.auth.signOut()` seguido de `window.location.href = '/signin'` para forzar la recarga completa. | **Lógica Duplicada e Incompleta:** La funcionalidad de logout estaba implementada incorrectamente en UserDropdown y correctamente en AccountPage. | **Centralizar Lógica Crítica:** Refactorizar la lógica de autenticación a hooks personalizados (`useAuth`) para evitar implementaciones inconsistentes. |
| **IMP-005** | 🟢 Medio | **UI Rota en Tabla de Historial:** La tabla de ingesta de datos (UploadHistoryTable) se mostraba desalineada con anchos fijos que no se adaptaban al contenedor. | Boris | 15-Sep | 16-Sep | 1 | Refactorizar la tabla para usar patrones de la plantilla: padding consistente en celdas y contenedor con overflow-x-auto en lugar de anchos fijos. | **Desviación del Sistema de Diseño:** Se aplicó una solución CSS (`table-fixed`) que no era consistente con el resto de la aplicación. | **Revisión de Patrones Existentes:** Antes de crear un componente complejo, verificar si la plantilla ya ofrece un patrón similar para reutilizar. |
| **IMP-006** | 🟡 Alto | **Feedback de UI incompleto:** Faltaban indicadores visuales para la validación de archivos y el estado de carga en el componente Dropzone. | Boris | 16-Sep | 17-Sep | 1 | 1. Crear componente Spinner reutilizable. 2. Modificar Dropzone para mostrar Spinner durante la carga y mensajes de error contextuales (formato, tamaño). | **Omisión en la Implementación:** Los estados se manejaron lógicamente (deshabilitando botones) pero no visualmente. | **Mejora del DoD:** Añadir al DoD: "Todos los procesos asíncronos deben tener un indicador de carga visual. Todos los formularios deben mostrar errores de validación contextuales." |
| **IMP-007** | 🔴 Crítico | **Fallo masivo de tests (`ApplicationContext`):** La suite de tests no arrancaba debido a fallos en la carga del contexto de Spring, bloqueando completamente el testing. | Jaime | 20-Sep | 22-Sep | 2 | 1. Crear perfil test (`application-test.properties`). 2. Configurar BD H2 en memoria para aislar tests. 3. Excluir SecurityConfig de tests para evitar dependencias de variables de entorno. | **Configuración de Entorno Faltante:** El proyecto carecía de una configuración de base de datos específica para el entorno de prueba, causando conflictos con la configuración de producción. | **Estandarizar Configuración de Tests:** Definir como práctica estándar que todo nuevo proyecto Spring Boot debe incluir un perfil de test con BD en memoria desde el inicio. |
| **IMP-008** | 🟡 Alto | **Fallo de persistencia con Clave Compuesta:** La inserción en `fact_production` fallaba con `violates not-null constraint` en la columna `id` al usar `@EmbeddedId`. | Boris | 20-Sep | 23-Sep | 3 | Refactorizar la entidad FactProduction para usar una clave primaria simple (`@Id @GeneratedValue(strategy = GenerationType.SEQUENCE)`) en lugar de una compuesta (`@EmbeddedId`). | **Anti-patrón de Persistencia:** El uso de `BIGSERIAL` en una parte de una clave primaria compuesta es un anti-patrón que no es manejado limpiamente por JPA/Hibernate. | **Decisión de Arquitectura:** Establecer como regla de diseño que todas las nuevas tablas deben usar claves primarias simples (sustitutas) y auto-incrementales. |
| **IMP-009** | 🔴 Crítico | **Rendimiento de ETL inaceptable:** El procesamiento de un archivo de 17,000 filas tardaba más de 4 minutos, haciendo inviable el uso en producción. | Boris | 23-Sep | 24-Sep | 1 | 1. Refactorizar ParserService para usar un caché en memoria de las dimensiones. 2. Cambiar la estrategia de generación de ID a `SEQUENCE`. 3. Configurar batching de JDBC (`hibernate.jdbc.batch_size=50`) en application.properties. | **Cuello de Botella en la Base de Datos:** El parser realizaba miles de llamadas `SELECT` individuales a la BD dentro de un bucle (problema N+1), y la estrategia `IDENTITY` impedía el batching real de los `INSERT`. | **Principio de Diseño de ETL:** Establecer como principio que toda lógica de parseo masivo debe precargar las dimensiones en un caché y toda inserción masiva debe usar batching de JDBC. Resultado: reducción de 4+ min a <30 seg (mejora de 800%). |
| **IMP-010** | 🟡 Alto | **Rediseño de la Planificación de Sprints:** El alcance y la duración de los sprints originales (4 sprints de 2 semanas) resultaron ser inadecuados tras el análisis detallado. | Equipo | 15-Sep | 20-Sep | 5 | Re-analizar las historias de usuario, re-estimar el esfuerzo usando Planning Poker y consolidar el plan de 4 a 3 sprints con un alcance redefinido y más realista para cada uno. | **Estimación Inicial Imprecisa:** La complejidad y las dependencias de las historias de usuario no se evaluaron con suficiente profundidad durante la planificación inicial del proyecto. | **Mejora del Sprint Planning:** Implementar sesiones de refinamiento del backlog (`Backlog Refinement`) a mitad de cada sprint para re-evaluar y ajustar las estimaciones del siguiente sprint con información más actualizada. |
| **IMP-011** | 🟢 Medio | **Conexión rechazada (`ECONNREFUSED`) entre Frontend y Backend:** El proxy de Vite no lograba conectar con el backend a pesar de que este estaba en ejecución en el puerto 8080. | Jaime | 29-Sep | 29-Sep | 1 | Modificar el `target` del proxy en `vite.config.ts` de `http://localhost:8080` a `http://127.0.0.1:8080` para resolver un conflicto de resolución de red. | **Inconsistencia de Red Local:** El entorno de desarrollo resolvía `localhost` y `127.0.0.1` de manera diferente para distintas aplicaciones (IPv4 vs IPv6). | **Estandarizar Proxies:** Definir como práctica estándar el uso de `127.0.0.1` en las configuraciones de proxy de Vite para garantizar una comunicación de red consistente. |
| **IMP-012** | 🟢 Medio | **Error `400 Bad Request` de Metabase:** El `iframe` no cargaba el dashboard, y la consola de Metabase mostraba un error "Dashboard no encontrado". | Jaime | 29-Sep | 29-Sep | 1 | Acceder a la configuración del dashboard en la UI de Metabase (Settings > Sharing & Embedding) y hacer clic en "Publish" para habilitar su embedding público. | **Desconocimiento de Requisito:** Se desconocía que un dashboard debe ser explícitamente "publicado" en Metabase para poder ser embebido en aplicaciones externas. | **Mejorar Documentación:** Actualizar la guía interna de embedding de Metabase con un checklist que incluya la "publicación" como un paso obligatorio antes de generar tokens. |
| **IMP-013** | 🔴 Crítico | **Bucle de Renderizado Infinito:** La página de dashboards generaba peticiones masivas al backend (cientos por segundo), activando el RateLimiter y degradando el servicio. | Jaime | 30-Sep | 1-Oct | 1 | Envolver las funciones de callback (`handleDashboardError`, `handleDashboardLoad`) en DashboardsPage.tsx con el hook `useCallback` para estabilizar sus referencias. | **Dependencia Inestable en `useEffect`:** Se pasaban funciones re-creadas en cada render como props a DashboardEmbed, que las usaba en su array de dependencias de `useEffect`, causando un loop infinito. | **Guía de Estilo para Hooks:** Establecer como regla: "Toda función pasada como prop a un componente que la use en un `useEffect` DEBE estar memoizada con `useCallback`". |
| **IMP-014** | 🟢 Medio | **Violación de Política de Seguridad de Contenido (CSP):** El navegador bloqueaba los estilos en línea (`style="..."`), causando que los iframes no se renderizaran con la altura correcta. | Jaime | 1-Oct | 1-Oct | 1 | Refactorizar DashboardEmbed y DashboardsPage para eliminar todos los estilos en línea, pasando las dimensiones a través de clases de Tailwind CSS (`h-[600px]`, `h-screen`, etc.). | **Uso de Estilos en Línea:** Se utilizó el atributo `style` para definir alturas dinámicas, lo cual es incompatible con una CSP estricta (`style-src 'self'`). | **Mejora del DoD:** Añadir al DoD: "El código no debe introducir nuevos usos de estilos en línea. Se deben preferir siempre las clases de CSS/Tailwind o CSS Modules." |
| **IMP-015** | 🟡 Alto | **Fallo de Conexión a BD en Arranque Local:** La aplicación no podía conectar a la base de datos (`FATAL: password authentication failed`) debido a que el perfil de Spring 'dev' no se activaba consistentemente. | Jaime | 1-Oct | 2-Oct | 1 | 1. Diagnosticar el problema usando un puerto diferente (8081). 2. Identificar que `-Dspring.profiles.active=dev` no era robusto. 3. Establecer `spring.profiles.active=dev` directamente en `application.properties` como perfil por defecto. | **Configuración de Entorno Frágil:** La activación de perfiles de Spring dependía de argumentos de línea de comandos (`-D...`) que no se propagaban de manera fiable al ejecutar la aplicación a través del Maven Wrapper (`./mvnw spring-boot:run`). | **Estandarizar Arranque Local:** Definir como práctica estándar la activación del perfil de desarrollo por defecto directamente en `application.properties` para garantizar un arranque consistente y simplificado. |
| **IMP-016** | 🔴 Crítico | **Agotamiento de Conexiones a la BD (`MaxClientsInSessionMode`):** La aplicación agotaba el límite de conexiones de Supabase (15 en Session Mode), causando fallos en el arranque y la operación. | Jaime | 2-Oct | 2-Oct | 1 | 1. Diagnosticar la acumulación de conexiones `idle`. 2. Implementar una arquitectura de `DataSource` dual: un pool principal en "Modo Transacción" (eficiente, 20 conexiones) y un pool secundario muy pequeño en "Modo Sesión" (solo para `Advisory Locks`, 2 conexiones). | **Conflicto de Modos de Conexión:** El uso de `Advisory Locks` requería el "Modo de Sesión" del pooler de Supabase (PgBouncer), que es ineficiente y tiene un límite bajo de conexiones, mientras que el resto de la aplicación se beneficiaba del "Modo de Transacción". | **Principio de Diseño de Conexiones:** Para sistemas con `Advisory Locks` y poolers como PgBouncer, utilizar siempre una arquitectura de `DataSource` dual para aislar las conexiones de sesión y optimizar el uso de recursos. |

---

## 📊 Resumen y Métricas

### Distribución por Impacto

| Impacto | Cantidad | Porcentaje | Tiempo Promedio de Resolución |
|---------|----------|------------|-------------------------------|
| 🔴 **Crítico** | 4 | 25% | 1.25 días |
| 🟡 **Alto** | 6 | 37.5% | 1.5 días |
| 🟢 **Medio** | 6 | 37.5% | 1.0 día |
| **TOTAL** | **16** | **100%** | **1.2 días** |

### Velocidad de Resolución

- **Más rápido:** IMP-003, IMP-011, IMP-012, IMP-014 (1 día - mismo día)
- **Más lento:** IMP-010 (5 días - requirió re-planificación con stakeholders)
- **Promedio general:** 1.2 días
- **Mediana:** 1 día

### Distribución por Responsable

| Responsable | Impedimentos Resueltos | Porcentaje |
|-------------|------------------------|------------|
| **Jaime (SM + Dev)** | 10 | 62.5% |
| **Boris (PO + Dev)** | 5 | 31.25% |
| **Equipo** | 1 | 6.25% |

### Distribución por Categoría Técnica

| Categoría | Cantidad | Ejemplos |
|-----------|----------|----------|
| **Backend/Infraestructura** | 7 | IMP-007, IMP-008, IMP-009, IMP-015, IMP-016 |
| **Frontend/UI** | 5 | IMP-001, IMP-005, IMP-006, IMP-013, IMP-014 |
| **Integración** | 2 | IMP-011, IMP-012 |
| **Calidad/Proceso** | 2 | IMP-002, IMP-010 |

### Evolución Temporal

| Semana | Nuevos | Resueltos | Acumulados |
|--------|--------|-----------|------------|
| **Semana 1** (8-12 Sep) | 6 | 6 | 0 |
| **Semana 2** (15-17, 20 Sep) | 5 | 3 | 2 |
| **Semana 3** (22-26 Sep) | 1 | 4 | 0 |
| **Semana 4** (29 Sep - 3 Oct) | 4 | 4 | 0 |

---

## 🎓 Top 3 Lecciones Aprendidas

### 1. **La Optimización de Rendimiento Debe Ser Proactiva, No Reactiva** (IMP-009)
- **Impacto:** El impedimento más crítico en términos de viabilidad del producto (4+ minutos → <30 segundos).
- **Lección:** La arquitectura ETL debe diseñarse desde el inicio con principios de escalabilidad: caché de dimensiones, batching JDBC, y estrategias de ID apropiadas.
- **Acción:** Incluir en el DoD de futuras historias de ingesta: "El procesamiento debe ser capaz de manejar al menos 50,000 registros en <2 minutos".

### 2. **Las Configuraciones de Entorno Son Impedimentos Ocultos** (IMP-007, IMP-015, IMP-016)
- **Impacto:** 3 impedimentos críticos/altos relacionados con configuración bloquearon el desarrollo por días.
- **Lección:** La inversión inicial en configuraciones robustas (perfiles de test, conexiones a BD, variables de entorno) es crítica y debe ser tratada como infraestructura de primera clase.
- **Acción:** Crear un "Environment Setup Checklist" que se valide en el Sprint 0 o la fase de setup inicial de cada módulo.

### 3. **La Calidad del Código Debe Ser Parte del Flujo, No una Revisión Posterior** (IMP-002, IMP-013, IMP-014)
- **Impacto:** Múltiples impedimentos de calidad surgieron por omitir buenas prácticas (tipado, memoización, CSP).
- **Lección:** El DoD actual es insuficiente. Se necesitan criterios explícitos de calidad de código que se validen antes de marcar una tarea como "terminada".
- **Acción:** Actualizar el DoD con criterios técnicos específicos y considerar integrar linters/formatters en el pipeline de CI/CD.

---

## 📋 Leyenda

### Impacto
- 🔴 **Crítico:** Bloqueó trabajo completamente, puso en riesgo el Sprint Goal o causó fallas del sistema.
- 🟡 **Alto:** Impacto significativo en la productividad o calidad, pero había workaround temporal.
- 🟢 **Medio:** Molestia o ineficiencia que ralentizó el trabajo, pero no fue bloqueante.

### Estados
- ✅ **Resuelto:** El impedimento fue completamente resuelto y la solución fue validada.
- 🔄 **En Progreso:** Se está trabajando activamente en la resolución.
- 🆕 **Nuevo:** Impedimento identificado pero aún no se inició el plan de acción.

---

**Última actualización:** 3 Octubre 2025 - Post Sprint Review  
**Próxima revisión:** Sprint 2 Planning (7 Octubre 2025)

---

**Documentos relacionados:**
- Sprint Backlog: `.gemini/sprints/Sprint-1-Backlog.md`
- Daily Scrum Summary: `.gemini/sprints/DAILY_SCRUM_SUMMARY_SPRINT_1.md`
- Risk Register: `.gemini/evidencias/RISK_REGISTER_SCRUM.md`

