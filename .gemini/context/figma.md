# 🎨 Guía Completa de Figma: AI Insights Panel

## Estructura del Archivo Figma

```
📁 IOC - AI Insights Design System
├── 🎨 Cover (Portada del proyecto)
├── 📐 Design Tokens (Sistema de diseño)
├── 🧩 Components (Componentes reutilizables)
├── 📱 Screens (Pantallas completas)
├── 🎬 Prototype (Flujo interactivo)
└── 📋 Handoff (Specs para desarrollo)
```

---

## 1. Setup Inicial: Design Tokens

### 🎨 Paleta de Colores (Color Styles)

```
PRIMARIOS:
├── Purple/50    #FAF5FF
├── Purple/100   #F3E8FF
├── Purple/200   #E9D5FF
├── Purple/500   #A855F7
├── Purple/600   #9333EA  ← Principal
├── Purple/700   #7E22CE

SECUNDARIOS:
├── Blue/50      #EFF6FF
├── Blue/100     #DBEAFE
├── Blue/500     #3B82F6
├── Blue/600     #2563EB
├── Blue/700     #1D4ED8

INDIGO:
├── Indigo/50    #EEF2FF
├── Indigo/100   #E0E7FF

ESTADOS:
├── Green/50     #F0FDF4
├── Green/100    #DCFCE7
├── Green/600    #16A34A
├── Green/700    #15803D

├── Red/50       #FEF2F2
├── Red/100      #FEE2E2
├── Red/600      #DC2626
├── Red/700      #B91C1C

├── Yellow/50    #FEFCE8
├── Yellow/100   #FEF3C7
├── Yellow/300   #FDE047
├── Yellow/600   #CA8A04

NEUTRALES:
├── Gray/50      #F9FAFB
├── Gray/100     #F3F4F6
├── Gray/200     #E5E7EB
├── Gray/300     #D1D5DB
├── Gray/400     #9CA3AF
├── Gray/500     #6B7280
├── Gray/600     #4B5563
├── Gray/700     #374151
├── Gray/800     #1F2937
├── Gray/900     #111827

BACKGROUNDS:
├── Gradient/Purple-Blue    Linear: 135°, Purple/600 → Blue/600
├── Gradient/Purple-Light   Linear: 135°, Purple/50 → Blue/50 → Indigo/50
├── Gradient/Decorative     Radial: Purple/200 20% opacity
```

---

### ✍️ Tipografía (Text Styles)

```
HEADINGS:
├── H1/Bold          Inter, 32px, Bold (700), -0.02em
├── H2/Bold          Inter, 24px, Bold (700), -0.01em
├── H3/Bold          Inter, 18px, Bold (700), -0.01em
├── H4/Semibold      Inter, 16px, Semibold (600)

BODY:
├── Body/Regular     Inter, 14px, Regular (400), 1.5 line-height
├── Body/Medium      Inter, 14px, Medium (500)
├── Body/Semibold    Inter, 14px, Semibold (600)

SMALL:
├── Caption/Regular  Inter, 12px, Regular (400)
├── Caption/Medium   Inter, 12px, Medium (500)
├── Caption/Mono     JetBrains Mono, 11px, Regular (400)

MICRO:
├── Label/Regular    Inter, 10px, Regular (400)
├── Label/Medium     Inter, 10px, Medium (500)
```

---

### 📏 Espaciado (Grid System)

```
SPACING SCALE (Auto Layout spacing):
├── 2px   (XXS)
├── 4px   (XS)
├── 8px   (SM)
├── 12px  (MD)
├── 16px  (LG)
├── 20px  (XL)
├── 24px  (2XL)
├── 32px  (3XL)
├── 48px  (4XL)

BORDER RADIUS:
├── 4px   (Small - badges)
├── 8px   (Medium - buttons)
├── 12px  (Large - cards)
├── 16px  (XLarge - panels)
├── 999px (Full - pills)

SHADOWS:
├── SM   0px 1px 2px rgba(0,0,0,0.05)
├── MD   0px 4px 6px rgba(0,0,0,0.07)
├── LG   0px 10px 15px rgba(0,0,0,0.1)
├── XL   0px 20px 25px rgba(0,0,0,0.15)
```

---

## 2. Componentes Base (Components Library)

### 🔘 Button Component

**Frame: `Button`** (Create Component)

**Variantes:**
```
Property: Type
├── Primary
├── Secondary
├── Ghost

Property: Size
├── Small (h: 32px)
├── Medium (h: 40px)
├── Large (h: 48px)

Property: State
├── Default
├── Hover
├── Pressed
├── Disabled
```

**Configuración Auto Layout (Primary/Medium/Default):**
```
Direction: Horizontal
Padding: Horizontal 16px, Vertical 10px
Gap: 8px
Fill: Gradient/Purple-Blue
Corner Radius: 8px
```

**Contenido:**
```
├── Icon (24x24 frame) [Optional]
└── Text "Button" (Body/Semibold, White)
```

**Efectos:**
```
Shadow: MD
Hover: Brightness +10%, Shadow LG
```

---

### 💬 Bullet Item Component

**Frame: `BulletItem`**

**Auto Layout:**
```
Direction: Horizontal
Padding: 0
Gap: 12px
Align: Top
```

**Estructura:**
```
├── Bullet
│   ├── Size: 8x8
│   ├── Fill: Purple/600
│   ├── Corner Radius: Full
│   └── Align: Top (margin-top: 6px)
│
└── Text Container
    ├── Width: Fill container
    ├── Text: Body/Regular, Gray/800
    └── Line height: 1.5
```

**Variante con Highlight:**
```
Crear nested component para números:
├── Number/Money     (Bold, Blue/700, bg: Blue/50)
├── Number/Negative  (Bold, Red/600, bg: Red/50)
├── Number/Positive  (Bold, Green/600, bg: Green/50)
```

---

### 🏷️ Badge Component

**Frame: `Badge`**

**Variantes:**
```
Property: Type
├── Cached
├── Model
├── Status

Property: Color
├── Blue
├── Purple
├── Green
```

**Configuración (Cached/Blue):**
```
Auto Layout:
├── Direction: Horizontal
├── Padding: H 12px, V 6px
├── Gap: 6px
├── Fill: Blue/100
├── Corner Radius: 999px

Contenido:
├── Indicator (6x6, Blue/500, blur pulse animation)
└── Text "Cache • 2 min" (Caption/Medium, Blue/700)
```

---

### 👍 Feedback Button Component

**Frame: `FeedbackButton`**

**Variantes:**
```
Property: Type
├── Positive
├── Negative

Property: State
├── Default
├── Active
├── Disabled
```

**Configuración (Positive/Default):**
```
Auto Layout:
├── Padding: 8px
├── Gap: 4px
├── Fill: Transparent
├── Border: None
├── Corner Radius: 8px
├── Hover: bg Gray/50

Contenido:
├── Icon ThumbsUp (16x16, Gray/400)
└── Count "24" (Caption/Medium, Gray/500)
```

**Estado Active:**
```
Fill: Green/100
Icon color: Green/700
Text color: Green/700
```

---

## 3. Panel Principal: AIInsightPanel

### 📐 Frame Principal

**Nombre:** `AIInsightPanel/Success`

**Dimensiones:**
```
Width: 1200px (Desktop) / Fill container
Height: Auto (Hug contents)
```

**Auto Layout:**
```
Direction: Vertical
Padding: 24px
Gap: 20px
Fill: Gradient/Purple-Light
Border: 2px, Purple/200
Corner Radius: 16px
```

**Effects:**
```
Shadow: LG
Hover: Shadow XL (transition 200ms)
```

---

### 🏗️ Estructura Interna

#### **1. Header Section**

**Auto Layout:**
```
Direction: Horizontal
Justify: Space between
Align: Center
Gap: 16px
Padding: 0
Height: Hug
```

**Contenido Izquierdo:**
```
├── Icon Container
│   ├── Size: 40x40
│   ├── Fill: Gradient/Purple-Blue
│   ├── Corner Radius: 8px
│   ├── Shadow: MD
│   └── Icon: Sparkles (20x20, White)
│
└── Text Container (Auto Layout Vertical, Gap: 2px)
    ├── Title "Resumen Ejecutivo" (H3/Bold, Gray/900)
    └── Subtitle "Generado automáticamente con IA" (Caption/Regular, Gray/500)
```

**Contenido Derecho:**
```
Auto Layout Horizontal, Gap: 8px

├── Badge/Cached [Conditional]
├── Button Icon (ChevronUp, 32x32)
└── Button Icon (Close, 32x32)
```

---

#### **2. Bullets Section**

**Auto Layout:**
```
Direction: Vertical
Gap: 12px
Padding: 0
```

**Contenido:**
```
├── BulletItem (Instance) x4
│   └── Text: "Ventas totales alcanzaron $1.2M..."
│       └── Aplicar highlight components a números
```

**Implementación del Highlight:**

En Figma, crear un Text con estilos mezclados:
1. Selecciona el número (ej: "$1.2M")
2. Aplica estilos individuales:
   - Color: Blue/700
   - Weight: Bold
   - Opcional: Background (usando rectangle detrás)

**Truco para backgrounds en texto:**
```
Crear component "Highlight Chip":
├── Rectangle (auto-width, height: 20px, bg: Blue/50, radius: 4px, padding: 0 4px)
└── Text inside (Blue/700, Bold)
```

---

#### **3. Footer Section**

**Auto Layout:**
```
Direction: Vertical
Gap: 16px
Padding-top: 16px
Border-top: 1px, Purple/200
```

**Metadata Row:**
```
Auto Layout Horizontal, Gap: 8px, Wrap

├── Badge/Model "🤖 gemini-1.5-flash"
├── Divider (4px circle, Gray/300)
├── Badge/Tokens "856 tokens"
├── Divider
└── Text "hace 2 min" (Caption/Regular, Gray/500)
```

**Actions Row:**
```
Auto Layout Horizontal, Justify: Space between

├── Left Group (Gap: 12px)
│   ├── Text "¿Te fue útil?" (Body/Medium, Gray/600)
│   └── Feedback Group (Gap: 4px)
│       ├── FeedbackButton/Positive
│       └── FeedbackButton/Negative
│
└── Right Group (Gap: 8px)
    ├── Button/Secondary "Regenerar" (icon: RefreshCw)
    └── Button/Primary "Exportar" (icon: Share2)
```

---

### 🎨 Efectos Decorativos (Background Blur)

**Crear 2 círculos:**

**Círculo 1:**
```
Size: 256x256
Position: Top-right (-50px, -50px)
Fill: Radial Gradient
  ├── Center: Purple/200, 40% opacity
  └── Edge: Transparent
Blur: 60px
Blend Mode: Normal
Z-index: 0 (Send to back)
```

**Círculo 2:**
```
Size: 192x192
Position: Bottom-left (-30px, -30px)
Fill: Radial Gradient
  ├── Center: Indigo/200, 30% opacity
  └── Edge: Transparent
Blur: 50px
Blend Mode: Normal
Z-index: 0
```

---

## 4. Variantes del Panel

### 🔄 LoadingState Component

**Nombre:** `AIInsightPanel/Loading`

**Estructura:**
```
Mismo container base, pero reemplazar bullets con:

├── Progress Bar
│   ├── Container (height: 8px, width: 100%, bg: Purple/100, radius: Full)
│   └── Fill (height: 8px, bg: Gradient/Purple-Blue, radius: Full)
│       └── Animate width 0→100% (use Auto-animate en Prototype)
│
└── Skeleton Bullets (x4)
    ├── Auto Layout Horizontal, Gap: 12px
    ├── Dot (8x8, Purple/400, pulse animation)
    └── Rectangle (height: 16px, width: random%, bg: Purple/200, radius: 4px)
        └── Usar Shimmer effect (ver abajo)
```

**Shimmer Effect en Figma:**
```
1. Crear rectangle con gradient:
   ├── Linear gradient horizontal
   ├── Stops: Gray/200 → White → Gray/200
   └── Angle: 0°

2. Duplicate y offset para efecto:
   ├── Frame 1: gradient position 0%
   ├── Frame 2: gradient position 100%
   └── Prototype: Auto-animate loop 2000ms
```

---

### ❌ ErrorState Component

**Nombre:** `AIInsightPanel/Error`

**Cambios vs Success:**
```
├── Border: Yellow/300
├── Background: Gradient Yellow/50 → Orange/50
├── Icon: AlertTriangle (Yellow/600)
├── Title: "No se pudo generar el resumen"
└── Content:
    ├── Message text
    ├── Fallback bullets container (white bg)
    └── Retry button (Yellow/500 bg)
```

---

### 📌 MinimizedState Component

**Nombre:** `AIInsightPanel/Minimized`

**Dimensiones:**
```
Width: Fill container
Height: 48px
```

**Auto Layout:**
```
Direction: Horizontal
Padding: H 24px, V 12px
Justify: Space between
Fill: Gradient/Purple-Blue
Corner Radius: 16px
```

**Contenido:**
```
├── Left (Auto Layout, Gap: 12px)
│   ├── Sparkles icon (20x20, White, pulse)
│   └── Text "Ver Resumen Ejecutivo IA" (Body/Semibold, White)
│
└── Right
    └── ChevronDown icon (20x20, White)
```

**Hover State:**
```
Shadow: XL
Scale: 1.01
Cursor: Pointer
```

---

## 5. Dashboard Context (Pantalla Completa)

### 📱 Frame: Desktop 1440px

**Nombre:** `Dashboard - With AI Insights`

**Estructura:**
```
├── Header (height: 80px, bg: White, shadow: SM)
│   ├── Logo section
│   └── User profile
│
├── Main Content (padding: 24px)
│   ├── Dashboard Title + Filters
│   ├── AIInsightPanel/Success ← El componente
│   └── Metabase Embed Mock
│
└── Background (bg: Gray/50)
```

---

### 🎯 Header Component

**Auto Layout:**
```
Width: Fill
Height: 80px
Padding: H 32px
Justify: Space between
Fill: White
Border-bottom: 1px, Gray/200
Shadow: SM
```

**Contenido:**
```
├── Logo Group (Gap: 16px)
│   ├── Icon container (48x48, Purple/600, radius: 8px)
│   │   └── Sparkles (24x24, White)
│   └── Text Group
│       ├── "IOC - Cambiaso" (H2/Bold)
│       └── "Plataforma de Inteligencia..." (Caption)
│
└── Right Group (Gap: 12px)
    ├── Status badge "✓ Sistema activo" (Green)
    └── Avatar (40x40, gradient, text: "A")
```

---

### 🔍 Filters Bar

**Auto Layout:**
```
Direction: Horizontal
Gap: 12px
Padding: 0
Wrap: Wrap
```

**Filter Chip Component:**
```
Variant: Type (Region, Canal, Periodo)

Structure:
├── Auto Layout Horizontal, Gap: 8px
├── Padding: H 16px, V 10px
├── Fill: White
├── Border: 1px, Gray/200
├── Radius: 8px
├── Shadow: SM

Content:
├── Icon (16x16, Gray/500)
├── Label text (Body/Medium, Gray/700)
└── Value text (Body/Semibold, Purple/600)
    OR Dropdown chevron
```

---

### 📊 Metabase Mock

**Frame:**
```
Width: Fill container
Height: 600px (or auto)
Padding: 24px
Fill: White
Border: 1px, Gray/200
Radius: 12px
Shadow: SM
```

**KPI Cards Grid:**
```
Auto Layout Horizontal, Gap: 16px

Card structure (each):
├── Size: 1fr (equal distribution)
├── Padding: 20px
├── Radius: 8px
├── Fill: Gradient (specific per metric)
├── Border: 1px matching gradient

Content:
├── Header (icon + label)
├── Value (H1/Bold)
└── Change indicator (color-coded)
```

**KPI 1 (Ventas):**
```
Gradient: Blue/50 → Blue/100
Border: Blue/200
Icon: TrendingDown (Red/500)
Value: "$1.2M" (Gray/900)
Change: "▼ 8%" (Red/600, bg: Red/50, radius: 4px)
```

---

## 6. Componentes Avanzados

### 💬 Export Menu (Dropdown)

**Frame: `ExportMenu`**

**Configuración:**
```
Width: 180px
Padding: 8px
Fill: White
Border: 1px, Gray/200
Radius: 8px
Shadow: XL
```

**Items:**
```
Auto Layout Vertical, Gap: 0

MenuItem component (x3):
├── Padding: H 12px, V 10px
├── Radius: 6px
├── Hover: bg Gray/50
├── Content:
    ├── Icon (16x16, Gray/600)
    └── Text (Body/Regular, Gray/700)

Items:
1. "Descargar PDF" (Download icon)
2. "Enviar por Email" (Send icon)
3. "Copiar enlace" (Share2 icon)
```

**Prototype:**
```
Trigger: Click on "Exportar" button
Action: Open overlay
Position: Below button, aligned right
Overlay: Translucent black 20%
```

---

### 📱 Mobile Variant

**Frame: iPhone 14 Pro (393x852)**

**Adjustments:**
```
Panel:
├── Padding: 16px (reduced)
├── Bullets font-size: 13px
├── Stack badges vertically
├── Single-column footer

MinimizedState:
├── Fixed bottom (iOS safe area)
├── Full width - 32px margin
```

---

## 7. Prototipo Interactivo

### 🎬 Flujos a Prototipar

#### **Flujo 1: Carga y Expansión**

```
Screen 1: Loading State (2s auto-advance)
    ↓ After delay
Screen 2: Success State (expanded)
    ↓ Click "Minimizar"
Screen 3: Minimized State
    ↓ Click anywhere
Screen 2: Success State
```

**Configuración:**
```
Transition: Smart Animate
Duration: 300ms
Easing: Ease out
```

---

#### **Flujo 2: Feedback**

```
Screen: Success State
    ↓ Click "👍"
Screen: Success State (ThumbsUp active + confirmation message)
```

**Interacción:**
```
Trigger: Click
Action: Change to (ThumbsUp active variant)
Animation: Smart Animate
Duration: 200ms

Confirmation:
├── Animate in from bottom
├── Green/50 background
├── Auto-hide after 3s (use Time after delay)
```

---

#### **Flujo 3: Export Menu**

```
Screen: Success State
    ↓ Click "Exportar"
Screen: Success State + ExportMenu overlay
    ↓ Click outside
Screen: Success State
```

**Overlay Config:**
```
Background: Black 20% opacity
Click outside: Close overlay
Menu: Slide from top (100ms)
```

---

#### **Flujo 4: Error Recovery**

```
Screen: Error State
    ↓ Click "Reintentar"
Screen: Loading State (2s)
    ↓ Auto
Screen: Success State
```

---

### 🎨 Animaciones Específicas

**Pulse en Loading:**
```
1. Duplicate frame
2. Change opacity: 100% → 40% → 100%
3. Prototype: After delay, navigate to self
4. Duration: 2000ms, Loop
```

**Shimmer en Skeleton:**
```
1. Create 2 frames with gradient offset
2. Use Smart Animate
3. After delay: 1500ms
4. Loop infinitely
```

**Scale en Hover:**
```
While hovering:
├── Scale: 1.02
├── Shadow: XL
├── Duration: 200ms
└── Easing: Ease out
```

---

## 8. Specs para Developers (Handoff)

### 📋 Panel de Inspect

**Crear frame "Developer Handoff":**

```
Layout grid:
├── Columns: 12
├── Gutter: 24px
├── Margin: 32px

Annotations:
├── All spacing values
├── Color variables (link to tokens)
├── Typography scale
├── Border radius values
├── Shadow definitions
```

---

### 📐 Measurement Tool

**Usar Figma's built-in inspect:**

1. Select panel component
2. In right sidebar: Toggle "Inspect"
3. Developers can click elements to see:
   ```
   ├── CSS code
   ├── iOS code
   ├── Android code
   └── Measurements
   ```

---

### 🎨 Export Assets

**Icons to Export:**
```
Exportar cada icon como SVG:
├── Sparkles.svg
├── ThumbsUp.svg
├── ThumbsDown.svg
├── RefreshCw.svg
├── Share2.svg
├── ChevronUp.svg
└── Close.svg

Settings:
├── Format: SVG
├── Scale: 1x
├── Suffix: None
```

---

## 9. Plugins Recomendados

### Esenciales:

```
1. Iconify
   └── Acceso rápido a Lucide icons

2. Unsplash
   └── Imágenes placeholder

3. Stark
   └── Check de contraste y accesibilidad

4. Content Reel
   └── Data mock (nombres, emails, números)

5. Autoflow
   └── Crear diagramas de flujo

6. Contrast
   └── Validar WCAG compliance

7. Component Inspector
   └── Auditoría de componentes
```

---

## 10. Link a Template Pre-construido

Como no puedo crear archivos Figma directamente aquí, te doy **dos opciones**:

### Opción A: Duplicar Community File

Busca en Figma Community:
```
Template similar ya existente:
"Dashboard UI Kit" + "AI Components"

Luego personaliza:
├── Colores → IOC palette
├── Typography → Inter
└── Componentes → según specs arriba
```

### Opción B: Partir de UI Kit

```
1. File > New from template
2. Buscar: "Dashboard Analytics Template"
3. Reemplazar componentes con los del sistema IOC
```

---

## 📦 Checklist de Implementación

```
Design Tokens:
☐ Crear color styles (30 colors)
☐ Crear text styles (12 styles)
☐ Definir spacing scale
☐ Configurar shadows
☐ Crear gradients

Components:
☐ Button (4 variants)
☐ Badge (3 types)
☐ BulletItem
☐ FeedbackButton
☐ ExportMenu

Main Panels:
☐ AIInsightPanel/Success
☐ AIInsightPanel/Loading
☐ AIInsightPanel/Error
☐ AIInsightPanel/Minimized

Screens:
☐ Dashboard Desktop (1440px)
☐ Dashboard Mobile (393px)
☐ Metabase mock

Prototype:
☐ Loading flow
☐ Expand/collapse
☐ Feedback interaction
☐ Export menu
☐ Error recovery

Handoff:
☐ Dev specs annotations
☐ Export icons SVG
☐ Color variables CSS
☐ Typography CSS
```

---

## 🎯 Guía Rápida de 30 Minutos

Si quieres el **MVP más rápido** en Figma:

```
Minuto 0-5: Setup
├── Crear file
├── Instalar plugin Iconify
└── Configurar 8px grid

Minuto 5-10: Tokens
├── 6 colores básicos (Purple, Blue, Gray, Red, Green, Yellow)
├── 3 text styles (Heading, Body, Caption)
└── 2 shadows (MD, LG)

Minuto 10-20: Panel Success
├── Container con auto-layout
├── Header con icon + text
├── 4 bullets (text plain)
└── Footer con buttons

Minuto 20-25: Loading variant
├── Duplicate panel
├── Replace bullets con rectangles
└── Add pulse animation

Minuto 25-30: Prototype
├── Connect loading → success
├── Add "minimize" interaction
└── Test en Presentation mode
```

---

## 🚀 ¿Quieres que...?

1. **Te cree un archivo Figma completo** y te comparta el link (necesitaría tu email de Figma)
2. **Te grabe un video Loom** construyendo el panel paso a paso
3. **Te genere un FigJam** con el wireframe y flujo de usuario primero
4. **Te exporte el código CSS** de todos los design tokens automáticamente

Dime qué prefieres y lo hago ahora mismo! 🎨