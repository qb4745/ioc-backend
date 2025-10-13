# Solución al Problema de Pool de Conexiones con Supabase

## 🔴 Problema Identificado

Después del `git reset`, tu aplicación estaba usando la configuración por defecto de HikariCP, que establece:
- **maximum-pool-size: 10 conexiones** (valor por defecto)
- Sin límites explícitos en los archivos de properties

Esto causaba que tu aplicación **excediera el límite de Supabase Free Tier** (3-5 conexiones simultáneas), resultando en:
- ❌ Conexiones rechazadas
- ❌ Timeouts en health checks de Render
- ❌ Errores `connection refused` o `pool exhausted`

## ✅ Solución Implementada

He agregado configuraciones **explícitas y agresivas** de HikariCP en todos tus archivos de properties para limitar el pool a 3 conexiones máximo:

### 📄 Archivos Modificados

#### 1. `application.properties` (Base - Desarrollo Local)
```properties
spring.datasource.hikari.maximum-pool-size=3
spring.datasource.hikari.minimum-idle=1
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=540000
spring.datasource.hikari.keepalive-time=120000
spring.datasource.hikari.validation-timeout=5000
spring.datasource.hikari.leak-detection-threshold=60000
```

#### 2. `application-dev.properties` (Desarrollo Local)
- Mismo pool-size de 3 conexiones
- Configuración optimizada para desarrollo compartiendo el pool de Supabase

#### 3. `application-prod.properties` (Render - Producción) ⚠️ MÁS CRÍTICO
```properties
# Pool MINIMO para Supabase free tier
spring.datasource.hikari.maximum-pool-size=3
spring.datasource.hikari.minimum-idle=1
spring.datasource.hikari.connection-timeout=30000

# Tiempos MÁS CORTOS para evitar que Supabase cierre conexiones antes que Hikari
spring.datasource.hikari.idle-timeout=240000        # 4 minutos
spring.datasource.hikari.max-lifetime=300000        # 5 minutos

# Keepalive para mantener conexiones vivas
spring.datasource.hikari.keepalive-time=120000      # 2 minutos
spring.datasource.hikari.validation-timeout=3000
spring.datasource.hikari.connection-test-query=SELECT 1
```

## 🔑 Configuraciones Clave Explicadas

### Maximum Pool Size: 3
- **Por qué:** Supabase Free Tier permite 3-5 conexiones simultáneas máximo
- **Impacto:** Previene errores de "too many connections"

### Max Lifetime: 300000ms (5 minutos)
- **Por qué:** Supabase puede cerrar conexiones inactivas antes de lo que Hikari espera
- **Impacto:** Hikari recicla conexiones antes de que Supabase las cierre

### Keepalive Time: 120000ms (2 minutos)
- **Por qué:** Mantiene las conexiones "vivas" con pings periódicos
- **Impacto:** Evita que Supabase cierre conexiones por inactividad

### Connection Test Query: SELECT 1
- **Por qué:** Valida que la conexión está realmente activa antes de usarla
- **Impacto:** Detecta conexiones cerradas por el servidor

## 🚀 Próximos Pasos

### 1. Commit y Push
```bash
git add src/main/resources/application*.properties
git commit -m "fix: limit HikariCP pool to 3 connections for Supabase free tier"
git push origin main
```

### 2. Verificar en Render
Después del despliegue, monitorea los logs para confirmar:
- ✅ `HikariPool-1 - Starting... maximum-pool-size: 3`
- ✅ `/actuator/health` responde con `{"status":"UP"}`
- ✅ No hay errores de `connection refused`

### 3. Verificar Uso de Conexiones en Supabase
En tu dashboard de Supabase:
1. Ve a **Settings → Database**
2. Mira el gráfico de **Active Connections**
3. Confirma que nunca excede 3 conexiones simultáneas

## 📊 Resumen de Cambios

| Configuración | Antes (Default) | Después (Optimizado) |
|---------------|-----------------|---------------------|
| maximum-pool-size | 10 | **3** |
| minimum-idle | 10 | **1** |
| max-lifetime | 1800000ms (30min) | **300000ms (5min)** |
| keepalive-time | No configurado | **120000ms (2min)** |
| connection-test-query | No configurado | **SELECT 1** |

## ⚠️ Notas Importantes

1. **ETL Advisory Locks:** Están deshabilitados en producción (`etl.lock.enabled=false`) para evitar problemas con datasources secundarios que ya no existen después del git reset.

2. **Batch Size:** El batch_size de Hibernate (500) puede parecer alto con solo 3 conexiones, pero funciona porque:
   - Las operaciones batch usan UNA conexión a la vez
   - El batch se ejecuta en memoria y luego se envía en un solo round-trip
   - No requiere múltiples conexiones simultáneas

3. **Health Check:** El endpoint `/actuator/health` solo verifica el datasource primario, no hay datasources secundarios.

## 🔍 Monitoreo Continuo

Para verificar que el pool está funcionando correctamente, puedes:

```bash
# Ver logs de HikariCP en Render
# Busca líneas como:
HikariPool-1 - Starting...
HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@...
HikariPool-1 - Pool stats (total=3, active=1, idle=2, waiting=0)
```

## 📚 Referencias

- [HikariCP Configuration](https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby)
- [Supabase Connection Pooling](https://supabase.com/docs/guides/database/connecting-to-postgres#connection-pooler)
- [Spring Boot DataSource Properties](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html#application-properties.data.spring.datasource)

