# Agente frontend: cómo cambiar roles de un usuario (sin tocar backend)

Propósito
- Documentar un flujo seguro y robusto para que un agente IA o el frontend actualicen los roles de un usuario usando los endpoints existentes en el backend (sin necesitar modificar DTOs ni servicios del servidor).

Descripción del problema
- Qué ocurre: desde el frontend muchos desarrolladores intentan actualizar los roles de un usuario enviando un campo `roles` dentro del cuerpo (JSON) de la petición PUT a `/api/v1/admin/users/{id}`.
- Por qué falla: el backend no procesa ese campo porque el DTO `UsuarioUpdateRequest` no tiene ninguna propiedad `roles`, y la implementación de `UserAdminService.update` tampoco reconcilia ni modifica las asignaciones de roles del usuario. En consecuencia, los cambios de roles enviados en el PUT son ignorados (y, en algunos entornos, podrían provocar un error si el deserializador rechaza propiedades desconocidas).
- Efecto práctico: el frontend muestra que la petición PUT fue exitosa para los datos del usuario (nombre, planta, etc.), pero las asignaciones de roles no se actualizan. La API ya proporciona endpoints separados para asignar/quitar roles (`/api/v1/admin/assignments/...`) y este documento describe cómo usar esos endpoints desde el frontend/agent para lograr el resultado esperado sin tocar el backend.
- Ejemplo típico: el desarrollador envía un PUT con `roles: ["ADMIN","GERENTE"]` esperando que el backend reemplace las asignaciones previas, pero tras la respuesta el usuario sigue teniendo sus roles anteriores.

Resumen rápido
- El endpoint `PUT /api/v1/admin/users/{id}` no acepta `roles` en el request body.
- Hay endpoints ya implementados para asignar y remover roles:
  - POST /api/v1/admin/assignments/users/{userId}/roles/{roleId}
  - DELETE /api/v1/admin/assignments/users/{userId}/roles/{roleId}
- Flujo recomendado: leer roles actuales, calcular diferencias, y llamar a los endpoints de asignación/remoción para sincronizar el estado.

Precondiciones y permisos
- El token que use el agente debe incluir la autoridad `ROLE_ADMIN` (todos los endpoints administrativos requieren esta autoridad).
- El agente necesita poder leer roles existentes (`GET /api/v1/admin/roles`) para resolver `roleId` desde `roleName`, o mantener una caché local de mapeos nombre→id.

Contrato mínimo (qué haremos)
- Input: userId (Long), desiredRoles (Array<string> de nombres de roles), adminToken (JWT).
- Output: éxito (roles actualizados) o error con motivo (403/404/500). 
- Error modes: token sin permisos, rol inexistente, fallo de red.

Checklist (qué implementa el agente)
- [ ] Obtener roles actuales del usuario.
- [ ] Resolver roleId para cada rol por nombre (buscar o usar caché).
- [ ] Calcular toAdd y toRemove.
- [ ] Ejecutar POST para asignar y DELETE para remover.
- [ ] Manejar errores, reintentos y reporte de estado al usuario.

Paso a paso detallado

1) Leer el estado actual
- Endpoint: GET /api/v1/admin/users/{id}
- Ejemplo: devuelve la representación del usuario con un campo `roles` (array de strings).

2) Calcular diferencias
- desired = set de roles deseados (por ejemplo: ["ADMIN","GERENTE"]).
- current = set de roles devueltos por GET.
- toAdd = desired \ current
- toRemove = current \ desired

3) Resolver roleId desde roleName
- Endpoint: GET /api/v1/admin/roles?search={name}
- Nota: la respuesta es paginada; busca dentro de `page.content` (o `content`) el objeto cuyo `name` coincida exactamente (ignorando mayúsculas).
- Recomendación: mantener una cache (p. ej. en memoria o IndexedDB) con la lista completa de roles para evitar múltiples búsquedas. Puedes obtener la lista completa con paginación y guardarla.

4) Asignar roles (toAdd)
- Para cada roleName en toAdd:
  - obtener roleId
  - POST /api/v1/admin/assignments/users/{userId}/roles/{roleId}
  - Opcional: añadir `?assignedBy={adminUserId}` si quieres registrar quién asignó
- El backend es idempotente en creación: verifica existencia antes de insertar.

5) Remover roles (toRemove)
- Para cada roleName en toRemove:
  - obtener roleId
  - DELETE /api/v1/admin/assignments/users/{userId}/roles/{roleId}

6) Verificación final
- Volver a GET /api/v1/admin/users/{id} y comparar roles con desired; reportar éxito o error.

Estrategias de ejecución y comportamiento
- Paralelismo: Puedes ejecutar las llamadas POST/DELETE en paralelo (Promise.all) para velocidad, pero considera límites del servidor y orden (no hay dependencia entre las operaciones).
- Atomicidad: No existe operación atómica. Si necesitas garantízala, debes cambiar backend (ver sección "Cambiar backend").
- Reintentos: implementar 2-3 reintentos exponenciales para errores de red (5xx y timeouts). No reintentes en 4xx (salvo 429 rate limit — ahí backoff).
- Consistencia eventual: informar al usuario que el cambio puede tardar unos segundos en propagarse.

Manejo de errores y mensajes al usuario
- 401/403: token inválido o sin permisos → redirigir a login o mostrar mensaje de falta de permisos.
- 404 en role resolution: rol inexistente → mostrar error específico ("El rol X no existe").
- 409/422/5xx: mostrar mensaje amigable y permitir reintento.
- Registro: guardar logs de la secuencia (qué roles se intentaron asignar/borrar y respuestas del servidor).

Ejemplos prácticos

1) cURL: obtener usuario

```bash
curl -i -X GET 'http://127.0.0.1:8080/api/v1/admin/users/7' \
  -H 'Authorization: Bearer <TOKEN>' \
  -H 'Accept: application/json'
```

2) cURL: buscar role (obtener id)

```bash
curl -i -X GET 'http://127.0.0.1:8080/api/v1/admin/roles?search=ADMIN' \
  -H 'Authorization: Bearer <TOKEN>' \
  -H 'Accept: application/json'
```

3) cURL: asignar role (ejemplo roleId=2)

```bash
curl -i -X POST 'http://127.0.0.1:8080/api/v1/admin/assignments/users/7/roles/2' \
  -H 'Authorization: Bearer <TOKEN>' \
  -H 'Accept: application/json'
```

4) cURL: remover role (ejemplo roleId=3)

```bash
curl -i -X DELETE 'http://127.0.0.1:8080/api/v1/admin/assignments/users/7/roles/3' \
  -H 'Authorization: Bearer <TOKEN>' \
  -H 'Accept: application/json'
```

Snippet frontend (fetch / JS)

```js
// inputs: userId, desiredRoleNames (array de strings), token
async function syncUserRoles(userId, desiredRoleNames, token) {
  const headers = { 'Authorization': `Bearer ${token}`, 'Accept': 'application/json' };

  // 1) get current
  const userRes = await fetch(`/api/v1/admin/users/${userId}`, { headers });
  if (!userRes.ok) throw new Error('Failed to fetch user: ' + userRes.status);
  const user = await userRes.json();
  const current = new Set(user.roles || []);

  // 2) desired set
  const desired = new Set(desiredRoleNames);
  const toAdd = [...desired].filter(r => !current.has(r));
  const toRemove = [...current].filter(r => !desired.has(r));

  // helper: resolve role id
  async function getRoleId(roleName) {
    const r = await fetch(`/api/v1/admin/roles?search=${encodeURIComponent(roleName)}`, { headers });
    if (!r.ok) throw new Error('Failed to fetch roles list');
    const page = await r.json();
    const list = page.content || page; // depender de la estructura
    const found = list.find(x => x.name && x.name.toLowerCase() === roleName.toLowerCase());
    return found ? found.id : null;
  }

  // 3) assign
  await Promise.all(toAdd.map(async roleName => {
    const roleId = await getRoleId(roleName);
    if (!roleId) throw new Error('Role not found: ' + roleName);
    const res = await fetch(`/api/v1/admin/assignments/users/${userId}/roles/${roleId}`, { method: 'POST', headers });
    if (!res.ok) throw new Error('Failed to assign ' + roleName + ': ' + res.status);
  }));

  // 4) remove
  await Promise.all(toRemove.map(async roleName => {
    const roleId = await getRoleId(roleName);
    if (!roleId) throw new Error('Role not found: ' + roleName);
    const res = await fetch(`/api/v1/admin/assignments/users/${userId}/roles/${roleId}`, { method: 'DELETE', headers });
    if (!res.ok) throw new Error('Failed to remove ' + roleName + ': ' + res.status);
  }));

  // 5) verify
  const verify = await fetch(`/api/v1/admin/users/${userId}`, { headers });
  const refreshed = await verify.json();
  return refreshed.roles || [];
}
```

Buenas prácticas UX
- Mostrar un resumen antes de ejecutar (roles a asignar/remover).
- Indicar progreso (spinner + lista de operaciones con estado).
- En caso de fallo parcial, permitir reintentar sólo las operaciones que fallaron.

Alternativa: cambiar backend (ventajas y coste)
- Cambiar `UsuarioUpdateRequest` para incluir `List<String> roles` y modificar `UserAdminService.update` para reconciliar roles (crear y borrar UserRole según venga en el request).
- Ventaja: una única operación PUT para actualizar usuario y roles (más simple para frontend y atómica desde la API perspective).
- Coste: tocar DTOs, servicio y agregar tests, además de validar autorización y auditoría.

Próximos pasos sugeridos
- Implementar la versión frontend (usar el snippet anterior), probar con un usuario real y token admin.
- Añadir cache de roles en frontend para evitar demasiadas búsquedas.
- Si necesitas atomicidad o soporte para bulk replace, pedir que implemente la pequeña modificación en backend (puedo proponer el patch y test unitarios).

Contacto técnico y notas
- Endpoints relevantes revisados en el backend actual: 
  - `AdminUserController` (GET/PUT/POST/DELETE /api/v1/admin/users)
  - `AssignmentController` (POST/DELETE /api/v1/admin/assignments/users/{userId}/roles/{roleId})
  - `RoleController` (GET /api/v1/admin/roles)

Fin del documento.
