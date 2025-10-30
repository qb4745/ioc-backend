# INFORME ACTUALIZADO: Solución para Supabase FREE Tier

**Fecha:** 2025-10-30  
**Estado:** ✅ IMPLEMENTADO

---

## 🎯 **CONCLUSIÓN: La solución del FREE TIER es la CORRECTA para tu proyecto**

### **¿Por qué?**

1. **Supabase FREE NO permite modificar claims del JWT** → La solución del informe original NO funcionará
2. **Tu arquitectura ya está preparada:**
   - ✅ Tienes `AppUser.supabaseUserId` (UUID) mapeado al `sub` del JWT
   - ✅ Tienes `user_roles` con relación `AppUser ↔ Role`
   - ✅ Tienes `UserRoleRepository.findRoleNamesByUserId()` funcionando
   - ✅ Tienes `SupabaseAuthService` para crear usuarios automáticamente

3. **Solo faltaba el enriquecimiento de authorities** → YA IMPLEMENTADO

---

## ✅ **CAMBIOS IMPLEMENTADOS**

### **1. `SecurityConfig.java` - Enriquecimiento desde PostgreSQL**

Se agregó el paso 3 en `jwtGrantedAuthoritiesConverter()`:

```java
// 🔥 3) NUEVO: Enriquecer desde PostgreSQL (SOLUCIÓN PARA FREE TIER)
String supabaseUserIdStr = jwt.getSubject(); // "sub" del JWT
if (supabaseUserIdStr != null) {
    try {
        java.util.UUID supabaseUserId = java.util.UUID.fromString(supabaseUserIdStr);
        
        // Buscar usuario en la BD local
        appUserRepository.findBySupabaseUserId(supabaseUserId).ifPresent(appUser -> {
            // Obtener roles desde user_roles
            List<String> dbRoles = userRoleRepository.findRoleNamesByUserId(appUser.getId());
            dbRoles.forEach(roleName -> 
                authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName.toUpperCase()))
            );
        });
    } catch (IllegalArgumentException e) {
        // UUID inválido, ignorar
    }
}
```

**¿Qué hace esto?**
1. Extrae el `sub` (UUID de Supabase) del JWT
2. Busca el usuario en `app_users` por `supabase_user_id`
3. Consulta sus roles en `user_roles`
4. Agrega `ROLE_ADMIN`, `ROLE_USER`, etc. a las authorities de Spring Security

---

## 🚀 **FLUJO COMPLETO PARA CREAR Y USAR ADMIN**

### **Paso 1: Crear usuario en Supabase**

```bash
export SUPABASE_URL="https://tu-proyecto.supabase.co"
export SUPABASE_SERVICE_ROLE_KEY="eyJ...TU_SERVICE_ROLE_KEY..."

curl -s -X POST "$SUPABASE_URL/auth/v1/admin/users" \
  -H "apikey: $SUPABASE_SERVICE_ROLE_KEY" \
  -H "Authorization: Bearer $SUPABASE_SERVICE_ROLE_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "AdminP@ss123!",
    "email_confirm": true
  }' | jq '.id'
```

**Salida esperada:**
```
"a1b2c3d4-5678-90ab-cdef-1234567890ab"
```

---

### **Paso 2: Insertar en PostgreSQL**

Edita el archivo `setup_admin_user.sql` (creado en la raíz del proyecto) reemplazando el UUID:

```sql
INSERT INTO app_users (
    supabase_user_id, 
    email, 
    primer_nombre, 
    primer_apellido, 
    planta_id,
    is_active,
    created_at,
    updated_at
)
VALUES (
    'a1b2c3d4-5678-90ab-cdef-1234567890ab'::uuid, -- 🔥 UUID del paso 1
    'admin@example.com',
    'Admin',
    'Sistema',
    1,
    true,
    NOW(),
    NOW()
);
```

Ejecuta el script:

```bash
psql $DATABASE_URL -f setup_admin_user.sql
```

---

### **Paso 3: Obtener token de acceso**

```bash
export SUPABASE_ANON_KEY="eyJ...TU_ANON_KEY..."

TOKEN=$(curl -s -X POST "$SUPABASE_URL/auth/v1/token?grant_type=password" \
  -H "apikey: $SUPABASE_ANON_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "AdminP@ss123!"
  }' | jq -r '.access_token')

echo $TOKEN
```

---

### **Paso 4: Llamar al endpoint protegido**

```bash
curl -i -X POST 'http://localhost:8080/api/v1/admin/users' \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "nuevo.usuario@example.com",
    "password": "UserP@ss123!",
    "primerNombre": "Nuevo",
    "primerApellido": "Usuario",
    "plantaId": 1,
    "roles": ["USER"]
  }'
```

**Respuesta esperada:** `201 Created`

---

## 📊 **COMPARACIÓN DE SOLUCIONES**

| Aspecto | Solución Original (claims en JWT) | ✅ Solución FREE Tier (consulta a BD) |
|---------|-----------------------------------|----------------------------------------|
| **Compatible con Supabase FREE** | ❌ NO (requiere Auth Hooks / Pro) | ✅ SÍ |
| **Modificación de código** | ❌ Imposible sin upgrade | ✅ Solo `SecurityConfig.java` |
| **Performance** | ✅ Sin consultas extra | ⚠️ 1 query por request (cacheable) |
| **Control de roles** | ⚠️ Depende de Supabase | ✅ Total en tu BD |
| **Auditoría** | ⚠️ Limitada | ✅ Completa en PostgreSQL |
| **Costo** | 💰 $25/mes (Pro tier) | ✅ Gratis |

---

## 🔒 **SEGURIDAD**

### **Variables de entorno requeridas**

En `application.properties` o en tu entorno de deploy (Render, AWS, etc.):

```properties
# Supabase Configuration
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://tu-proyecto.supabase.co/auth/v1
supabase.url=https://tu-proyecto.supabase.co
supabase.service-role-key=${SUPABASE_SERVICE_ROLE_KEY}
supabase.anon-key=${SUPABASE_ANON_KEY}
```

⚠️ **NUNCA commitear las keys en el repositorio**

---

## ⚡ **OPTIMIZACIÓN (OPCIONAL): Caché de roles**

Si te preocupa el performance (1 query por request), puedes agregar caché:

```java
// En SecurityConfig.java, dentro del ifPresent:
@Cacheable(value = "userRoles", key = "#appUser.id")
List<String> dbRoles = userRoleRepository.findRoleNamesByUserId(appUser.getId());
```

Configuración en `application.properties`:

```properties
spring.cache.type=caffeine
spring.cache.caffeine.spec=maximumSize=10000,expireAfterWrite=5m
```

---

## 📝 **ARCHIVOS CREADOS/MODIFICADOS**

- ✅ **`SecurityConfig.java`** - Enriquecimiento de authorities implementado
- ✅ **`setup_admin_user.sql`** - Script para crear admin inicial
- ✅ **Este informe** - Documentación completa

---

## 🧪 **TESTING**

Para probar en tests sin BD:

```java
@Test
@WithMockUser(authorities = "ROLE_ADMIN")
void testAdminEndpoint() {
    // Tu test aquí
}
```

O con JWT mock:

```java
@Test
void testWithMockJwt() {
    Jwt jwt = Jwt.withTokenValue("token")
        .header("alg", "ES256")
        .claim("sub", "uuid-del-usuario")
        .build();
    
    // Mock del repository
    when(appUserRepository.findBySupabaseUserId(any()))
        .thenReturn(Optional.of(mockUser));
    when(userRoleRepository.findRoleNamesByUserId(any()))
        .thenReturn(List.of("ADMIN"));
    
    // Test...
}
```

---

## ✅ **RESUMEN**

**Estado:** La solución está implementada y lista para usar.

**Próximos pasos:**
1. Crear usuario admin en Supabase (Paso 1)
2. Ejecutar `setup_admin_user.sql` con el UUID correcto (Paso 2)
3. Obtener token y probar el endpoint (Pasos 3-4)

**Ventajas de esta solución:**
- ✅ Funciona con Supabase FREE
- ✅ Control total de roles en tu BD
- ✅ Fácil de auditar y mantener
- ✅ Ya integrada con tu arquitectura existente

---

**¿Dudas?** Revisa los logs de Spring Security con:

```properties
logging.level.org.springframework.security=DEBUG
```

