# Agente frontend: cómo cambiar permisos de un rol (sin tocar backend)

Propósito
- Documentar un flujo seguro y reproducible para que un agente IA o el frontend actualicen los permisos asignados a un rol usando los endpoints existentes del backend, sin necesidad de modificar DTOs o servicios del servidor.

Descripción del problema
- Qué ocurre: desde el frontend a menudo se intenta actualizar la lista de permisos de un rol enviando un PUT al endpoint de roles (por ejemplo `PUT /api/v1/admin/roles/{id}`) con un campo `permissions` en el cuerpo JSON.
- Por qué falla: el backend puede no procesar ese campo en el `RoleRequest` o no reconducir las relaciones role↔permission en el método `RoleService.update`. Como con usuarios y roles, el proyecto expone endpoints separados para asignar y remover permisos a roles.
- Efecto práctico: el PUT de rol actualiza algunos campos del rol (nombre, descripción), pero NO modifica la colección de permisos asociada; los cambios esperados en permisos no se aplican.
- Ejemplo típico: enviar `PUT /api/v1/admin/roles/5` con `{"name":"GERENTE","permissions":[1,9]}` y observar que la lista de permisos del rol no cambia.

Resumen rápido
- No confíes en que `PUT /api/v1/admin/roles/{id}` vaya a reemplazar permisos.
- Usa los endpoints disponibles para gestión de relaciones:
  - POST /api/v1/admin/assignments/roles/{roleId}/permissions/{permissionId}  → asigna permiso al rol
  - DELETE /api/v1/admin/assignments/roles/{roleId}/permissions/{permissionId} → elimina permiso del rol
- Flujo recomendado: leer permisos actuales del rol, calcular diferencias con los permisos deseados y ejecutar POST/DELETE según corresponda.

Precondiciones y permisos
- El token usado por el agente debe tener `ROLE_ADMIN`.
- El agente debe poder listar permisos disponibles (por ejemplo, `GET /api/v1/admin/permissions` o similar) para resolver permissionId desde un nombre/código, o mantener una caché local.

Contrato mínimo (qué haremos)
- Input: roleId (Integer), desiredPermissionIds (Array<Integer> o desiredPermissionKeys si usas nombres), adminToken (JWT).
- Output: éxito (permisos sincronizados) o error (403/404/500).
- Error modes: token sin permisos, permissionId inexistente, fallo de red.

Checklist
- [ ] Obtener permisos actuales del rol.
- [ ] Resolver permissionId si el frontend tiene nombres en vez de ids.
- [ ] Calcular toAdd y toRemove.
- [ ] Ejecutar POST para asignar y DELETE para remover.
- [ ] Verificar el resultado mediante GET y manejar errores/reintentos.

Paso a paso detallado

1) Leer permisos actuales del rol
- Endpoint sugerido: GET /api/v1/admin/roles/{id} -> la respuesta `RoleResponse` suele incluir detalles y posiblemente una lista `permissions`.
- Si `GET /roles/{id}` no incluye permisos, busca un endpoint de detalle o una ruta `/api/v1/admin/roles/{id}/permissions` (verifica en la API). Si no hay endpoint directo, puedes derivar los permisos consultando una tabla de asignaciones o un endpoint de búsqueda.

2) Calcular diferencias
- desired = set de permisos deseados (por ejemplo [1,9] o nombres codificados).
- current = set de permisos devueltos por GET.
- toAdd = desired \ current
- toRemove = current \ desired

3) Resolver permissionId desde nombre (si aplica)
- Si tu UI trabaja con nombres o códigos en vez de IDs, resuelve cada nombre a un ID usando un endpoint como `GET /api/v1/admin/permissions?search={name}` o mediante una cache local cargada al inicio.

4) Asignar permisos (toAdd)
- Para cada permissionId en toAdd:
  - POST /api/v1/admin/assignments/roles/{roleId}/permissions/{permissionId}
  - Verifica la respuesta; el servicio suele ser idempotente (comprueba existencia antes de insertar).

5) Remover permisos (toRemove)
- Para cada permissionId en toRemove:
  - DELETE /api/v1/admin/assignments/roles/{roleId}/permissions/{permissionId}

6) Verificación final
- Volver a GET /api/v1/admin/roles/{id} y comparar la lista de permisos con la lista deseada.

Estrategias y consideraciones
- Paralelismo: puedes ejecutar llamadas en paralelo (Promise.all) para velocidad; sin embargo, para control de errores y orden, a veces es preferible ejecutar en serie y hacer reintentos selectivos.
- Atomicidad: no existe operación atómica por defecto. Si necesitas reemplazar todos los permisos atómicamente, hay que introducir un endpoint server-side que haga la reconciliación en una transacción.
- Idempotencia y reintentos: el endpoint de asignación del backend debería ser idempotente (comprueba si la relación ya existe). Implementa 2-3 reintentos exponenciales en errores temporales (5xx, timeouts). Para 4xx (salvo 429) no reintentes.
- Rates: evita explotar el servidor con demasiadas peticiones concurrentes (usa un pool/concurrency limit si actualizas muchas relaciones a la vez).

Manejo de errores y UX
- 401/403: mostrar falta de permisos o forzar re-login.
- 404 en resolución de permissionId: informar que el permiso no existe.
- Fallo parcial: en caso de que algunas asignaciones fallen, mostrar qué operaciones fallaron y permitir reintento selectivo.
- Feedback: mostrar una lista de acciones (asignar X, eliminar Y) antes de ejecutar, y mostrar estado (pendiente/ok/error) por acción.

Ejemplos prácticos (cURL)

1) Obtener rol y permisos (si `RoleResponse` incluye `permissions`)

```bash
curl -i -X GET 'http://127.0.0.1:8080/api/v1/admin/roles/5' \
  -H 'Authorization: Bearer <TOKEN>' \
  -H 'Accept: application/json'
```

2) Buscar permiso por nombre (obtener id)

```bash
curl -i -X GET 'http://127.0.0.1:8080/api/v1/admin/permissions?search=CREATE_USER' \
  -H 'Authorization: Bearer <TOKEN>' \
  -H 'Accept: application/json'
```

3) Asignar permiso (ejemplo permissionId=9)

```bash
curl -i -X POST 'http://127.0.0.1:8080/api/v1/admin/assignments/roles/5/permissions/9' \
  -H 'Authorization: Bearer <TOKEN>' \
  -H 'Accept: application/json'
```

4) Remover permiso (ejemplo permissionId=9)

```bash
curl -i -X DELETE 'http://127.0.0.1:8080/api/v1/admin/assignments/roles/5/permissions/9' \
  -H 'Authorization: Bearer <TOKEN>' \
  -H 'Accept: application/json'
```

Snippet frontend (fetch / JS)

```js
// inputs: roleId, desiredPermissionIds (array of ints), token
async function syncRolePermissions(roleId, desiredPermissionIds, token) {
  const headers = { 'Authorization': `Bearer ${token}`, 'Accept': 'application/json' };

  // 1) get current
  const roleRes = await fetch(`/api/v1/admin/roles/${roleId}`, { headers });
  if (!roleRes.ok) throw new Error('Failed to fetch role: ' + roleRes.status);
  const role = await roleRes.json();
  const current = new Set((role.permissions || []).map(p => p.id !== undefined ? p.id : p));

  // 2) desired set
  const desired = new Set(desiredPermissionIds);
  const toAdd = [...desired].filter(id => !current.has(id));
  const toRemove = [...current].filter(id => !desired.has(id));

  // 3) assign (with limited concurrency)
  await Promise.all(toAdd.map(async permissionId => {
    const res = await fetch(`/api/v1/admin/assignments/roles/${roleId}/permissions/${permissionId}`, { method: 'POST', headers });
    if (!res.ok) throw new Error('Failed to assign permission ' + permissionId + ': ' + res.status);
  }));

  // 4) remove
  await Promise.all(toRemove.map(async permissionId => {
    const res = await fetch(`/api/v1/admin/assignments/roles/${roleId}/permissions/${permissionId}`, { method: 'DELETE', headers });
    if (!res.ok) throw new Error('Failed to remove permission ' + permissionId + ': ' + res.status);
  }));

  // 5) verify
  const verify = await fetch(`/api/v1/admin/roles/${roleId}`, { headers });
  const refreshed = await verify.json();
  return (refreshed.permissions || []).map(p => p.id !== undefined ? p.id : p);
}
```

Buenas prácticas
- Cachear la lista completa de permisos y roles (nombre→id) en el frontend.
- Mostrar confirmación y explicitar qué se va a cambiar.
- Limitar concurrencia a N (p. ej. 5) cuando hagas muchas operaciones.

Alternativa: cambiar backend
- Añadir capacidad en `RoleRequest`/`RoleService.update` para aceptar y reconciliar `permissions` en una sola operación transaccional. Esto requiere cambios en backend y tests, pero ofrece una operación atómica y simplifica frontend.

Próximos pasos
- Implementar este flujo en el frontend (snippet) y probar con un token admin.
- Si quieres, puedo también: (A) añadir un hook React completo con manejo de estado y retry/backoff; o (B) proponer los cambios mínimos en backend para soportar replace de permisos en un PUT.

Fin del documento.

