# Fix Crítico: Conexiones Idle por Uso de Puerto 5432 (Session Mode)

## 🔴 Problema Identificado

### Evidencia del Problema:
```json
{
  "pid": 1547683,
  "application_name": "PostgreSQL JDBC Driver",
  "client_addr": "2600:1f1e:75b:4b01:d427:372f:93c8:6ae3",
  "state": "idle",
  "tiempo_conexion_activa": "00:24:33.890115",
  "tiempo_inactiva": "00:23:13.748991",
  "tipo_conexion": "❌ CONEXIÓN DIRECTA (INCORRECTO)"
}
```

**14+ conexiones idle** desde la misma IP, todas con:
- `application_name`: "PostgreSQL JDBC Driver" (tu backend Spring Boot)
- `state`: "idle" (inactivas desde hace minutos/horas)
- `client_addr`: Misma IP (todas desde tu aplicación)

### Causa Raíz:

Después del `git reset`, tu `application.properties` tenía:

```properties
# ❌ INCORRECTO - Puerto 5432 = Session Mode
spring.datasource.url=jdbc:postgresql://...supabase.com:5432/postgres
```

**¿Por qué esto es un problema?**

| Puerto | Modo | Comportamiento |
|--------|------|----------------|
| **5432** | Session Mode | ❌ Conexiones persistentes que NO se liberan automáticamente |
| **6543** | Transaction Mode | ✅ Conexiones se devuelven al pool inmediatamente después de la transacción |

Cuando usas **puerto 5432 (Session Mode)**:
1. HikariCP abre 3 conexiones (configuración `maximum-pool-size=3`)
2. Cada conexión se "casa" con una conexión directa a PostgreSQL
3. Las conexiones permanecen `idle` indefinidamente
4. **Resultado:** Consumes todas las conexiones disponibles de Supabase Free Tier

### ¿Por qué tantas conexiones idle?

HikariCP mantiene un pool de conexiones listo para usar. En Session Mode:
- **minimum-idle=1**: Mantiene al menos 1 conexión siempre abierta
- **maximum-pool-size=3**: Puede abrir hasta 3 conexiones
- **max-lifetime=540000ms (9 min)**: Las conexiones se reciclan después de 9 minutos

Pero como el Session Mode NO libera conexiones automáticamente, estas se acumulan como `idle` en PostgreSQL, consumiendo el límite de tu plan.

## ✅ Solución Aplicada

He cambiado el puerto de **5432 → 6543 (Transaction Mode)** en todos los archivos de properties:

### 1. `application.properties`
```properties
# ✅ CORRECTO - Puerto 6543 = Transaction Mode
spring.datasource.url=jdbc:postgresql://aws-1-sa-east-1.pooler.supabase.com:6543/postgres?reWriteBatchedInserts=true

# Pool limitado a 3 conexiones
spring.datasource.hikari.maximum-pool-size=3
spring.datasource.hikari.minimum-idle=1
```

### 2. `application-dev.properties`
```properties
# ✅ CORRECTO - Puerto 6543 = Transaction Mode
spring.datasource.url=jdbc:postgresql://aws-1-sa-east-1.pooler.supabase.com:6543/postgres?reWriteBatchedInserts=true
```

### 3. `application-prod.properties`
Ya estaba correcto desde los commits anteriores.

### 4. Advisory Locks Deshabilitados
Como después del `git reset` NO tienes el datasource secundario (puerto 5432 para advisory locks):
```properties
etl.lock.enabled=false
```

## 📊 Comparación: Session Mode vs Transaction Mode

### Session Mode (Puerto 5432) ❌

```
App → HikariCP Pool (3 conexiones)
       ↓
Supabase Session Pooler (Puerto 5432)
       ↓
PostgreSQL (14+ conexiones IDLE acumuladas)
```

**Problemas:**
- ❌ Conexiones permanecen idle indefinidamente
- ❌ Consumes todo el límite de Supabase (3-5 conexiones)
- ❌ Errores de "too many connections"
- ❌ No apto para HikariCP/Connection Pooling

### Transaction Mode (Puerto 6543) ✅

```
App → HikariCP Pool (3 conexiones locales)
       ↓
Supabase Transaction Pooler (Puerto 6543)
       ↓
PostgreSQL (Solo 1-2 conexiones activas, se reciclan)
```

**Beneficios:**
- ✅ Conexiones se liberan automáticamente después de cada transacción
- ✅ Supabase reutiliza eficientemente las conexiones reales
- ✅ No hay acumulación de conexiones idle
- ✅ Compatible con HikariCP y batch inserts

## 🔍 Cómo Verificar la Solución

### 1. Después de deployar, verifica las conexiones activas:

Ejecuta en Supabase SQL Editor:
```sql
SELECT 
  pid,
  application_name,
  client_addr,
  state,
  state_change,
  backend_start,
  query_start
FROM pg_stat_activity
WHERE datname = 'postgres'
  AND application_name = 'PostgreSQL JDBC Driver'
ORDER BY backend_start DESC;
```

**Resultado esperado:**
- ✅ Solo 1-3 conexiones activas (no 14+)
- ✅ Tiempos de conexión cortos (segundos, no minutos)
- ✅ Estado `active` o `idle in transaction`, no `idle` prolongado

### 2. Verifica en logs de Render:

Busca líneas como:
```
HikariPool-1 - Starting...
HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@...
HikariPool-1 - Pool stats (total=3, active=1, idle=2, waiting=0)
```

### 3. Monitorea el uso de conexiones en Supabase Dashboard:

Ve a **Settings → Database → Connection pooling**
- Deberías ver que el uso de conexiones se mantiene bajo (1-3)
- No deberían acumularse conexiones idle

## 🚀 Deploy de la Solución

```bash
# 1. Verificar cambios
git status

# 2. Commit
git add src/main/resources/application*.properties
git commit -m "fix: change to port 6543 (Transaction Mode) to prevent idle connections"

# 3. Push a Render
git push origin main

# 4. Monitorear en Render
# Espera que el deploy complete y verifica /actuator/health
```

## 📝 Configuración Final Óptima

### Para Aplicaciones en Supabase Free Tier:

```properties
# CRITICO: Usar puerto 6543 (Transaction Mode)
spring.datasource.url=jdbc:postgresql://...supabase.com:6543/postgres?reWriteBatchedInserts=true

# Pool MINIMO para no exceder límite de Supabase
spring.datasource.hikari.maximum-pool-size=3
spring.datasource.hikari.minimum-idle=1

# Tiempos cortos para reciclar conexiones
spring.datasource.hikari.max-lifetime=300000        # 5 min
spring.datasource.hikari.keepalive-time=120000      # 2 min
spring.datasource.hikari.idle-timeout=240000        # 4 min
```

### Cuándo usar Session Mode (Puerto 5432):

**Solo** para casos muy específicos:
- Advisory Locks (pg_advisory_lock)
- LISTEN/NOTIFY
- Transacciones que necesitan mantener estado de sesión

**En estos casos:**
- Usa un datasource secundario SEPARADO
- Pool muy pequeño (1-2 conexiones)
- NO lo uses para operaciones JPA normales

## ⚠️ Lección Aprendida

El `git reset` nos devolvió a un estado donde:
1. ❌ No teníamos datasource dual
2. ❌ Usábamos puerto 5432 (Session Mode) para TODO
3. ❌ Acumulábamos conexiones idle sin control

**Solución permanente:**
- ✅ Usar puerto 6543 (Transaction Mode) como datasource principal
- ✅ Pool limitado a 3 conexiones para Supabase Free Tier
- ✅ Si necesitas Advisory Locks, crear un datasource secundario separado

## 🎯 Resultado Esperado

Con esta configuración:
- ✅ Máximo 3 conexiones en el pool local de HikariCP
- ✅ Solo 1-2 conexiones reales a PostgreSQL (recicladas por Transaction Mode)
- ✅ Sin acumulación de conexiones idle
- ✅ Cumplimiento del límite de Supabase Free Tier
- ✅ Performance óptima con batch inserts (batch_size=100)

