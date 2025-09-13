# GEMINI.MD: AI Collaboration Guide

This document provides essential context for AI models interacting with this project. Adhering to these guidelines will ensure consistency and maintain code quality.

## 1. Project Overview & Purpose

* **Primary Goal:** This is a REST API backend for the "Inteligencia Operacional Cambiaso" project.
* **Business Domain:** Inferred: Operational Intelligence. The specific domain is not clear from the provided information.

## 2. Core Technologies & Stack

* **Languages:** Java 21
* **Frameworks & Runtimes:** Spring Boot 3.5.5
* **Databases:** PostgreSQL
* **Key Libraries/Dependencies:** 
    - Spring Boot Starter Data JPA
    - Spring Boot Starter Security
    - Spring Boot Starter Validation
    - Spring Boot Starter Web
    - Lombok
* **Package Manager(s):** Maven

## 3. Architectural Patterns

* **Overall Architecture:** Monolithic Application, likely following a Model-View-Controller (MVC) or similar layered architecture common in Spring Boot applications.
* **Directory Structure Philosophy:** Standard Maven project structure:
    * `/src/main/java`: Contains the primary Java source code.
    * `/src/main/resources`: Contains application configuration and static assets.
    * `/src/test/java`: Contains all unit and integration tests.
    * `/target`: Contains the compiled application and build artifacts.

## 4. Coding Conventions & Style Guide

* **Formatting:** Standard Java conventions. (Inferred from the limited source code).
* **Naming Conventions:** 
    * `classes`: PascalCase (`IocbackendApplication`)
    * `methods`, `variables`: camelCase (`main`, `args`)
    * `packages`: lowercase (`com.cambiaso.ioc`)
* **API Design:** RESTful principles are assumed given the use of Spring Boot Starter Web.
* **Error Handling:** Not specified, but likely uses Spring Boot's default error handling mechanisms.

## 5. Key Files & Entrypoints

* **Main Entrypoint(s):** `src/main/java/com/cambiaso/ioc/IocbackendApplication.java`
* **Configuration:** `src/main/resources/application.properties`
* **CI/CD Pipeline:** No CI/CD pipeline configuration file was found.

## 6. Development & Testing Workflow

* **Local Development Environment:** Run the `main` method in `IocbackendApplication.java` to start the application.
* **Testing:** Run tests via `mvn test`. The project uses JUnit 5 for testing. New code should have corresponding unit tests.
* **CI/CD Process:** Not specified.

## 7. Specific Instructions for AI Collaboration

* **Contribution Guidelines:** No `CONTRIBUTING.md` file was found.
* **Infrastructure (IaC):** No Infrastructure as Code directory was found.
* **Security:** Be mindful of security. Do not hardcode secrets or keys. Ensure any changes to authentication logic are secure and vetted.
* **Dependencies:** When adding a new dependency, add it to the `<dependencies>` section of the `pom.xml` file.
* **Commit Messages:** Commit messages are short and descriptive, in Spanish. Example: "supabase configurado".
