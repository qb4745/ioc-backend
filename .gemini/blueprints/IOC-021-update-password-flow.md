# Blueprint: Actualización de Contraseña Post-Reseteo

Esta guía detalla el proceso paso a paso para implementar la funcionalidad que permite a un usuario actualizar su contraseña después de haber solicitado un reseteo.

## 1. Objetivo y Alcance
*   **Objetivo:** Crear una página segura y funcional en la ruta `/update-password` donde los usuarios puedan establecer una nueva contraseña tras hacer clic en el enlace de recuperación enviado a su correo.
*   **Stack Tecnológico Involucrado:** React, TypeScript, Supabase, React Router, Tailwind CSS.

## 2. Prerrequisitos
*   **Dependencias:** No se requieren nuevas dependencias. Se utilizarán las existentes (`@supabase/supabase-js`, `react-router-dom`).
*   **Configuración:** La URL de redirección en el componente `ResetPasswordForm.tsx` debe apuntar a `/update-password`.

## 3. Implementación Paso a Paso

### 3.1 Frontend (React)
*   **Paso 1: Crear el Componente del Formulario (`UpdatePasswordForm.tsx`)**
    *   **Propósito:** Este componente contendrá el formulario y la lógica para actualizar la contraseña del usuario. Manejará el evento `PASSWORD_RECOVERY` de Supabase.
    *   **Archivo:** `src/components/auth/UpdatePasswordForm.tsx`
    *   **Código:**
        ```tsx
        import { useState, useEffect } from "react";
        import { useNavigate } from "react-router-dom";
        import { supabase } from "../../lib/supabaseClient";
        import Label from "../form/Label";
        import Input from "../form/input/InputField";
        import Button from "../ui/button/Button";

        export default function UpdatePasswordForm() {
          const [password, setPassword] = useState("");
          const [confirmPassword, setConfirmPassword] = useState("");
          const [loading, setLoading] = useState(false);
          const [message, setMessage] = useState("");
          const [error, setError] = useState("");
          const navigate = useNavigate();

          useEffect(() => {
            const { data: { subscription } } = supabase.auth.onAuthStateChange((event, session) => {
              if (event === 'PASSWORD_RECOVERY') {
                // La sesión ya está activa aquí, el usuario está autenticado temporalmente
                // para cambiar su contraseña.
              }
            });

            return () => subscription.unsubscribe();
          }, []);

          const handleUpdatePassword = async (e: React.FormEvent) => {
            e.preventDefault();
            if (password !== confirmPassword) {
              setError("Las contraseñas no coinciden.");
              return;
            }
            if (password.length < 6) {
              setError("La contraseña debe tener al menos 6 caracteres.");
              return;
            }

            setLoading(true);
            setError("");
            setMessage("");

            const { error } = await supabase.auth.updateUser({ password: password });

            if (error) {
              setError("Error al actualizar la contraseña: " + error.message);
            } else {
              setMessage("Tu contraseña ha sido actualizada con éxito. Serás redirigido para iniciar sesión.");
              setTimeout(() => {
                navigate("/signin");
              }, 3000);
            }
            setLoading(false);
          };

          return (
            <div className="flex flex-col flex-1">
              <div className="flex flex-col justify-center flex-1 w-full max-w-md mx-auto">
                <div className="mb-5 sm:mb-8">
                  <h1 className="text-title-sm sm:text-title-md mb-2 font-semibold text-gray-800 dark:text-white/90">
                    Restablece tu Contraseña
                  </h1>
                  <p className="text-sm text-gray-500 dark:text-gray-400">
                    Define una nueva contraseña para acceder a tu cuenta con la confianza y calidad de siempre.
                  </p>
                </div>
                <div>
                  <form onSubmit={handleUpdatePassword}>
                    <div className="space-y-5">
                      <div>
                        <Label htmlFor="password">
                          Nueva Contraseña<span className="text-error-500">*</span>
                        </Label>
                        <Input
                          type="password"
                          id="password"
                          name="password"
                          placeholder="Ingresa tu nueva contraseña"
                          value={password}
                          onChange={(e) => setPassword(e.target.value)}
                          required
                        />
                      </div>
                      <div>
                        <Label htmlFor="confirmPassword">
                          Confirmar Contraseña<span className="text-error-500">*</span>
                        </Label>
                        <Input
                          type="password"
                          id="confirmPassword"
                          name="confirmPassword"
                          placeholder="Confirma tu nueva contraseña"
                          value={confirmPassword}
                          onChange={(e) => setConfirmPassword(e.target.value)}
                          required
                        />
                      </div>
                      <div>
                        <Button className="w-full" size="sm" type="submit" disabled={loading}>
                          {loading ? "Actualizando..." : "Actualizar Contraseña"}
                        </Button>
                      </div>
                    </div>
                  </form>
                  {error && (
                    <p className="mt-4 text-sm text-center text-error-500">
                      {error}
                    </p>
                  )}
                  {message && (
                    <p className="mt-4 text-sm text-center text-success-500">
                      {message}
                    </p>
                  )}
                </div>
              </div>
            </div>
          );
        }
        ```

*   **Paso 2: Crear la Página de Vista (`UpdatePassword.tsx`)**
    *   **Propósito:** Renderizar la página en la nueva ruta, utilizando el `AuthLayout` para mantener la consistencia visual.
    *   **Archivo:** `src/pages/AuthPages/UpdatePassword.tsx`
    *   **Código:**
        ```tsx
        import PageMeta from "../../components/common/PageMeta";
        import AuthLayout from "./AuthPageLayout";
        import UpdatePasswordForm from "../../components/auth/UpdatePasswordForm";

        export default function UpdatePassword() {
          return (
            <>
              <PageMeta
                title="Actualizar Contraseña | TailAdmin - React Dashboard"
                description="Página para actualizar la contraseña de usuario."
              />
              <AuthLayout>
                <UpdatePasswordForm />
              </AuthLayout>
            </>
          );
        }
        ```

*   **Paso 3: Añadir la Ruta en el Enrutador (`App.tsx`)**
    *   **Propósito:** Hacer que la nueva página sea accesible a través de la URL `/update-password`.
    *   **Archivo:** `src/App.tsx`
    *   **Código a añadir:**
        ```tsx
        // 1. Añadir la importación
        import UpdatePassword from "./pages/AuthPages/UpdatePassword";

        // 2. Añadir la ruta dentro de <Routes>
        <Route path="/update-password" element={<UpdatePassword />} />
        ```

## 4. Flujo de Verificación
*   1. Ir a la página `/reset-password` y solicitar un enlace de reseteo.
*   2. Abrir el correo electrónico y hacer clic en el enlace.
*   3. Ser redirigido a la nueva página `/update-password`.
*   4. Ingresar una nueva contraseña en ambos campos y hacer clic en "Actualizar Contraseña".
*   5. **Resultado Esperado:** Ver un mensaje de éxito y ser redirigido a la página de inicio de sesión (`/signin`) después de unos segundos.
*   6. Intentar iniciar sesión con la nueva contraseña.

## 5. Consideraciones de Seguridad y Buenas Prácticas
*   El token de recuperación de contraseña es de un solo uso y caduca. La lógica de Supabase maneja esto automáticamente.
*   Se debe proporcionar feedback claro al usuario, tanto en caso de éxito como de error, para evitar confusiones.
