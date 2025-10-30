# Fix Implementado: Creación Automática de Usuarios en Supabase

## Problema Resuelto

**ANTES**: El admin tenía que:
1. Ir a Supabase Dashboard
2. Crear el usuario manualmente
3. Copiar el UUID
4. Usar ese UUID en el endpoint del backend

**AHORA**: El admin simplemente:
1. Llama al endpoint con email y password
2. El backend automáticamente crea el usuario en Supabase
3. Todo se hace en una sola operación atómica

## Cambios Implementados

### 1. Dependencias Agregadas
- **WebFlux** (`spring-boot-starter-webflux`): Para hacer llamadas HTTP a la API de Supabase

### 2. Nuevos Archivos Creados

#### `SupabaseAuthService.java`
Servicio que se comunica con la API de Supabase Auth para:
- Crear usuarios automáticamente con `createSupabaseUser(email, password)`
- Eliminar usuarios (rollback) con `deleteSupabaseUser(uuid)`

#### `WebClientConfig.java`
Configuración del bean `WebClient.Builder` necesario para las llamadas HTTP

### 3. Archivos Modificados

#### `UsuarioCreateRequest.java`
- **NUEVO**: Campo `password` (opcional, mínimo 6 caracteres)
- **DEPRECATED**: Campo `supabaseUserId` (mantiene compatibilidad temporal)

#### `UserAdminService.java`
Lógica actualizada en el método `create()`:
- Si viene `password`: Crea el usuario en Supabase automáticamente
- Si viene `supabaseUserId`: Usa el flujo antiguo (deprecated)
- Si falla la BD: Hace rollback automático eliminando el usuario de Supabase

#### Archivos de configuración
Agregada configuración en:
- `application.properties`
- `application-dev.properties`
- `application-prod.properties`
- `application-local.properties`

Nuevas propiedades:
```properties
supabase.url=${SUPABASE_URL:https://bdyvzjpkycnekjrlqlfp.supabase.co}
supabase.service-role-key=${SUPABASE_SERVICE_ROLE_KEY}
```

## Uso del Endpoint

### Ejemplo de Request (NUEVO - Recomendado)

```bash
POST /api/admin/users
Content-Type: application/json
Authorization: Bearer {admin-jwt-token}

{
  "email": "nuevo.usuario@cambiaso.com",
  "password": "temporal123",  // <-- NUEVO: Password temporal
  "primerNombre": "Juan",
  "primerApellido": "Pérez",
  "plantaId": 1,
  "roles": ["ROLE_USER"]
}
```

### Ejemplo de Request (LEGACY - Deprecated)

```bash
POST /api/admin/users
Content-Type: application/json
Authorization: Bearer {admin-jwt-token}

{
  "email": "nuevo.usuario@cambiaso.com",
  "supabaseUserId": "550e8400-e29b-41d4-a716-446655440000",  // Deprecated
  "primerNombre": "Juan",
  "primerApellido": "Pérez",
  "plantaId": 1,
  "roles": ["ROLE_USER"]
}
```

## Variables de Entorno Requeridas

Para que funcione la creación automática, debes configurar:

```bash
# Service Role Key (obtenerlo de Supabase Dashboard > Settings > API)
export SUPABASE_SERVICE_ROLE_KEY="eyJhbGc..."

# URL de tu proyecto Supabase
export SUPABASE_URL="https://bdyvzjpkycnekjrlqlfp.supabase.co"
```

### Cómo obtener el Service Role Key:
1. Ve a tu proyecto en Supabase Dashboard
2. Settings → API
3. Copia el valor de "service_role" (¡NO el anon key!)
4. ⚠️ **IMPORTANTE**: Este key es secreto, nunca lo comitees al repo

## Flujo de Creación

```
1. Admin llama POST /api/admin/users con email + password
2. Backend valida que el email no exista
3. Backend llama a Supabase Auth API para crear el usuario
4. Supabase responde con el UUID del nuevo usuario
5. Backend guarda el usuario en la BD con ese UUID
6. Si falla paso 5: Backend elimina el usuario de Supabase (rollback)
```

## Ventajas

✅ **UX mejorada**: El admin no necesita acceso a Supabase Dashboard  
✅ **Atomicidad**: Si falla algo, se hace rollback automático  
✅ **Seguridad**: El Service Role Key solo está en el servidor  
✅ **Trazabilidad**: Todo queda loggeado con SLF4J  
✅ **Compatibilidad**: El flujo antiguo sigue funcionando temporalmente  

## Próximos Pasos

1. ✅ Agregar variable `SUPABASE_SERVICE_ROLE_KEY` en Render
2. ✅ Probar el endpoint con un usuario real
3. ⏳ Actualizar el frontend para enviar `password` en lugar de `supabaseUserId`
4. ⏳ Enviar email al usuario con instrucciones para cambiar password
5. ⏳ Remover el campo `supabaseUserId` deprecated en v2.0

## Testing

```bash
# Compilar el proyecto
./mvnw clean compile

# Ejecutar tests
./mvnw test

# Ejecutar aplicación
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## Notas Técnicas

- El usuario se crea con `email_confirm: true` (email auto-confirmado)
- El password es temporal - el usuario debería cambiarlo en su primer login
- El Service Role Key bypasea RLS (Row Level Security) - úsalo solo en el backend
- WebFlux se agregó solo para WebClient (no convierte la app a reactiva)

