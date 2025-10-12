# 🚨 Metabase Deployment Troubleshooting Guide

**Fecha**: 12 de Octubre, 2025  
**Error**: `Message seems corrupt or manipulated` (400 Bad Request)  
**Contexto**: Deployment en Vercel (Frontend) + Render (Backend) + Metabase

---

## 🔍 Diagnóstico del Problema

### Error Observado
```
GET https://treated-paste-eos-memo.trycloudflare.com/api/embed/dashboard/[TOKEN] 400 (Bad Request)
{status: 400, data: 'Message seems corrupt or manipulated', isCancelled: false}
```

### Causa Raíz
El JWT generado por el backend **no coincide** con el secret key configurado en Metabase, debido a una de estas razones:

1. **Desalineación de URLs**: El `metabase.site-url` en el backend no coincide con `MB_SITE_URL` en Metabase
2. **Secret Key diferente**: El `METABASE_SECRET_KEY` usado por el backend no coincide con `MB_EMBEDDING_SECRET_KEY` en Metabase
3. **Algoritmo JWT incorrecto**: Metabase espera HS256 pero el backend puede estar usando HS512

---

## ✅ Solución Paso a Paso

### 1. Decidir la Arquitectura de Deployment

Tienes 3 opciones:

#### **Opción A: Metabase en Render (RECOMENDADO para producción)**
```
Frontend (Vercel) → Backend (Render) → Metabase (Render)
                                      ↓
                                   Supabase (Postgres)
```

**Ventajas**:
- ✅ Todo en la nube, sin depender de tunnels inestables
- ✅ URLs estáticas y confiables
- ✅ HTTPS nativo
- ✅ Escalable

**Desventajas**:
- ❌ Requiere plan de pago en Render ($7/mes para instancia con 512MB RAM)

#### **Opción B: Metabase en servidor propio con Cloudflare Tunnel**
```
Frontend (Vercel) → Backend (Render) → Cloudflare Tunnel → Metabase (Local/VPS)
                                                          ↓
                                                       Supabase
```

**Ventajas**:
- ✅ Metabase puede correr en tu propia infraestructura
- ✅ No requiere abrir puertos públicos
- ✅ Gratis (excepto costo del servidor)

**Desventajas**:
- ❌ URL del tunnel puede cambiar si no usas un dominio custom
- ❌ Dependes de que el tunnel esté siempre activo
- ❌ Latencia adicional

#### **Opción C: Metabase local solo para desarrollo**
```
Frontend (localhost) → Backend (localhost) → Metabase (Docker local)
```

---

### 2. Configuración para Opción A: Metabase en Render

#### **Paso 2.1: Crear servicio de Metabase en Render**

1. Ve a tu dashboard de Render
2. Crea un nuevo **Web Service**
3. Selecciona "Deploy an image from a registry"
4. Imagen: `metabase/metabase:latest`
5. Plan: Starter ($7/mes) - Mínimo 512MB RAM

**Environment Variables** (en Render):
```bash
MB_DB_TYPE=postgres
MB_DB_HOST=aws-1-sa-east-1.pooler.supabase.com
MB_DB_PORT=5432
MB_DB_DBNAME=postgres
MB_DB_USER=postgres.bdyvzjpkycnekjrlqlfp
MB_DB_PASS=[TU_PASSWORD_DE_SUPABASE]

# URL pública de Metabase (Render te dará esta URL)
MB_SITE_URL=https://ioc-metabase.onrender.com

# Secret key para embedding (genera uno seguro)
MB_EMBEDDING_SECRET_KEY=[GENERA_UN_SECRET_DE_64_CARACTERES]

# Permitir embedding desde Vercel
MB_EMBEDDING_APP_ORIGIN=https://tu-app.vercel.app

# Configuración regional
MB_SITE_LOCALE=es
JAVA_TIMEZONE=America/Santiago
```

**Cómo generar un secret key seguro**:
```bash
openssl rand -base64 64 | tr -d '\n'
```

#### **Paso 2.2: Configurar Backend en Render**

En tu **ioc-backend** en Render, añade/actualiza estas variables de entorno:

```bash
# URL pública de Metabase en Render
METABASE_URL=https://ioc-metabase.onrender.com

# El MISMO secret key que configuraste en Metabase
METABASE_SECRET_KEY=[EL_MISMO_SECRET_DE_64_CARACTERES]
```

#### **Paso 2.3: Actualizar application.properties**

```properties
# Usa la variable de entorno
metabase.site-url=${METABASE_URL}
metabase.secret-key=${METABASE_SECRET_KEY}
metabase.token-expiration-minutes=10
```

---

### 3. Configuración para Opción B: Cloudflare Tunnel

Si decides usar Cloudflare Tunnel, sigue estos pasos:

#### **Paso 3.1: Configurar Cloudflare Tunnel con dominio custom**

**Opción 3.1.1: Usar un dominio propio** (RECOMENDADO)
```bash
# En tu túnel de Cloudflare
cloudflare-tunnels create ioc-metabase
cloudflare-tunnels route dns ioc-metabase metabase.tudominio.com
```

Esto te da una URL estable: `https://metabase.tudominio.com`

**Opción 3.1.2: Usar el dominio .trycloudflare.com**
- La URL `https://treated-paste-eos-memo.trycloudflare.com` puede cambiar al reiniciar el tunnel
- No recomendado para producción

#### **Paso 3.2: Actualizar docker-compose.yml**

```yaml
metabase:
  image: metabase/metabase:latest
  container_name: ioc_metabase
  ports:
    - "3000:3000"
  environment:
    # Usa la URL del tunnel
    MB_SITE_URL: "https://metabase.tudominio.com"  # O tu URL de .trycloudflare.com
    MB_EMBEDDING_SECRET_KEY: "${METABASE_SECRET_KEY}"
    MB_EMBEDDING_APP_ORIGIN: "https://tu-app.vercel.app"
    
    # Base de datos INTERNA de Metabase (metadatos)
    MB_DB_TYPE: postgres
    MB_DB_HOST: "postgres"
    MB_DB_PORT: "5432"
    MB_DB_DBNAME: "${POSTGRES_DB}"
    MB_DB_USER: "${POSTGRES_USER}"
    MB_DB_PASS: "${POSTGRES_PASSWORD}"
    
    MB_SITE_LOCALE: "es"
    JAVA_TIMEZONE: "America/Santiago"
```

#### **Paso 3.3: Configurar Backend**

En tu `.env` local o variables de Render:
```bash
METABASE_URL=https://metabase.tudominio.com
METABASE_SECRET_KEY=[EL_MISMO_SECRET_QUE_EN_DOCKER]
```

---

### 4. Verificación del Secret Key

El error "Message seems corrupt or manipulated" casi siempre indica que el secret key no coincide. Verifica:

#### **En Metabase (Admin UI)**:
1. Ve a: `https://[tu-metabase]/admin/settings/embedding`
2. Habilita "Static embedding"
3. Copia el **Embedding secret key** que aparece ahí

#### **En el Backend**:
```bash
# Verifica que esta variable esté configurada EXACTAMENTE igual
echo $METABASE_SECRET_KEY
```

#### **Formato del Secret Key**:
- Debe ser una cadena Base64 larga (ej: `abc123XYZ...`)
- **NO** debe incluir prefijos como `Bearer ` o similares
- Debe tener al menos 32 caracteres (recomendado 64+)

---

### 5. Verificación del Algoritmo JWT

#### **Problema**: Mismatch entre HS256 y HS512

Metabase por defecto usa **HS256** para validar tokens JWT, pero el backend actualmente usa **HS512**.

#### **Paso 5.1: Verificar el código del backend**

Abre: `src/main/java/com/cambiaso/ioc/service/MetabaseEmbeddingService.java`

Busca esta línea:
```java
.signWith(key, SignatureAlgorithm.HS512)  // ❌ INCORRECTO
```

Debe ser:
```java
.signWith(key, SignatureAlgorithm.HS256)  // ✅ CORRECTO
```

#### **Paso 5.2: Aplicar el fix**

```java
String jwt = Jwts.builder()
    .setClaims(claims)
    .setIssuedAt(Date.from(now))
    .setExpiration(Date.from(expiration))
    .signWith(key, SignatureAlgorithm.HS256)  // Cambiar a HS256
    .compact();
```

---

### 6. Verificación de Content Security Policy

Los warnings de CSP que ves son esperados y NO son la causa del error 400:

```
Refused to apply inline style because it violates the following Content Security Policy directive...
```

Estos son warnings internos de Metabase y no afectan la funcionalidad. Puedes ignorarlos de forma segura.

---

## 🧪 Testing de la Configuración

### Test 1: Verificar que Metabase está accesible
```bash
curl -I https://[tu-metabase-url]/api/health
# Debe retornar: 200 OK
```

### Test 2: Verificar el backend genera tokens válidos
```bash
# Autenticarte y obtener un JWT de usuario
curl -X GET "https://[tu-backend-url]/api/v1/dashboards/5" \
  -H "Authorization: Bearer [TU_JWT_DE_USUARIO]"

# Respuesta esperada:
# {"signedUrl":"https://[metabase]/embed/dashboard/[TOKEN]#bordered=true&titled=true"}
```

### Test 3: Verificar que el token funciona en Metabase
```bash
# Copia la signedUrl del paso anterior y prueba en el navegador
# Debe cargar el dashboard SIN error 400
```

---

## 📋 Checklist de Configuración

Antes de deployar, verifica:

### Backend (Render)
- [ ] Variable `METABASE_URL` apunta a la URL correcta de Metabase
- [ ] Variable `METABASE_SECRET_KEY` coincide EXACTAMENTE con Metabase
- [ ] Código usa `SignatureAlgorithm.HS256` (NO HS512)
- [ ] `application.properties` usa variables de entorno

### Metabase
- [ ] `MB_SITE_URL` está configurado con la URL pública correcta
- [ ] `MB_EMBEDDING_SECRET_KEY` coincide con el backend
- [ ] Static embedding está habilitado en Admin → Settings → Embedding
- [ ] `MB_EMBEDDING_APP_ORIGIN` incluye tu dominio de Vercel
- [ ] Dashboards tienen embedding habilitado individualmente

### Frontend (Vercel)
- [ ] Variable `VITE_API_URL` apunta al backend en Render
- [ ] CORS está configurado en el backend para permitir tu dominio de Vercel

---

## 🔧 Configuración Recomendada para Producción

```env
# Backend (.env en Render)
METABASE_URL=https://ioc-metabase.onrender.com
METABASE_SECRET_KEY=tu_secret_key_de_64_caracteres_aqui
SUPABASE_DB_PASSWORD=tu_password_supabase
SUPABASE_JWT_ISSUER_URI=https://bdyvzjpkycnekjrlqlfp.supabase.co/auth/v1

# Metabase (Environment vars en Render)
MB_SITE_URL=https://ioc-metabase.onrender.com
MB_EMBEDDING_SECRET_KEY=el_mismo_secret_key_del_backend
MB_EMBEDDING_APP_ORIGIN=https://tu-app.vercel.app
MB_DB_TYPE=postgres
MB_DB_HOST=aws-1-sa-east-1.pooler.supabase.com
MB_DB_PORT=5432
MB_DB_DBNAME=postgres
MB_DB_USER=postgres.bdyvzjpkycnekjrlqlfp
MB_DB_PASS=tu_password_supabase
MB_SITE_LOCALE=es
JAVA_TIMEZONE=America/Santiago

# Frontend (.env.production en Vercel)
VITE_API_URL=https://ioc-backend.onrender.com
```

---

## 📌 Próximos Pasos

1. **Decide tu arquitectura** (Opción A o B)
2. **Aplica el fix del algoritmo JWT** (HS512 → HS256)
3. **Sincroniza los secret keys** entre backend y Metabase
4. **Actualiza las URLs** en todas las configuraciones
5. **Redeploy** backend y Metabase
6. **Prueba** con el checklist de testing

---

## ✅ SOLUCIÓN APLICADA (Tu Caso Específico)

### Arquitectura Actual
```
Frontend (Vercel) → Backend (Render) → Metabase (EC2: 54.232.229.228:3000)
                                              ↓
                                          Supabase (Postgres)
```

### Cambios Realizados

#### 1. **Backend actualizado** (`application.properties`)
```properties
metabase.site-url=http://54.232.229.228:3000
metabase.secret-key=${METABASE_SECRET_KEY}
```

#### 2. **Docker Compose actualizado** (`docker.ec2`)
```yaml
MB_SITE_URL: "http://54.232.229.228:3000"
MB_EMBEDDING_APP_ORIGIN: "http://localhost:5173 http://54.232.229.228:3000 https://*.vercel.app"
```

#### 3. **Archivo de producción creado** (`application-prod.properties`)
Específico para deployment en Render con las mismas configuraciones.

### Pasos para Aplicar los Cambios

#### En tu EC2 (Metabase):
```bash
# 1. Conectarte a tu EC2
ssh tu-usuario@54.232.229.228

# 2. Ir al directorio del proyecto
cd /ruta/donde/esta/metabase

# 3. Copiar el nuevo docker.ec2 a docker-compose.yml
# (o simplemente usa: docker-compose -f docker.ec2 up -d)

# 4. Reiniciar los contenedores
docker-compose down
docker-compose up -d

# 5. Verificar que levantó correctamente
docker-compose logs -f metabase
```

#### En Render (Backend):
```bash
# Las variables de entorno ya deberían estar configuradas:
# - METABASE_SECRET_KEY (debe coincidir con el del .env de EC2)
# - METABASE_URL=http://54.232.229.228:3000 (opcional, usa el valor por defecto)

# Redeploy automático cuando hagas push al repositorio
git add .
git commit -m "fix: sincronizar URLs de Metabase entre backend y EC2"
git push origin main
```

#### En Vercel (Frontend):
No requiere cambios, solo asegúrate de que la variable de entorno `VITE_API_URL` apunte a tu backend en Render.

### Verificación Post-Deployment

#### Test 1: Backend genera URLs correctas
```bash
curl -X GET "https://tu-backend.onrender.com/api/v1/dashboards/1" \
  -H "Authorization: Bearer TU_JWT_DE_USUARIO"

# Respuesta esperada:
# {"signedUrl":"http://54.232.229.228:3000/embed/dashboard/TOKEN#bordered=true&titled=true"}
```

#### Test 2: Metabase acepta el token
Abre la URL del paso anterior en el navegador. Debe cargar el dashboard sin error 400.

#### Test 3: Frontend carga los dashboards
1. Abre tu app en Vercel
2. Ve a la página de dashboards
3. Verifica en la consola del navegador:
   - ✅ `Dashboard URL received: http://54.232.229.228:3000/embed/dashboard/...`
   - ✅ `Dashboard loaded successfully`
   - ❌ NO debe aparecer `{status: 400, data: 'Message seems corrupt or manipulated'}`

### Troubleshooting si persiste el error

Si después de aplicar los cambios sigues viendo el error 400:

1. **Verifica que el secret key coincide EXACTAMENTE:**
   ```bash
   # En EC2
   grep METABASE_SECRET_KEY .env
   
   # En Render (variables de entorno)
   # Debe ser IDÉNTICO al de EC2
   ```

2. **Verifica los logs de Metabase:**
   ```bash
   docker-compose logs metabase | grep -i "embedding\|secret\|jwt"
   ```

3. **Usa jwt.io para debug:**
   - Copia el token de una URL generada por el backend
   - Pégalo en https://jwt.io
   - Verifica:
     - Algoritmo: `HS256` ✅
     - Payload: `{"resource":{"dashboard":X},"params":{...}}`
     - Signature: Usa el `METABASE_SECRET_KEY` como secret

---

## 🆘 Si el problema persiste

1. **Verifica los logs del backend** en Render
2. **Verifica los logs de Metabase** (en EC2: `docker-compose logs -f metabase`)
3. **Usa la herramienta de debug de JWT**: https://jwt.io
   - Pega el token que genera tu backend
   - Verifica que el algoritmo sea HS256
   - Verifica que el payload contenga `{"resource":{"dashboard":X}}`
4. **Comprueba que el secret key no tiene espacios** al inicio o final

---

**Última actualización**: 12 de Octubre, 2025
