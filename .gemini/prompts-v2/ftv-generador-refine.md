# 🎯 Sí, Refine tiene soporte oficial para Supabase

## 📦 Paquete Oficial

```bash
npm install @refinedev/supabase @supabase/supabase-js
```

---

## ⚠️ IMPORTANTE: Tu Caso Específico

En tu proyecto tienes una arquitectura híbrida:

```
┌─────────────────────────────────────────────┐
│  Frontend (React + Refine)                  │
├─────────────────────────────────────────────┤
│  Backend REST API (Spring Boot)             │  ← Tu API custom
├─────────────────────────────────────────────┤
│  Supabase Auth (JWT)                        │  ← Solo autenticación
├─────────────────────────────────────────────┤
│  PostgreSQL en Supabase                     │  ← Solo base de datos
└─────────────────────────────────────────────┘
```

### ❌ NO uses `@refinedev/supabase` para Data Provider

**Razón**: Tu backend es Spring Boot con endpoints REST custom (`/api/v1/admin/users`, etc.), NO la API auto-generada de Supabase.

**Usa**: Data Provider REST genérico de Refine.

### ✅ SÍ usa `@refinedev/supabase` para Auth Provider

**Razón**: Sí usas Supabase Auth para autenticación JWT.

---

## 📋 Actualización del Prompt para el Agente IA

# 📋 Instrucciones para Generación de FTV - IOC-004 (Actualizado)

**Destinatario**: Agente IA Generador de Componentes  
**Proyecto**: User Role Management Frontend  
**Versión**: 1.1

---

## 🎯 Objetivo

Generar componentes frontend (FTV) que implementen operaciones CRUD utilizando una arquitectura específica de 3 capas:

```
Capa 1: Refine Core (solo hooks de lógica, SIN componentes visuales)
Capa 2: TailAdmin v2.0.1 (componentes UI puros)
Capa 3: React Hook Form + Zod (gestión de formularios y validación)
```

---

## 📦 Dependencias Requeridas

Instalar las **últimas versiones** de:

### Capa de Lógica (Refine Headless)
- `@refinedev/core`
- `@refinedev/react-router-v6`
- `@refinedev/react-hook-form`

### ⚡ NUEVO: Integración Supabase
- `@refinedev/supabase` (solo para Auth Provider)
- `@supabase/supabase-js`

### Capa de Formularios y Validación
- `react-hook-form`
- `zod`
- `@hookform/resolvers`

### Capa de HTTP
- `axios`

### Capa de Estilos
- `tailwindcss` v4.x
- `postcss`
- `autoprefixer`

**Comando de instalación**:
```bash
npm install @refinedev/core@latest \
            @refinedev/react-router-v6@latest \
            @refinedev/react-hook-form@latest \
            @refinedev/supabase@latest \
            @supabase/supabase-js@latest \
            react-hook-form@latest \
            zod@latest \
            @hookform/resolvers@latest \
            axios@latest

npm install -D tailwindcss@latest postcss@latest autoprefixer@latest
```

---

## 🏗️ Arquitectura Obligatoria

### Separación de Responsabilidades

**Refine Core** se encarga de:
- Gestión de estado de datos (useTable, useForm, useDelete, useOne)
- Comunicación con API REST de Spring Boot
- Paginación, filtrado, ordenamiento
- Mutaciones (crear, actualizar, eliminar)
- Gestión de caché y refetch automático

**Supabase (solo Auth)** se encarga de:
- Autenticación de usuarios (login, logout, session)
- Gestión de tokens JWT
- Verificación de permisos
- **NO se usa para operaciones CRUD** (eso lo hace Spring Boot)

**TailAdmin** se encarga de:
- Todos los componentes visuales (inputs, botones, tablas, tarjetas)
- Sistema de diseño completo (colores, tipografía, sombras, espaciado)
- Modo oscuro
- Responsive design

**React Hook Form + Zod** se encarga de:
- Gestión del estado de formularios
- Validación de datos con schemas tipados
- Manejo de errores de validación
- Integración entre Refine y los inputs visuales

---

## 🔧 Configuración Específica de Providers

### Data Provider: REST (NO Supabase)

**Usar**: Data Provider REST genérico de Refine que apunte a tu Spring Boot API.

**Razón**: Tu backend es Spring Boot custom con endpoints específicos, NO la API auto-generada de Supabase.

**Endpoints objetivo**:
- `POST /api/v1/admin/users`
- `GET /api/v1/admin/users`
- `PUT /api/v1/admin/users/{id}`
- etc.

### Auth Provider: Supabase

**Usar**: `@refinedev/supabase` para el Auth Provider.

**Razón**: Usas Supabase Auth para autenticación JWT.

**Operaciones**:
- Login con email/password
- Logout
- Verificación de sesión
- Obtención de token JWT
- Refresh de token

---

## ✅ Restricciones HACER

1. **Configurar DOS providers separados**:
   - **Data Provider**: REST genérico → Spring Boot API
   - **Auth Provider**: Supabase → Supabase Auth

2. **Usar SOLO hooks de Refine** para operaciones de datos:
   - `useTable` para listados con paginación
   - `useForm` (de @refinedev/react-hook-form) para crear/editar
   - `useDelete` para eliminaciones
   - `useOne` para obtener un registro individual

3. **Usar cliente Supabase SOLO para autenticación**:
   - `supabase.auth.signInWithPassword()`
   - `supabase.auth.signOut()`
   - `supabase.auth.getSession()`
   - **NO usar** `supabase.from('users')` (eso es bypass de Spring Boot)

4. **Usar SOLO componentes visuales de TailAdmin**:
   - Todas las clases CSS deben seguir el sistema de diseño TailAdmin
   - Respetar la paleta de colores exacta (brand-*, success-*, error-*, gray-*)
   - Usar las sombras predefinidas (shadow-theme-xs, shadow-theme-sm, etc.)
   - Implementar modo oscuro con prefijo `dark:`

5. **Validar TODOS los formularios con Zod**:
   - Crear schemas de validación tipados
   - Integrar con `zodResolver` de @hookform/resolvers
   - Mostrar mensajes de error en español

6. **Mantener tipado TypeScript estricto**:
   - Usar interfaces exactas del backend (ver backend-sync-brief)
   - No usar tipo `any`
   - Tipar todos los hooks de Refine con genéricos

7. **Implementar responsive design**:
   - Usar breakpoints de TailAdmin (sm:, md:, lg:, xl:)
   - Adaptar layouts para móvil y desktop

8. **Mostrar estados de carga**:
   - Spinners durante fetch de datos
   - Botones disabled durante submit
   - Feedback visual de operaciones

---

## ❌ Restricciones NO HACER

1. **NO usar Supabase para operaciones CRUD**:
   - ❌ `supabase.from('users').select()`
   - ❌ `supabase.from('users').insert()`
   - ✅ Usar hooks de Refine que llaman a Spring Boot

2. **NO instalar ni usar librerías de componentes UI**:
   - ❌ Material-UI (@mui/material)
   - ❌ Ant Design (antd)
   - ❌ Chakra UI
   - ❌ shadcn/ui
   - ❌ Radix UI primitives

3. **NO usar paquetes de Refine con UI integrada**:
   - ❌ @refinedev/antd
   - ❌ @refinedev/mui
   - ❌ @refinedev/chakra-ui

4. **NO hacer fetch manual de datos**:
   - ❌ useState + useEffect para cargar datos
   - ❌ axios/fetch directo en componentes
   - ❌ Gestión manual de loading/error states

5. **NO crear validaciones con condicionales manuales**:
   - ❌ if/else para validar campos
   - ❌ Regex directas sin Zod

6. **NO hardcodear URLs de API**:
   - ❌ `fetch('/api/v1/users')`
   - ✅ Usar el data provider de Refine con `resource`

7. **NO ignorar el modo oscuro**:
   - Toda clase visual debe tener su variante `dark:`

---

## 📁 Estructura de Archivos Esperada

```
src/
├── types/
│   └── [entidad].types.ts          # Interfaces del backend
├── schemas/
│   └── [entidad].schema.ts         # Schemas Zod de validación
├── pages/
│   └── [entidad]/
│       ├── list.tsx                # Listado con useTable
│       ├── create.tsx              # Formulario con useForm
│       └── edit.tsx                # Formulario con useForm + useOne
├── providers/
│   ├── dataProvider.ts             # REST API → Spring Boot
│   ├── authProvider.ts             # Supabase Auth
│   └── supabaseClient.ts           # Cliente Supabase configurado
└── utils/
    └── axios.ts                    # Axios con interceptors JWT
```

---

## 🔌 Configuración de Providers (Concepto)

### Data Provider (REST)
- Apunta a: `http://localhost:8080/api/v1` (Spring Boot)
- Añade header: `Authorization: Bearer {token}` (JWT de Supabase)
- Mapea recursos: `admin/users`, `admin/roles`, `admin/permissions`

### Auth Provider (Supabase)
- Usa: `@refinedev/supabase`
- Operaciones: login, logout, check, getIdentity
- Almacena: JWT token en localStorage o cookies
- Refresca: Token automáticamente

### Axios Instance
- Interceptor: Añade JWT token a cada request
- Interceptor: Redirige a login en 401
- Base URL: Variable de entorno

---

## 🎨 Sistema de Diseño a Respetar

### Referencia de Colores
- **Primarios**: brand-500, brand-600
- **Estados**: success-500, error-500, warning-500
- **Textos**: gray-800 (principal), gray-500 (secundario)
- **Bordes**: gray-200, gray-100
- **Fondos**: white, gray-50

### Referencia de Sombras
- **Elementos pequeños**: shadow-theme-xs
- **Tarjetas**: shadow-theme-sm, shadow-theme-md
- **Modales**: shadow-theme-xl
- **Focus states**: shadow-focus-ring

### Referencia de Tipografía
- **Pequeño**: text-theme-xs (12px)
- **Normal**: text-theme-sm o text-sm (14px)
- **Énfasis**: font-medium

Consultar documento "TailAdmin - Configuración de Diseño" para clases exactas.

---

## 📚 Documentos de Referencia

Para generar componentes correctamente, consultar:

1. **backend-sync-brief-IOC-004.md**: Contratos de API, tipos TypeScript, endpoints
2. **TailAdmin - Configuración de Diseño.md**: Tokens de diseño, clases CSS exactas
3. **Guía de Generación de Componentes FTV - IOC-004**: Patrones de código detallados

---

## 🎯 Criterios de Aceptación

Un componente FTV está correctamente generado si:

- ✅ Usa hooks de Refine para toda lógica de datos
- ✅ Usa Supabase SOLO para autenticación
- ✅ Las operaciones CRUD van a Spring Boot (NO a Supabase directamente)
- ✅ Usa clases CSS de TailAdmin (no inventadas)
- ✅ Tiene validación Zod en formularios
- ✅ Compila sin errores TypeScript
- ✅ Incluye modo oscuro funcional
- ✅ Es responsive (móvil + desktop)
- ✅ Muestra loading states
- ✅ Maneja errores con mensajes visuales
- ✅ NO tiene dependencias de librerías de componentes UI

---

## 🚫 Si Tienes Dudas

**Pregunta ANTES de generar** si:
- No está claro si usar Supabase o Spring Boot para algo
- No encuentras una clase CSS de TailAdmin para algo visual
- Necesitas un componente complejo (modals, dropdowns)
- No está claro cómo integrar un hook de Refine con TailAdmin

**NO improvises** con:
- Librerías externas de UI
- Clases CSS personalizadas fuera del sistema TailAdmin
- Lógica de datos fuera de Refine
- Llamadas directas a Supabase para CRUD (solo auth)

---

## 📊 Flujo de Datos Correcto

```
Usuario hace login
  → Supabase Auth (signInWithPassword)
  → Obtiene JWT token
  → Token se guarda en localStorage

Usuario lista usuarios
  → useTable de Refine
  → Data Provider REST
  → GET /api/v1/admin/users + header Authorization
  → Spring Boot valida JWT con Supabase JWKS
  → Devuelve datos

Usuario crea usuario
  → useForm de Refine
  → Data Provider REST
  → POST /api/v1/admin/users + header Authorization
  → Spring Boot valida JWT y crea en PostgreSQL
```

---

**Última actualización**: 2025-10-27  
**Versión**: 1.1 (Actualizado con Supabase Auth)

---

**Resumen Ejecutivo**: Usa Refine para la lógica, Supabase SOLO para Auth, Spring Boot para CRUD, TailAdmin para la vista, Zod para validación. No uses nada más.