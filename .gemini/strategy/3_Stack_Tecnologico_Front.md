# GEMINI.MD: AI Collaboration Guide

This document provides essential context for AI models interacting with this project. Adhering to these guidelines will ensure consistency and maintain code quality.

## 1. Project Overview & Purpose

* **Primary Goal:** This project, named "TailAdmin React," is a free and open-source admin dashboard template. It's designed to provide developers with a comprehensive, data-driven back-end, dashboard, or admin panel solution. It includes a variety of UI components, elements, and pages to facilitate the rapid development of feature-rich web projects.
* **Business Domain:** This is a general-purpose admin dashboard template, applicable to various business domains such as E-commerce, Analytics, Marketing, and CRM.

## 2. Core Technologies & Stack

* **Languages:** TypeScript
* **Frameworks & Runtimes:** React 19, Node.js (v18.x or later, v20.x recommended)
* **Databases:** Not applicable (Frontend project).
* **Key Libraries/Dependencies:** Tailwind CSS, ApexCharts, React Router, FullCalendar, Flatpickr.
* **Package Manager(s):** npm (or yarn)

## 3. Architectural Patterns

* **Overall Architecture:** This is a client-side, component-based architecture typical of a React application. It appears to be a monolithic frontend application.
* **Directory Structure Philosophy:**
    * `/src`: Contains all the primary source code for the React application.
        * `/src/components`: Reusable UI components.
        * `/src/pages`: Top-level page components, likely corresponding to routes.
        * `/src/layout`: Components related to the overall application layout (sidebar, header).
        * `/src/context`: React context providers for state management.
        * `/src/hooks`: Custom React hooks.
        * `/src/icons`: SVG icons used in the application.
    * `/public`: Contains static assets like images and favicons.
    * `/dist`: (Generated) Contains the production build of the application.

## 4. Coding Conventions & Style Guide

* **Formatting:** The project uses ESLint for code linting. The configuration (`eslint.config.js`) extends recommended rules for JavaScript and TypeScript. Specific formatting rules (like indentation or line length) are not explicitly defined in the provided configuration, but consistency with existing code should be maintained.
* **Naming Conventions:**
    * `variables`, `functions`: camelCase (`myVariable`)
    * `classes`, `components`: PascalCase (`MyClass`)
    * `files`: PascalCase for components (`UserProfile.tsx`), camelCase for others.
* **API Design:** Not applicable (Frontend project).
* **Error Handling:** Error handling patterns are not explicitly defined in the project-level documentation. It is inferred that standard React error handling practices (e.g., Error Boundaries, try...catch in async functions) are followed.

## 5. Key Files & Entrypoints

* **Main Entrypoint(s):** `src/main.tsx`
* **Configuration:**
    * `vite.config.ts`: Vite configuration for the development server and build process.
    * `tailwind.config.js`: Tailwind CSS configuration.
    * `tsconfig.json`: TypeScript compiler options.
    * `eslint.config.js`: ESLint configuration.
* **CI/CD Pipeline:** There are no CI/CD pipeline configuration files present in the repository.

## 6. Development & Testing Workflow

* **Local Development Environment:**
    1. Clone the repository.
    2. Install dependencies using `npm install` or `yarn install`.
    3. Start the development server using `npm run dev` or `yarn dev`.
* **Testing:** There are no testing frameworks or test files apparent in the project structure. It is recommended to add a testing strategy (e.g., using Jest and React Testing Library).
* **CI/CD Process:** Not applicable.

## 7. Specific Instructions for AI Collaboration

* **Contribution Guidelines:** No formal `CONTRIBUTING.md` file exists. Follow standard open-source contribution etiquette.
* **Infrastructure (IaC):** Not applicable.
* **Security:** Be mindful of security. Do not hardcode secrets or keys. Sanitize user input to prevent XSS attacks.
* **Dependencies:** When adding a new dependency, use `npm install` or `yarn add` and update the `package.json` file.
* **Commit Messages:** The commit history is not available to infer a specific commit message style. It is recommended to follow the [Conventional Commits](https.conventionalcommits.org/) specification.
