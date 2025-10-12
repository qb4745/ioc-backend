--# 📊 Assessment de Implementación: Integración de Metabase (Frontend)

**Proyecto:** IOC Frontend - Inteligencia Operacional Cambiaso  
**Fecha:** 9 de Octubre, 2025  
**Versión:** 1.0  
**Estado:** ✅ IMPLEMENTATION COMPLETE - Mejoras Recomendadas

---

## 📋 Índice

1. [Resumen Ejecutivo](#resumen-ejecutivo)
2. [Análisis por Componente](#análisis-por-componente)
3. [Evaluación por Pilares](#evaluación-por-pilares)
4. [Issues Identificadas](#issues-identificadas)
5. [Fortalezas Destacadas](#fortalezas-destacadas)
6. [Plan de Mejoras](#plan-de-mejoras)
7. [Conclusiones](#conclusiones)

---

## 1. Resumen Ejecutivo

### 🎯 Estado General: **85% COMPLETADO** 

La implementación de la integración de Metabase en el frontend React está **funcionalmente completa** y cumple con los requisitos básicos establecidos en el blueprint. El sistema permite visualizar dashboards de forma segura con autenticación, manejo de errores y auto-refresco de tokens.

### 📊 Métricas de Calidad

| Aspecto | Puntaje | Estado |
|---------|---------|--------|
| **Completitud Funcional** | 100% | 🟢 Perfecto |
| **Seguridad** | 85% | 🟡 Bueno |
| **Resiliencia** | 90% | 🟢 Muy Bueno |
| **UX/UI** | 80% | 🟡 Bueno |
| **Código Limpio** | 85% | 🟡 Muy Bueno |
| **Testing** | 0% | 🔴 Crítico |
| **Documentación** | 70% | 🟡 Aceptable |
| **TypeScript** | 90% | 🟢 Muy Bueno |
| **PROMEDIO GENERAL** | **75%** | 🟡 **Production-Ready con Mejoras** |

### ✅ Logros Principales

1. ✅ **Componente Reutilizable Completo**
   - `DashboardEmbed.tsx` implementado con todas las props necesarias
   - Manejo de estados (loading, error, success)
   - Callbacks para eventos (onLoad, onError)

2. ✅ **Integración de Seguridad**
   - Autenticación con Supabase JWT
   - Token inyectado en headers de cada petición
   - Iframe con sandbox restrictivo

3. ✅ **Resiliencia Implementada**
   - Auto-refresco de token cada 8 minutos
   - Timeout de 15 segundos con AbortController
   - Manejo de errores HTTP específicos (403, 404, 503)

4. ✅ **Navegación Integrada**
   - Ruta `/dashboards` protegida en `App.tsx`
   - Enlace en sidebar con icono (`PieChartIcon`)
   - Página contenedora `DashboardsPage.tsx`

5. ✅ **TypeScript Bien Tipado**
   - Interfaces definidas en `types/dashboard.ts`
   - Props fuertemente tipadas
   - No hay errores de compilación (solo 1 warning menor)

### 🔴 Issues Críticas Identificadas

1. **NO HAY TESTS** - Severidad: CRÍTICA
   - Sin tests unitarios para `DashboardEmbed`
   - Sin tests de integración para `DashboardsPage`
   - Sin mocks de Supabase

2. **Configuración de API Endpoint** - Severidad: ALTA
   - URL hardcodeada `/api/v1/dashboards/${dashboardId}`
   - Sin proxy configurado en `vite.config.ts`
   - Potencial problema en producción con CORS

3. **Simulación de Roles** - Severidad: MEDIA
   - Roles hardcodeados en `DashboardsPage`
   - Comentario indica que es temporal
   - No hay integración real con backend

---

## 2. Análisis por Componente

### 2.1 `DashboardEmbed.tsx` ✅ 90/100

**Ubicación**: `src/components/DashboardEmbed.tsx`

#### ✅ Fortalezas

1. **Arquitectura Sólida**
   ```typescript
   // Separación clara de responsabilidades
   - fetchDashboardUrl: Lógica de petición
   - useEffect: Orquestación y cleanup
   - Renderizado condicional: UI por estado
   ```

2. **Manejo de Errores Robusto**
   ```typescript
   // Mensajes personalizados por código HTTP
   if (response.status === 403) {
     errorMessage = 'You do not have permission...';
   } else if (response.status === 404) {
     errorMessage = 'Dashboard not found...';
   } else if (response.status === 503) {
     errorMessage = 'Service temporarily unavailable...';
   }
   ```

3. **Seguridad del Iframe**
   ```typescript
   <iframe
     sandbox="allow-scripts allow-same-origin allow-forms allow-popups"
     // Sandbox restrictivo: solo permite lo necesario
   />
   ```

4. **Auto-Refresco Implementado**
   ```typescript
   // Refresca token cada 8 minutos (expira en 10)
   refreshInterval = setInterval(() => {
     void fetchDashboardUrl();
   }, 8 * 60 * 1000);
   ```

5. **Cleanup Correcto**
   ```typescript
   return () => {
     isMounted = false;
     if (refreshInterval) clearInterval(refreshInterval);
     if (abortControllerRef.current) abortControllerRef.current.abort();
   };
   ```

#### ⚠️ Issues Identificadas

1. **Falta Prop `action` en Alert** (Severidad: BAJA)
   ```typescript
   // El blueprint especifica un botón "Retry"
   <Alert 
     variant="error" 
     title="Error Loading Dashboard" 
     message={error}
     // FALTANTE: action={{ label: 'Retry', onClick: fetchDashboardUrl }}
   />
   ```
   
   **Impacto**: El usuario no puede reintentar si falla la carga.

2. **No Hay Logging de Eventos** (Severidad: BAJA)
   ```typescript
   // Solo hay console.log para refresh
   console.log('Refreshing dashboard token...');
   
   // FALTA: Logging estructurado para:
   // - Inicio de carga
   // - Éxito de carga
   // - Errores (más allá del callback)
   ```

3. **URL del Backend Hardcodeada** (Severidad: ALTA)
   ```typescript
   const response = await fetch(`/api/v1/dashboards/${dashboardId}`, {
     // Sin variable de entorno o configuración centralizada
   ```
   
   **Riesgo**: En producción puede fallar si el backend está en otro dominio.

#### 📊 Desglose de Puntaje

- Arquitectura: **20/20** ✅
- Manejo de errores: **18/20** 🟡 (falta retry)
- Seguridad: **18/20** 🟡 (URL hardcodeada)
- Resiliencia: **20/20** ✅
- Limpieza de recursos: **10/10** ✅
- TypeScript: **10/10** ✅

**Total: 90/100** - Muy Bueno ✨

---

### 2.2 `DashboardsPage.tsx` ✅ 75/100

**Ubicación**: `src/pages/DashboardsPage.tsx`

#### ✅ Fortalezas

1. **Integración de Layout**
   ```typescript
   <PageBreadcrumb pageTitle="Dashboards Analíticos" />
   // Usa componentes existentes del proyecto
   ```

2. **Callbacks Implementados**
   ```typescript
   const handleDashboardError = (error: Error) => {
     console.error('Dashboard error:', error);
     // TODO: Sentry
   };
   ```

3. **Renderizado Condicional por Roles**
   ```typescript
   {(hasRole('ROLE_ADMIN') || hasRole('ROLE_MANAGER')) && (
     <DashboardEmbed dashboardId={5} />
   )}
   ```

4. **Mensaje de Acceso Denegado**
   ```typescript
   {userRoles.length === 0 && (
     <div className="text-center py-12">
       <p>No tienes acceso a ningún dashboard...</p>
     </div>
   )}
   ```

#### ⚠️ Issues Identificadas

1. **Roles Simulados (CRÍTICO para Producción)**
   ```typescript
   // 🚨 HARDCODEADO - NO PRODUCTION-READY
   const userRoles = user ? ['ROLE_ADMIN', 'ROLE_USER'] : [];
   ```
   
   **Problema**: Todos los usuarios autenticados tienen ADMIN y USER.
   
   **Solución Esperada**:
   ```typescript
   // Extraer del JWT o de user metadata
   const userRoles = user?.app_metadata?.roles || [];
   ```

2. **IDs de Dashboard Hardcodeados** (Severidad: MEDIA)
   ```typescript
   <DashboardEmbed dashboardId={5} />
   <DashboardEmbed dashboardId={6} />
   ```
   
   **Riesgo**: Si los IDs cambian en backend, hay que modificar código.
   
   **Mejora**: Cargar desde configuración o endpoint `/api/v1/dashboards`.

3. **No Hay Estados de Carga Global** (Severidad: BAJA)
   ```typescript
   // Cada DashboardEmbed muestra su propio spinner
   // pero no hay loader de página mientras se inicializa
   ```

#### 📊 Desglose de Puntaje

- Integración: **20/20** ✅
- Manejo de roles: **10/25** 🔴 (simulados)
- UX/UI: **15/20** 🟡 (falta loader global)
- Callbacks: **15/15** ✅
- Código limpio: **15/20** 🟡 (hardcoded values)

**Total: 75/100** - Bueno pero necesita trabajo en roles 🔧

---

### 2.3 `types/dashboard.ts` ✅ 95/100

**Ubicación**: `src/types/dashboard.ts`

#### ✅ Fortalezas

1. **Interfaces Completas**
   ```typescript
   export interface DashboardEmbedProps {
     dashboardId: number;
     height?: string;
     onError?: (error: Error) => void;
     onLoad?: () => void;
     className?: string;
   }
   ```

2. **Documentación con Props Opcionales**
   - `height` tiene valor por defecto
   - Callbacks son opcionales
   - `className` permite extensión de estilos

3. **Tipado de Respuesta del Backend**
   ```typescript
   export interface DashboardUrlResponse {
     signedUrl: string;
     expiresInMinutes: number;
     dashboardId: number;
   }
   ```

#### ⚠️ Issues Identificadas

1. **Interfaz No Usada** (Severidad: TRIVIAL)
   ```typescript
   export interface DashboardError {
     // ⚠️ WARNING: Unused interface
   ```
   
   **Solución**: Eliminar o usar en manejo de errores.

#### 📊 Desglose de Puntaje

- Completitud: **40/40** ✅
- Tipado: **40/40** ✅
- Documentación: **10/10** ✅
- Código limpio: **5/10** 🟡 (interfaz no usada)

**Total: 95/100** - Excelente ✨

---

### 2.4 Integración en `App.tsx` ✅ 100/100

**Ubicación**: `src/App.tsx`

#### ✅ Fortalezas

1. **Ruta Protegida Correctamente**
   ```typescript
   <Route element={<ProtectedRoute />}>
     <Route element={<AppLayout />}>
       <Route path="/dashboards" element={<DashboardsPage />} /> 
     </Route>
   </Route>
   ```

2. **Posición Lógica en la Jerarquía**
   - Después de `/account`
   - Antes de rutas de admin
   - Dentro de `AppLayout`

#### 📊 Desglose de Puntaje

- Estructura: **50/50** ✅
- Protección: **50/50** ✅

**Total: 100/100** - Perfecto ✨

---

### 2.5 Integración en `AppSidebar.tsx` ✅ 95/100

**Ubicación**: `src/layout/AppSidebar.tsx`

#### ✅ Fortalezas

1. **Icono Apropiado**
   ```typescript
   {
     icon: <PieChartIcon />,
     name: "Dashboards Analíticos",
     path: "/dashboards",
   }
   ```

2. **Orden Lógico en Menú**
   - Después de "Dashboard"
   - Antes de "Mi Cuenta"

3. **Activación Correcta**
   ```typescript
   const isActive = useCallback(
     (path: string) => {
       if (path === '/') return location.pathname === '/';
       return location.pathname.startsWith(path)
     },
     [location.pathname]
   );
   ```

#### ⚠️ Issues Identificadas

1. **No Hay Control de Acceso** (Severidad: MEDIA)
   ```typescript
   // El item se muestra a todos los usuarios autenticados
   // Debería ocultarse si el usuario no tiene roles apropiados
   ```

#### 📊 Desglose de Puntaje

- Integración: **40/40** ✅
- UX: **40/40** ✅
- Control de acceso: **10/20** 🟡

**Total: 95/100** - Muy Bueno ✨

---

## 3. Evaluación por Pilares

### 3.1 🔒 Seguridad: 85/100

#### ✅ Controles Implementados

1. **Autenticación con Supabase**
   ```typescript
   const session = await supabase.auth.getSession();
   const token = session?.data?.session?.access_token;
   ```

2. **JWT en Headers**
   ```typescript
   headers: {
     'Authorization': `Bearer ${token}`,
     'Content-Type': 'application/json'
   }
   ```

3. **Sandbox Restrictivo**
   ```typescript
   sandbox="allow-scripts allow-same-origin allow-forms allow-popups"
   ```

4. **Validación de Token**
   ```typescript
   if (!token) {
     setError('No authentication token available');
     return;
   }
   ```

#### ⚠️ Observaciones de Seguridad

1. **URL del Backend No Validada** (Severidad: ALTA)
   ```typescript
   // PROBLEMA: No hay validación de origen
   const response = await fetch(`/api/v1/dashboards/${dashboardId}`, {
   ```
   
   **Riesgo**: Si el proxy falla, podría hacer peticiones a localhost en producción.

2. **Sin Validación de HTTPS** (Severidad: MEDIA)
   ```typescript
   // No hay verificación de que la signedUrl use HTTPS
   setIframeUrl(data.signedUrl);
   ```

3. **Console.error Expone Stack Traces** (Severidad: BAJA)
   ```typescript
   console.error('Dashboard fetch error:', err);
   // En producción, esto expone información sensible en la consola
   ```

#### 📊 Desglose de Puntaje

- Autenticación: **25/25** ✅
- Headers de seguridad: **20/20** ✅
- Sandbox: **15/15** ✅
- Validación de inputs: **10/15** 🟡
- Configuración: **10/20** 🟡 (URL hardcodeada)
- Logging seguro: **5/10** 🟡

**Total: 85/100** - Bueno ✨

---

### 3.2 🛡️ Resiliencia: 90/100

#### ✅ Patrones Implementados

1. **Timeout con AbortController**
   ```typescript
   const timeoutId = setTimeout(() => 
     abortControllerRef.current?.abort(), 15000
   );
   ```

2. **Cancelación de Peticiones Previas**
   ```typescript
   if (abortControllerRef.current) {
     abortControllerRef.current.abort();
   }
   ```

3. **Auto-Refresco de Token**
   ```typescript
   refreshInterval = setInterval(() => {
     void fetchDashboardUrl();
   }, 8 * 60 * 1000);
   ```

4. **Cleanup Completo**
   ```typescript
   return () => {
     isMounted = false;
     clearInterval(refreshInterval);
     abortControllerRef.current?.abort();
   };
   ```

5. **Manejo de Errores HTTP**
   ```typescript
   if (response.status === 403) { ... }
   else if (response.status === 404) { ... }
   else if (response.status === 503) { ... }
   ```

#### ⚠️ Issues Identificadas

1. **No Hay Retry Automático** (Severidad: MEDIA)
   ```typescript
   // Si falla por red, no reintenta automáticamente
   // El usuario debe refrescar manualmente la página
   ```

2. **No Hay Exponential Backoff** (Severidad: BAJA)
   ```typescript
   // El auto-refresh es cada 8 minutos fijo
   // Si Metabase está caído, seguirá intentando cada 8 min
   ```

#### 📊 Desglose de Puntaje

- Timeout: **20/20** ✅
- Cancelación: **15/15** ✅
- Auto-refresh: **20/20** ✅
- Cleanup: **15/15** ✅
- Retry: **10/20** 🟡 (manual, no automático)
- Error handling: **10/10** ✅

**Total: 90/100** - Muy Bueno ✨

---

### 3.3 ⚡ UX/UI: 80/100

#### ✅ Fortalezas

1. **Estados Visuales Claros**
   ```typescript
   if (loading) return <Spinner />;
   if (error) return <Alert variant="error" />;
   return <iframe />;
   ```

2. **Mensajes de Error Amigables**
   ```typescript
   errorMessage = 'You do not have permission to view this dashboard.';
   // vs
   errorMessage = 'HTTP 403 Forbidden';
   ```

3. **Estilos Responsivos**
   ```typescript
   <div className={`dashboard-embed-container ${className}`}>
   ```

4. **Dark Mode Support**
   ```typescript
   className="text-gray-600" // Usa sistema de colores del proyecto
   ```

#### ⚠️ Issues Identificadas

1. **Sin Botón de Retry** (Severidad: MEDIA)
   ```typescript
   // El Alert no tiene botón para reintentar
   // El usuario debe refrescar toda la página
   ```

2. **Sin Indicador de Refresh** (Severidad: BAJA)
   ```typescript
   // Cuando refresca el token cada 8 min, no hay feedback visual
   console.log('Refreshing dashboard token...');
   ```

3. **Sin Skeleton Loader** (Severidad: BAJA)
   ```typescript
   // Muestra spinner genérico en lugar de skeleton del dashboard
   ```

4. **Sin Mensaje de "No Dashboards"** (Severidad: TRIVIAL)
   ```typescript
   // Si un usuario con permisos no ve ningún dashboard
   // no hay mensaje explicativo
   ```

#### 📊 Desglose de Puntaje

- Estados visuales: **20/20** ✅
- Mensajes: **15/20** 🟡 (falta retry)
- Estilos: **20/20** ✅
- Feedback: **10/20** 🟡 (sin indicador de refresh)
- Accesibilidad: **15/20** 🟡 (sin aria-labels en iframe)

**Total: 80/100** - Bueno ✨

---

### 3.4 🧪 Testing: 0/100 🔴

#### ❌ NO HAY TESTS IMPLEMENTADOS

**Archivos Faltantes**:
- `src/components/DashboardEmbed.test.tsx` ❌
- `src/pages/DashboardsPage.test.tsx` ❌
- `src/test/mocks/supabase.ts` ❌

#### 📋 Tests Requeridos

1. **DashboardEmbed - Tests Unitarios**
   ```typescript
   describe('DashboardEmbed', () => {
     it('should show loading state initially');
     it('should fetch dashboard URL on mount');
     it('should display iframe when URL is loaded');
     it('should show error alert on 403');
     it('should show error alert on 404');
     it('should show error alert on timeout');
     it('should call onLoad callback when loaded');
     it('should call onError callback on error');
     it('should refresh token every 8 minutes');
     it('should cleanup interval on unmount');
     it('should abort pending requests on unmount');
   });
   ```

2. **DashboardsPage - Tests de Integración**
   ```typescript
   describe('DashboardsPage', () => {
     it('should render breadcrumb');
     it('should show dashboards for ADMIN');
     it('should show only operational dashboard for USER');
     it('should show no access message for guests');
     it('should handle dashboard errors gracefully');
   });
   ```

#### 📊 Desglose de Puntaje

- Tests unitarios: **0/50** 🔴
- Tests de integración: **0/30** 🔴
- Cobertura: **0/20** 🔴

**Total: 0/100** - CRÍTICO 🚨

---

### 3.5 📝 Código Limpio: 85/100

#### ✅ Fortalezas

1. **Separación de Responsabilidades**
   - Componente de presentación (`DashboardEmbed`)
   - Página contenedora (`DashboardsPage`)
   - Tipos en archivo separado

2. **Hooks Correctos**
   ```typescript
   const abortControllerRef = useRef<AbortController | null>(null);
   const fetchDashboardUrl = useCallback(async () => { ... }, [deps]);
   ```

3. **TypeScript Estricto**
   - Props bien tipadas
   - No hay `any` types
   - Interfaces exportadas

4. **Comentarios Útiles**
   ```typescript
   // Simulación de roles hasta que se implementen en el backend/JWT
   ```

#### ⚠️ Issues Identificadas

1. **Magic Numbers** (Severidad: BAJA)
   ```typescript
   setTimeout(() => ..., 15000); // ❌
   setInterval(() => ..., 8 * 60 * 1000); // ❌
   
   // MEJOR:
   const REQUEST_TIMEOUT_MS = 15000;
   const TOKEN_REFRESH_INTERVAL_MS = 8 * 60 * 1000;
   ```

2. **Callback sin Memoización** (Severidad: TRIVIAL)
   ```typescript
   const handleDashboardError = (error: Error) => { ... }
   // MEJOR:
   const handleDashboardError = useCallback((error: Error) => { ... }, []);
   ```

3. **Hardcoded Values** (Severidad: MEDIA)
   ```typescript
   const userRoles = user ? ['ROLE_ADMIN', 'ROLE_USER'] : [];
   <DashboardEmbed dashboardId={5} />
   ```

#### 📊 Desglose de Puntaje

- Estructura: **25/25** ✅
- Hooks: **20/25** 🟡 (callbacks sin memo)
- TypeScript: **20/20** ✅
- Constantes: **10/15** 🟡 (magic numbers)
- Hardcoded values: **10/15** 🟡

**Total: 85/100** - Muy Bueno ✨

---

### 3.6 🔧 Configuración: 60/100

#### ⚠️ Issues Identificadas

1. **Sin Proxy en Vite** (Severidad: ALTA)
   ```typescript
   // vite.config.ts NO TIENE:
   export default defineConfig({
     server: {
       proxy: {
         '/api': {
           target: 'http://localhost:8080',
           changeOrigin: true
         }
       }
     }
   });
   ```

2. **Sin Variables de Entorno** (Severidad: ALTA)
   ```typescript
   // .env.example FALTANTE:
   VITE_API_BASE_URL=http://localhost:8080
   VITE_METABASE_URL=http://localhost:3000
   ```

3. **Sin Configuración de Dashboard IDs** (Severidad: MEDIA)
   ```typescript
   // IDEAL:
   VITE_DASHBOARD_GERENCIAL_ID=5
   VITE_DASHBOARD_OPERACIONAL_ID=6
   ```

#### 📊 Desglose de Puntaje

- Proxy: **0/30** 🔴
- Variables de entorno: **10/40** 🟡 (solo Supabase)
- Documentación: **20/30** 🟡

**Total: 60/100** - Necesita Mejoras 🔧

---

## 4. Issues Identificadas

### 🔴 CRÍTICAS (Antes de Producción)

#### **ISSUE-FE-01**: NO HAY TESTS

**Severidad**: 🔴 CRÍTICA  
**Componente**: Todo el módulo de dashboards  
**Impacto**: No hay garantía de que el código funcione después de cambios

**Descripción**:
- No hay tests unitarios para `DashboardEmbed`
- No hay tests de integración para `DashboardsPage`
- No hay mocks de Supabase
- Coverage: 0%

**Solución Requerida**:
```bash
# Crear tests
touch src/components/DashboardEmbed.test.tsx
touch src/pages/DashboardsPage.test.tsx
touch src/test/mocks/supabase.ts
```

**Estimación**: 4 horas

---

#### **ISSUE-FE-02**: Configuración de Proxy y Variables de Entorno

**Severidad**: 🔴 ALTA  
**Componente**: `vite.config.ts`, `.env`  
**Impacto**: Fallas en producción por CORS y URLs hardcodeadas

**Descripción**:
```typescript
// PROBLEMA: URL hardcodeada
const response = await fetch(`/api/v1/dashboards/${dashboardId}`, {
```

**Solución Requerida**:

1. Añadir proxy en `vite.config.ts`:
```typescript
export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: process.env.VITE_API_BASE_URL || 'http://localhost:8080',
        changeOrigin: true,
        secure: false
      }
    }
  }
});
```

2. Crear `.env.example`:
```bash
# Backend API
VITE_API_BASE_URL=http://localhost:8080

# Supabase (ya existe)
VITE_SUPABASE_URL=...
VITE_SUPABASE_ANON_KEY=...

# Dashboard IDs
VITE_DASHBOARD_GERENCIAL_ID=5
VITE_DASHBOARD_OPERACIONAL_ID=6
```

3. Usar en código:
```typescript
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '';
const response = await fetch(`${API_BASE_URL}/api/v1/dashboards/${dashboardId}`, {
```

**Estimación**: 1 hora

---

### 🟡 IMPORTANTES (Primera Iteración Post-Launch)

#### **ISSUE-FE-03**: Roles Simulados

**Severidad**: 🟡 MEDIA  
**Componente**: `DashboardsPage.tsx`  
**Impacto**: Todos los usuarios autenticados ven todos los dashboards

**Descripción**:
```typescript
// 🚨 TEMPORAL - NO PRODUCTION-READY
const userRoles = user ? ['ROLE_ADMIN', 'ROLE_USER'] : [];
```

**Solución Requerida**:

1. Extraer roles del JWT de Supabase:
```typescript
const userRoles = useMemo(() => {
  if (!user) return [];
  return user.app_metadata?.roles || [];
}, [user]);
```

2. Configurar Supabase Auth Hook para incluir roles en JWT:
```sql
-- En Supabase SQL Editor
CREATE OR REPLACE FUNCTION public.custom_access_token_hook(event jsonb)
RETURNS jsonb
LANGUAGE plpgsql
AS $$
BEGIN
  event := jsonb_set(
    event,
    '{claims,app_metadata,roles}',
    COALESCE(
      (SELECT jsonb_agg(role_name) FROM user_roles WHERE user_id = (event->>'user_id')::uuid),
      '[]'::jsonb
    )
  );
  RETURN event;
END;
$$;
```

**Estimación**: 2 horas

---

#### **ISSUE-FE-04**: Falta Botón de Retry

**Severidad**: 🟡 MEDIA  
**Componente**: `DashboardEmbed.tsx`, `Alert.tsx`  
**Impacto**: UX pobre cuando hay errores

**Descripción**:
```typescript
// El Alert no tiene botón de retry
<Alert 
  variant="error" 
  title="Error Loading Dashboard" 
  message={error}
  // FALTANTE: action prop
/>
```

**Solución Requerida**:

1. Actualizar `Alert.tsx` para soportar `action` prop:
```typescript
interface AlertProps {
  variant: "success" | "error" | "warning" | "info";
  title: string;
  message: string;
  action?: {
    label: string;
    onClick: () => void;
  };
  // ...existing props
}
```

2. Usar en `DashboardEmbed.tsx`:
```typescript
<Alert 
  variant="error" 
  title="Error Loading Dashboard" 
  message={error}
  action={{
    label: 'Retry',
    onClick: fetchDashboardUrl
  }}
/>
```

**Estimación**: 1 hora

---

### 🟢 SPRINT 3: Mejoras (Backlog)

**Estimación Total**: 3 horas

| Task | Prioridad | Estimación | Asignado |
|------|-----------|-----------|----------|
| **FE-TASK-17**: Añadir indicador visual de refresh | P2 | 30min | - |
| **FE-TASK-18**: Añadir skeleton loader | P2 | 1h | - |
| **FE-TASK-19**: Añadir retry automático con exponential backoff | P2 | 1h | - |
| **FE-TASK-20**: Limpiar interfaz DashboardError no usada | P2 | 5min | - |
| **FE-TASK-21**: Añadir aria-labels para accesibilidad | P2 | 30min | - |

---

## 7. Conclusiones

### 7.1 ✅ Veredicto Final

**LA IMPLEMENTACIÓN ESTÁ FUNCIONALMENTE COMPLETA Y ES USABLE EN PRODUCCIÓN CON RESERVAS**

**Calificación Global**: **75/100** - Production-Ready con Mejoras Recomendadas 🟡

### 7.2 📊 Análisis SWOT

#### Strengths (Fortalezas)
- ✅ Componente reutilizable bien diseñado
- ✅ Manejo de errores robusto
- ✅ Auto-refresco de tokens implementado
- ✅ TypeScript estricto y bien tipado
- ✅ Integración perfecta con arquitectura existente
- ✅ Seguridad consciente (sandbox, validación de token)

#### Weaknesses (Debilidades)
- 🔴 **NO HAY TESTS** (0% coverage)
- 🔴 Sin configuración de proxy (riesgo de CORS)
- 🟡 Roles simulados (no production-ready)
- 🟡 URLs hardcodeadas

#### Opportunities (Oportunidades)
- 🟢 Añadir retry automático
- 🟢 Implementar skeleton loaders
- 🟢 Mejorar feedback visual (indicador de refresh)
- 🟢 Añadir accesibilidad (aria-labels)

#### Threats (Amenazas)
- ⚠️ Sin tests, los cambios futuros pueden romper funcionalidad
- ⚠️ CORS puede fallar en producción
- ⚠️ Roles hardcodeados dan acceso no autorizado

### 7.3 🎯 Recomendaciones Finales

#### Para Producción INMEDIATA:
```markdown
🔴 NO DESPLEGAR sin completar SPRINT 1
- Tests (cobertura mínima 80%)
- Configuración de proxy
- Variables de entorno
```

#### Para Primera Iteración Post-Launch:
```markdown
🟡 Completar SPRINT 2 en las primeras 2 semanas
- Integración de roles reales
- Botón de retry
- Dashboard IDs configurables
```

#### Para Mejora Continua:
```markdown
🟢 SPRINT 3 como mejoras incrementales
- Indicadores visuales
- Retry automático
- Accesibilidad
```

### 7.4 📈 Comparación con Blueprint

| Aspecto | Blueprint | Implementado | Status |
|---------|-----------|--------------|--------|
| DashboardEmbed.tsx | ✅ | ✅ | 100% |
| DashboardsPage.tsx | ✅ | ✅ | 100% |
| types/dashboard.ts | ✅ | ✅ | 100% |
| Integración en App.tsx | ✅ | ✅ | 100% |
| Integración en Sidebar | ✅ | ✅ | 100% |
| Tests unitarios | ✅ | ❌ | 0% |
| Tests de integración | ✅ | ❌ | 0% |
| Configuración de proxy | ✅ | ❌ | 0% |
| Variables de entorno | ✅ | ⚠️ | 30% |
| Prop `action` en Alert | ✅ | ❌ | 0% |

**Completitud del Blueprint**: **60%** (6/10 tareas completas)

### 7.5 🏆 Reconocimientos

**Implementación Destacada**:
- Manejo de memoria y prevención de leaks (⭐⭐⭐⭐⭐)
- Uso de React Hooks (⭐⭐⭐⭐⭐)
- TypeScript estricto (⭐⭐⭐⭐⭐)
- Integración con arquitectura existente (⭐⭐⭐⭐⭐)

**El código implementado demuestra conocimiento sólido de React y buenas prácticas. La principal deuda técnica es la ausencia de tests, que debe ser abordada antes de producción.**

---

## 6. Plan de Mejoras

### 🔴 SPRINT 1: Crítico (Antes de Producción)

**Estimación Total**: 6 horas

| Task | Prioridad | Estimación | Asignado |
|------|-----------|-----------|----------|
| **FE-TASK-08**: Crear tests unitarios de DashboardEmbed | P0 | 2h | - |
| **FE-TASK-09**: Crear tests de integración de DashboardsPage | P0 | 2h | - |
| **FE-TASK-10**: Configurar proxy en vite.config.ts | P0 | 30min | - |
| **FE-TASK-11**: Crear .env.example con variables requeridas | P0 | 30min | - |
| **FE-TASK-12**: Refactorizar URLs hardcodeadas a usar env vars | P0 | 1h | - |

---

### 🟡 SPRINT 2: Importante (Primera Iteración)

**Estimación Total**: 4 horas

| Task | Prioridad | Estimación | Asignado |
|------|-----------|-----------|----------|
| **FE-TASK-13**: Integrar roles reales desde Supabase JWT | P1 | 2h | - |
| **FE-TASK-14**: Añadir prop `action` a Alert.tsx | P1 | 1h | - |
| **FE-TASK-15**: Añadir botón Retry en DashboardEmbed | P1 | 30min | - |
| **FE-TASK-16**: Externalizar dashboard IDs a env vars | P1 | 30min | - |

---

### 🟢 SPRINT 3: Mejoras (Backlog)

**Estimación Total**: 3 horas

| Task | Prioridad | Estimación | Asignado |
|------|-----------|-----------|----------|
| **FE-TASK-17**: Añadir indicador visual de refresh | P2 | 30min | - |
| **FE-TASK-18**: Añadir skeleton loader | P2 | 1h | - |
| **FE-TASK-19**: Añadir retry automático con exponential backoff | P2 | 1h | - |
| **FE-TASK-20**: Limpiar interfaz DashboardError no usada | P2 | 5min | - |
| **FE-TASK-21**: Añadir aria-labels para accesibilidad | P2 | 30min | - |

---

## 🐛 Troubleshooting Guide

### Error 400: "Message seems corrupt or manipulated"

**Síntomas:**
```
GET http://localhost:3000/api/embed/dashboard/TOKEN 400 (Bad Request)
{status: 400, data: 'Message seems corrupt or manipulated'}
```

**Causa:**
El backend está configurado con una URL incorrecta de Metabase que incluye `/api` en el path.

**Diagnóstico:**
1. El iframe se carga correctamente con: `http://localhost:3000/embed/dashboard/TOKEN`
2. Pero Metabase internamente intenta validar en: `http://localhost:3000/api/embed/dashboard/TOKEN`
3. Este path diferente hace que el token JWT falle la validación

**Solución:**

1. **Verifica la configuración del backend** (`application.properties` o `application.yml`):

```properties
# ❌ INCORRECTO
metabase.site.url=http://localhost:3000/api

# ✅ CORRECTO
metabase.site.url=http://localhost:3000
```

2. **Verifica el secret key** en Metabase:
   - Ve a: `http://localhost:3000/admin/settings/embedding`
   - Copia el **Embedding secret key**
   - Asegúrate de que coincida con `metabase.secret.key` en el backend

3. **Reinicia el backend** después de hacer los cambios

4. **Refresca el frontend** y verifica que los dashboards carguen correctamente

**Validación:**
Deberías ver en la consola del navegador:
```
📊 Dashboard URL received: http://localhost:3000/embed/dashboard/TOKEN#bordered=true&titled=true
⏰ Expires in: 10 minutes
Dashboard loaded successfully
```

Y **NO** deberías ver errores 400 de Metabase.

---

### Error: "Request timeout"

**Síntomas:**
```
Request timeout: The dashboard is taking too long to load. Please verify that the backend is running.
```

**Causa:**
El backend de Spring Boot no está corriendo en `localhost:8080`.

**Solución:**
1. Ve a la carpeta del backend
2. Inicia el servidor:
   ```bash
   ./mvnw spring-boot:run  # Maven
   # o
   ./gradlew bootRun       # Gradle
   ```
3. Verifica que el backend responda:
   ```bash
   curl http://localhost:8080/api/v1/health
   ```

---

### CSP Warnings (Content Security Policy)

**Síntomas:**
```
Refused to apply inline style because it violates the following Content Security Policy directive...
```

**Causa:**
Metabase tiene políticas de seguridad estrictas que pueden causar warnings en la consola.

**Impacto:**
- ⚠️ Son solo **warnings**, no errores críticos
- ✅ El dashboard sigue funcionando correctamente
- 🔒 Es una buena práctica de seguridad de Metabase

**Solución:**
- No requiere acción inmediata
- Los warnings no afectan la funcionalidad
- Para eliminarlos completamente, necesitarías ajustar la CSP de Metabase (no recomendado)

---
