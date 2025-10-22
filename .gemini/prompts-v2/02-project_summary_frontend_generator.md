# 📄 PROMPT 2: Frontend - Project Summary Completer

```markdown
# COMPLETADOR DE RESUMEN TÉCNICO - MÓDULO FRONTEND

## 1. CONFIGURACIÓN

**Contexto de Ejecución**: Este prompt se ejecuta en el repositorio `ioc-frontend`.

**Objetivo**: Leer el `project-summary.md` generado por el backend y completar todas las secciones específicas de frontend.

**Prerequisito**: El Backend Module debe haber generado el archivo base.

**Salida**: `@.gemini/project-summary.md` (versión completa y sincronizada)

---

## 2. MANDATO OPERATIVO (PARA LA IA)

**Tu Rol**: Frontend Lead con conocimiento profundo de arquitectura de UI.

**Tu Responsabilidad**:
1. Leer el archivo `project-summary.md` existente (generado por backend)
2. Identificar TODAS las secciones marcadas con `<!-- FRONTEND: completar -->`
3. Analizar el repositorio frontend
4. Completar las secciones faltantes
5. Validar consistencia con la información del backend
6. Actualizar metadatos de sincronización

---

## 3. PROTOCOLO DE EJECUCIÓN

### FASE 1: Validación de Pre-requisitos

#### Acción 1.1: Verificar Repositorio Correcto

```bash
# Ejecuta este script mental:

¿Existe package.json con "react" o "vue" o "angular"? → ✅ Frontend confirmado
¿Existe pom.xml o solo archivos Java? → ❌ ERROR: Estás en el repo backend
¿Existe src/ con estructura de frontend? → ✅ Continuar

# Resultado esperado:
MODO = "FRONTEND"
```

**Si estás en el repositorio incorrecto**:

```markdown
❌ ERROR: Repositorio Incorrecto

Este prompt está diseñado para ejecutarse en el repositorio FRONTEND (ioc-frontend).

Repositorio detectado: [ioc-backend / otro]

🔧 Acción requerida:
1. Navega al repositorio correcto: `cd ../ioc-frontend`
2. Vuelve a ejecutar este prompt

DETENER EJECUCIÓN
```

---

#### Acción 1.2: Verificar Existencia del Archivo Base

```bash
# Buscar archivo en:
RUTA_1: @.gemini/project-summary.md (mismo repo)
RUTA_2: ../@.gemini/project-summary.md (repo padre compartido)
RUTA_3: ../ioc-backend/@.gemini/project-summary.md (repo hermano)
```

**Si NO se encuentra el archivo**:

```markdown
⚠️ ARCHIVO BASE NO ENCONTRADO

No se encontró el archivo `project-summary.md` generado por el Backend Module.

Ubicaciones buscadas:
- @.gemini/project-summary.md
- ../@.gemini/project-summary.md  
- ../ioc-backend/@.gemini/project-summary.md

🔧 Opciones:

A) El backend aún no ha generado el documento
   → Ejecutar primero: "Backend - Project Summary Generator"
   
B) El archivo está en una ubicación diferente
   → Especifica la ruta: [Usuario debe proporcionar]
   
C) Quieres generar un documento frontend-only (sin integración)
   → Usar prompt alternativo (no recomendado para multi-repo)

¿Qué opción prefieres? (A/B/C)

DETENER HASTA RECIBIR RESPUESTA
```

---

#### Acción 1.3: Validar Estructura del Archivo Base

```bash
# Leer archivo encontrado y validar:
- ¿Contiene "# Resumen Técnico del Proyecto"? → ✅
- ¿Contiene "generated_by: \"Backend Module\""? → ✅
- ¿Contiene al menos 1 sección "<!-- FRONTEND: completar -->"? → ✅
- ¿El estado es "INCOMPLETE"? → ✅

# Si todas las validaciones pasan:
ARCHIVO_VALIDO = true
CONTINUAR_CON_ANALISIS
```

**Si el archivo ya está completo**:

```markdown
⚠️ DOCUMENTO YA COMPLETADO

El archivo `project-summary.md` no contiene secciones pendientes de frontend.

Metadata detectada:
- Status: [COMPLETE / INCOMPLETE]
- Última actualización: [fecha]
- Generado por: [Backend Module / Frontend Module / Full]

🔧 Opciones:

A) Regenerar secciones de frontend (sobrescribir)
   → Continuaré reemplazando las secciones frontend existentes
   
B) Verificar y actualizar solo si hay cambios
   → Compararé con el código actual y actualizaré solo si es necesario
   
C) Cancelar (documento ya está completo)
   → Detener ejecución

¿Qué opción prefieres? (A/B/C - Recomendado: B)

ESPERAR RESPUESTA
```

---

### FASE 2: Análisis del Frontend

#### Acción 2.1: Análisis de package.json

```json
// Extraer información crítica:
{
  "name": "[nombre-del-proyecto]",
  "version": "[version]",
  "scripts": {
    "dev": "[comando de desarrollo]",
    "build": "[comando de build]",
    "test": "[comando de tests]"
  },
  "dependencies": {
    "react": "[version]",          // O vue, angular, etc.
    "[otras-dependencias]": "[version]"
  },
  "devDependencies": {
    "[herramientas]": "[version]"
  }
}

// Identificar:
FRAMEWORK = [react | vue | angular | svelte]
VERSION_FRAMEWORK = [version]
BUILD_TOOL = [vite | webpack | turbopack | rollup]
PACKAGE_MANAGER = [detectar de lock files: package-lock.json → npm, pnpm-lock.yaml → pnpm, yarn.lock → yarn]
```

---

#### Acción 2.2: Categorización de Dependencias

Clasifica todas las dependencias en categorías:

```typescript
interface DependencyMap {
  // CORE
  framework: { name: string, version: string },
  language: 'TypeScript' | 'JavaScript',
  buildTool: { name: string, version: string },
  
  // ROUTING
  routing: Array<{ name: string, version: string }>,
  // Ejemplos: react-router-dom, vue-router, @angular/router
  
  // STATE MANAGEMENT
  state: Array<{ name: string, version: string }>,
  // Ejemplos: zustand, redux, @reduxjs/toolkit, pinia, @tanstack/react-query
  
  // HTTP CLIENT
  http: Array<{ name: string, version: string }>,
  // Ejemplos: axios, @tanstack/react-query, swr
  
  // UI/STYLING
  ui: Array<{ name: string, version: string }>,
  // Ejemplos: tailwindcss, @mui/material, antd, shadcn/ui
  
  // FORMS & VALIDATION
  forms: Array<{ name: string, version: string }>,
  // Ejemplos: react-hook-form, formik, zod, yup
  
  // UTILITIES
  utils: Array<{ name: string, version: string }>,
  // Ejemplos: date-fns, dayjs, lodash, clsx
  
  // NOTIFICATIONS/FEEDBACK
  notifications: Array<{ name: string, version: string }>,
  // Ejemplos: react-hot-toast, sonner, react-toastify
  
  // ICONS
  icons: Array<{ name: string, version: string }>,
  // Ejemplos: lucide-react, react-icons, @heroicons/react
  
  // TESTING
  testing: Array<{ name: string, version: string }>,
  // Ejemplos: vitest, jest, @testing-library/react, cypress, playwright
  
  // OTROS
  other: Array<{ name: string, version: string, purpose: string }>
}
```

---

#### Acción 2.3: Análisis de Estructura de Directorios

```bash
# Escanear src/ y mapear estructura:

src/
├── components/     # Componentes reutilizables
├── pages/          # Componentes de página (si router basado en archivos)
├── views/          # Vistas/páginas (si router manual)
├── layouts/        # Layouts compartidos
├── hooks/          # Custom hooks
├── services/       # Servicios (API clients, etc.)
├── stores/         # Estado global (zustand, redux, pinia)
├── context/        # React Context providers
├── types/          # TypeScript types/interfaces
├── interfaces/     # Interfaces (alternativa a types/)
├── utils/          # Utilidades
├── helpers/        # Helpers (similar a utils)
├── lib/            # Configuraciones de librerías
├── constants/      # Constantes
├── config/         # Configuraciones
├── assets/         # Recursos estáticos
├── styles/         # Estilos globales
├── api/            # Endpoints API
└── [otros directorios detectados]

# Contar archivos por directorio:
COMPONENTS_COUNT = [número de archivos en components/]
PAGES_COUNT = [número de archivos en pages/ o views/]
HOOKS_COUNT = [número de archivos en hooks/]
```

---

#### Acción 2.4: Detección de Features Implementadas

```typescript
// Analizar rutas y componentes principales:

// OPCIÓN A: Router basado en configuración (react-router-dom)
// Buscar archivo de rutas (routes.tsx, App.tsx, router.tsx)
import { Route } from 'react-router-dom';

// Extraer rutas:
const routes = [
  { path: '/login', component: 'Login' },
  { path: '/dashboard', component: 'Dashboard' },
  { path: '/admin/*', component: 'AdminLayout' },
  // etc.
];

// OPCIÓN B: Router basado en archivos (Next.js, Remix)
// Escanear directorio pages/ o app/

// OPCIÓN C: Vue Router
// Buscar router/index.ts o router.ts

// Generar lista de features:
FEATURES_DETECTADAS = [
  'Autenticación (Login/Signup)',
  'Dashboard de Analytics',
  'Panel de Administración',
  'Ingesta de Datos (ETL)',
  // etc.
]
```

---

#### Acción 2.5: Análisis de Integración con Backend

```typescript
// Buscar archivos de configuración de API:
// - src/services/api.ts
// - src/lib/axios.ts
// - src/config/api.ts
// - src/api/client.ts

// Extraer:
BASE_URL = [detectar de variable VITE_API_URL, REACT_APP_API_URL, etc.]
TIMEOUT = [detectar configuración de timeout]
INTERCEPTORS = [¿hay interceptores de autenticación?]

// Buscar llamadas a endpoints:
// Escanear archivos en services/ o api/
// Buscar patrones: axios.get(), fetch(), api.get(), etc.

ENDPOINTS_LLAMADOS = [
  { method: 'GET', path: '/api/v1/dashboards/:id', file: 'services/dashboard.ts' },
  { method: 'POST', path: '/api/v1/etl/upload', file: 'services/etl.ts' },
  // etc.
]
```

---

#### Acción 2.6: Análisis de Autenticación Frontend

```typescript
// Buscar implementación de auth:
// - src/services/auth.ts
// - src/hooks/useAuth.ts
// - src/context/AuthContext.tsx
// - src/stores/authStore.ts

// Detectar:
AUTH_PROVIDER = [Supabase Client | Custom | Auth0 | Firebase]
TOKEN_STORAGE = [localStorage | sessionStorage | cookie | memory]
PROTECTED_ROUTES = [¿hay HOC o middleware de protección?]

// Ejemplo con Supabase:
import { createClient } from '@supabase/supabase-js';
// Detectar configuración de Supabase
```

---

#### Acción 2.7: Análisis de Testing

```json
// Buscar configuración de tests:
// - vitest.config.ts
// - jest.config.js
// - cypress.config.ts
// - playwright.config.ts

// Detectar en package.json:
{
  "scripts": {
    "test": "[comando]",
    "test:unit": "[comando]",
    "test:e2e": "[comando]",
    "test:coverage": "[comando]"
  }
}

// Escanear directorios:
// - src/**/*.test.tsx
// - src/**/*.spec.tsx
// - tests/
// - __tests__/
// - e2e/
// - cypress/

TEST_FRAMEWORK = [vitest | jest | none]
TEST_COUNT = [número de archivos de test]
E2E_FRAMEWORK = [cypress | playwright | none]
```

---

#### Acción 2.8: Análisis de Build y Deploy

```typescript
// Archivos a analizar:
// - vite.config.ts / webpack.config.js
// - Dockerfile
// - vercel.json / netlify.toml
// - .github/workflows/*.yml

// Detectar:
BUILD_OUTPUT_DIR = [dist | build | out]
DEPLOY_PLATFORM = [Vercel | Netlify | AWS S3 | Custom]
CI_CD = [GitHub Actions | GitLab CI | None]

// Extraer de scripts package.json:
BUILD_COMMAND = [npm run build | pnpm build | etc.]
DEV_COMMAND = [npm run dev | etc.]
```

---

### FASE 3: Completar el Documento

#### Acción 3.1: Cargar Documento Existente

```typescript
// Pseudo-código del proceso:

const documentoOriginal = leerArchivo('@.gemini/project-summary.md');
const seccionesPendientes = extraerSeccionesConTag(
  documentoOriginal, 
  '<!-- FRONTEND: completar -->'
);

console.log(`Secciones a completar: ${seccionesPendientes.length}`);

// Ejemplo de estructura:
interface SeccionPendiente {
  titulo: string;           // Ej: "3.1. Frontend"
  ubicacion: number;        // Línea donde empieza
  placeholder: string;      // Texto del placeholder
  contenidoNuevo: string;  // A generar
}
```

---

#### Acción 3.2: Generar Contenido para Cada Sección

Completar las siguientes secciones (mantener TODO el contenido del backend intacto):

##### SECCIÓN 1.4: Estado Actual - Frontend

```markdown
**Frontend**:
- ✅ Autenticación (Login, Signup, Recuperación de contraseña)
- ✅ [Feature 1 detectada de rutas]
- ✅ [Feature 2 detectada de rutas]
- 🚧 [Feature en desarrollo - detectada de comentarios TODO o ramas]
- 📋 [Feature planificada - detectada de issues o backlog]

**Componentes Implementados**: [X] componentes reutilizables, [Y] páginas

**Integración con Backend**:
- ✅ Cliente API configurado
- ✅ Autenticación JWT implementada
- ✅ [X] endpoints integrados
- ⏳ [Y] endpoints pendientes (mock data)
```

##### SECCIÓN 2.2: Decisiones Arquitectónicas - Frontend

```markdown
| Decisión | Tecnología Elegida | Razón |
|----------|-------------------|-------|
| **Frontend Framework** | [React 19] | [Detectado: Ecosistema maduro, hooks avanzados] |
| **Lenguaje** | [TypeScript 5.x] | [Type safety, mejor DX] |
| **Build Tool** | [Vite 5.x] | [HMR rápido, configuración simple] |
| **UI Framework** | [Tailwind CSS] | [Utility-first, customizable] |
| **State Management** | [Zustand / Redux / etc.] | [Detectado: Simple, menos boilerplate] |
| **Routing** | [React Router v6] | [Estándar de la industria] |
| **HTTP Client** | [Axios / Fetch + TanStack Query] | [Detectado: Cache automático, revalidación] |
```

##### SECCIÓN 3.1: Frontend Stack Completo

```markdown
### 3.1. Frontend ✅

#### Lenguaje y Framework Core

| Componente | Tecnología | Versión |
|------------|------------|---------|
| **Lenguaje** | [TypeScript] | [DETECTADO de package.json] |
| **Framework** | [React] | [DETECTADO de package.json] |
| **Build Tool** | [Vite] | [DETECTADO de vite.config.ts] |
| **Package Manager** | [npm/pnpm/yarn] | [DETECTADO de lock files] |

#### Dependencias Principales (Extraídas de package.json)

| Categoría | Librería | Versión | Propósito |
|-----------|----------|---------|-----------|
| **Core** | react | [VERSION] | Framework UI |
| **Core** | react-dom | [VERSION] | Renderizado DOM |
| **Routing** | react-router-dom | [VERSION] | Navegación SPA |
| **HTTP** | axios | [VERSION] | Cliente HTTP |
| **Estado** | zustand / @reduxjs/toolkit | [VERSION] | Gestión de estado global |
| **UI Framework** | tailwindcss | [VERSION] | Utilidades CSS |
| **Componentes** | shadcn/ui / @mui/material | [VERSION] | Componentes UI |
| **Forms** | react-hook-form | [VERSION] | Manejo de formularios |
| **Validación** | zod / yup | [VERSION] | Schemas de validación |
| **Autenticación** | @supabase/supabase-js | [VERSION] | Cliente Supabase |
| **Notificaciones** | react-hot-toast / sonner | [VERSION] | Feedback al usuario |
| **Fechas** | date-fns / dayjs | [VERSION] | Manejo de fechas |
| **Icons** | lucide-react / react-icons | [VERSION] | Iconografía |
| **[Otros]** | [DETECTAR Y LISTAR] | - | - |

#### Testing (Si configurado)

| Componente | Herramienta | Versión | Propósito |
|------------|-------------|---------|-----------|
| **Test Runner** | vitest / jest | [VERSION] | Ejecución de tests |
| **Testing Library** | @testing-library/react | [VERSION] | Testing de componentes |
| **Testing Utils** | @testing-library/user-event | [VERSION] | Simulación de interacciones |
| **Mocking** | msw | [VERSION] | Mock de API calls |
| **E2E** | cypress / playwright | [VERSION] | Tests end-to-end |

#### Estructura de Directorios

<!-- GENERADO DEL ESCANEO DE src/ -->

```
src/
├── components/          # [X] componentes reutilizables
│   ├── ui/             # Componentes base (buttons, inputs, etc.)
│   ├── layout/         # Componentes de layout (Header, Sidebar, etc.)
│   └── [otros grupos]
├── pages/              # [Y] páginas/vistas
│   ├── Login.tsx
│   ├── Dashboard.tsx
│   └── [otras páginas detectadas]
├── layouts/            # Layouts compartidos
│   └── MainLayout.tsx
├── hooks/              # [Z] custom hooks
│   ├── useAuth.ts
│   └── [otros hooks]
├── services/           # Servicios de API
│   ├── api.ts         # Cliente HTTP base
│   ├── auth.service.ts
│   └── [otros servicios]
├── stores/             # Estado global ([Zustand/Redux/etc.])
│   └── authStore.ts
├── types/              # Tipos TypeScript
│   └── [tipos detectados]
├── utils/              # Utilidades
├── constants/          # Constantes
│   └── routes.ts
└── assets/             # Recursos estáticos

Total: [X] componentes, [Y] páginas, [Z] hooks, [W] servicios
```

#### Configuración de Routing

<!-- EXTRAER DE ARCHIVO DE RUTAS -->

```typescript
// Rutas principales detectadas:

/                           → Landing/Home
/login                      → Página de login
/signup                     → Registro de usuario
/dashboard                  → Dashboard principal
/admin/*                    → Rutas de administración
  /admin/etl                → Ingesta de datos
  /admin/analytics          → Dashboards de Metabase
/[otras rutas detectadas]   → [Propósito]

// Rutas protegidas: [DETECTAR mecanismo de protección]
- PrivateRoute component: [SI/NO]
- Route guard middleware: [SI/NO]
- Redirect to login: [SI/NO]
```
```

##### SECCIÓN 6.1: Variables de Entorno - Frontend

```markdown
### 6.1. Variables de Entorno (Frontend) ✅

**Archivos**: `.env.local`, `.env.example`

<!-- EXTRAER DE .env.example o archivos de configuración -->

```bash
# === API BACKEND ===
VITE_API_URL=[DETECTADO o http://localhost:8080]
VITE_API_TIMEOUT=[DETECTADO o 10000]

# === SUPABASE ===
VITE_SUPABASE_URL=[DETECTADO de código]
VITE_SUPABASE_ANON_KEY=[PLACEHOLDER - ver .env.example]

# === METABASE (si se usa desde frontend) ===
VITE_METABASE_URL=[DETECTADO si aplica]

# === FEATURE FLAGS (si aplica) ===
VITE_ENABLE_ANALYTICS=[true/false]
VITE_ENABLE_DEBUG=[true/false]

# === OTROS ===
VITE_APP_VERSION=[DETECTADO de package.json]
VITE_APP_NAME=[DETECTADO de package.json]

# === [OTRAS VARIABLES DETECTADAS] ===
[LISTAR TODAS LAS VARIABLES ENCONTRADAS]
```

**⚠️ Seguridad**: 
- Nunca commitear archivos `.env.local` con valores reales
- Usar `.env.example` como template
- Variables con prefijo `VITE_` son expuestas al cliente (no incluir secrets)
```

##### SECCIÓN 7.1: Deployment - Frontend

```markdown
### 7.1. Frontend ✅

**Plataforma**: [DETECTADO: Vercel / Netlify / AWS S3+CloudFront / Custom]

<!-- SI SE DETECTA VERCEL -->
**Configuración Vercel**:
- Archivo: `vercel.json` [SI EXISTE]
- Framework Preset: [DETECTADO o Auto-detect]
- Build Command: `[DETECTADO de vercel.json o package.json]`
- Output Directory: `[dist / build / out]`
- Install Command: `[npm install / pnpm install]`

**URLs**:
- **Producción**: [ESPECIFICAR - ej: https://ioc.cambiaso.com]
- **Staging**: [ESPECIFICAR - ej: https://staging-ioc.cambiaso.com]
- **Preview**: Automático en cada PR

<!-- SI SE DETECTA NETLIFY -->
**Configuración Netlify**:
- Archivo: `netlify.toml` [SI EXISTE]
- Build Command: `[DETECTADO]`
- Publish Directory: `[DETECTADO]`

<!-- SI SE DETECTA DOCKERFILE -->
**Configuración Docker**:
```dockerfile
# Dockerfile detectado
[MOSTRAR CONTENIDO RELEVANTE]
```

**Build Command**:
```bash
[npm run build / pnpm build / yarn build]

# Genera: [dist/ / build/ / out/]
```

**Deploy Process**:

<!-- SI SE DETECTA CI/CD EN .github/workflows/ -->
✅ **CI/CD Automático**:
- Pipeline: [GitHub Actions / GitLab CI]
- Archivo: `.github/workflows/[nombre].yml`
- Trigger: 
  - Push a `main` → Deploy a producción
  - Push a `staging` → Deploy a staging
  - Pull Request → Preview deployment

**Steps detectados**:
```yaml
[EXTRAER STEPS PRINCIPALES DEL WORKFLOW]

1. Checkout code
2. Setup Node.js [version]
3. Install dependencies
4. Run tests [SI EXISTE]
5. Build application
6. Deploy to [plataforma]
```

<!-- SI NO HAY CI/CD -->
⏳ **Deployment Manual**:
- Proceso: [PENDIENTE - Especificar proceso actual]
- Recomendación: Configurar CI/CD con GitHub Actions o similar
```

##### SECCIÓN 8.1: Testing - Frontend

```markdown
### 8.1. Frontend ✅

**Framework**: [Vitest / Jest / Ninguno]

<!-- SI HAY TESTS CONFIGURADOS -->
**Tipos de Tests Implementados**:

```
[ESCANEAR src/**/*.test.* y src/**/*.spec.*]

✅ **Unit Tests**: [X] archivos
   - Componentes: [Y] tests
   - Hooks: [Z] tests
   - Utilities: [W] tests
   - Ubicación: src/**/*.test.tsx
   
✅ **Integration Tests**: [X] archivos
   - Flujos de usuario
   - Integración con API (mocked)
   - Ubicación: src/__tests__/integration/
   
[SI EXISTE E2E]:
✅ **E2E Tests**: [X] archivos
   - Framework: [Cypress / Playwright]
   - Ubicación: e2e/ o cypress/
   - Tests: [Listar principales]
```

**Configuración**:

```typescript
// vitest.config.ts (si existe)
[MOSTRAR CONFIGURACIÓN RELEVANTE]

// Setup files detectados:
- [src/setupTests.ts]
- [src/test/setup.ts]
```

**Coverage**:

<!-- DETECTAR DE package.json o vitest.config.ts -->
```
✅ Coverage configurado
   - Objetivo: [80% / 70% / No especificado]
   - Exclusiones: [Detectar de config]
   - Reporte: [html / lcov / text]
```

**Comandos**:

```bash
# Ejecutar tests
[DETECTAR de package.json scripts]
npm run test              # Modo watch
npm run test:run          # Single run
npm run test:coverage     # Con coverage
npm run test:e2e          # E2E (si existe)

# Ubicación de reportes:
- Coverage: coverage/index.html
- Test results: [DETECTAR]
```

<!-- SI NO HAY TESTS -->
⚠️ **Testing no configurado**:
- Recomendación: Implementar Vitest + Testing Library
- Prioridad: Alta para features críticas (autenticación, formularios)
```

##### SECCIÓN 10.2: Documentación - Frontend

```markdown
### 10.2. Documentación de Frontend ✅

**Repositorio**: `ioc-frontend/`

```
ioc-frontend/
├── README.md                    # Instrucciones de setup
├── @.gemini/
│   ├── project-summary.md      # Este archivo (sincronizado)
│   ├── blueprints/
│   │   ├── ftv-*.md           # Fichas Técnicas de Vista
│   │   └── [otros blueprints]
│   └── sprints/
│       └── [documentación de sprints]
├── docs/                       # [SI EXISTE]
│   └── [documentación adicional]
```

**Storybook**: [DETECTAR]

<!-- SI HAY STORYBOOK -->
```
✅ Storybook configurado
   - Versión: [DETECTADO de package.json]
   - URL local: http://localhost:6006
   - Historias: [X] componentes documentados
   - Comando: npm run storybook
```

**Design System**:

<!-- BUSCAR REFERENCIAS A FIGMA, etc. -->
```
[SI SE DETECTA EN README O COMENTARIOS]:
✅ Figma: [URL]
✅ Tokens de diseño: [src/styles/tokens.css / similar]

[SI NO]:
⏳ Pendiente: Documentar design system
```

**Guías de Desarrollo**:

<!-- BUSCAR ARCHIVOS CONTRIBUTING.md, CONVENTIONS.md, etc. -->
```
[SI EXISTEN]:
- CONTRIBUTING.md: Guía para contribuir
- CONVENTIONS.md: Convenciones de código
- ARCHITECTURE.md: Decisiones arquitectónicas

[SI NO]:
⏳ Recomendación: Crear guías de desarrollo
```
```

---

#### Acción 3.3: Validar Consistencia con Backend

```typescript
// Verificar que la información sea consistente:

// VALIDACIÓN 1: Endpoints llamados vs Endpoints documentados por backend
const endpointsBackend = extraerDeSeccion(documento, '4.2. Endpoints de Negocio');
const endpointsFrontend = ENDPOINTS_LLAMADOS; // De análisis frontend

const endpointsSinImplementar = endpointsFrontend.filter(
  fe => !endpointsBackend.some(be => be.path === fe.path && be.method === fe.method)
);

if (endpointsSinImplementar.length > 0) {
  AGREGAR_ADVERTENCIA(`
    ⚠️ INCONSISTENCIA DETECTADA
    
    El frontend está llamando a endpoints que no están documentados en el backend:
    ${endpointsSinImplementar.map(e => `- ${e.method} ${e.path}`).join('\n')}
    
    Acción recomendada:
    - Verificar si el backend los implementó y no documentó
    - O remover las llamadas del frontend (código muerto)
  `);
}

// VALIDACIÓN 2: URLs base
const apiUrlFrontend = [VITE_API_URL detectado];
const apiUrlBackend = extraerDeSeccion(documento, '4.2. Endpoints de Negocio');

if (apiUrlFrontend !== apiUrlBackend) {
  AGREGAR_NOTA(`
    📝 NOTA: Diferencia en URL base
    
    Backend documenta: ${apiUrlBackend}
    Frontend configurado: ${apiUrlFrontend}
    
    Esto es normal si están en diferentes ambientes (dev/prod).
  `);
}

// VALIDACIÓN 3: Versión de autenticación
const authBackend = extraerDeSeccion(documento, '5.1. Autenticación');
const authFrontend = AUTH_PROVIDER; // Detectado en frontend

if (!authFrontend.includes('supabase') && authBackend.includes('Supabase')) {
  AGREGAR_ADVERTENCIA(`
    ⚠️ INCONSISTENCIA: Backend usa Supabase pero frontend no tiene cliente configurado
  `);
}
```

---

#### Acción 3.4: Generar Documento Final

```typescript
// Proceso de merge:

1. Leer documento original línea por línea
2. Cuando encuentres "<!-- FRONTEND: completar -->":
   a. Identificar la sección
   b. Reemplazar el placeholder con el contenido generado
   c. Mantener formato y estructura
3. Actualizar sección de Metadata (al final)
4. Actualizar sección de Changelog
5. Escribir archivo

// Pseudo-código:
let documentoFinal = documentoOriginal;

seccionesPendientes.forEach(seccion => {
  const contenidoNuevo = generarContenido(seccion.titulo);
  documentoFinal = reemplazarSeccion(
    documentoFinal,
    seccion.placeholder,
    contenidoNuevo
  );
});

// Actualizar metadata
documentoFinal = actualizarMetadata(documentoFinal, {
  generated_by: "Backend + Frontend Modules",
  status: "COMPLETE",
  last_updated: new Date().toISOString(),
  frontend_version: "1.0",
  backend_version: extraerVersion(documentoOriginal)
});

// Actualizar changelog
documentoFinal = agregarEntradaChangelog(documentoFinal, {
  version: "1.0-FULL",
  fecha: new Date().toISOString(),
  autor: "Frontend Module (IA)",
  cambios: "Completar secciones de frontend"
});
```

---

#### Acción 3.5: Actualizar Metadata de Sincronización

```markdown
## METADATA PARA SINCRONIZACIÓN

```yaml
# NO EDITAR - Usado para sincronización automática
generated_by: "Backend + Frontend Modules"
source_repos: 
  - "ioc-backend"
  - "ioc-frontend"
version: "1.0-FULL"
status: "COMPLETE"
backend_analyzed: "[ISO 8601 de cuando se analizó backend]"
frontend_analyzed: "[ISO 8601 actual]"
last_updated: "[ISO 8601 actual]"
checksums:
  backend: "[HASH del código backend analizado - opcional]"
  frontend: "[HASH del código frontend analizado - opcional]"
```
```

---

### FASE 4: Validación y Reporte

#### Acción 4.1: Checklist de Validación

```markdown
## ✅ Checklist de Validación Frontend

### Completitud
- [ ] Todas las secciones "<!-- FRONTEND: completar -->" fueron rellenadas
- [ ] Sección 1.4 (Estado Actual - Frontend) está completa
- [ ] Sección 3.1 (Frontend Stack) está completa
- [ ] Sección 6.1 (Variables de Entorno Frontend) está completa
- [ ] Sección 7.1 (Deployment Frontend) está completa
- [ ] Sección 8.1 (Testing Frontend) está completa
- [ ] Sección 10.2 (Documentación Frontend) está completa

### Consistencia
- [ ] Endpoints llamados por frontend coinciden con los documentados por backend
- [ ] URLs base son consistentes (o explicada la diferencia)
- [ ] Mecanismo de autenticación es compatible entre frontend y backend
- [ ] No hay contradicciones entre secciones de backend y frontend

### Calidad
- [ ] Todas las dependencias principales están listadas
- [ ] Versiones son correctas (verificadas de package.json)
- [ ] Estructura de directorios es precisa
- [ ] Comandos de build/test son correctos

### Metadata
- [ ] Status actualizado a "COMPLETE"
- [ ] Changelog incluye entrada de esta actualización
- [ ] Metadata de sincronización está actualizada
```

---

#### Acción 4.2: Generar Reporte de Cambios

```markdown
## 📋 REPORTE DE ACTUALIZACIÓN

**Documento**: `@.gemini/project-summary.md`  
**Fecha**: [ISO 8601]  
**Módulo**: Frontend Completer

---

### Secciones Completadas

✅ **Sección 1.4 - Estado Actual (Frontend)**
   - Agregadas: [X] features implementadas
   - Agregadas: [Y] features en desarrollo
   - Total componentes: [Z]

✅ **Sección 2.2 - Decisiones Arquitectónicas (Frontend)**
   - Agregadas: [X] decisiones técnicas

✅ **Sección 3.1 - Frontend Stack**
   - Listadas: [X] dependencias principales
   - Categorías: [Y] categorías de librerías
   - Estructura de directorios: [Z] directorios documentados

✅ **Sección 6.1 - Variables de Entorno**
   - Agregadas: [X] variables de entorno

✅ **Sección 7.1 - Deployment**
   - Plataforma: [Detectada]
   - CI/CD: [Detectado/No detectado]

✅ **Sección 8.1 - Testing**
   - Tests detectados: [X] archivos
   - Framework: [Detectado]

✅ **Sección 10.2 - Documentación**
   - Archivos documentados: [X]

---

### Estadísticas del Análisis

**Código Frontend Analizado**:
- Total archivos escaneados: [X]
- Componentes React: [Y]
- Custom hooks: [Z]
- Servicios API: [W]
- Tests: [V]

**Dependencias**:
- Dependencias de producción: [X]
- Dependencias de desarrollo: [Y]
- Total: [X+Y]

---

### Advertencias y Recomendaciones

[SI HAY INCONSISTENCIAS]:
⚠️ **Inconsistencias Detectadas**:
- [Listar inconsistencias]

[SI FALTAN TESTS]:
⚠️ **Testing**:
- No se detectaron tests configurados
- Recomendación: Implementar testing con Vitest + Testing Library

[SI NO HAY CI/CD]:
⚠️ **Deployment**:
- No se detectó pipeline de CI/CD
- Recomendación: Configurar GitHub Actions para deploy automático

[OTRAS RECOMENDACIONES]:
💡 **Mejoras Sugeridas**:
- [Listar mejoras basadas en análisis]

---

### Próximos Pasos

1. ✅ Revisar el documento actualizado
2. ✅ Validar que la información técnica es correcta
3. ✅ Completar secciones marcadas como [PENDIENTE] (si las hay)
4. ✅ Compartir con el equipo

---

**Documento generado automáticamente por**: Frontend - Project Summary Completer v1  
**Repositorio analizado**: ioc-frontend  
**Fecha de análisis**: [ISO 8601]
```

---

## 4. FORMATO DE SALIDA FINAL

El documento final debe:

1. ✅ Mantener TODA la estructura y contenido del backend
2. ✅ Reemplazar SOLO los placeholders "<!-- FRONTEND: completar -->"
3. ✅ Actualizar metadata de sincronización
4. ✅ Agregar entrada en Changelog
5. ✅ Incluir reporte de advertencias si hay inconsistencias

**Estructura del archivo final**:

```markdown
# Resumen Técnico del Proyecto: [NOMBRE]

> **Generado por**: Backend + Frontend Modules  
> **Versión**: 1.0-FULL  
> **Estado**: ✅ COMPLETE

[... TODO EL CONTENIDO COMBINADO ...]

---

## METADATA PARA SINCRONIZACIÓN

```yaml
generated_by: "Backend + Frontend Modules"
source_repos: ["ioc-backend", "ioc-frontend"]
version: "1.0-FULL"
status: "COMPLETE"
last_updated: "[ISO 8601]"
```

---

## ⚠️ ADVERTENCIAS Y RECOMENDACIONES (Si aplica)

[SI HAY ADVERTENCIAS, MOSTRARLAS AQUÍ]
[SI NO, OMITIR ESTA SECCIÓN]

---

**Documento generado automáticamente por**: Project Summary Generator (Multi-Repo)  
**Última sincronización completa**: [ISO 8601]
```

---

## 5. MANEJO DE ERRORES

### Error 1: No se puede encontrar el archivo base

```markdown
❌ ERROR CRÍTICO: Archivo base no encontrado

Intenté leer el archivo `project-summary.md` en:
- @.gemini/project-summary.md
- ../@.gemini/project-summary.md
- ../ioc-backend/@.gemini/project-summary.md

Ninguna de estas rutas contiene el archivo.

🔧 SOLUCIÓN:

Opción A: Generar archivo base primero
```bash
cd ../ioc-backend
gemini-cli < @.gemini/prompts/generate-project-summary-backend.md
```

Opción B: Especificar ruta manualmente
"El archivo está en: [PEGA LA RUTA AQUÍ]"

Opción C: Copiar archivo a ubicación esperada
```bash
cp /ruta/al/archivo @.gemini/project-summary.md
```

DETENER HASTA QUE SE RESUELVA
```

---

### Error 2: Archivo corrupto o formato inesperado

```markdown
❌ ERROR: Archivo con formato inesperado

El archivo existe pero no tiene el formato esperado.

Validaciones fallidas:
- [ ] No contiene "# Resumen Técnico del Proyecto"
- [ ] No contiene metadata de generación
- [ ] No parece ser generado por el Backend Module

🔧 SOLUCIÓN:

¿Es este el archivo correcto?
[MOSTRAR PRIMERAS 20 LÍNEAS DEL ARCHIVO]

Opciones:
A) Regenerar desde backend
B) Este no es el archivo correcto (especifica ruta correcta)
C) Continuar de todas formas (no recomendado)

ESPERAR RESPUESTA
```

---

### Error 3: Documento ya completo

```markdown
⚠️ ADVERTENCIA: Documento ya parece estar completo

Metadata detectada:
- Status: COMPLETE
- Última actualización: [FECHA]
- Generado por: Backend + Frontend Modules

No se encontraron secciones con "<!-- FRONTEND: completar -->"

🔧 OPCIONES:

A) Regenerar secciones de frontend (sobrescribir)
   → Útil si el código frontend cambió significativamente
   → Se reemplazarán todas las secciones frontend actuales

B) Verificar y actualizar solo si hay cambios
   → Compararé el código actual con lo documentado
   → Solo actualizaré si hay diferencias significativas

C) Cancelar (documento está actualizado)
   → No se harán cambios

¿Qué prefieres? (A/B/C)

ESPERAR RESPUESTA
```

---

## 6. OPCIONES AVANZADAS

### Opción A: Modo Comparación

Si el documento ya está completo y se elige opción B:

```typescript
// Comparar código actual vs documentado:

const cambiosDetectados = [];

// 1. Comparar dependencias
const depsActuales = extraerDePackageJson();
const depsDocumentadas = extraerDeSeccion(documento, '3.1. Frontend');

const nuevasDeps = depsActuales.filter(d => !depsDocumentadas.includes(d));
const depsEliminadas = depsDocumentadas.filter(d => !depsActuales.includes(d));

if (nuevasDeps.length > 0) {
  cambiosDetectados.push({
    seccion: '3.1',
    tipo: 'Nuevas dependencias',
    detalles: nuevasDeps
  });
}

// 2. Comparar estructura de directorios
// 3. Comparar rutas
// 4. Comparar variables de entorno
// etc.

// Generar reporte:
if (cambiosDetectados.length > 0) {
  MOSTRAR_REPORTE_CAMBIOS(cambiosDetectados);
  PREGUNTAR("¿Actualizar documento con estos cambios? (S/N)");
} else {
  MENSAJE("✅ Documento está actualizado. No se requieren cambios.");
  SALIR();
}
```

---

### Opción B: Modo Debug

```bash
# Activar modo debug para troubleshooting:
MODO_DEBUG = true

# Mostrar información detallada:
- Ruta exacta del archivo leído
- Número de líneas del archivo
- Secciones detectadas
- Placeholders encontrados
- Cada paso del proceso de merge
- Contenido antes/después de cada reemplazo
```

---

## 7. INSTRUCCIONES FINALES

**Archivo de salida**: 
- Sobrescribir: `@.gemini/project-summary.md` (mismo archivo leído)
- O crear en: `@.gemini/project-summary.md` (si se copió a frontend)

**Backup automático**:
```bash
# Antes de sobrescribir, crear backup:
cp @.gemini/project-summary.md @.gemini/project-summary.backup.md
```

**Validación final antes de escribir**:
```markdown
✅ PRE-FLIGHT CHECK

Antes de guardar el archivo, verifico:
- [ ] Todas las secciones frontend están completas
- [ ] No se eliminó contenido del backend
- [ ] Metadata está actualizada
- [ ] Changelog tiene nueva entrada
- [ ] No hay placeholders sin completar
- [ ] El formato markdown es válido

¿Todo correcto? Procediendo a guardar...

✅ ARCHIVO GUARDADO: @.gemini/project-summary.md
```

---

## 8. EJEMPLO DE EJECUCIÓN

```bash
# Paso 1: Usuario ejecuta el prompt en ioc-frontend
cd ~/projects/ioc-frontend
gemini-cli < @.gemini/prompts/complete-project-summary-frontend.md

# Paso 2: La IA ejecuta:
[IA] 🔍 Verificando que estoy en el repositorio correcto...
[IA] ✅ Repositorio frontend confirmado (package.json con React detectado)

[IA] 🔍 Buscando archivo base generado por backend...
[IA] ✅ Encontrado en: ../ioc-backend/@.gemini/project-summary.md

[IA] 🔍 Validando estructura del archivo...
[IA] ✅ Archivo válido (generado por Backend Module)

[IA] 📊 Analizando repositorio frontend...
[IA] ✅ Detectadas 47 dependencias
[IA] ✅ Detectados 23 componentes
[IA] ✅ Detectadas 8 páginas
[IA] ✅ Detectados 12 hooks personalizados

[IA] ✏️ Completando secciones pendientes...
[IA] ✅ Sección 1.4 - Estado Actual (Frontend)
[IA] ✅ Sección 3.1 - Frontend Stack
[IA] ✅ Sección 6.1 - Variables de Entorno
[IA] ✅ Sección 7.1 - Deployment
[IA] ✅ Sección 8.1 - Testing
[IA] ✅ Sección 10.2 - Documentación

[IA] 🔍 Validando consistencia con backend...
[IA] ⚠️  Advertencia: Frontend llama a GET /api/v1/stats no documentado en backend

[IA] 💾 Creando backup...
[IA] ✅ Backup guardado: @.gemini/project-summary.backup.md

[IA] 💾 Guardando documento final...
[IA] ✅ Archivo guardado: @.gemini/project-summary.md

[IA] 📋 Generando reporte...

---

✅ PROCESO COMPLETADO

Documento actualizado exitosamente.
Ver reporte detallado arriba.

Advertencias: 1
Recomendaciones: 3

Próximo paso: Revisar el archivo y validar la información.
```

---
