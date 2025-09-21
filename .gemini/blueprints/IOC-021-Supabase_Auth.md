# IMPLEMENTATION_GUIDE-Supabase_Auth.md

This document outlines the step-by-step process for integrating Supabase Authentication into our Vite + React application.

## 1. Overview & Objectives

*   **Primary Goal:** To implement a complete, secure, and user-friendly authentication flow using Supabase, including user sign-up, sign-in, sign-out, and session management.
*   **Key Features:**
    *   Email/Password based sign-up and sign-in.
    *   Protected routes accessible only to authenticated users.
    *   A global authentication context (Auth Provider) to manage user state across the application.
    *   Redirects for unauthenticated users.

## 2. Prerequisites & Setup

*   **Dependencies:** We will add the official Supabase client library.
    *   Run the following command in your terminal:
        ```bash
        npm install @supabase/supabase-js
        ```
*   **Environment Variables:** Create a new file named `.env` in the root of the project (next to `package.json`) and add the following variables. You can find these keys in your Supabase project dashboard under `Settings` > `API`.

    ```env
    VITE_SUPABASE_URL="YOUR_SUPABASE_PROJECT_URL"
    VITE_SUPABASE_ANON_KEY="YOUR_SUPABASE_ANON_KEY"
    ```

## 3. Step-by-Step Implementation

### Step 3.1: Initialize Supabase Client
Create a new file to initialize and export a singleton Supabase client. This ensures we use a single instance throughout the app.

*   **File Location:** `src/lib/supabaseClient.ts`
*   **Code:**
    ```typescript
    import { createClient } from '@supabase/supabase-js';

    const supabaseUrl = import.meta.env.VITE_SUPABASE_URL;
    const supabaseAnonKey = import.meta.env.VITE_SUPABASE_ANON_KEY;

    if (!supabaseUrl || !supabaseAnonKey) {
      throw new Error("Supabase URL and Anon Key must be defined in the environment variables.");
    }

    export const supabase = createClient(supabaseUrl, supabaseAnonKey);
    ```

### Step 3.2: Create the Authentication Provider
Design a React Context Provider to manage authentication state (user, session, loading status).

*   **File Location:** `src/context/AuthProvider.tsx`
*   **Functionality:**
    *   Exposes user, session, and methods like `signIn`, `signUp`, and `signOut`.
    *   Uses Supabase's `onAuthStateChange` to listen for session changes in real-time.
*   **Code:**
    ```typescript
    import { createContext, useContext, useEffect, useState } from 'react';
    import { supabase } from '../lib/supabaseClient';
    import type { AuthChangeEvent, Session, User } from '@supabase/supabase-js';

    interface AuthContextType {
      user: User | null;
      session: Session | null;
      loading: boolean;
    }

    const AuthContext = createContext<AuthContextType>({
      user: null,
      session: null,
      loading: true,
    });

    export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
      const [user, setUser] = useState<User | null>(null);
      const [session, setSession] = useState<Session | null>(null);
      const [loading, setLoading] = useState(true);

      useEffect(() => {
        const setData = async () => {
          const { data: { session }, error } = await supabase.auth.getSession();
          if (error) throw error;
          setSession(session);
          setUser(session?.user ?? null);
          setLoading(false);
        };

        const { data: listener } = supabase.auth.onAuthStateChange((_event: AuthChangeEvent, session: Session | null) => {
          setSession(session);
          setUser(session?.user ?? null);
          setLoading(false);
        });

        setData();

        return () => {
          listener?.subscription.unsubscribe();
        };
      }, []);

      const value = {
        user,
        session,
        loading,
      };

      return (
        <AuthContext.Provider value={value}>
          {!loading && children}
        </AuthContext.Provider>
      );
    };

    export const useAuth = () => {
      const context = useContext(AuthContext);
      if (context === undefined) {
        throw new Error('useAuth must be used within an AuthProvider');
      }
      return context;
    };
    ```

### Step 3.3: Integrate Provider in App Root
Wrap the main application component with the newly created `AuthProvider`.

*   **File Location:** `src/main.tsx`
*   **Code:**
    ```typescript
    import React from 'react';
    import ReactDOM from 'react-dom/client';
    import App from './App';
    import './index.css';
    import { AuthProvider } from './context/AuthProvider';

    ReactDOM.createRoot(document.getElementById('root') as HTMLElement).render(
      <React.StrictMode>
        <AuthProvider>
          <App />
        </AuthProvider>
      </React.StrictMode>
    );
    ```

### Step 3.4: Build UI Components
Provide functional, unstyled React components for the core authentication UI.

*   **Login Component (`src/pages/AuthPages/SignIn.tsx`):**
    ```typescript
    import { useState } from 'react';
    import { supabase } from '../../lib/supabaseClient';
    import { useNavigate } from 'react-router-dom';

    const SignIn = () => {
      const [email, setEmail] = useState('');
      const [password, setPassword] = useState('');
      const navigate = useNavigate();

      const handleSignIn = async (e: React.FormEvent) => {
        e.preventDefault();
        const { error } = await supabase.auth.signInWithPassword({ email, password });
        if (error) {
          alert(error.message);
        } else {
          navigate('/');
        }
      };

      return (
        <div style={{ maxWidth: '420px', margin: '96px auto' }}>
          <h2>Sign In</h2>
          <form onSubmit={handleSignIn}>
            <input type="email" placeholder="Your email" value={email} onChange={(e) => setEmail(e.target.value)} required />
            <input type="password" placeholder="Your password" value={password} onChange={(e) => setPassword(e.target.value)} required />
            <button type="submit">Sign In</button>
          </form>
        </div>
      );
    };

    export default SignIn;
    ```

*   **Signup Component (`src/pages/AuthPages/SignUp.tsx`):**
    ```typescript
    import { useState } from 'react';
    import { supabase } from '../../lib/supabaseClient';
    import { useNavigate } from 'react-router-dom';

    const SignUp = () => {
      const [email, setEmail] = useState('');
      const [password, setPassword] = useState('');
      const navigate = useNavigate();

      const handleSignUp = async (e: React.FormEvent) => {
        e.preventDefault();
        const { error } = await supabase.auth.signUp({ email, password });
        if (error) {
          alert(error.message);
        } else {
          alert('Check your email for the login link!');
          navigate('/signin');
        }
      };

      return (
        <div style={{ maxWidth: '420px', margin: '96px auto' }}>
          <h2>Sign Up</h2>
          <form onSubmit={handleSignUp}>
            <input type="email" placeholder="Your email" value={email} onChange={(e) => setEmail(e.target.value)} required />
            <input type="password" placeholder="Your password" value={password} onChange={(e) => setPassword(e.target.value)} required />
            <button type="submit">Sign Up</button>
          </form>
        </div>
      );
    };

    export default SignUp;
    ```

*   **Account Page (`src/pages/Account.tsx`):**
    ```typescript
    import { useAuth } from '../context/AuthProvider';
    import { supabase } from '../lib/supabaseClient';
    import { useNavigate } from 'react-router-dom';

    const Account = () => {
      const { user } = useAuth();
      const navigate = useNavigate();

      const handleSignOut = async () => {
        const { error } = await supabase.auth.signOut();
        if (error) {
          console.error('Error signing out:', error);
        } else {
          navigate('/signin');
        }
      };

      return (
        <div style={{ maxWidth: '420px', margin: '96px auto' }}>
          <h2>Account</h2>
          <p>Email: {user?.email}</p>
          <button onClick={handleSignOut}>Sign Out</button>
        </div>
      );
    };

    export default Account;
    ```

### Step 3.5: Implement Protected Routes
Create a wrapper component that checks for an active user session. If no user is logged in, it should redirect to the login page.

*   **File Location:** `src/components/auth/ProtectedRoute.tsx`
*   **Code:**
    ```typescript
    import { Navigate, Outlet } from 'react-router-dom';
    import { useAuth } from '../../context/AuthProvider';

    export const ProtectedRoute = () => {
      const { user } = useAuth();

      if (!user) {
        return <Navigate to="/signin" replace />;
      }

      return <Outlet />;
    };
    ```

*   **Router Integration (`src/App.tsx`):**
    ```typescript
    // In your App.tsx or wherever your router is configured
    import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
    import { ProtectedRoute } from "./components/auth/ProtectedRoute";
    import Account from "./pages/Account";
    import SignIn from "./pages/AuthPages/SignIn";
    import SignUp from "./pages/AuthPages/SignUp";
    // ... other imports

    export default function App() {
      return (
        <Router>
          <Routes>
            {/* Protected Routes */}
            <Route element={<ProtectedRoute />}>
              <Route path="/account" element={<Account />} />
              {/* Add other protected routes here, e.g., the main AppLayout */}
              {/* 
              <Route element={<AppLayout />}>
                <Route index path="/" element={<Home />} />
                ...
              </Route>
              */}
            </Route>

            {/* Public Routes */}
            <Route path="/signin" element={<SignIn />} />
            <Route path="/signup" element={<SignUp />} />
            
            {/* Fallback Route */}
            <Route path="*" element={<NotFound />} />
          </Routes>
        </Router>
      );
    }
    ```

## 4. Verification & Next Steps

*   **Testing Flow:**
    1.  Navigate to `/signup` and create a new account.
    2.  Check your email and click the confirmation link.
    3.  Navigate to `/signin` and log in with your new credentials.
    4.  You should be redirected to the `/` or `/account` page upon successful login.
    5.  Click the "Sign Out" button.
    6.  Attempt to access `/account` directly. You should be redirected back to `/signin`.

*   **Next Steps:**
    *   **Password Reset:** Create a "Forgot Password" flow using `supabase.auth.resetPasswordForEmail()`.
    *   **User Profiles:** Create a `profiles` table in Supabase to store additional user data (e.g., username, avatar URL) and link it to the `auth.users` table via a foreign key relationship.

## 5. Security & Best Practices

*   **Row-Level Security (RLS):** RLS is crucial for securing your data. Always enable RLS on tables that contain sensitive information. For example, to ensure users can only access their own profile data in a `profiles` table:
    1.  Navigate to `Authentication` > `Policies` in your Supabase dashboard.
    2.  Click "New Policy" on your `profiles` table.
    3.  Create a policy with a `USING` expression like: `auth.uid() = user_id` (assuming you have a `user_id` column that is a foreign key to `auth.users.id`).

*   **Client-Side Security:** Remember that the `VITE_SUPABASE_ANON_KEY` is public and visible in the client-side code. It is safe to expose as long as you have RLS enabled on your tables. Any highly sensitive operations should be handled in server-side Supabase Edge Functions, which can be called securely from your application.
