# Guía de Estilo Fundamental y Design Tokens (v1.1)

Este documento formaliza el sistema de diseño de la plataforma IOC, basado en un análisis del código de la plantilla TailAdmin. Sirve como la fuente de verdad canónica para toda la construcción de UI.

## 1. Reglas de Oro del Diseño

*   **Regla 1: Jerarquía Clara a través del Contraste.** La distinción entre elementos primarios y secundarios se logra principalmente a través del contraste de color del texto, no del tamaño.
    *   **Primario:** `text-gray-800 dark:text-white/90`
    *   **Secundario:** `text-gray-500 dark:text-gray-400`
*   **Regla 2: Separación Sutil con Bordes.** Usa bordes y esquinas redondeadas para definir y separar contenedores. Evita usar colores de fondo diferentes para la separación, manteniendo la interfaz limpia.
*   **Regla 3: Feedback Interactivo Predecible.** Todos los elementos interactivos deben reaccionar al `hover` del usuario, ya sea aclarando su fondo o intensificando su color principal.

---

## 2. Design Tokens Formalizados

### A. Layout y Contenedores
*   **Padding de Página Principal:** `p-4 md:p-6`
    *   **Caso de Uso:** Espaciado general para el contenedor principal de cada página dentro del `AppLayout`.
*   **Padding de Tarjeta:** `p-5 sm:p-6`
    *   **Caso de Uso:** Espaciado interno para todos los componentes de tarjeta (`ComponentCard`).
*   **Radio de Borde (Contenedores):** `rounded-2xl`
    *   **Caso de Uso:** Estilo de borde estándar para todas las tarjetas y contenedores principales.
*   **Borde de Separación:** `border border-gray-200 dark:border-gray-800`
    *   **Caso de Uso:** Borde estándar para tarjetas y separadores visuales.

### B. Tipografía
*   **Título de Página (h2):** `text-xl font-semibold text-gray-800 dark:text-white/90`
    *   **Caso de Uso:** Para el título principal de cada vista, generalmente usado en `PageBreadcrumb`.
*   **Título de Sección (h3):** `text-lg font-semibold text-gray-800 dark:text-white/90`
    *   **Caso de Uso:** Encabezados dentro de una página, como los títulos de las tarjetas.
*   **Texto Principal / Párrafo:** `text-sm text-gray-500 dark:text-gray-400`
    *   **Caso de Uso:** Texto descriptivo, párrafos y contenido principal.
*   **Labels y Metadatos:** `text-xs text-gray-500 dark:text-gray-400`
    *   **Caso de Uso:** Para etiquetas de formulario (`Label`) y texto descriptivo de bajo énfasis.

### C. Paleta de Colores
*   **Primario (Acciones):** `brand-500`
    *   **Caso de Uso:** Usado como color de fondo en botones principales (`bg-brand-500`) y como color de texto en enlaces importantes (`text-brand-500`).
*   **Fondo Principal:** `bg-white dark:bg-gray-900`
    *   **Caso de Uso:** Color de fondo para el cuerpo principal de la aplicación.
*   **Fondo de Tarjeta:** `bg-white dark:bg-white/[0.03]`
    *   **Caso de Uso:** Fondo para todos los componentes de tarjeta para darles una ligera elevación.
*   **Éxito:** `success-500` (para texto/iconos), `bg-success-50` (para fondos de alerta)
*   **Error:** `error-500` (para texto/iconos), `bg-error-50` (para fondos de alerta)
*   **Advertencia:** `warning-500` (para texto/iconos), `bg-warning-50` (para fondos de alerta)

### D. Elementos Interactivos (Botones e Inputs)
*   **Radio de Borde:** `rounded-lg`
    *   **Caso de Uso:** Estilo de borde estándar para todos los botones, inputs y selects.
*   **Sombra:** `shadow-theme-xs`
    *   **Caso de Uso:** Sombra sutil aplicada a botones e inputs para darles profundidad.
*   **Color de Foco (Focus Ring):** `focus:ring-brand-500/10`
    *   **Caso de Uso:** Anillo de foco estándar para todos los elementos interactivos, asegurando la accesibilidad.

---

## 3. Componentes Compuestos (Patrones Comunes)

*   **Tarjeta Estándar:**
    *   **Estructura:** Un `div` con `rounded-2xl`, `border`, `p-5 sm:p-6` y los colores de fondo y borde definidos.
    *   **Ejemplo:** El `ComponentCard.tsx` es la implementación canónica de este patrón.
*   **Formulario Estándar:**
    *   **Estructura:** Los campos del formulario se separan verticalmente con `space-y-5`. Cada campo consiste en un `<Label>` seguido de un `<Input>` o `<Select>`.
    *   **Ejemplo:** Los formularios de `SignInForm.tsx` y `SignUpForm.tsx` son las implementaciones de referencia.