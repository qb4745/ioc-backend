# FTV-003: FilePreview (Componente UI)

## Metadata
- **ID**: FTV-003
- **Technical Design**: TD-001-validacion-carga-archivos-etl-v2.md
- **Tipo**: UI Component (Presentacional)
- **Ubicación**: `src/components/FilePreview.tsx`
- **Sprint**: Sprint 2
- **Fecha Creación**: 2025-10-19
- **Estado**: Draft

---

## 1. Propósito

### 1.1. Descripción
Componente de UI que muestra las primeras 14 líneas de un archivo de texto con números de línea, escapando el contenido para prevenir XSS. Incluye scroll si el contenido excede el espacio disponible.

### 1.2. Caso de Uso
```
Como usuario que está validando un archivo,
Cuando el archivo pasa la validación inicial,
Entonces veo un preview de las primeras 14 líneas,
Para verificar visualmente que el contenido es correcto.
```

### 1.3. Ubicación en la App
- Mostrado en `DataIngestionPage` después de seleccionar un archivo
- Ubicado encima de `FileValidationResult`
- Siempre visible durante el proceso de validación (requisito del TD)

---

## 2. Especificación Visual

### 2.1. Wireframe

```
┌─ Preview del Archivo ───────────────────────────────────────────────┐
│ Mostrando primeras 14 líneas de produccion_2024-01-15.txt          │
│                                                                      │
│  1  Fecha de contabilización|Máquina|Número de Log...                │
│  2  15/01/2024|MAQ001|1234567...                                     │
│  3  15/01/2024|MAQ002|1234568...                                     │
│  4  [TOTALES SAP - Línea ignorada en validación] 🔵                  │
│  5  15/01/2024|MAQ003|1234569...                                     │
│  6  15/01/2024|MAQ004|1234570...                                     │
│  7  15/01/2024|MAQ005|1234571...                                     │
│  8  15/01/2024|MAQ006|1234572...                                     │
│  9  15/01/2024|MAQ007|1234573...                                     │
│ 10  15/01/2024|MAQ008|1234574...                                     │
│ 11  15/01/2024|MAQ009|1234575...                                     │
│ 12  15/01/2024|MAQ010|1234576...                                     │
│ 13  15/01/2024|MAQ011|1234577...                                     │
│ 14  15/01/2024|MAQ012|1234578...                                     │
│                                                                      │
│ ℹ️ La línea 4 contiene totales de SAP y no se valida                 │
└──────────────────────────────────────────────────────────────────────┘
```

### 2.2. Estados Visuales

**Estado Normal**:
- Fondo gris claro (`bg-gray-50`)
- Texto monospace para alineación
- Números de línea en gris (`text-gray-500`)
- Contenido en negro (`text-gray-900`)

**Estado Destacado (línea 4)**:
- Background azul suave (`bg-blue-50`)
- Icono 🔵 o badge "SAP"
- Texto explicativo abajo del preview

**Estado Scroll**:
- Si contenido > altura del contenedor
- Scrollbar vertical
- Sombra interna para indicar más contenido

---

## 3. Props y API

### 3.1. Interface de Props

```typescript
export interface FilePreviewProps {
  file?: File;                    // Archivo a previsualizar
  lines?: string[];               // Alternativa: líneas ya parseadas
  fileName?: string;              // Nombre del archivo (si lines se usa)
  maxLines?: number;              // Máximo de líneas a mostrar (default: 14)
  highlightLines?: number[];      // Líneas a destacar (ej: [4] para SAP)
  encoding?: 'UTF-8' | 'Windows-1252'; // Encoding detectado (por defecto Windows-1252)
  className?: string;             // Clases CSS adicionales
  onError?: (error: Error) => void; // Callback si falla lectura
}
```

### 3.2. Valores por Defecto

```typescript
const defaultProps: Partial<FilePreviewProps> = {
  maxLines: 14,
  highlightLines: [4],
  encoding: 'Windows-1252'
};
```

### 3.3. Ejemplos de Uso

**Ejemplo 1: Con archivo File**
```tsx
<FilePreview 
  file={selectedFile}
  highlightLines={[4]}
/>
```

**Ejemplo 2: Con líneas pre-parseadas**
```tsx
<FilePreview 
  lines={parsedLines}
  fileName="produccion.txt"
  encoding="Windows-1252"
/>
```

**Ejemplo 3: Custom styling**
```tsx
<FilePreview 
  file={file}
  maxLines={20}
  className="border-2 border-blue-500"
  onError={(err) => console.error('Preview failed:', err)}
/>
```

---

## 4. Estado Interno

### 4.1. Variables de Estado

```typescript
// Líneas del archivo
const [lines, setLines] = useState<string[]>([]);

// Estado de carga
const [isLoading, setIsLoading] = useState<boolean>(false);

// Error de lectura
const [error, setError] = useState<Error | null>(null);
```

### 4.2. Efectos

```typescript
useEffect(() => {
  // Si se pasa 'file', leerlo
  if (file) {
    loadFileLines(file);
  }
  // Si se pasan 'lines', usarlas directamente
  else if (lines) {
    setLines(lines.slice(0, maxLines));
  }
}, [file, lines, maxLines]);
```

---

## 5. Lógica de Negocio

### 5.1. Lectura del Archivo

```typescript
async function loadFileLines(file: File): Promise<void> {
  setIsLoading(true);
  setError(null);
  
  try {
    // Leer primeras N líneas (suficiente para maxLines)
    const chunk = await file.slice(0, 10 * 1024).arrayBuffer(); // 10KB
    
    // Decodificar según encoding (preferir Windows-1252 si así indicado)
    const decoder = new TextDecoder(encoding === 'Windows-1252' ? 'windows-1252' : 'utf-8');
    const text = decoder.decode(chunk);
    
    // Partir en líneas
    const allLines = text.split(/\r?\n/);
    const limitedLines = allLines.slice(0, maxLines);
    
    setLines(limitedLines);
  } catch (err) {
    const error = err instanceof Error ? err : new Error('Error leyendo archivo');
    setError(error);
    onError?.(error);
  } finally {
    setIsLoading(false);
  }
}
```

### 5.2. Sanitización de Contenido

```typescript
function sanitizeLine(line: string): string {
  // React escapa automáticamente, pero podemos truncar líneas muy largas
  const maxLength = 200;
  if (line.length > maxLength) {
    return line.substring(0, maxLength) + '...';
  }
  return line;
}
```

### 5.3. Detección de Líneas Destacadas

```typescript
function isHighlightedLine(lineNumber: number): boolean {
  return highlightLines?.includes(lineNumber) ?? false;
}
```

---

## 6. Renderizado

### 6.1. Código JSX Completo

```tsx
import React, { useState, useEffect } from 'react';

export interface FilePreviewProps {
  file?: File;
  lines?: string[];
  fileName?: string;
  maxLines?: number;
  highlightLines?: number[];
  encoding?: 'UTF-8' | 'Windows-1252';
  className?: string;
  onError?: (error: Error) => void;
}

export function FilePreview({
  file,
  lines: propLines,
  fileName,
  maxLines = 14,
  highlightLines = [4],
  encoding = 'Windows-1252',
  className = '',
  onError
}: FilePreviewProps) {
  const [lines, setLines] = useState<string[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);
  
  // Efecto: cargar líneas del archivo
  useEffect(() => {
    if (file) {
      loadFileLines(file);
    } else if (propLines) {
      setLines(propLines.slice(0, maxLines));
    }
  }, [file, propLines, maxLines]);
  
  async function loadFileLines(file: File) {
    setIsLoading(true);
    setError(null);
    
    try {
      const chunk = await file.slice(0, 10 * 1024).arrayBuffer();
      const decoder = new TextDecoder(encoding === 'Windows-1252' ? 'windows-1252' : 'utf-8');
      const text = decoder.decode(chunk);
      const allLines = text.split(/\r?\n/);
      setLines(allLines.slice(0, maxLines));
    } catch (err) {
      const error = err instanceof Error ? err : new Error('Error leyendo archivo');
      setError(error);
      onError?.(error);
    } finally {
      setIsLoading(false);
    }
  }
  
  const displayFileName = fileName || file?.name || 'archivo.txt';
  
  if (isLoading) {
    return (
      <div className={`border rounded-lg p-4 bg-gray-50 ${className}`}>
        <div className="flex items-center justify-center py-8">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600" />
          <span className="ml-3 text-gray-600">Cargando preview...</span>
        </div>
      </div>
    );
  }
  
  if (error) {
    return (
      <div className={`border border-red-300 rounded-lg p-4 bg-red-50 ${className}`}>
        <div className="flex items-center text-red-700">
          <span className="text-2xl mr-2">⚠️</span>
          <div>
            <div className="font-medium">Error al cargar preview</div>
            <div className="text-sm">{error.message}</div>
          </div>
        </div>
      </div>
    );
  }
  
  if (lines.length === 0) {
    return null;
  }
  
  const hasSAPLine = highlightLines.includes(4);
  
  return (
    <div className={`border rounded-lg overflow-hidden ${className}`}>
      {/* Header */}
      <div className="bg-gray-100 px-4 py-2 border-b flex items-center justify-between">
        <div className="font-medium text-gray-700">
          Preview del Archivo
        </div>
        <div className="text-sm text-gray-500">
          Mostrando primeras {lines.length} líneas de {displayFileName}
        </div>
      </div>
      
      {/* Content */}
      <div className="bg-gray-50 p-4 max-h-96 overflow-y-auto font-mono text-sm">
        {lines.map((line, index) => {
          const lineNumber = index + 1;
          const isHighlighted = highlightLines.includes(lineNumber);
          
          return (
            <div
              key={index}
              className={`flex py-1 ${
                isHighlighted ? 'bg-blue-50 -mx-4 px-4 border-l-4 border-blue-500' : ''
              }`}
            >
              {/* Número de línea */}
              <div className="w-10 flex-shrink-0 text-right text-gray-500 select-none pr-3">
                {lineNumber}
              </div>
              
              {/* Contenido */}
              <div className="flex-1 text-gray-900 overflow-x-auto">
                {line.length > 200 ? line.substring(0, 200) + '...' : line}
              </div>
              
              {/* Badge para línea SAP */}
              {isHighlighted && lineNumber === 4 && (
                <div className="ml-2 flex-shrink-0">
                  <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-blue-100 text-blue-800">
                    SAP
                  </span>
                </div>
              )}
            </div>
          );
        })}
      </div>
      
      {/* Footer (info sobre línea SAP) */}
      {hasSAPLine && (
        <div className="bg-blue-50 px-4 py-2 border-t border-blue-200 text-sm text-blue-700 flex items-center">
          <span className="mr-2">ℹ️</span>
          La línea 4 contiene totales de SAP y no se valida
        </div>
      )}
    </div>
  );
}
```

---

## 7. Seguridad

### 7.1. Prevención de XSS

```tsx
// ✅ SEGURO: React escapa automáticamente
<div>{line}</div>

// ❌ NUNCA usar:
// <div dangerouslySetInnerHTML={{ __html: line }} />
```

### 7.2. Límite de Contenido

```typescript
// Limitar lectura del archivo
const chunk = await file.slice(0, 10 * 1024).arrayBuffer(); // Solo 10KB

// Limitar longitud de línea
if (line.length > 200) {
  return line.substring(0, 200) + '...';
}
```

---

## 8. Accesibilidad

### 8.1. ARIA Attributes

```tsx
<div 
  role="region"
  aria-label="Vista previa del archivo"
  className="..."
>
  {/* contenido */}
</div>
```

### 8.2. Navegación por Teclado

- Scroll del contenedor es accesible con flechas/rueda del mouse
- No hay elementos interactivos (componente read-only)

### 8.3. Contraste

- Texto: `text-gray-900` sobre `bg-gray-50` (ratio > 7:1)
- Línea destacada: `text-gray-900` sobre `bg-blue-50` (ratio > 4.5:1)

---

## 9. Performance

### 9.1. Optimizaciones

- **Lectura parcial**: Solo lee 10KB del archivo
- **Límite de líneas**: Máximo 14 líneas renderizadas
- **Memo**: Componente puede memoizarse con `React.memo`

```tsx
export const FilePreview = React.memo(FilePreviewComponent);
```

### 9.2. Métricas

- Render inicial: < 50ms
- Lectura de archivo: < 100ms (10KB)

---

## 10. Testing

### 10.1. Unit Tests

```typescript
import { render, screen, waitFor } from '@testing-library/react';
import { FilePreview } from './FilePreview';

describe('FilePreview', () => {
  it('renderiza líneas del archivo', async () => {
    const lines = [
      'Fecha|Maquina|Log',
      '15/01/2024|MAQ001|123',
      '16/01/2024|MAQ002|124'
    ];
    
    render(<FilePreview lines={lines} />);
    
    expect(screen.getByText('Fecha|Maquina|Log')).toBeInTheDocument();
    expect(screen.getByText('15/01/2024|MAQ001|123')).toBeInTheDocument();
  });
  
  it('muestra números de línea', () => {
    const lines = ['line1', 'line2', 'line3'];
    
    render(<FilePreview lines={lines} />);
    
    expect(screen.getByText('1')).toBeInTheDocument();
    expect(screen.getByText('2')).toBeInTheDocument();
    expect(screen.getByText('3')).toBeInTheDocument();
  });
  
  it('destaca línea 4 (SAP)', () => {
    const lines = ['l1', 'l2', 'l3', 'TOTALES SAP', 'l5'];
    
    const { container } = render(
      <FilePreview lines={lines} highlightLines={[4]} />
    );
    
    const line4 = container.querySelector('.border-blue-500');
    expect(line4).toBeInTheDocument();
    expect(screen.getByText('SAP')).toBeInTheDocument();
  });
  
  it('muestra mensaje informativo sobre línea SAP', () => {
    const lines = ['l1', 'l2', 'l3', 'SAP', 'l5'];
    
    render(<FilePreview lines={lines} highlightLines={[4]} />);
    
    expect(screen.getByText(/línea 4 contiene totales de SAP/i)).toBeInTheDocument();
  });
  
  it('trunca líneas muy largas', () => {
    const longLine = 'x'.repeat(300);
    const lines = [longLine];
    
    const { container } = render(<FilePreview lines={lines} />);
    
    const displayedText = container.textContent;
    expect(displayedText).toContain('...');
    expect(displayedText?.length).toBeLessThan(250);
  });
  
  it('muestra spinner mientras carga', async () => {
    const file = new File(['content'], 'test.txt');
    
    render(<FilePreview file={file} />);
    
    expect(screen.getByText(/cargando preview/i)).toBeInTheDocument();
    
    await waitFor(() => {
      expect(screen.queryByText(/cargando/i)).not.toBeInTheDocument();
    });
  });
  
  it('muestra error si lectura falla', async () => {
    const mockError = vi.fn();
    const invalidFile = new File([new ArrayBuffer(0)], 'test.txt');
    
    render(<FilePreview file={invalidFile} onError={mockError} />);
    
    await waitFor(() => {
      expect(screen.getByText(/error al cargar preview/i)).toBeInTheDocument();
    });
  });
});
```

### 10.2. Tests de Accesibilidad

```typescript
it('no tiene violaciones de accesibilidad', async () => {
  const lines = ['line1', 'line2'];
  const { container } = render(<FilePreview lines={lines} />);
  
  const results = await axe(container);
  expect(results).toHaveNoViolations();
});
```

---

## 11. Estilos (Tailwind)

```typescript
// Clases principales
const styles = {
  container: 'border rounded-lg overflow-hidden',
  header: 'bg-gray-100 px-4 py-2 border-b',
  content: 'bg-gray-50 p-4 max-h-96 overflow-y-auto font-mono text-sm',
  line: 'flex py-1',
  lineHighlight: 'bg-blue-50 -mx-4 px-4 border-l-4 border-blue-500',
  lineNumber: 'w-10 flex-shrink-0 text-right text-gray-500 select-none pr-3',
  lineContent: 'flex-1 text-gray-900 overflow-x-auto',
  footer: 'bg-blue-50 px-4 py-2 border-t border-blue-200 text-sm text-blue-700',
  badge: 'inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-blue-100 text-blue-800'
};
```

---

## 12. Dependencias

### 12.1. Externas
- React
- Tailwind CSS (para estilos)

### 12.2. Internas
- Ninguna (componente standalone)

---

## 13. Checklist de Implementación

- [ ] Crear `src/components/FilePreview.tsx`
- [ ] Implementar lectura de archivo con FileReader
- [ ] Implementar renderizado de líneas con números
- [ ] Añadir highlight de línea 4 (SAP)
- [ ] Añadir estados de loading y error
- [ ] Implementar truncado de líneas largas
- [ ] Añadir estilos Tailwind
- [ ] Escribir tests unitarios
- [ ] Validar accesibilidad con axe
- [ ] Probar con archivos UTF-8 y Windows-1252
- [ ] Documentar en Storybook (opcional)

---

## 14. Referencias
- TD: `.gemini/sprints/technical-designs/TD-001-validacion-carga-archivos-etl-v2.md`
- FileReader API: https://developer.mozilla.org/en-US/docs/Web/API/FileReader
- TextDecoder API: https://developer.mozilla.org/en-US/docs/Web/API/TextDecoder
