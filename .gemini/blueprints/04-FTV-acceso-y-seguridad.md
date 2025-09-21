## Metadatos de la Ficha Técnica
*   **ID:** `FTV-4/4`
*   **Vista:** `Acceso y Seguridad`
---

# Ficha Técnica de Vista: Acceso y Seguridad (v1.4)

## 1. Misión y Flujo de Usuario
*   **Misión de la Vista:** Proporcionar al administrador un control centralizado y seguro sobre el ciclo de vida de los usuarios y la granularidad de sus permisos, garantizando que el acceso a los datos cumpla con las políticas de la compañía.
*   **Flujo de Usuario Clave:**
    1.  El administrador navega a "Acceso y Seguridad" (`AccessSecurityPage`).
    2.  En la pestaña "Gestión de Usuarios", abre el `UserCreateModal` para crear un nuevo usuario.
    3.  Asigna un rol predefinido al nuevo usuario.
    4.  Navega a la pestaña "Gestión de Roles" para abrir el `PermissionsMatrixModal` y ajustar los permisos de dicho rol.

## 2. Arquitectura de Componentes
Esta vista se renderiza dentro del `AppLayout` y utilizará un componente de Pestañas (`Tabs`) para organizar sus dos sub-secciones.

### A. Componentes Reutilizados
*   **Del Proyecto (`@src/`):** `PageBreadcrumb`, `Table`, `Modal`, `Button`, `Input`, `Select`, `Checkbox`, `Dropdown`, `Alert`.

### B. Nuevos Componentes a Crear
#### B.1. Componente de Página (Orquestador)
*   **Nombre:** `AccessSecurityPage.tsx`
*   **Responsabilidad:** Orquestar las sub-vistas, gestionar el estado de la pestaña activa y los datos compartidos (lista de usuarios y roles).

#### B.2. Componentes de UI (Reutilizables)
*   **Nombre:** `UsersDataTable.tsx`
*   **Responsabilidad:** Mostrar una tabla paginada y con capacidad de búsqueda de todos los usuarios del sistema.
*   **Interfaz de Props (Props Interface):**
    ```typescript
    interface UsersDataTableProps {
      users: UserData[];
      currentUserEmail: string; // Para identificar al usuario actual
      onEditRole: (userId: string) => void;
      onDeleteUser: (userId: string) => void;
      "data-testid"?: string;
    }
    ```
*   **Nombre:** `UserCreateModal.tsx`
*   **Responsabilidad:** Un formulario modal para la creación de nuevos usuarios.
*   **Interfaz de Props (Props Interface):**
    ```typescript
    interface UserCreateModalProps {
      isOpen: boolean;
      onClose: () => void;
      roles: { value: string; label: string; }[];
      "data-testid"?: string;
    }
    ```
*   **Nombre:** `PermissionsMatrixModal.tsx`
*   **Responsabilidad:** Mostrar una matriz de checkboxes (permisos vs. rol) para una asignación de permisos visual.
*   **Interfaz de Props (Props Interface):**
    ```typescript
    interface PermissionsMatrixModalProps {
      isOpen: boolean;
      onClose: () => void;
      roleId: string;
      "data-testid"?: string;
    }
    ```

## 3. Estrategia de Estado y Efectos Secundarios
*   **Gestión de Estado:** El estado principal (`users`, `roles`, `loading`, `error`) se gestionará en `AccessSecurityPage.tsx`. Los modales gestionarán su propio estado de formulario interno.
*   **Efectos Secundarios (`useEffect`):** Un `useEffect` principal en `AccessSecurityPage.tsx` cargará la lista de usuarios y roles al montar la vista, manejando los estados de `loading` y `error`.

## 4. Copy Aprobado por Marca (Ejemplos)
*   **Título Principal de la Vista:** "Administración de Acceso y Seguridad"
*   **Título de Pestaña:** "Gestión de Usuarios", "Gestión de Roles y Permisos"
*   **Texto del Botón Principal:** "Crear Nuevo Usuario"
*   **Mensaje de Éxito:** "El usuario ha sido creado con éxito, manteniendo el compromiso de nuestra tradición."

## 5. Manejo de Casos Límite y Estados Vacíos
*   **Estado de Carga:** Mientras se cargan los datos de las tablas (`UsersDataTable`, `RolesTable`), se mostrarán `SkeletonLoaders`.
*   **Estado de Error:** Si la carga de datos falla, se mostrará un `Alert` de tipo `error` en la pestaña correspondiente.
*   **Estado Vacío (Sin Usuarios/Roles):** Si no hay usuarios o roles, la tabla respectiva mostrará un estado vacío con un mensaje claro y un botón para "Crear el Primer Usuario" o "Crear el Primer Rol".
*   **Prevención de Auto-Bloqueo:** La UI debe impedir que un administrador se elimine a sí mismo o cambie su propio rol a uno con menos privilegios. El `Dropdown` de acciones para el usuario actual debe tener estas opciones deshabilitadas.
*   **Confirmación de Eliminación:** La acción de eliminar un usuario (`onDeleteUser`) debe abrir un `ConfirmDeleteDialog` que pida al administrador que escriba el nombre del usuario para confirmar, previniendo eliminaciones accidentales.

## 6. Dependencias de Datos y Contratos API
*   **Endpoints Consumidos:**
    *   `GET /api/admin/users`: Obtiene la lista de usuarios.
    *   `POST /api/admin/users`: Crea un nuevo usuario.
    *   `PUT /api/admin/users/{userId}/role`: Actualiza el rol de un usuario.
    *   `DELETE /api/admin/users/{userId}`: Elimina un usuario.
    *   `GET /api/admin/roles`: Obtiene la lista de roles.
    *   `GET /api/admin/roles/{roleId}/permissions`: Obtiene los permisos de un rol.
    *   `PUT /api/admin/roles/{roleId}/permissions`: Actualiza los permisos de un rol.
*   **Contrato de Datos (Tipos Clave):**
    ```typescript
    // Para la tabla de usuarios
    type UserData = {
      id: string;
      firstName: string;
      lastName: string;
      email: string;
      role: string;
    };

    // Para la matriz de permisos
    type Permission = {
      id: string;
      description: string;
      isAssigned: boolean;
    };
    ```

## 7. Criterios de Aceptación Técnicos (CAT)
*   **CAT-1 (Carga):** Al entrar en la vista, las tablas deben mostrar un estado de `SkeletonLoader`.
*   **CAT-2 (Error de Carga):** Si la API de usuarios o roles falla, se debe mostrar un `Alert` de error.
*   **CAT-3 (Estado Vacío):** Si no hay usuarios, la tabla de usuarios debe mostrar el mensaje y botón de estado vacío.
*   **CAT-4 (Auto-Protección):** En la `UsersDataTable`, las opciones de "Eliminar" y "Editar Rol" deben estar deshabilitadas (`disabled`) en la fila correspondiente al `currentUserEmail`.
*   **CAT-5 (Confirmación Destructiva):** Al intentar eliminar un usuario, se debe mostrar un modal de confirmación que requiera una acción explícita (ej. escribir el nombre) antes de habilitar el botón de confirmación final.
