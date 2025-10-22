# Backend Service Specification: Global Exception Handling

## Metadata
- ID: BSS-IOC-004-20
- Technical Design: .gemini/sprints/technical-designs/TD-IOC-004-User-Role-Management-claude.md
- Clase: `GlobalExceptionHandler`
- Tipo: Controller Advice
- Package: `com.cambiaso.ioc.exception`
- Sprint: Sprint 3
- Estado: [x] Draft
- Autor: IA
- Fecha: 2025-10-22

---

## 1. Propósito
Centralizar el manejo de errores de la API y responder con un contrato consistente `ErrorResponse`.

---

## 2. Excepciones Personalizadas (sugeridas)
- `ConflictException` (409)
- `ResourceNotFoundException` (404)
- `ForbiddenException` (403)
- `UnauthorizedException` (401)
- `BadRequestException` (400)

---

## 3. ErrorResponse (DTO estándar)

```java
public class ErrorResponse {
  private String code;      // ej: RESOURCE_NOT_FOUND
  private String message;   // mensaje legible
  private int status;       // HTTP status code
  private OffsetDateTime timestamp;
  private String path;      // request path
  private Map<String, String> validationErrors; // campo -> error
}
```

---

## 4. Implementación (esqueleto)

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
    return build("RESOURCE_NOT_FOUND", ex.getMessage(), HttpStatus.NOT_FOUND, req.getRequestURI());
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex, HttpServletRequest req) {
    return build("CONFLICT", ex.getMessage(), HttpStatus.CONFLICT, req.getRequestURI());
  }

  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException ex, HttpServletRequest req) {
    return build("FORBIDDEN", ex.getMessage(), HttpStatus.FORBIDDEN, req.getRequestURI());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
    Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
      .collect(Collectors.toMap(FieldError::getField, DefaultMessageSourceResolvable::getDefaultMessage, (a,b) -> a));
    ErrorResponse body = base("VALIDATION_ERROR", "Errores de validación", HttpStatus.BAD_REQUEST.value(), req.getRequestURI());
    body.setValidationErrors(errors);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
    log.error("Unexpected error", ex);
    return build("INTERNAL_ERROR", "Error interno", HttpStatus.INTERNAL_SERVER_ERROR, req.getRequestURI());
  }

  private ResponseEntity<ErrorResponse> build(String code, String message, HttpStatus status, String path) {
    return ResponseEntity.status(status).body(base(code, message, status.value(), path));
  }

  private ErrorResponse base(String code, String message, int status, String path) {
    ErrorResponse err = new ErrorResponse();
    err.setCode(code);
    err.setMessage(message);
    err.setStatus(status);
    err.setTimestamp(OffsetDateTime.now());
    err.setPath(path);
    return err;
  }
}
```

---

## 5. Tests
- Validación Bean → 400 con mapa de errores
- NotFound/Conflict → 404/409
- Genéricos → 500

