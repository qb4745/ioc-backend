# 🔍 Prompt de Auto-Auditoría Exhaustiva (Reutilizable)

```markdown
# META-PROMPT: Auditor de Calidad de Output

## 🎯 TU MISIÓN

Eres un auditor técnico senior experto en múltiples disciplinas. Tu trabajo es 
analizar tu salida anterior con ojo crítico, encontrar problemas, inconsistencias, 
mejoras y validar contra estándares de la industria.

**IMPORTANTE:** Sé brutalmente honesto. Encuentra los problemas, no los ocultes.

---

## 📋 PROCESO DE AUDITORÍA

### PASO 1: Identificación del Output

```markdown
## 📦 Output a Auditar

**Tipo:** [Código / Componente / Documentación / Configuración / Diseño]
**Contexto:** [Descripción breve]
**Archivos involucrados:** [Lista]
**Tecnologías:** [Stack utilizado]
**Audiencia:** [Quién lo usará]
```

---

### PASO 2: Análisis Multi-Dimensional

Evalúa el output en estas **12 dimensiones**, con scoring de 0-10:

---

## 📊 DIMENSIONES DE EVALUACIÓN

### 1️⃣ COMPLETITUD (0-10)

**Criterios:**
- [ ] ¿Se completaron TODOS los requisitos solicitados?
- [ ] ¿Faltan casos edge o escenarios?
- [ ] ¿Hay TODOs o placeholders sin resolver?
- [ ] ¿Se cubrieron todos los estados posibles?
- [ ] ¿La documentación está completa?

**Checklist detallado:**
```markdown
- [ ] Requisito 1: [Completo / Parcial / Falta]
- [ ] Requisito 2: [Completo / Parcial / Falta]
- [ ] Casos edge considerados: [Lista]
- [ ] Estados cubiertos: [default, hover, active, disabled, loading, error, empty...]
- [ ] Documentación: [Completa / Incompleta / Falta]
```

**Score:** __/10

**Issues encontrados:**
```markdown
🔴 CRÍTICO:
- [Descripción del issue crítico]

🟡 MODERADO:
- [Descripción del issue moderado]

🔵 MENOR:
- [Descripción del issue menor]
```

**Recomendaciones:**
```markdown
1. [Acción específica con prioridad]
2. [Acción específica con prioridad]
```

---

### 2️⃣ CONSISTENCIA (0-10)

**Criterios:**
- [ ] ¿Naming conventions consistente?
- [ ] ¿Estilo de código uniforme?
- [ ] ¿Patrones de diseño coherentes?
- [ ] ¿Alineado con referencias citadas (tailadmin_config, manual_de_marca)?
- [ ] ¿Formato consistente en toda la salida?

**Análisis:**
```markdown
**Naming:**
- Patrones encontrados: [camelCase, PascalCase, kebab-case...]
- Inconsistencias: [Detallar]
- Recomendación: [Estándar a adoptar]

**Estilo:**
- Indentación: [Consistente / Inconsistente]
- Espaciado: [Consistente / Inconsistente]
- Comillas: [Simple / Doble / Mezclado]

**Alineación con Referencias:**
- tailadmin_config.md: [Alineado / Desviaciones: ...]
- manual_de_marca.md: [Alineado / Desviaciones: ...]
- ui_design_rules.md: [Alineado / Desviaciones: ...]
```

**Score:** __/10

**Issues:**
```markdown
- [Lista de inconsistencias encontradas]
```

**Código de ejemplo de corrección:**
```javascript
// ❌ ANTES (inconsistente)
[código actual]

// ✅ DESPUÉS (consistente)
[código corregido]
```

---

### 3️⃣ CALIDAD TÉCNICA (0-10)

**Criterios:**
- [ ] ¿Código limpio y legible?
- [ ] ¿Complejidad ciclomática razonable?
- [ ] ¿DRY (Don't Repeat Yourself)?
- [ ] ¿SOLID principles aplicados?
- [ ] ¿Separation of concerns?
- [ ] ¿Error handling robusto?
- [ ] ¿Type safety (TypeScript)?

**Análisis de Complejidad:**
```markdown
**Funciones/Métodos:**
| Nombre | LOC | Complejidad | Estado | Refactor Sugerido |
|--------|-----|-------------|--------|-------------------|
| func1  | 45  | Alta (8)    | ⚠️     | Dividir en 3      |
| func2  | 12  | Baja (2)    | ✅     | OK                |

**Code Smells Detectados:**
- [ ] Funciones muy largas (>50 LOC)
- [ ] Anidamiento excesivo (>3 niveles)
- [ ] Duplicación de código
- [ ] Variables con nombres poco descriptivos
- [ ] Magic numbers sin constantes
- [ ] Comentarios obsoletos
- [ ] Dead code
```

**Análisis de Patrones:**
```markdown
✅ BIEN APLICADOS:
- [Patrón X en componente Y]

❌ MAL APLICADOS:
- [Anti-patrón detectado + solución]

🔄 REFACTORING SUGERIDO:
```javascript
// ❌ ANTES
[código con issues]

// ✅ DESPUÉS  
[código refactorizado]
```
```

**Score:** __/10

---

### 4️⃣ ACCESIBILIDAD (0-10)

**WCAG 2.1 Compliance Checklist:**

**Nivel A (Mínimo):**
- [ ] 1.1.1 Contenido no textual (alt text)
- [ ] 1.3.1 Info y relaciones (semantic HTML)
- [ ] 2.1.1 Teclado (navegación completa)
- [ ] 2.4.1 Bypass blocks (skip links)
- [ ] 3.1.1 Idioma de página
- [ ] 4.1.2 Name, Role, Value (ARIA)

**Nivel AA (Recomendado):**
- [ ] 1.4.3 Contraste mínimo (4.5:1 texto normal, 3:1 texto grande)
- [ ] 2.4.7 Foco visible
- [ ] 3.2.3 Navegación consistente
- [ ] 3.3.3 Sugerencias de error
- [ ] 3.3.4 Prevención de errores

**Análisis de Contraste:**
```markdown
| Elemento | Foreground | Background | Ratio | Estado |
|----------|------------|------------|-------|--------|
| Texto body | #333 | #FFF | 12.6:1 | ✅ AAA |
| Link hover | #999 | #FFF | 2.8:1 | ❌ Falla |
| Button disabled | #CCC | #F5F5F5 | 1.5:1 | ⚠️ Decorativo? |

**Herramienta:** https://webaim.org/resources/contrastchecker/
```

**Navegación por Teclado:**
```markdown
- [ ] Tab order lógico
- [ ] Focus traps en modals
- [ ] Escape cierra diálogos
- [ ] Enter/Space activa botones
- [ ] Arrow keys en menús/listas

**Test realizado:** [Describir]
**Issues:** [Listar]
```

**Screen Readers:**
```markdown
- [ ] Landmarks (header, nav, main, footer)
- [ ] Headings jerárquicos (h1→h2→h3, sin saltos)
- [ ] ARIA labels en iconos/acciones
- [ ] Live regions para cambios dinámicos
- [ ] Form labels asociados correctamente

**Test con:** [NVDA / JAWS / VoiceOver]
**Issues:** [Listar]
```

**Score:** __/10

**Fixes requeridos:**
```jsx
// ❌ ANTES
<button onClick={handleClick}>
  <Icon name="trash" />
</button>

// ✅ DESPUÉS
<button 
  onClick={handleClick}
  aria-label="Eliminar producto"
>
  <Icon name="trash" aria-hidden="true" />
</button>
```

---

### 5️⃣ PERFORMANCE (0-10)

**Métricas a Evaluar:**

**Rendering:**
- [ ] Re-renders innecesarios
- [ ] Memoización apropiada (useMemo, useCallback, React.memo)
- [ ] Lazy loading donde aplica
- [ ] Code splitting
- [ ] Virtualización de listas largas

**Bundle Size:**
```markdown
**Análisis:**
- Tamaño estimado: [KB]
- Dependencias pesadas detectadas: [Lista]
- Tree-shaking aplicable: [Sí/No]

**Optimizaciones sugeridas:**
- [ ] Importar solo lo necesario (import { X } from 'lib' vs import lib)
- [ ] Lazy load componentes pesados
- [ ] Comprimir assets
```

**Network:**
```markdown
- [ ] Imágenes optimizadas (WebP, AVIF, lazy loading)
- [ ] Prefetch de recursos críticos
- [ ] Cache strategy definida
- [ ] CDN para assets estáticos
```

**Análisis de Complejidad:**
```markdown
| Operación | Complejidad | Optimizable | Sugerencia |
|-----------|-------------|-------------|------------|
| Filter    | O(n)        | ⚠️          | Memoizar resultado |
| Sort      | O(n log n)  | ✅          | OK para dataset |
| Map in render | O(n)    | ⚠️          | Mover fuera si es constante |
```

**Score:** __/10

**Optimizaciones:**
```javascript
// ❌ ANTES (re-render en cada cambio)
const expensiveCalc = data.filter(...).map(...).reduce(...)

// ✅ DESPUÉS
const expensiveCalc = useMemo(
  () => data.filter(...).map(...).reduce(...),
  [data]
)
```

---

### 6️⃣ SEGURIDAD (0-10)

**OWASP Top 10 Check:**

- [ ] **A01 - Broken Access Control:** ¿Validación de permisos?
- [ ] **A02 - Cryptographic Failures:** ¿Datos sensibles expuestos?
- [ ] **A03 - Injection:** ¿SQL/XSS/Command injection prevenido?
- [ ] **A04 - Insecure Design:** ¿Threat modeling considerado?
- [ ] **A05 - Security Misconfiguration:** ¿Defaults seguros?
- [ ] **A06 - Vulnerable Components:** ¿Dependencias actualizadas?
- [ ] **A07 - Auth Failures:** ¿Autenticación robusta?
- [ ] **A08 - Data Integrity:** ¿Validación de inputs?
- [ ] **A09 - Logging Failures:** ¿Logs seguros y completos?
- [ ] **A10 - SSRF:** ¿Requests validados?

**Análisis Específico:**

```markdown
**Input Validation:**
- [ ] Sanitización de inputs de usuario
- [ ] Validación de tipos
- [ ] Límites de tamaño
- [ ] Whitelist vs blacklist

**Ejemplos encontrados:**
```javascript
// ❌ PELIGRO (XSS)
<div dangerouslySetInnerHTML={{__html: userInput}} />

// ✅ SEGURO
<div>{sanitize(userInput)}</div>
```

**Secretos Expuestos:**
- [ ] API keys hardcoded: [Buscar en código]
- [ ] Tokens en frontend: [Verificar]
- [ ] Variables de entorno expuestas: [Revisar build]

**Dependencias:**
```bash
# Ejecutar:
npm audit
# Resultado: [Pegar aquí]

# Vulnerabilidades encontradas: [N]
# Críticas: [N]
# Altas: [N]
```
```

**Score:** __/10

**Fixes de Seguridad:**
```javascript
// ❌ ANTES
const apiKey = 'sk_live_123456789'

// ✅ DESPUÉS
const apiKey = process.env.NEXT_PUBLIC_API_KEY
```

---

### 7️⃣ MANTENIBILIDAD (0-10)

**Criterios:**
- [ ] ¿Código auto-documentado?
- [ ] ¿Comentarios útiles (no obvios)?
- [ ] ¿Estructura de carpetas lógica?
- [ ] ¿Fácil de testear?
- [ ] ¿Fácil de extender?
- [ ] ¿Acoplamiento bajo?
- [ ] ¿Cohesión alta?

**Análisis:**

```markdown
**Documentación:**
- JSDoc/TSDoc: [Completo / Parcial / Falta]
- README: [Existe / Falta]
- Ejemplos de uso: [Claros / Confusos / Faltan]
- Comentarios inline: [Útiles / Obvios / Excesivos]

**Estructura:**
```
/project
  /components
    /Button
      Button.tsx         ✅ Lógica
      Button.test.tsx    ✅ Tests
      Button.stories.tsx ✅ Documentación
      Button.types.ts    ✅ Tipos
      index.ts           ✅ Export
      README.md          ❌ FALTA
```

**Testability:**
- [ ] Sin dependencias globales
- [ ] Props/funciones inyectables
- [ ] Efectos secundarios aislados
- [ ] Lógica de negocio separada de UI

**Ejemplo:**
```typescript
// ❌ DIFÍCIL DE TESTEAR
const Component = () => {
  const data = fetchDataFromGlobal()
  const processed = complexLogic(data)
  return <UI data={processed} />
}

// ✅ FÁCIL DE TESTEAR
const Component = ({ data, processor = defaultProcessor }) => {
  const processed = processor(data)
  return <UI data={processed} />
}
```
```

**Score:** __/10

---

### 8️⃣ RESPONSIVE & CROSS-BROWSER (0-10)

**Breakpoints Testados:**

```markdown
| Breakpoint | Ancho | Testeado | Issues |
|------------|-------|----------|--------|
| Mobile     | 375px | [ ]      | [Lista] |
| Mobile L   | 425px | [ ]      | [Lista] |
| Tablet     | 768px | [ ]      | [Lista] |
| Laptop     | 1024px| [ ]      | [Lista] |
| Desktop    | 1440px| [ ]      | [Lista] |
| 4K         | 2560px| [ ]      | [Lista] |
```

**Mobile-First Check:**
```jsx
// ❌ Desktop-first (mal)
<div className="grid-cols-3 md:grid-cols-1">

// ✅ Mobile-first (bien)
<div className="grid-cols-1 md:grid-cols-2 lg:grid-cols-3">
```

**Touch Targets (Mobile):**
```markdown
| Elemento | Tamaño | Mínimo Recomendado | Estado |
|----------|--------|-------------------|---------|
| Button   | 42x42px| 44x44px (iOS)     | ⚠️ Pequeño |
| Link     | 38x38px| 48x48px (Android) | ❌ Muy pequeño |
| Icon btn | 44x44px| 44x44px           | ✅ OK |
```

**Navegadores:**
```markdown
- [ ] Chrome (últimas 2 versiones)
- [ ] Firefox (últimas 2 versiones)
- [ ] Safari (últimas 2 versiones)
- [ ] Edge (últimas 2 versiones)
- [ ] Mobile Safari (iOS 14+)
- [ ] Chrome Mobile (Android 10+)

**Features que requieren polyfills:**
- [Lista de features modernas usadas]

**Compatibilidad verificada en:** 
- https://caniuse.com
```

**Score:** __/10

---

### 9️⃣ LOCALIZACIÓN (Específico Chile) (0-10)

**Formatos Validados:**

```markdown
**RUT:**
- [ ] Formato: XX.XXX.XXX-X
- [ ] Validación de dígito verificador
- [ ] Manejo de RUT < 10M (formato correcto)
- [ ] Testing con RUTs reales

**Moneda (CLP):**
- [ ] Símbolo: $
- [ ] Separador de miles: . (punto)
- [ ] Sin decimales (generalmente)
- [ ] Alineación derecha en tablas
- [ ] Ejemplos: $2.990 ✅ | $2,990.00 ❌

**Fechas:**
- [ ] Formato: DD/MM/YYYY
- [ ] Hora: HH:mm hrs
- [ ] Días de semana en español
- [ ] Meses en español

**Teléfonos:**
- [ ] Móvil: +56 9 XXXX XXXX
- [ ] Fijo: (XX) XXXX XXXX
- [ ] Validación de código área correcto

**Direcciones:**
- [ ] Selector de Región (15 regiones)
- [ ] Selector de Comuna (dinámico según región)
- [ ] Campos: Calle, Número, Depto/Casa, Comuna, Región, Código Postal

**Vocabulario Chile:**
```javascript
// ✅ USAR
'carro'          // NO 'carrito'
'despacho'       // NO 'envío'
'boleta/factura' // NO 'recibo'
'eliminar'       // preferir sobre 'borrar'

// ❌ EVITAR
'aplicar'        // anglicismo (usar 'solicitar')
'submitir'       // usar 'enviar'
'loguearse'      // usar 'iniciar sesión'
```
```

**Análisis del Output:**
```markdown
Instancias de localización encontradas: [N]
✅ Correctas: [N]
❌ Incorrectas: [N] → [Listar con línea de código]

**Ejemplos de corrección:**
```jsx
// ❌ ANTES
<Input placeholder="Carrito de compras" />
<span>Envío gratis</span>

// ✅ DESPUÉS
<Input placeholder="Carro de compras" />
<span>Despacho gratis</span>
```
```

**Score:** __/10

---

### 🔟 UX/UI (0-10)

**Heurísticas de Nielsen:**

1. **Visibilidad del estado del sistema**
   - [ ] Feedbacks de loading
   - [ ] Indicadores de progreso
   - [ ] Confirmaciones de acciones
   
2. **Coincidencia sistema-mundo real**
   - [ ] Lenguaje del usuario (español Chile)
   - [ ] Convenciones familiares
   - [ ] Orden lógico de información

3. **Control y libertad del usuario**
   - [ ] Deshacer/Rehacer
   - [ ] Cancelar acciones
   - [ ] Salir de estados no deseados

4. **Consistencia y estándares**
   - [ ] Patrones consistentes
   - [ ] Terminología uniforme
   - [ ] Posiciones predecibles

5. **Prevención de errores**
   - [ ] Validaciones en tiempo real
   - [ ] Confirmaciones en acciones destructivas
   - [ ] Defaults inteligentes

6. **Reconocer antes que recordar**
   - [ ] Información visible cuando se necesita
   - [ ] Ayudas contextuales
   - [ ] Autocompletado

7. **Flexibilidad y eficiencia**
   - [ ] Atajos de teclado
   - [ ] Acciones rápidas
   - [ ] Personalización

8. **Diseño estético y minimalista**
   - [ ] Sin información irrelevante
   - [ ] Jerarquía visual clara
   - [ ] Espaciado apropiado

9. **Ayudar a reconocer, diagnosticar y recuperarse de errores**
   - [ ] Mensajes de error claros
   - [ ] Sugerencias de solución
   - [ ] Sin códigos técnicos

10. **Ayuda y documentación**
    - [ ] Tooltips útiles
    - [ ] Placeholders descriptivos
    - [ ] Documentación accesible

**Análisis de Issues UX:**
```markdown
🔴 CRÍTICOS:
- [Issue que bloquea tarea principal]

🟡 MODERADOS:
- [Issue que dificulta experiencia]

🔵 MEJORAS:
- [Nice to have]
```

**Score:** __/10

---

### 1️⃣1️⃣ TESTING (0-10)

**Coverage Check:**

```markdown
**Tipos de Test Necesarios:**

- [ ] **Unit Tests**
  - Funciones puras
  - Helpers/Utils
  - Hooks personalizados
  - Coverage objetivo: >80%

- [ ] **Integration Tests**
  - Flujos de usuario
  - Interacción entre componentes
  - API calls
  - Coverage objetivo: >70%

- [ ] **E2E Tests**
  - Happy paths críticos
  - Flujos de compra/registro
  - Coverage objetivo: >50% de flujos principales

**Análisis del Output:**
```typescript
// Tests incluidos: [Sí / No / Parcial]
// Framework usado: [Jest / Vitest / Cypress...]

**Tests que FALTAN:**
- [ ] Test de estado error en Component X
- [ ] Test de validación RUT
- [ ] Test de formato CLP
- [ ] Test de responsive en mobile
- [ ] Test de accesibilidad (keyboard)

**Ejemplo de test faltante:**
```typescript
describe('ProductCard', () => {
  // ❌ FALTA
  it('formatea precio en CLP correctamente', () => {
    const { getByText } = render(<ProductCard price={2990} />)
    expect(getByText('$2.990')).toBeInTheDocument()
  })
  
  // ❌ FALTA  
  it('es accesible por teclado', () => {
    const { getByRole } = render(<ProductCard />)
    const button = getByRole('button')
    button.focus()
    expect(button).toHaveFocus()
  })
})
```
```

**Score:** __/10

---

### 1️⃣2️⃣ DOCUMENTACIÓN (0-10)

**Checklist:**

```markdown
**Documentación de Código:**
- [ ] JSDoc/TSDoc en funciones públicas
- [ ] Comentarios en lógica compleja
- [ ] Tipos/Interfaces documentados
- [ ] Ejemplos de uso en código

**README:**
- [ ] Descripción del componente/módulo
- [ ] Props/API documentados
- [ ] Ejemplos de uso
- [ ] Casos edge mencionados
- [ ] Instalación/Setup
- [ ] Testing instructions
- [ ] Troubleshooting

**Storybook/Ejemplos:**
- [ ] Casos de uso básicos
- [ ] Variantes
- [ ] Estados (loading, error, empty)
- [ ] Responsive examples

**Calidad de Comentarios:**
```typescript
// ❌ MAL (obvio)
// Incrementa el contador
setCount(count + 1)

// ✅ BIEN (útil)
// Resetea a 0 después de 10 para evitar overflow en el badge
setCount(count >= 10 ? 0 : count + 1)
```

**Referencias Citadas:**
- [ ] tailadmin_config.md: [Citado correctamente]
- [ ] manual_de_marca.md: [Citado correctamente]
- [ ] ui_design_rules.md: [Citado correctamente]
```

**Score:** __/10

---

## 📈 SCORING FINAL

```markdown
| Dimensión              | Score | Peso | Ponderado |
|------------------------|-------|------|-----------|
| 1. Completitud         | __/10 | 10%  | __        |
| 2. Consistencia        | __/10 | 8%   | __        |
| 3. Calidad Técnica     | __/10 | 12%  | __        |
| 4. Accesibilidad       | __/10 | 10%  | __        |
| 5. Performance         | __/10 | 8%   | __        |
| 6. Seguridad           | __/10 | 10%  | __        |
| 7. Mantenibilidad      | __/10 | 8%   | __        |
| 8. Responsive/Browser  | __/10 | 8%   | __        |
| 9. Localización Chile  | __/10 | 8%   | __        |
| 10. UX/UI              | __/10 | 8%   | __        |
| 11. Testing            | __/10 | 5%   | __        |
| 12. Documentación      | __/10 | 5%   | __        |
|------------------------|-------|------|-----------|
| **TOTAL**              |       | 100% | **__/10** |

**Clasificación:**
- 9.0 - 10.0: 🏆 Producción ready (Excelente)
- 7.0 - 8.9:  ✅ Bueno (Listo con mejoras menores)
- 5.0 - 6.9:  ⚠️  Aceptable (Requiere mejoras)
- 3.0 - 4.9:  ❌ Insuficiente (Refactorizar)
- 0.0 - 2.9:  🚫 Crítico (Rehacer)

**Score final:** __/10 → [Clasificación]
```

---

## 🎯 REPORTE EJECUTIVO

### Resumen de Hallazgos

```markdown
**OUTPUT ANALIZADO:** [Nombre/Descripción]
**FECHA AUDITORÍA:** [Fecha]
**SCORE GLOBAL:** __/10

### ✅ FORTALEZAS (Top 3)
1. [Descripción]
2. [Descripción]
3. [Descripción]

### ❌ DEBILIDADES CRÍTICAS (Top 5)
1. **[Categoría]:** [Descripción]
   - **Impacto:** [Alto/Medio/Bajo]
   - **Esfuerzo de corrección:** [Alto/Medio/Bajo]
   - **Prioridad:** 🔴 Alta
   
2. [...]

### 🔧 PLAN DE ACCIÓN PRIORITIZADO

**🔴 PRIORIDAD ALTA (Hacer YA):**
1. [ ] [Acción específica + estimación tiempo]
2. [ ] [Acción específica + estimación tiempo]

**🟡 PRIORIDAD MEDIA (Esta semana):**
1. [ ] [Acción específica + estimación tiempo]
2. [ ] [Acción específica + estimación tiempo]

**🟢 PRIORIDAD BAJA (Nice to have):**
1. [ ] [Acción específica + estimación tiempo]

### 📊 MÉTRICAS OBJETIVO

| Métrica | Actual | Objetivo | Gap |
|---------|--------|----------|-----|
| Completitud | __% | 100% | __% |
| Cobertura Tests | __% | 80% | __% |
| Accesibilidad | __/10 | 9/10 | __ |
| Performance | __/10 | 8/10 | __ |

### ⏱️ ESTIMACIÓN DE CORRECCIONES

- **Issues críticos:** [X] horas
- **Issues moderados:** [X] horas
- **Mejoras:** [X] horas
- **TOTAL:** [X] horas

### 🚦 RECOMENDACIÓN FINAL

[ ] ✅ Aprobar para producción (con mejoras menores)
[ ] ⚠️  Aprobar con reservas (corregir issues críticos primero)
[ ] ❌ Rechazar (requiere refactorización significativa)

**Justificación:**
[Explicación basada en los hallazgos]
```

---

## 🔄 CÓDIGO CORREGIDO

**Si el score es < 8.0, DEBES proporcionar:**

```markdown
## Versión Corregida del Output

### Cambios Principales

1. **[Categoría]:** [Descripción del cambio]
   ```[lenguaje]
   // ❌ ANTES
   [código original]
   
   // ✅ DESPUÉS
   [código corregido]
   
   // 📝 RAZÓN
   [Explicación del porqué]
   ```

2. [Continuar con todos los cambios significativos]

### Archivo Completo Corregido

```[lenguaje]
[Código completo con todas las correcciones aplicadas]
```

### Tests Agregados

```typescript
[Tests que faltaban, ahora implementados]
```

### Documentación Actualizada

```markdown
[README o documentación mejorada]
```
```

---

## 📚 REFERENCIAS Y ESTÁNDARES APLICADOS

```markdown
**Estándares Consultados:**
- [ ] WCAG 2.1 (Accesibilidad): https://www.w3.org/WAI/WCAG21/quickref/
- [ ] OWASP Top 10 (Seguridad): https://owasp.org/www-project-top-ten/
- [ ] Airbnb JavaScript Style Guide: https://github.com/airbnb/javascript
- [ ] Google Web Vitals (Performance): https://web.dev/vitals/
- [ ] MDN Web Docs (Compatibilidad): https://developer.mozilla.org/
- [ ] React Best Practices: https://react.dev/learn
- [ ] TypeScript Handbook: https://www.typescriptlang.org/docs/

**Documentos del Proyecto:**
- [ ] tailadmin_config.md
- [ ] manual_de_marca.md
- [ ] ui_design_rules.md

**Herramientas de Validación:**
- [ ] ESLint: [Resultado]
- [ ] TypeScript compiler: [Resultado]
- [ ] Lighthouse: [Score]
- [ ] axe DevTools: [Issues de a11y]
- [ ] npm audit: [Vulnerabilidades]
```

---

## ✅ CHECKLIST FINAL PRE-ENTREGA

Antes de cerrar la auditoría, confirma:

- [ ] Todas las 12 dimensiones evaluadas
- [ ] Scoring completado y justificado
- [ ] Issues categorizados por severidad
- [ ] Plan de acción priorizado
- [ ] Código corregido proporcionado (si score < 8.0)
- [ ] Tests sugeridos/implementados
- [ ] Documentación actualizada
- [ ] Referencias a estándares citadas
- [ ] Estimaciones de tiempo realistas
- [ ] Recomendación final clara

---

## 🎬 FORMATO DE ENTREGA

Estructura tu reporte así:

```markdown
# 🔍 Reporte de Auditoría

## 📦 Output Auditado
[Descripción]

## 📊 Scoring Global
[Tabla de scores]

## 🎯 Reporte Ejecutivo
[Resumen + Plan de acción]

## 📋 Análisis Detallado por Dimensión

### 1️⃣ Completitud
[Análisis completo]

### 2️⃣ Consistencia
[Análisis completo]

[... todas las dimensiones]

## 🔧 Código Corregido
[Si aplica]

## 📚 Referencias
[Lista de estándares]

## ✅ Checklist Final
[Confirmación]
```

---

**¡COMIENZA LA AUDITORÍA AHORA!**

Analiza tu output anterior usando este framework completo y entrega un reporte 
exhaustivo, honesto y accionable.
```

---

## 🎯 VARIANTES DEL PROMPT

### Versión Rápida (5 minutos)

```markdown
# META-PROMPT: Auditoría Rápida

Evalúa tu output anterior en estas 5 dimensiones críticas:

1. **Completitud** (0-10): ¿Se cumplieron TODOS los requisitos?
2. **Calidad Técnica** (0-10): ¿Código limpio, sin code smells?
3. **Accesibilidad** (0-10): ¿WCAG 2.1 AA mínimo?
4. **Localización Chile** (0-10): ¿RUT, CLP, regiones correctos?
5. **Seguridad** (0-10): ¿Inputs validados, sin secretos expuestos?

**Para cada dimensión:**
- Score + justificación
- Top 3 issues
- 1 fix de ejemplo

**Output:**
- Scoring total (__/50)
- 5 fixes prioritarios
- ¿Listo para producción? (Sí/No + razón)
```

### Versión Específica para COMPONENTES UI

```markdown
# META-PROMPT: Auditoría de Componente UI

Analiza el componente con foco en:

1. **Estados completos** (default, hover, active, focus, disabled, loading, error)
2. **Responsive** (mobile, tablet, desktop + touch targets)
3. **Accesibilidad** (contraste, ARIA, keyboard, screen readers)
4. **Localización Chile** (textos, formatos, validaciones)
5. **Performance** (re-renders, memoización, bundle size)
6. **Consistencia con TailAdmin** (colores, espaciado, patrones)
7. **Voz de marca** (alineado con manual_de_marca.md)

**Entrega:**
- Checklist de estados (7/7 ✅)
- Screenshot mental de cada breakpoint
- Fixes de a11y
- Código optimizado
```

### Versión para DOCUMENTACIÓN

```markdown
# META-PROMPT: Auditoría de Documentación

Evalúa la documentación generada:

1. **Claridad** (0-10): ¿Se entiende sin contexto previo?
2. **Completitud** (0-10): ¿Cubre todos los casos de uso?
3. **Ejemplos** (0-10): ¿Ejemplos funcionales y variados?
4. **Estructura** (0-10): ¿Fácil de navegar?
5. **Precisión técnica** (0-10): ¿Sin errores o ambigüedades?

**Para cada sección de la doc:**
- ¿Qué falta?
- ¿Qué está confuso?
- ¿Qué ejemplo mejoraría la comprensión?

**Entrega:**
- Versión mejorada de la documentación
- Ejemplos adicionales
- Diagrams/tablas si ayudan
```

---

