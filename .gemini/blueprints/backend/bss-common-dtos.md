# Backend Service Specification: Common DTOs

## Metadata
- ID: BSS-IOC-004-21
- Technical Design: .gemini/sprints/technical-designs/TD-IOC-004-User-Role-Management-claude.md
- Módulo: `Common DTOs`
- Sprint: Sprint 3
- Estado: [x] Draft
- Autor: IA
- Fecha: 2025-10-22

---

## 1. Propósito
Estandarizar DTOs reutilizables como `ErrorResponse` y `PageResponse<T>`.

---

## 2. ErrorResponse (compartido)

```java
public class ErrorResponse {
  private String code;
  private String message;
  private int status;
  private OffsetDateTime timestamp;
  private String path;
  private Map<String, String> validationErrors;
}
```

---

## 3. PageResponse<T>

```java
public class PageResponse<T> {
  private List<T> items;
  private long totalElements;
  private int totalPages;
  private int currentPage;
  private int pageSize;
  private boolean hasNext;
  private boolean hasPrevious;
}
```

---

## 4. Ubicación sugerida
- `src/main/java/com/cambiaso/ioc/dto/common`

