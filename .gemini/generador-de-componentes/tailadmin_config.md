<!-- BLOCK:BLOCK:001:START -->
<!-- BLOCK_META: {"id":"BLOCK:001","type":"text","title":"TailAdmin - Configuración de Diseño","lines":7,"hash":"4f5a4440","created":"2025-10-27T16:08:40.080Z"} -->
# TailAdmin - Configuración de Diseño
> Documentación auto-generada desde el proyecto
> Fecha: 27-10-2025
> Versión TailAdmin: 2.0.1

Este documento detalla los tokens de diseño, componentes base y patrones de uso extraídos del proyecto TailAdmin. Sirve como una referencia centralizada para mantener la consistencia visual y estructural en todo el desarrollo.

<!-- BLOCK:BLOCK:001:END -->

<!-- BLOCK:BLOCK:002:START -->
<!-- BLOCK_META: {"id":"BLOCK:002","type":"section","title":"📋 Metadatos","lines":5,"hash":"602ee0f5","created":"2025-10-27T16:08:40.080Z"} -->
## 📋 Metadatos
- **Ruta del proyecto:** `/mnt/ssd-480/repos/captone/tailadmin-free-tailwind-dashboard-template`
- **Framework detectado:** HTML, AlpineJS
- **Versión Tailwind:** 4.0.0

<!-- BLOCK:BLOCK:002:END -->

<!-- BLOCK:BLOCK:003:START -->
<!-- BLOCK_META: {"id":"BLOCK:003","type":"code","title":"🎨 Tokens de Diseño","lines":131,"hash":"620de0e8","created":"2025-10-27T16:08:40.081Z"} -->
---

## 🎨 Tokens de Diseño

### Colores

```javascript
// 📁 Fuente: src/css/style.css > @theme

colors: {
  current: 'currentColor',
  transparent: 'transparent',
  white: '#ffffff',
  black: '#101828',

  brand: {
    25: '#f2f7ff',
    50: '#ecf3ff',
    100: '#dde9ff',
    200: '#c2d6ff',
    300: '#9cb9ff',
    400: '#7592ff',
    500: '#465fff',
    600: '#3641f5',
    700: '#2a31d8',
    800: '#252dae',
    900: '#262e89',
    950: '#161950',
  },

  'blue-light': {
    25: '#f5fbff',
    50: '#f0f9ff',
    100: '#e0f2fe',
    200: '#b9e6fe',
    300: '#7cd4fd',
    400: '#36bffa',
    500: '#0ba5ec',
    600: '#0086c9',
    700: '#026aa2',
    800: '#065986',
    900: '#0b4a6f',
    950: '#062c41',
  },

  gray: {
    25: '#fcfcfd',
    50: '#f9fafb',
    100: '#f2f4f7',
    200: '#e4e7ec',
    300: '#d0d5dd',
    400: '#98a2b3',
    500: '#667085',
    600: '#475467',
    700: '#344054',
    800: '#1d2939',
    900: '#101828',
    950: '#0c111d',
    dark: '#1a2231',
  },

  orange: {
    25: '#fffaf5',
    50: '#fff6ed',
    100: '#ffead5',
    200: '#fddcab',
    300: '#feb273',
    400: '#fd853a',
    500: '#fb6514',
    600: '#ec4a0a',
    700: '#c4320a',
    800: '#9c2a10',
    900: '#7e2410',
    950: '#511c10',
  },

  success: {
    25: '#f6fef9',
    50: '#ecfdf3',
    100: '#d1fadf',
    200: '#a6f4c5',
    300: '#6ce9a6',
    400: '#32d583',
    500: '#12b76a',
    600: '#039855',
    700: '#027a48',
    800: '#05603a',
    900: '#054f31',
    950: '#053321',
  },

  error: {
    25: '#fffbfa',
    50: '#fef3f2',
    100: '#fee4e2',
    200: '#fecdca',
    300: '#fda29b',
    400: '#f97066',
    500: '#f04438',
    600: '#d92d20',
    700: '#b42318',
    800: '#912018',
    900: '#7a271a',
    950: '#55160c',
  },

  warning: {
    25: '#fffcf5',
    50: '#fffaeb',
    100: '#fef0c7',
    200: '#fedf89',
    300: '#fec84b',
    400: '#fdb022',
    500: '#f79009',
    600: '#dc6803',
    700: '#b54708',
    800: '#93370d',
    900: '#7a2e0e',
    950: '#4e1d09',
  },
  
  'theme-pink': {
      500: '#ee46bc'
  },
  
  'theme-purple': {
      500: '#7a5af8'
  }
}
```

<!-- BLOCK:BLOCK:003:END -->

<!-- BLOCK:BLOCK:004:START -->
<!-- BLOCK_META: {"id":"BLOCK:004","type":"code","title":"Tipografía","lines":22,"hash":"57c08532","created":"2025-10-27T16:08:40.081Z"} -->
### Tipografía

```javascript
// 📁 Fuente: src/css/style.css > @theme

fontFamily: {
  outfit: 'Outfit, sans-serif'
}

// 📁 Fuente: src/css/style.css > @theme
fontSize: {
  'title-2xl': '72px (line-height: 90px)',
  'title-xl': '60px (line-height: 72px)',
  'title-lg': '48px (line-height: 60px)',
  'title-md': '36px (line-height: 44px)',
  'title-sm': '30px (line-height: 38px)',
  'theme-xl': '20px (line-height: 30px)',
  'theme-sm': '14px (line-height: 20px)',
  'theme-xs': '12px (line-height: 18px)',
}
```

<!-- BLOCK:BLOCK:004:END -->

<!-- BLOCK:BLOCK:005:START -->`
3. Reemplaza el contenido hasta `<!-- BLOCK:BLOCK:005:END -->

<!-- BLOCK:BLOCK:006:START -->
<!-- BLOCK_META: {"id":"BLOCK:006","type":"code","title":"Sombras (Shadows)","lines":21,"hash":"3d6fb9f1","created":"2025-10-27T16:08:40.081Z"} -->
### Sombras (Shadows)

```javascript
// 📁 Fuente: src/css/style.css > @theme
boxShadow: {
  'theme-md': '0px 4px 8px -2px rgba(16, 24, 40, 0.1), 0px 2px 4px -2px rgba(16, 24, 40, 0.06)',
  'theme-lg': '0px 12px 16px -4px rgba(16, 24, 40, 0.08), 0px 4px 6px -2px rgba(16, 24, 40, 0.03)',
  'theme-sm': '0px 1px 3px 0px rgba(16, 24, 40, 0.1), 0px 1px 2px 0px rgba(16, 24, 40, 0.06)',
  'theme-xs': '0px 1px 2px 0px rgba(16, 24, 40, 0.05)',
  'theme-xl': '0px 20px 24px -4px rgba(16, 24, 40, 0.08), 0px 8px 8px -4px rgba(16, 24, 40, 0.03)',
  'datepicker': '-5px 0 0 #262d3c, 5px 0 0 #262d3c',
  'focus-ring': '0px 0px 0px 4px rgba(70, 95, 255, 0.12)',
  'slider-navigation': '0px 1px 2px 0px rgba(16, 24, 40, 0.1), 0px 1px 3px 0px rgba(16, 24, 40, 0.1)',
  'tooltip': '0px 4px 6px -2px rgba(16, 24, 40, 0.05), -8px 0px 20px 8px rgba(16, 24, 40, 0.05)',
}

dropShadow: {
    '4xl': ['0 35px 35px rgba(0, 0, 0, 0.25)', '0 45px 65px rgba(0, 0, 0, 0.15)']
}
```

<!-- BLOCK:BLOCK:006:END -->

<!-- BLOCK:BLOCK:007:START -->
<!-- BLOCK_META: {"id":"BLOCK:007","type":"code","title":"Puntos de Ruptura (Breakpoints)","lines":15,"hash":"9f36430f","created":"2025-10-27T16:08:40.081Z"} -->
### Puntos de Ruptura (Breakpoints)

```javascript
// 📁 Fuente: src/css/style.css > @theme
screens: {
  '2xsm': '375px',
  'xsm': '425px',
  'sm': '640px',
  'md': '768px',
  'lg': '1024px',
  'xl': '1280px',
  '2xl': '1536px',
  '3xl': '2000px',
}
```
<!-- BLOCK:BLOCK:007:END -->

<!-- BLOCK:BLOCK:008:START -->
<!-- BLOCK_META: {"id":"BLOCK:008","type":"code","title":"🧩 Componentes Base Encontrados","lines":17,"hash":"df8a1bd2","created":"2025-10-27T16:08:40.081Z"} -->
---
## 🧩 Componentes Base Encontrados

### Botones

```html
<!-- Primario -->
<button class="inline-flex items-center gap-2 px-4 py-3 text-sm font-medium text-white transition rounded-lg bg-brand-500 shadow-theme-xs hover:bg-brand-600">
  Texto del Botón
</button>

<!-- Secundario -->
<button class="inline-flex items-center gap-2 rounded-lg bg-white px-4 py-3 text-sm font-medium text-gray-700 shadow-theme-xs ring-1 ring-inset ring-gray-300 transition hover:bg-gray-50 dark:bg-gray-800 dark:text-gray-400 dark:ring-gray-700 dark:hover:bg-white/[0.03]">
  Texto del Botón
</button>
```

<!-- BLOCK:BLOCK:008:END -->

<!-- BLOCK:BLOCK:009:START -->
<!-- BLOCK_META: {"id":"BLOCK:009","type":"code","title":"Formularios","lines":10,"hash":"5b035d32","created":"2025-10-27T16:08:40.081Z"} -->
### Formularios

```html
<!-- Input Base -->
<input type="text" class="dark:bg-dark-900 shadow-theme-xs focus:border-brand-300 focus:ring-brand-500/10 dark:focus:border-brand-800 h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 py-2.5 text-sm text-gray-800 placeholder:text-gray-400 focus:ring-3 focus:outline-hidden dark:border-gray-700 dark:bg-gray-900 dark:text-white/90 dark:placeholder:text-white/30" />

<!-- Label -->
<label class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-400" />
```

<!-- BLOCK:BLOCK:009:END -->

<!-- BLOCK:BLOCK:010:START -->
<!-- BLOCK_META: {"id":"BLOCK:010","type":"code","title":"Tarjetas (Cards)","lines":14,"hash":"c2445ae2","created":"2025-10-27T16:08:40.081Z"} -->
### Tarjetas (Cards)

```html
<div class="rounded-2xl border border-gray-200 bg-white dark:border-gray-800 dark:bg-white/[0.03]">
  <div class="px-6 py-5">
    <h3 class="text-base font-medium text-gray-800 dark:text-white/90">
      Título de la Tarjeta
    </h3>
  </div>
  <div class="border-t border-gray-100 p-4 dark:border-gray-800 sm:p-6">
    <!-- Contenido de la tarjeta -->
  </div>
</div>
```
<!-- BLOCK:BLOCK:010:END -->

<!-- BLOCK:BLOCK:011:START -->
<!-- BLOCK_META: {"id":"BLOCK:011","type":"code","title":"🔌 Plugins Detectados","lines":9,"hash":"8dfcf4dc","created":"2025-10-27T16:08:40.081Z"} -->
---
## 🔌 Plugins Detectados

```javascript
// 📁 Fuente: package.json
plugins: [
  "@tailwindcss/forms"
]
```
<!-- BLOCK:BLOCK:011:END -->

<!-- BLOCK:BLOCK:012:START -->
<!-- BLOCK_META: {"id":"BLOCK:012","type":"text","title":"📊 Análisis de Uso","lines":11,"hash":"7cd5a277","created":"2025-10-27T16:08:40.081Z"} -->
---
## 📊 Análisis de Uso

### Análisis de Frecuencia de Clases
*Nota: Un conteo cuantitativo preciso (`{N} veces`) requiere un script de análisis estático. La siguiente es una observación cualitativa de las clases y patrones más comunes observados en los archivos HTML.*

- **Clases de Flexbox:** `flex`, `items-center`, `justify-between`, `gap-3` son extremadamente comunes.
- **Tipografía:** `text-sm`, `font-medium`, `text-gray-500`, `dark:text-gray-400` son la base para el texto.
- **Bordes y Fondos:** `rounded-lg`, `border`, `border-gray-200`, `dark:border-gray-800`, `bg-white`, `dark:bg-white/[0.03]` definen la mayoría de los contenedores.
- **Espaciado:** `p-4`, `sm:p-6`, `py-2.5`, `px-4` son patrones de padding recurrentes.

<!-- BLOCK:BLOCK:012:END -->

<!-- BLOCK:BLOCK:013:START -->
<!-- BLOCK_META: {"id":"BLOCK:013","type":"code","title":"🔍 Clases Personalizadas Detectadas","lines":22,"hash":"44fdefcd","created":"2025-10-27T16:08:40.081Z"} -->
---
## 🔍 Clases Personalizadas Detectadas

```css
/* 📁 Fuente: src/css/style.css */

@utility menu-item {
  @apply relative flex items-center gap-3 px-3 py-2 font-medium rounded-lg text-theme-sm;
}

@utility menu-item-active {
  @apply bg-brand-50 text-brand-500 dark:bg-brand-500/[0.12] dark:text-brand-400;
}

@utility no-scrollbar {
  /* Oculta la barra de scroll */
}

@utility custom-scrollbar {
  /* Estiliza la barra de scroll */
}
```
<!-- BLOCK:BLOCK:013:END -->

<!-- BLOCK:BLOCK:014:START -->
<!-- BLOCK_META: {"id":"BLOCK:014","type":"text","title":"⚠️ Inconsistencias Detectadas","lines":6,"hash":"dad4c511","created":"2025-10-27T16:08:40.081Z"} -->
---
## ⚠️ Inconsistencias Detectadas

- Uso mixto de diferentes tonos de `gray` para textos con propósitos similares.
- Ligeras variaciones en `padding` y `font-size` para botones que deberían ser idénticos.

<!-- BLOCK:BLOCK:014:END -->

<!-- BLOCK:BLOCK:015:START -->
<!-- BLOCK_META: {"id":"BLOCK:015","type":"text","title":"📝 Notas Adicionales","lines":6,"hash":"2cea498f","created":"2025-10-27T16:08:40.081Z"} -->
---
## 📝 Notas Adicionales

- El proyecto demuestra un uso avanzado de **Tailwind CSS v4**, moviendo la configuración al bloque `@theme` en el CSS principal, lo cual es una desviación importante de la v3.
- **Alpine.js** se utiliza para gestionar el estado de la UI, como los toggles de menú y modo oscuro. Las clases de Tailwind a menudo se aplican condicionalmente usando la sintaxis `:class` de Alpine.

<!-- BLOCK:BLOCK:015:END -->

<!-- BLOCK:BLOCK:016:START -->
<!-- BLOCK_META: {"id":"BLOCK:016","type":"text","title":"","lines":2,"hash":"918f10e3","created":"2025-10-27T16:08:40.081Z"} -->
---
*Archivo generado automáticamente - No editar manualmente*
<!-- BLOCK:BLOCK:016:END -->

---

---

## 📑 ÍNDICE DE BLOQUES (Auto-generado)

| ID        | Tipo    | Título                              | Líneas | Hash     |
| --------- | ------- | ----------------------------------- | ------ | -------- |
| BLOCK:001 | text    | TailAdmin - Configuración de Diseño | 7      | 4f5a4440 |
| BLOCK:002 | section | 📋 Metadatos                        | 5      | 602ee0f5 |
| BLOCK:003 | code    | 🎨 Tokens de Diseño                 | 131    | 620de0e8 |
| BLOCK:004 | code    | Tipografía                          | 22     | 57c08532 |
|           |         |                                     |        |          |
| BLOCK:006 | code    | Sombras (Shadows)                   | 21     | 3d6fb9f1 |
| BLOCK:007 | code    | Puntos de Ruptura (Breakpoints)     | 15     | 9f36430f |
| BLOCK:008 | code    | 🧩 Componentes Base Encontrados     | 17     | df8a1bd2 |
| BLOCK:009 | code    | Formularios                         | 10     | 5b035d32 |
| BLOCK:010 | code    | Tarjetas (Cards)                    | 14     | c2445ae2 |
| BLOCK:011 | code    | 🔌 Plugins Detectados               | 9      | 8dfcf4dc |
| BLOCK:012 | text    | 📊 Análisis de Uso                  | 11     | 7cd5a277 |
| BLOCK:013 | code    | 🔍 Clases Personalizadas Detectadas | 22     | 44fdefcd |
| BLOCK:014 | text    | ⚠️ Inconsistencias Detectadas       | 6      | dad4c511 |
| BLOCK:015 | text    | 📝 Notas Adicionales                | 6      | 2cea498f |
| BLOCK:016 | text    |                                     | 2      | 918f10e3 |

**Total de bloques:** 16
**Total de líneas:** NaN
**Promedio líneas/bloque:** NaN

*Última actualización: 27-10-2025, 1:23:48 p. m.*