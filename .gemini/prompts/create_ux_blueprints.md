# Prompt: Generar Fichas Técnicas de Vista (FTV)

## 1. CONFIGURACIÓN (PARA EL HUMANO)
*   **Propósito:** Este prompt transforma el diseño de producto en especificaciones técnicas detalladas (Fichas Técnicas) para cada vista principal de la UI.
*   **Acción Requerida:** Ninguna. Este prompt es de "un solo uso". Simplemente copia y pégalo en Gemini CLI.

---

## 2. MANDATO OPERATIVO (PARA LA IA)

Tu rol es el de un **Lead Frontend Architect**. Tu misión es descomponer el documento de diseño de producto en un conjunto de **Fichas Técnicas de Vista (FTV)**, que servirán como la especificación canónica para el desarrollo en React.

**Fuentes de Verdad (Sources of Truth):**
Debes sintetizar la información de las siguientes fuentes en orden de precedencia:
1.  **Requisitos:** `@.gemini/strategy/diseno_vista_admin.md`
2.  **Guía de Marca:** `@.gemini/context/cambiaso_manual_de_marca.md`
3.  **Sistema de Diseño:** `@.gemini/blueprints/tailadmin-react-component-docs.md`
4.  **Código Existente:** `@src/`

**Proceso de Razonamiento (Mandatorio):**

1.  **Análisis Holístico:** Primero, lee todas las fuentes para entender el alcance completo. Identifica el número total de vistas a generar (`{{TOTAL_VISTAS}}`).
2.  **Mapeo de Requisitos a Componentes:** En tu memoria, construye una **Tabla de Mapeo**. Para cada funcionalidad descrita en el documento de diseño, mapea los componentes (TailAdmin o existentes en `@src/`) que pueden satisfacerla. Este mapa informará tu decisión de reutilizar vs. crear.
3.  **Iteración y Generación:** Procesa cada vista principal secuencialmente, manteniendo un contador (`{{NUMERO_ACTUAL}}`), y genera una Ficha Técnica para cada una.

**Tarea de Salida:**

Para cada vista principal, genera un archivo de Ficha Técnica en `@.gemini/blueprints/`.

*   **Formato del Nombre de Archivo:** `[NN]-FTV-[Nombre-de-Vista-en-kebab-case].md` (donde `[NN]` es un número de dos dígitos, ej. `01`, `02`).
*   **Contenido del Archivo:** El contenido debe seguir estrictamente la siguiente plantilla:

---
### **PLANTILLA DE SALIDA OBLIGATORIA**

## Metadatos de la Ficha Técnica
*   **ID:** `FTV-{{NUMERO_ACTUAL}}/{{TOTAL_VISTAS}}`
*   **Vista:** `{{NOMBRE_DE_LA_VISTA}}`
---

# Ficha Técnica de Vista: {{NOMBRE_DE_LA_VISTA}}

## 1. Misión y Flujo de Usuario
*   **Misión de la Vista:** <!-- Describe la meta principal del usuario. -->
*   **Flujo de Usuario Clave:** <!-- Describe el "happy path" en una secuencia de pasos. -->

## 2. Arquitectura de Componentes
Esta vista se renderiza dentro del `DefaultLayout` y se compone de la siguiente jerarquía:

### A. Componentes Reutilizados
*   **De TailAdmin:** `Alert`, `Table`, `Modal`, `Button`, ...
*   **Del Proyecto (`@src/`):** `Breadcrumb`, ...

### B. Nuevos Componentes a Crear
#### B.1. Componente de Página (Orquestador)
*   **Nombre:** `[NombreDeLaPagina]Page.tsx`
*   **Responsabilidad:** Orquestar la vista, gestionar el estado principal y los efectos secundarios.

#### B.2. Componentes de UI (Reutilizables)
*   **Nombre:** `[NombreDelComponenteUI].tsx`
*   **Responsabilidad:** <!-- Describe su propósito específico. -->
*   **Interfaz de Props (Props Interface):**
    ```typescript
    interface [NombreDelComponenteUI]Props {
      // Define aquí las props, ej:
      // title: string;
      // onAction: () => void;
    }
    ```

## 3. Estrategia de Estado y Efectos Secundarios
*   **Gestión de Estado:** <!-- Describe la estrategia. Ej: "El estado se gestionará localmente en `[NombreDeLaPagina]Page.tsx` usando el hook `useState` para..." -->
*   **Efectos Secundarios (`useEffect`):** <!-- Lista los efectos necesarios. Ej: "Un `useEffect` se disparará al montar el componente para obtener los datos iniciales de la API." -->

## 4. Copy Aprobado por Marca (Ejemplos)
*   **Título Principal de la Vista:** <!-- Ej: "Centro de Ingesta de Datos de Producción" -->
*   **Texto del Botón Principal:** <!-- Ej: "Cargar Archivo de Producción" -->
*   **Mensaje de Éxito:** <!-- Ej: "El archivo de producción ha sido procesado exitosamente." -->

---
**Confirmación:** Al finalizar, confirma la operación listando el número total de Fichas Técnicas generadas y sus nombres de archivo.
