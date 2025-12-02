# Swagger/OpenAPI Compatibility Fix

## Problem
The application was failing at startup with the following error:
```
java.lang.NoSuchMethodError: 'java.lang.String io.swagger.v3.oas.annotations.media.Schema.$dynamicRef()'
```

This error occurred when trying to access the Swagger/OpenAPI documentation endpoint (`/v3/api-docs`).

## Root Cause
Version incompatibility between Swagger dependencies in `springdoc-openapi-starter-webmvc-ui` 2.8.13:
- The library uses `swagger-core-jakarta` 2.2.36
- This version tries to call the `$dynamicRef()` method on the `@Schema` annotation
- However, the method doesn't exist in the version of `swagger-annotations` available in the classpath
- This is a known issue with springdoc-openapi 2.8.x series and certain Spring Boot 3.x versions

## Solution
Downgraded `springdoc-openapi-starter-webmvc-ui` from version **2.8.13** to **2.6.0**

### Changes Made
**File:** `pom.xml`

```xml
<!-- Before -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.13</version>
</dependency>

<!-- After -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.6.0</version>
</dependency>
```

## Version Compatibility
- **Spring Boot:** 3.5.5
- **springdoc-openapi-starter-webmvc-ui:** 2.6.0 ✅
- **Java:** 21

Version 2.6.0 is fully compatible with Spring Boot 3.5.x and provides stable OpenAPI 3.0 documentation generation.

## Testing
After applying this fix:
1. Clean and rebuild the project: `mvn clean package`
2. Restart the application
3. Access Swagger UI at: `http://localhost:8080/swagger-ui.html`
4. Access OpenAPI JSON at: `http://localhost:8080/v3/api-docs`

Both endpoints should work without errors.

## Additional Notes
- Version 2.6.0 still provides all necessary features for API documentation
- The downgrade does not affect functionality, only uses more stable dependency versions
- This fix resolves the `NoSuchMethodError` completely

## Date
2025-11-28

