# Plan de Implementación: Reseteo de Contraseña

Este documento detalla los pasos para crear la página de reseteo de contraseña, basándose en la captura de pantalla proporcionada y utilizando los componentes existentes en el proyecto.

## Análisis Visual y de Componentes

-   **Diseño General:** La captura de pantalla muestra un formulario centrado que coincide con el `AuthLayout` utilizado en las páginas de `SignIn` y `SignUp`.
-   **Componentes Requeridos:**
    -   **Layout:** `AuthLayout` (`src/pages/AuthPages/AuthPageLayout.tsx`)
    -   **Título y Párrafo:** Etiquetas `h1` y `p` con clases de Tailwind CSS existentes.
    -   **Formulario:** Un `<form>` que contendrá los campos.
    -   **Etiqueta de Campo:** Componente `<Label>` (`src/components/form/Label.tsx`).
    -   **Campo de Entrada:** Componente `<Input>` (`src/components/form/input/InputField.tsx`).
    -   **Botón de Acción:** Componente `<Button>` (`src/components/ui/button/Button.tsx`).
    -   **Navegación:** Componente `<Link>` de `react-router-dom`.
    -   **Iconos:** `ChevronLeftIcon` (`src/icons/index.ts`).

## Plan de Implementación Paso a Paso

### Paso 1: Crear el Componente del Formulario (`ResetPasswordForm.tsx`)

Crearemos un nuevo componente reutilizable que contenga la lógica y la estructura del formulario de reseteo de contraseña, basándonos en el HTML proporcionado.

-   **Ubicación del Archivo:** `src/components/auth/ResetPasswordForm.tsx`
-   **Código:**
    ```tsx
    import { useState } from "react";
    import { Link } from "react-router-dom";
    import { supabase } from "../../lib/supabaseClient";
    import Label from "../form/Label";
    import Input from "../form/input/InputField";
    import Button from "../ui/button/Button";
    import { ChevronLeftIcon } from "../../icons";

    export default function ResetPasswordForm() {
      const [email, setEmail] = useState("");
      const [loading, setLoading] = useState(false);
      const [message, setMessage] = useState("");

      const handlePasswordReset = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        setMessage("");
        const { error } = await supabase.auth.resetPasswordForEmail(email, {
          redirectTo: window.location.origin + '/update-password',
        });

        if (error) {
          setMessage("Error: " + error.message);
        } else {
          setMessage("¡Enlace de reseteo enviado! Revisa tu correo electrónico.");
        }
        setLoading(false);
      };

      return (
        <div className="flex flex-col flex-1">
          <div className="w-full max-w-md pt-10 mx-auto">
            <Link
              to="/signin"
              className="inline-flex items-center text-sm text-gray-500 transition-colors hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-300"
            >
              <ChevronLeftIcon className="size-5" />
              Volver al Dashboard
            </Link>
          </div>
          <div className="flex flex-col justify-center flex-1 w-full max-w-md mx-auto">
            <div className="mb-5 sm:mb-8">
              <h1 className="text-title-sm sm:text-title-md mb-2 font-semibold text-gray-800 dark:text-white/90">
                ¿Olvidaste tu Contraseña?
              </h1>
              <p className="text-sm text-gray-500 dark:text-gray-400">
                Ingresa el correo electrónico asociado a tu cuenta y te enviaremos un enlace para resetear tu contraseña.
              </p>
            </div>
            <div>
              <form onSubmit={handlePasswordReset}>
                <div className="space-y-5">
                  <div>
                    <Label htmlFor="email">
                      Correo Electrónico<span className="text-error-500">*</span>
                    </Label>
                    <Input
                      type="email"
                      id="email"
                      name="email"
                      placeholder="Ingresa tu correo"
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                      required
                    />
                  </div>
                  <div>
                    <Button className="w-full" size="sm" type="submit" disabled={loading}>
                      {loading ? "Enviando..." : "Enviar Enlace de Reseteo"}
                    </Button>
                  </div>
                </div>
              </form>
              {message && (
                <p className="mt-4 text-sm text-center text-gray-600 dark:text-gray-300">
                  {message}
                </p>
              )}
              <div className="mt-5">
                <p className="text-center text-sm font-normal text-gray-700 sm:text-start dark:text-gray-400">
                  Espera, ya recuerdo mi contraseña...{" "}
                  <Link
                    to="/signin"
                    className="text-brand-500 hover:text-brand-600 dark:text-brand-400"
                  >
                    Click aquí
                  </Link>
                </p>
              </div>
            </div>
          </div>
        </div>
      );
    }
    ```

### Paso 2: Crear la Página de Vista (`ResetPassword.tsx`)

Esta será la página que se renderizará en la ruta `/reset-password`. Utilizará el `AuthLayout` y el `ResetPasswordForm` que acabamos de crear.

-   **Ubicación del Archivo:** `src/pages/AuthPages/ResetPassword.tsx`
-   **Código:**
    ```tsx
    import PageMeta from "../../components/common/PageMeta";
    import AuthLayout from "./AuthPageLayout";
    import ResetPasswordForm from "../../components/auth/ResetPasswordForm";

    export default function ResetPassword() {
      return (
        <>
          <PageMeta
            title="Resetear Contraseña | TailAdmin - React Dashboard"
            description="Página para resetear la contraseña de usuario."
          />
          <AuthLayout>
            <ResetPasswordForm />
          </AuthLayout>
        </>
      );
    }
    ```

### Paso 3: Añadir la Ruta en el Enrutador (`App.tsx`)

Finalmente, necesitamos hacer que la nueva página sea accesible a través de una URL.

-   **Archivo a Modificar:** `src/App.tsx`
-   **Acción:** Añadir una nueva ruta pública para `/reset-password`.
-   **Código Modificado:**
    ```tsx
    import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
    import { ProtectedRoute } from "./components/auth/ProtectedRoute";
    import Account from "./pages/Account";
    import SignIn from "./pages/AuthPages/SignIn";
    import SignUp from "./pages/AuthPages/SignUp";
    import ResetPassword from "./pages/AuthPages/ResetPassword"; // <-- 1. Importar la nueva página
    import NotFound from "./pages/OtherPage/NotFound";
    import AppLayout from "./layout/AppLayout";
    import Home from "./pages/Dashboard/Home";
    // ... otros imports

    export default function App() {
      return (
        <Router>
          <Routes>
            {/* Rutas Protegidas */}
            <Route element={<ProtectedRoute />}>
              <Route path="/account" element={<Account />} />
              <Route element={<AppLayout />}>
                <Route index path="/" element={<Home />} />
                {/* Aquí se restaurarán las otras rutas */}
              </Route>
            </Route>

            {/* Rutas Públicas */}
            <Route path="/signin" element={<SignIn />} />
            <Route path="/signup" element={<SignUp />} />
            <Route path="/reset-password" element={<ResetPassword />} /> {/* <-- 2. Añadir la nueva ruta */}
            
            {/* Ruta de Fallback */}
            <Route path="*" element={<NotFound />} />
          </Routes>
        </Router>
      );
    }
    ```
