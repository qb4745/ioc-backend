# ✅ CHECKLIST PRE-PUSH A MAIN - MVP CORE 1.0

**Fecha**: 2 de Diciembre, 2025  
**Rama**: `mvp-core-1.0` → `main`  
**Versión**: MVP Core 1.0

---

## 🔍 1. VERIFICACIONES DE CÓDIGO

### 1.1 Tests
- [ ] **Ejecutar todos los tests unitarios**: `./mvnw clean test`
- [ ] **Verificar que todos los tests pasen** (0 failures, 0 errors)
- [ ] **Revisar cobertura de tests** en componentes críticos
- [ ] **Tests de integración funcionales** (si aplica)

### 1.2 Compilación
- [ ] **Build exitoso sin errores**: `./mvnw clean package -DskipTests`
- [ ] **Build completo con tests**: `./mvnw clean install`
- [ ] **No hay warnings críticos** en la compilación
- [ ] **Generar JAR final** y verificar tamaño razonable

### 1.3 Calidad de Código
- [ ] **No hay código comentado** innecesario
- [ ] **No hay TODOs/FIXMEs** críticos pendientes
- [ ] **No hay prints/logs de debug** olvidados
- [ ] **Imports no utilizados** eliminados
- [ ] **Formato de código consistente**

---

## 🔒 2. SEGURIDAD

### 2.1 Credenciales y Secretos
- [ ] **No hay credenciales hardcodeadas** en el código
- [ ] **Variables de entorno documentadas** (pero no expuestas)
- [ ] **Archivos sensibles en .gitignore**:
  - `application-dev.properties`
  - `application-local.properties`
  - `.env` files
  - Archivos con contraseñas/tokens
- [ ] **Revisar historial de git** para secretos expuestos

### 2.2 Configuración
- [ ] **application-prod.properties** usa variables de entorno
- [ ] **No hay URLs/IPs** de desarrollo en configs de producción
- [ ] **CORS configurado correctamente** para producción
- [ ] **JWT/Auth configurado** adecuadamente

---

## 🗄️ 3. BASE DE DATOS

### 3.1 Migraciones y Schema
- [ ] **Schema de BD actualizado** y sincronizado
- [ ] **Scripts SQL documentados** en `/sql`
- [ ] **Hibernate ddl-auto=none** en producción
- [ ] **Índices creados** para queries críticas
- [ ] **No hay DROP TABLE** en scripts de producción

### 3.2 Conexiones
- [ ] **HikariCP configurado** correctamente
- [ ] **Pool de conexiones optimizado** para Supabase
- [ ] **Connection leaks verificados** (no hay fugas)
- [ ] **Timeouts configurados** apropiadamente

---

## 📚 4. DOCUMENTACIÓN

### 4.1 README y Docs
- [ ] **README.md actualizado** con:
  - Descripción del proyecto
  - Requisitos previos
  - Instrucciones de instalación
  - Cómo ejecutar localmente
  - Variables de entorno necesarias
- [ ] **Documentación técnica** actualizada
- [ ] **API endpoints documentados** (Swagger/OpenAPI)
- [ ] **Diagramas actualizados** (si aplica)

### 4.2 Comentarios y JavaDoc
- [ ] **Clases principales comentadas**
- [ ] **Métodos complejos documentados**
- [ ] **DTOs y Entities** con descripciones claras

---

## 🌐 5. API Y ENDPOINTS

### 5.1 Funcionalidad
- [ ] **Todos los endpoints funcionan** correctamente
- [ ] **Validaciones de entrada** implementadas
- [ ] **Manejo de errores** adecuado (excepciones, status codes)
- [ ] **Responses consistentes** (DTOs estandarizados)

### 5.2 Testing Manual
- [ ] **Probar endpoints críticos** con Postman/cURL:
  - Login/Authentication
  - CRUD de producción
  - Dashboards de Metabase
  - ETL workflows
  - AI features (si aplica)
- [ ] **Validar respuestas JSON** bien formateadas
- [ ] **Códigos HTTP correctos** (200, 201, 400, 401, 404, 500)

---

## 🚀 6. DEPLOYMENT Y PRODUCCIÓN

### 6.1 Configuración de Producción
- [ ] **application-prod.properties** listo para Render/producción
- [ ] **Variables de entorno** documentadas para deployment
- [ ] **Dockerfile** funcional (si aplica)
- [ ] **render.yaml** actualizado y correcto

### 6.2 Performance
- [ ] **Queries optimizadas** (no N+1 queries)
- [ ] **Lazy loading configurado** correctamente
- [ ] **Batch operations** donde sea apropiado
- [ ] **Cache configurado** (si aplica)

---

## 🔄 7. GIT Y CONTROL DE VERSIONES

### 7.1 Estado del Repositorio
- [ ] **Working tree limpio**: `git status`
- [ ] **Todos los cambios commiteados**
- [ ] **Mensajes de commit descriptivos**
- [ ] **No hay archivos grandes** innecesarios (> 50MB)

### 7.2 Ramas
- [ ] **Rama actualizada con develop/main**: `git pull origin main`
- [ ] **Conflictos resueltos** (si hay merge)
- [ ] **Branch limpio** de features experimentales

### 7.3 .gitignore
- [ ] **target/** ignorado
- [ ] **logs/** ignorado
- [ ] **IDE files** (.idea, .vscode, *.iml) ignorados
- [ ] **Archivos de configuración local** ignorados

---

## 📊 8. CARACTERÍSTICAS MVP CORE

### 8.1 Features Principales
- [ ] **ETL Workflow** (IOC-001):
  - Importación de Excel
  - Validación de datos
  - Carga a PostgreSQL
  - Logging y métricas
- [ ] **Gestión de Usuarios y Roles** (IOC-004):
  - RBAC implementado
  - Roles: ADMIN, GERENTE, ANALISTA
  - Permisos funcionando
- [ ] **Integración con Metabase** (IOC-006a):
  - Embedding de dashboards
  - JWT tokens funcionando
  - Dashboards: Gerencial y Operativo
- [ ] **Login y Autenticación** (IOC-021):
  - Supabase Auth integrado
  - JWT validation
  - Protected endpoints

### 8.2 AI Features (si están en MVP)
- [ ] **Dashboard Explanation** funcional
- [ ] **Streaming implementado** (V3)
- [ ] **Spring AI integrado** (V4)
- [ ] **Gemini API configurado**

---

## 🧪 9. TESTING FINAL

### 9.1 Tests Automatizados
```bash
# Ejecutar suite completa de tests
./mvnw clean test

# Verificar build completo
./mvnw clean install

# Package para producción
./mvnw clean package -Pprod -DskipTests
```

### 9.2 Tests Manuales
- [ ] **Iniciar aplicación localmente**:
  ```bash
  ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
  ```
- [ ] **Probar flujo completo de usuario**
- [ ] **Verificar logs sin errores** críticos
- [ ] **Verificar métricas** (actuator/prometheus)

---

## 📋 10. CHECKLIST PRE-MERGE

### Antes de hacer `git merge main`
- [ ] **Backup de la base de datos** (si aplica)
- [ ] **Notificar al equipo** sobre el merge
- [ ] **Documentar cambios** en CHANGELOG o release notes
- [ ] **Tag de versión**: `git tag -a v1.0.0-mvp -m "MVP Core Release"`

### Comandos para Push
```bash
# 1. Asegurar que estás en la rama correcta
git checkout mvp-core-1.0

# 2. Pull de main para actualizar
git checkout main
git pull origin main

# 3. Merge de mvp-core-1.0 a main
git merge mvp-core-1.0

# 4. Resolver conflictos (si hay)

# 5. Push a main
git push origin main

# 6. Crear tag de versión
git tag -a v1.0.0-mvp -m "MVP Core 1.0 - Production Ready"
git push origin v1.0.0-mvp
```

---

## ⚠️ ROLLBACK PLAN

### Si algo sale mal después del push
```bash
# Ver último commit
git log -1

# Revertir al commit anterior (sin borrar historial)
git revert HEAD

# O hacer hard reset (CUIDADO: borra cambios)
git reset --hard HEAD~1
git push --force origin main
```

---

## 📞 CONTACTOS DE EMERGENCIA

- **DevOps**: [Contacto]
- **DBA**: [Contacto]
- **Product Owner**: [Contacto]

---

## ✨ NOTAS FINALES

- Este es el **MVP Core 1.0** - primera versión estable
- **Supabase Free Tier**: Verificar límites de conexiones
- **Render.com**: Verificar cold starts y timeouts
- **Documentar cualquier issue conocido** en GitHub Issues

---

**Última actualización**: 2 de Diciembre, 2025  
**Preparado por**: GitHub Copilot  
**Proyecto**: IOC Backend - Inteligencia Operacional Cambiaso

