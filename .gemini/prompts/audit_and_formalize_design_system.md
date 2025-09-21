# Prompt: Auditar Código y Formalizar Guía de Estilo

## 1. CONFIGURACIÓN (PARA EL HUMANO)
*   **Propósito:** Este prompt instruye a la IA para que audite el código fuente de la plantilla TailAdmin, ignorando nuestro propio código, para formalizar los principios de diseño en un documento `design_tokens.md`.
*   **Acción Requerida:** Ninguna. Este prompt está listo para ser usado.
*   **Modo de Uso:** Copia y pega el contenido completo en Gemini CLI.

---

## 2. MANDATO OPERATIVO (PARA LA IA)

Tu rol es el de un **Auditor de Código y Arquitecto de Sistemas de Diseño**. Tu misión es realizar una auditoría sistemática del **código base original de la plantilla TailAdmin** para destilar y formalizar sus "design tokens" implícitos en un documento de directrices explícito y canónico.

**PROTOCOLO DE ANÁLISIS Y SÍNTESIS (MANDATORIO):**

### Fase 1: Auditoría Sistemática del Código Fuente
*   **Acción:** Realiza un escaneo recursivo de todos los archivos `.tsx` dentro del directorio `@src/`.
*   **Regla de Exclusión Crítica:** Debes **ignorar por completo** el contenido de los siguientes archivos durante tu análisis, ya que fueron generados por IA y no forman parte del sistema de diseño original de TailAdmin:
    *   `@src/pages/admin/AdminDashboardPage.tsx`
    *   `@src/pages/AuthPages/ResetPassword.tsx`
    *   `@src/pages/AuthPages/UpdatePassword.tsx`
    *   `@src/pages/Account.tsx`

### Fase 2: Análisis de Frecuencia y Contexto
*   **Acción:** Mientras auditas los archivos **permitidos**, construye en tu memoria una tabla de frecuencia de las clases de Tailwind CSS utilizadas. Presta especial atención al contexto (en qué tipo de elementos HTML/JSX se aplican).

### Fase 3: Síntesis y Generación del Reporte
*   **Acción:** Basado en tu análisis de los archivos de la plantilla original, sintetiza los patrones más consistentes. Descarta los estilos de un solo uso. Luego, genera el documento de guía de estilo.

**Tarea de Salida:**
Crea un nuevo archivo llamado `design_tokens.md` en la ruta `@.gemini/strategy/`. El contenido debe seguir estrictamente la siguiente plantilla:

---
### **PLANTILLA DE SALIDA OBLIGATORIA**

# Reporte de Auditoría y Guía de Estilo Fundamental

Este documento se generó a través de un análisis sistemático del código fuente original de la plantilla TailAdmin en `@src/` y formaliza los principios de diseño de la plataforma IOC. Se excluyeron del análisis los archivos generados por IA para asegurar la pureza de la guía de estilo.

## 1. Principios de Diseño Derivados
*   **Principio 1 (Contraste Visual):** <!-- Sintetiza un principio de alto nivel. -->
*   **Principio 2 (Jerarquía Tipográfica):** <!-- Sintetiza otro principio. -->

## 2. Design Tokens Formalizados

### A. Tipografía
*   **Texto de Párrafo:** `text-base`
    *   **Fuente / Justificación:** <!-- Cita los componentes originales donde se encontró este patrón. -->
*   **Labels y Metadatos:** `text-sm`, `font-medium`
    *   **Fuente / Justificación:** <!-- Cita componentes originales. -->

### B. Espaciado (Layout)
*   **Padding de Contenedor:** `py-6 px-7.5`
    *   **Fuente / Justificación:** <!-- Cita componentes originales. -->

### C. Paleta de Colores
*   **Primario (Acciones):** `bg-primary`
    *   **Fuente / Justificación:** <!-- Cita componentes originales. -->

*(Continúa este patrón para todas las categorías de tokens relevantes)*

## 3. Próximos Pasos Recomendados
1.  **Integración:** Este documento `design_tokens.md` debe ser añadido como "Fuente de Verdad" en el prompt `create_ux_blueprints.md`.
2.  **Refactorización:** Utilizar esta guía para auditar y alinear los componentes generados por IA (`AdminDashboardPage`, etc.) con el sistema de diseño formalizado.

---
**Confirmación:** Al finalizar, confirma la creación exitosa del archivo `@.gemini/strategy/design_tokens.md`.