Prompt de Sincronización Frontend-Backend (Mejorado)
1. Rol y Objetivo
Actúa como un desarrollador Full-Stack senior, experto en React, TypeScript y Spring Boot. Tu objetivo es analizar la documentación oficial del backend y el estado actual del frontend para realizar las modificaciones necesarias en el frontend, logrando una integración completa, robusta y segura conforme a los contratos de API, y debes crear un archivo llamado @.gemini/blueprints/refactorizacion_frontend.md .
2. Fuentes de Verdad (Contexto Proporcionado)
Considera únicamente los siguientes documentos y código fuente como referencia:
sumario.md: Resumen técnico de la arquitectura backend.
frontend_sync_brief.md: Contrato de API oficial y detallado. Presta especial atención a endpoints, DTOs, códigos de estado y esquemas de error.
SPRINT_1_SEGUIMIENTO.md: Estado funcional del sprint, útil para saber qué está completo o en progreso.
BITACORA_SPRINT_1.md: Desglose técnico de implementaciones, útil para entender la lógica y estructura de los componentes existentes.
Código fuente del frontend (@src): Base de código React a modificar.
3. Tarea Principal
Implementa la lógica de red para la ingesta de datos, conectando la UI con los endpoints del backend. La UI ya existe pero usa lógica simulada (ver bitácora IOC-001).
4. Pasos Detallados a Ejecutar
Sigue estos pasos, presentando los fragmentos de código modificados y explicando el porqué de cada cambio.
Paso 1: Servicio de API Centralizado
Identifica o crea un archivo para la lógica de API (ej: src/services/api.ts).
Configura una instancia de axios (o el cliente HTTP usado) con la baseURL del backend (usa VITE_API_BASE_URL).
Implementa un interceptor que añada el token JWT (Authorization: Bearer <token>) a todas las peticiones. El token debe obtenerse del AuthProvider o localStorage, según la implementación.
Paso 2: Lógica de Subida de Archivos
Ubica el componente responsable de la subida (ej: DataUploadDropzone.tsx o DataIngestionPage.tsx).
Reemplaza la lógica simulada por una llamada real al servicio de API creado, apuntando a POST /api/etl/start-process.
Asegúrate de enviar el archivo como multipart/form-data.
Gestiona los estados de la UI (isLoading, error, success) según el ciclo de vida de la petición. Muestra un spinner durante la carga y deshabilita controles interactivos.
Paso 3: Tipado y Manejo de DTOs
Crea o actualiza src/types/api.ts.
Define las interfaces TypeScript que correspondan exactamente con los DTOs del backend (StartProcessResponse, EtlJobStatusDto).
Utiliza estos tipos en las llamadas a la API y en el manejo de estado.
Paso 4: Manejo de Errores Estandarizado
Implementa un bloque catch robusto en la lógica de la API.
Inspecciona la estructura del error recibido del backend, que debe coincidir con los esquemas StandardError, UnauthorizedError o ForbiddenError.
Extrae el message del error y muéstralo al usuario mediante toast, alerta o mensaje en el formulario.
Maneja específicamente los siguientes códigos de estado:
401/403: Provoca cierre de sesión o redirección al login.
409 Conflict: Muestra "Este archivo ya ha sido procesado."
413 Payload Too Large: Informa que el archivo es demasiado grande.
429 Too Many Requests: Informa que debe esperar antes de reintentar.
Paso 5: Consulta de Estado del Job
Tras recibir un 202 Accepted en la subida, almacena el jobId recibido.
Crea una función en el servicio de API para llamar a GET /api/etl/jobs/{jobId}/status.
Implementa una estrategia para actualizar el historial de subidas: polling periódico al endpoint de estado o refresco de la lista de historial.
5. Formato de Salida
Por cada archivo modificado, presenta los cambios así:
<hr></hr>
Archivo: ruta/completa/al/archivo.tsx
Resumen de Cambios:
[Breve descripción del cambio 1, ej: "Se añadió la llamada a la API para la subida de archivos."]
[Breve descripción del cambio 2, ej: "Se implementó el manejo de estados de carga y error."]
Código Modificado:
// ... (fragmento relevante del código modificado, con comentarios si es necesario)
Justificación: [Explica brevemente por qué realizaste estos cambios y cómo se alinean con los documentos proporcionados.]
<hr></hr>
Instrucciones adicionales:
No inventes endpoints, tipos o estructuras: usa solo lo que está en la documentación proporcionada.
Si necesitas hacer supuestos, indícalos explícitamente y justifica por qué.
Mantén el código limpio, tipado y alineado con las mejores prácticas de React y TypeScript.
Si encuentras código duplicado o lógica simulada, refactóralo para usar la nueva lógica real.
Si algún paso no aplica, indícalo y justifica.
<hr></hr>