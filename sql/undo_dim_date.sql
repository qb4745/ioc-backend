-- WARNING: Este script elimina la tabla de dimensión de tiempo (dim_date).
-- Asegúrate de que ninguna otra tabla o vista dependa de 'dim_date'
-- antes de ejecutarlo.

-- Paso 1: Eliminar la tabla dim_date si existe
-- El comando CASCADE eliminará automáticamente cualquier objeto que dependa
-- de esta tabla (por ejemplo, claves foráneas en otras tablas).
-- Si no quieres que se eliminen las claves foráneas en cascada,
-- primero debes eliminar manualmente esas claves foráneas en tus tablas de hechos
-- (ej. fact_production) antes de ejecutar este DROP TABLE.
-- Para este escenario, se asume que aún no has creado claves foráneas en otras tablas.
DROP TABLE IF EXISTS public.dim_date CASCADE;

-- Paso 2 (Opcional): Eliminar las secuencias si se hubieran creado implícitamente
-- (Para dim_date no deberían crearse secuencias, pero es una buena práctica de cleanup)
-- DROP SEQUENCE IF EXISTS dim_date_id_seq; -- No aplicable para date_key INT PRIMARY KEY
