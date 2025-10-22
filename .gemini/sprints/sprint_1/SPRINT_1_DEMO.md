# Sprint Demo - Sprint 1

**Proyecto:** IOC (Inteligencia Operacional Cambiaso)  
**Fecha:** 3 Octubre 2025  
**Duración:** 15 minutos  
**Asistentes:** 2 personas (Boris - PO/Dev, Jaime - SM/Dev)

---

## Resumen Rápido

| Métrica | Planeado | Completado | % |
|---------|----------|------------|---|
| Historias | 5 | 5 | 100% |
| Story Points | 41 | 41 | 100% |
| Sprint Goal | - | Alcanzado | - |

**Sprint Goal:** Entregar un ciclo de valor de extremo a extremo: un usuario podrá autenticarse, cargar datos de producción a través de un archivo CSV y visualizar inmediatamente un dashboard con KPIs y gráficos que reflejen esa nueva información. COMPLETADO

---

## Funcionalidades Demostradas

### Módulo 1: Autenticación y Navegación

| ID | Funcionalidad | Demo por | Estado |
|----|---------------|----------|--------|
| IOC-021 | Login de usuario con Supabase Auth - Inicio de sesión seguro con validación de credenciales, redirección al dashboard y manejo de errores | Jaime | OK |
| IOC-022 | Logout seguro - Cierre de sesión con invalidación de tokens, prevención de acceso con botón "atrás" del navegador | Jaime | OK |
| IOC-023 | Navegación principal y rutas protegidas - Sidebar con acceso a módulos principales, protección de rutas para usuarios autenticados | Jaime | OK |

Demostración realizada:
1. Login exitoso: Usuario inicia sesión → Redirige a Dashboard
2. Login fallido: Credenciales incorrectas → Muestra toast con error específico
3. Navegación protegida: Acceso directo a módulos sin autenticación → Redirige a `/signin`
4. Logout: Cierra sesión desde menú de usuario → Invalida token → Redirige a login
5. Sidebar: Acceso a las secciones principales (ej. Ingesta de Datos, Dashboard)

---

### Módulo 2: Ingesta de Datos (ETL)

| ID | Funcionalidad | Demo por | Estado |
|----|---------------|----------|--------|
| IOC-001 | Carga y validación de archivos CSV de producción - Upload, validación de formato, procesamiento ETL asíncrono, tabla de historial con estados en tiempo real | Boris | OK |

Demostración realizada:
1. Upload de archivo válido:
   - Usuario sube `production_data_2025.csv`
   - Sistema valida formato, inicia job ETL
   - UI muestra spinner de carga y progreso del job
   - Polling cada 3 segundos actualiza el estado: PENDING → PROCESSING → COMPLETED
   - Tiempo de procesamiento: <30 segundos (optimizado con caché de dimensiones y batching JDBC)

2. Validación de archivo duplicado:
   - Usuario intenta resubir el mismo archivo → Sistema detecta duplicado
   - Muestra mensaje: "Este archivo ya fue procesado anteriormente"

3. Archivo con formato incorrecto:
   - Usuario sube archivo con columnas faltantes → Sistema rechaza con error claro
   - Muestra detalles: "Columna 'fecha_produccion' requerida no encontrada"

4. Tabla de historial:
   - Muestra los uploads realizados: Nombre archivo, Fecha, Estado
   - Estados visuales: Completado, Procesando, Fallido
   - Actualización automática en tiempo real (polling)

Características técnicas destacadas:
- De-duplicación intra-archivo: Previene registros lógicamente idénticos
- Advisory Locks: Serializa jobs concurrentes con rangos de fecha superpuestos
- Reintentos con backoff exponencial: Maneja colisiones transitorias de UNIQUE constraint
- Idempotencia: Hash SHA-256 del archivo para prevenir reprocesamiento
- Watchdog: Marca como "FALLO" jobs estancados >1 hora
- Observabilidad: Métricas con Micrometer + Health Indicator (`/actuator/health/etl`)

---

### Módulo 3: Visualización de Dashboards

| ID | Funcionalidad | Demo por | Estado |
|----|---------------|----------|--------|
| IOC-006 | Dashboard embebido de Metabase con KPIs - Visualización de métricas de producción, embedding seguro con tokens JWT, caché y circuit breaker | Boris | OK |

Demostración realizada:
1. Acceso al dashboard:
   - Usuario navega a "Dashboards" → Selecciona "Dashboard de Producción"
   - Backend genera token JWT firmado con secret de Metabase
   - Frontend embebe iframe con URL firmada
   - Dashboard carga con datos del archivo procesado en IOC-001

2. KPIs visualizados:
   - Total Barriles Producidos: Agregado por mes/año
   - Producción por Campo: Gráfico de barras
   - Tendencias: Línea de tiempo con evolución mensual

3. Resiliencia demostrada:
   - Simulación: Metabase no disponible → Circuit Breaker se abre
   - UI muestra mensaje: "Dashboard temporalmente no disponible"
   - Backend registra evento en logs de auditoría

Características técnicas destacadas:
- Tokens JWT con expiración: 10 minutos, previene acceso no autorizado
- Circuit Breaker (Resilience4j): Maneja caídas de Metabase gracefully
- Caché con Caffeine: Tokens cacheados por 5 min, reduce latencia
- Content Security Policy (CSP): Configurado para permitir iframes de Metabase
- Auditoría: Todos los accesos registrados con usuario, timestamp, dashboard ID

---

## Flujo End-to-End Demostrado

Escenario: Un usuario carga datos de producción y visualiza los KPIs inmediatamente.

1. Usuario: Login → Navega a "Ingesta de Datos" → Sube `production_sep_2025.csv`
2. Sistema: Valida archivo → Inicia ETL → Procesa registros en <30 segundos
3. Usuario: Ve en historial: Upload realizado - Estado COMPLETED
4. Usuario: Navega a "Dashboards" → Abre "Dashboard de Producción"
5. Sistema: Genera token JWT → Embebe Metabase → Carga dashboard
6. Usuario: Ve KPIs actualizados con datos recién cargados

Tiempo total del flujo: <2 minutos (desde upload hasta visualización)

---

## URLs de Ambientes Demostrados

| Ambiente | URL | Estado |
|----------|-----|--------|
| Frontend (Vercel) | https://ioc-frontend-git-feature-ioc-001-integr-b16d6d-qb4745s-projects.vercel.app/ | Online |
| Backend (Render) | https://ioc-backend.onrender.com | Online |
| Metabase (Cloudflare Tunnel) | https://treated-paste-eos-memo.trycloudflare.com/ | Online |
| Metabase (HTTP Directo) | http://54.232.229.228:3000/ | Online |
| Health Check | https://ioc-backend.onrender.com/actuator/health | Healthy |
| ETL Health Check | https://ioc-backend.onrender.com/actuator/health/etl | Healthy |

Credenciales de Demo:
- Usuario: `combustion.1@gmail.com` / `123pass`

---


### Rendimiento
- ETL Processing: Registros procesados en 28 segundos
- Mejora de performance: 800% vs. implementación inicial (4+ min → <30 seg)
- Dashboard load time: <5 segundos (con caché de tokens)
- API response time (P95): <300ms

### Cobertura de Tests
- Backend: Tests unitarios implementados para capa de servicio (ParserService, DataSyncService, EtlJobService)
- Frontend: Tests E2E pendientes (agregado a backlog Sprint 2)

### Observabilidad
- Métricas expuestas: `/actuator/metrics` con Micrometer (contadores, timers, gauges)
- Health checks: `/actuator/health` + `/actuator/health/etl` (custom indicator)
- Logs estructurados: Todos los eventos ETL con nivel INFO/WARN/ERROR

### Seguridad
- Autenticación: Supabase Auth con JWT (HS256)
- Autorización: Spring Security con validación de sesión
- CSP: Content-Security-Policy configurado para iframes Metabase
- Auditoría: Todos los accesos a dashboards registrados

---


## Conclusiones

### Logros Destacados:
1. Sprint Goal 100% alcanzado: Ciclo completo Auth → Ingesta → Visualización funcional
2. Performance excepcional: ETL optimizado 800% (4+ min → 28 seg)
3. Arquitectura production-ready: Circuit Breaker, caché, advisory locks, observabilidad
4. 41/41 Story Points completados: 100% del compromiso cumplido


### Aprendizajes Clave:
- La inversión en optimización de performance (caché de dimensiones, batching JDBC) fue crítica para la viabilidad del producto
- La integración con Metabase mediante embedding JWT es más robusta que iframe directo
- El procesamiento asíncrono con polling proporciona mejor UX que espera síncrona
- La instrumentación desde el día 1 (Micrometer, Health Indicators) facilita debugging

### Próximos Pasos:
- Incorporar feedback de stakeholders al Product Backlog
- Planificar Sprint 2 con foco en notificaciones y roles granulares
- Continuar con velocidad sostenible de 37 SP (no 41 SP) para evitar burnout

---

Elaborado por: Jaime Arancibia (Scrum Master)  
Revisado por: Boris Rojas (Product Owner)  
