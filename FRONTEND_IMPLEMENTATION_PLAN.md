# Plan de Implementación Frontend: Explicación de Dashboards con IA

Este documento detalla los pasos necesarios para integrar la funcionalidad de **Explicación de Dashboards con IA** en la aplicación frontend (React).

## 📋 Prerrequisitos

### Dependencias
Asegúrate de instalar las siguientes librerías:

```bash
npm install axios @heroicons/react clsx
# O si usas yarn:
# yarn add axios @heroicons/react clsx
```

### Configuración de Entorno
Crea o edita el archivo `.env` en la raíz del proyecto para configurar la URL del backend:

```properties
REACT_APP_API_URL=http://localhost:8080/api
```

### Requisitos del Sistema
*   Acceso al repositorio del frontend.
*   Backend corriendo con la versión v2 de la API (`/api/v2/ai/explain-dashboard`).
*   Usuario con permisos para ver dashboards.
*   **Tailwind CSS** configurado en el proyecto (los componentes usan clases de utilidad).

## 🛠️ 1. Definición de Tipos (TypeScript)

Crear o actualizar el archivo de tipos para incluir las interfaces de la nueva feature.

**Archivo sugerido:** `src/types/ai.ts`

```typescript
export interface DashboardExplanationRequest {
  dashboardId: number;
  fechaInicio: string; // YYYY-MM-DD
  fechaFin: string;    // YYYY-MM-DD
  filtros?: Record<string, string>;
}

export interface DashboardExplanationResponse {
  resumenEjecutivo: string;
  keyPoints: string[];
  insightsAccionables: string[];
  alertas: string[];
  dashboardId: number;
  dashboardTitulo: string;
  fechaInicio: string;
  fechaFin: string;
  generadoEn: string;
  fromCache: boolean;
}
```

## 🔌 2. Servicio de API

Implementar la función para consumir el endpoint del backend.

**Archivo sugerido:** `src/services/aiService.ts`

```typescript
import axios from 'axios'; // O tu cliente HTTP preferido
import { DashboardExplanationRequest, DashboardExplanationResponse } from '../types/ai';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

export const aiService = {
  explainDashboard: async (request: DashboardExplanationRequest): Promise<DashboardExplanationResponse> => {
    const response = await axios.post<DashboardExplanationResponse>(
      `${API_URL}/v2/ai/explain-dashboard`,
      request
    );
    return response.data;
  }
};
```

## 🎣 3. Custom Hook (Lógica de Negocio)

Encapsular la lógica de llamada, carga y manejo de errores.

**Archivo sugerido:** `src/hooks/useAiExplanation.ts`

```typescript
import { useState } from 'react';
import { aiService } from '../services/aiService';
import { DashboardExplanationRequest, DashboardExplanationResponse } from '../types/ai';

export const useAiExplanation = () => {
  const [explanation, setExplanation] = useState<DashboardExplanationResponse | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const generateExplanation = async (request: DashboardExplanationRequest) => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await aiService.explainDashboard(request);
      setExplanation(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Error generando la explicación');
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  return {
    explanation,
    isLoading,
    error,
    generateExplanation,
    clearExplanation: () => setExplanation(null)
  };
};
```

## 🧩 4. Componentes de UI

### 4.1 Botón de Acción

Un botón que se ubicará en la barra de herramientas del dashboard.

**Componente:** `AiExplanationButton.tsx`

```tsx
import React from 'react';
import { SparklesIcon } from '@heroicons/react/24/solid';

interface Props {
  onClick: () => void;
  isLoading: boolean;
}

/**
 * Botón de acción principal para IA.
 * Estilos basados en tailadmin_config.md (Botón Primario).
 */
export const AiExplanationButton: React.FC<Props> = ({ onClick, isLoading }) => {
  return (
    <button
      onClick={onClick}
      disabled={isLoading}
      className="
        inline-flex items-center gap-2 px-4 py-3 
        text-sm font-medium font-outfit text-white 
        bg-brand-500 rounded-lg shadow-theme-xs 
        hover:bg-brand-600 transition-colors
        disabled:opacity-50 disabled:cursor-not-allowed
        focus:ring-4 focus:ring-brand-500/10 focus:outline-hidden
      "
    >
      {isLoading ? (
        <>
          <svg className="animate-spin h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          Generando insights...
        </>
      ) : (
        <>
          <SparklesIcon className="w-5 h-5" /> 
          Analizar con IA
        </>
      )}
    </button>
  );
};
```

### 4.2 Panel de Resultados (Modal o Sidebar)

Componente para visualizar la respuesta estructurada.

**Componente:** `AiExplanationPanel.tsx`

```tsx
import React from 'react';
import { DashboardExplanationResponse } from '../types/ai';
import { XMarkIcon } from '@heroicons/react/24/outline';

interface Props {
  data: DashboardExplanationResponse;
  onClose: () => void;
}

/**
 * Panel lateral de resultados.
 * Estilos: tailadmin_config.md (Cards, Typography, Colors)
 * Tono: Institucional/Formal (cambiaso_manual_de_marca.md)
 */
export const AiExplanationPanel: React.FC<Props> = ({ data, onClose }) => {
  // Formateador de fecha Chile (ui_design_rules.md)
  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('es-CL', {
      day: '2-digit', month: '2-digit', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    }) + ' hrs';
  };

  return (
    <div className="fixed inset-0 bg-gray-900/50 backdrop-blur-sm flex justify-end z-50 transition-opacity">
      <div className="w-full max-w-lg bg-white h-full shadow-theme-xl overflow-y-auto animate-slide-in border-l border-gray-200">
        
        {/* Header */}
        <div className="sticky top-0 bg-white/95 backdrop-blur z-10 px-6 py-5 border-b border-gray-100 flex justify-between items-center">
          <div>
            <h2 className="text-title-sm font-outfit font-medium text-gray-800">
              Análisis Ejecutivo
            </h2>
            <p className="text-theme-sm text-gray-500 mt-1">
              Generado por IA • {formatDate(data.generadoEn)}
            </p>
          </div>
          <button 
            onClick={onClose} 
            className="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-lg transition-colors"
          >
            <XMarkIcon className="w-6 h-6" />
          </button>
        </div>

        <div className="p-6 space-y-8 font-outfit">
          
          {/* Resumen Ejecutivo */}
          <section className="bg-brand-25 rounded-2xl p-6 border border-brand-100">
            <h3 className="text-base font-medium text-brand-800 mb-3 flex items-center gap-2">
              📋 Resumen Ejecutivo
            </h3>
            <p className="text-gray-700 text-sm leading-relaxed">
              {data.resumenEjecutivo}
            </p>
          </section>

          {/* Puntos Clave */}
          <section>
            <h3 className="text-base font-medium text-gray-900 mb-4">
              📊 Indicadores Clave
            </h3>
            <ul className="space-y-3">
              {data.keyPoints.map((point, i) => (
                <li key={i} className="flex items-start gap-3 text-sm text-gray-600 bg-gray-25 p-3 rounded-lg border border-gray-100">
                  <span className="text-brand-500 font-bold mt-0.5">•</span>
                  {point}
                </li>
              ))}
            </ul>
          </section>

          {/* Insights Accionables */}
          <section>
            <h3 className="text-base font-medium text-gray-900 mb-4">
              💡 Recomendaciones
            </h3>
            <div className="space-y-3">
              {data.insightsAccionables.map((insight, i) => (
                <div key={i} className="bg-blue-light-50 p-4 rounded-xl border-l-4 border-blue-light-500 text-sm text-gray-700 shadow-theme-xs">
                  {insight}
                </div>
              ))}
            </div>
          </section>

          {/* Alertas */}
          {data.alertas.length > 0 && (
            <section>
              <h3 className="text-base font-medium text-error-700 mb-4">
                ⚠️ Alertas Críticas
              </h3>
              <ul className="space-y-2">
                {data.alertas.map((alerta, i) => (
                  <li key={i} className="flex items-start gap-3 text-sm text-error-700 bg-error-50 p-3 rounded-lg border border-error-100">
                    <span className="font-bold">!</span>
                    {alerta}
                  </li>
                ))}
              </ul>
            </section>
          )}
          
          <div className="pt-6 border-t border-gray-100 text-center">
            <p className="text-theme-xs text-gray-400">
              Información confidencial para uso interno de Cambiaso Hnos.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};
```

## 🚀 5. Integración en la Página del Dashboard

Finalmente, integrar todo en la vista principal del dashboard.

**Archivo:** `src/pages/DashboardPage.tsx` (o similar)

```tsx
import React from 'react';
import { useAiExplanation } from '../hooks/useAiExplanation';
import { AiExplanationButton } from '../components/AiExplanationButton';
import { AiExplanationPanel } from '../components/AiExplanationPanel';

export const DashboardPage = ({ dashboardId, dateRange, filters }) => {
  const { explanation, isLoading, error, generateExplanation, clearExplanation } = useAiExplanation();

  const handleAnalyze = () => {
    generateExplanation({
      dashboardId,
      fechaInicio: dateRange.start,
      fechaFin: dateRange.end,
      filtros: filters
    });
  };

  return (
    <div className="dashboard-container relative">
      {/* Header del Dashboard */}
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">Dashboard Operativo</h1>
        
        {/* Botón de IA */}
        <AiExplanationButton onClick={handleAnalyze} isLoading={isLoading} />
      </div>

      {/* Mensaje de Error (Toast o Banner) */}
      {error && (
        <div className="bg-red-100 text-red-700 p-3 rounded mb-4">
          {error}
        </div>
      )}

      {/* Contenido del Dashboard (iFrame Metabase, etc.) */}
      <div className="dashboard-content">
        {/* ... iframe ... */}
      </div>

      {/* Panel de Resultados */}
      {explanation && (
        <AiExplanationPanel 
          data={explanation} 
          onClose={clearExplanation} 
        />
      )}
    </div>
  );
};
```

## ✅ 6. Plan de Verificación

1.  **Verificar Renderizado**: El botón "Analizar con IA" debe aparecer en el header del dashboard.
2.  **Estado de Carga**: Al hacer click, el botón debe deshabilitarse y mostrar "Generando insights...".
3.  **Llamada Exitosa**:
    *   Verificar en Network tab que se hace POST a `/api/v2/ai/explain-dashboard`.
    *   Verificar que el payload incluye `dashboardId` y fechas correctas.
4.  **Visualización**: El panel lateral debe abrirse con la información estructurada (Resumen, Puntos Clave, etc.).
5.  **Manejo de Errores**: Simular un error 500 en el backend y verificar que aparece el mensaje de error en la UI.
