-- =============================================================================
-- ||                                                                         ||
-- ||    SCRIPT DE ANÁLISIS COMPLETO DEL ESQUEMA 'public' EN POSTGRESQL        ||
-- ||                                                                         ||
-- =============================================================================
-- ||                                                                         ||
-- ||    Instrucciones:                                                       ||
-- ||    1. Copia y pega este script completo en el SQL Editor de Supabase.    ||
-- ||    2. Ejecútalo.                                                         ||
-- ||    3. Revisa los resultados de cada consulta en la parte inferior.       ||
-- ||                                                                         ||
-- =============================================================================


-- =============================================================================
-- SECCIÓN 1: LISTADO DE TODAS LAS TABLAS
-- Muestra todas las tablas de datos base en el esquema 'public'.
-- =============================================================================
SELECT
    table_name AS "Nombre de la Tabla"
FROM
    information_schema.tables
WHERE
    table_schema = 'public' AND table_type = 'BASE TABLE'
ORDER BY
    table_name;


-- =============================================================================
-- SECCIÓN 2: LISTADO DE TODAS LAS VISTAS
-- Muestra todas las vistas (tablas virtuales) en el esquema 'public'.
-- =============================================================================
SELECT
    table_name AS "Nombre de la Vista"
FROM
    information_schema.views
WHERE
    table_schema = 'public'
ORDER BY
    table_name;


-- =============================================================================
-- SECCIÓN 3: DETALLE DE COLUMNAS PARA TODAS LAS TABLAS
-- Muestra cada columna para cada tabla, su tipo de dato, si permite nulos
-- y su valor por defecto.
-- =============================================================================
SELECT
    table_name AS "Nombre de la Tabla",
    column_name AS "Nombre de la Columna",
    data_type AS "Tipo de Dato",
    is_nullable AS "Permite Nulos",
    column_default AS "Valor por Defecto"
FROM
    information_schema.columns
WHERE
    table_schema = 'public'
ORDER BY
    table_name,
    ordinal_position;


-- =============================================================================
-- SECCIÓN 4: LISTADO DE TODAS LAS RESTRICCIONES (CONSTRAINTS)
-- Muestra todas las llaves primarias, foráneas, únicas y de chequeo.
-- Esencial para entender las relaciones y reglas de integridad de los datos.
-- =============================================================================
SELECT
    conrelid::regclass AS "Tabla",
    conname AS "Nombre de la Restricción",
    CASE contype
        WHEN 'p' THEN 'Primary Key'
        WHEN 'f' THEN 'Foreign Key'
        WHEN 'u' THEN 'Unique'
        WHEN 'c' THEN 'Check'
        ELSE 'Desconocido'
    END AS "Tipo",
    pg_get_constraintdef(c.oid) AS "Definición"
FROM
    pg_constraint c
JOIN
    pg_namespace n ON n.oid = c.connamespace
WHERE
    n.nspname = 'public'
ORDER BY
    "Tabla",
    "Tipo";


-- =============================================================================
-- SECCIÓN 5: LISTADO DE TODOS LOS ÍNDICES
-- Muestra todos los índices del esquema, que son clave para el rendimiento
-- de las consultas.
-- =============================================================================
SELECT
    tablename AS "Tabla",
    indexname AS "Nombre del Índice",
    indexdef AS "Definición del Índice"
FROM
    pg_indexes
WHERE
    schemaname = 'public'
ORDER BY
    tablename,
    indexname;


-- =============================================================================
-- SECCIÓN 6: LISTADO DE TODAS LAS FUNCIONES Y PROCEDIMIENTOS
-- Muestra toda la lógica personalizada (funciones) que has almacenado
-- en la base de datos.
-- =============================================================================
SELECT
    routine_name AS "Nombre de la Función/Procedimiento",
    routine_type AS "Tipo",
    data_type AS "Tipo de Retorno"
FROM
    information_schema.routines
WHERE
    specific_schema = 'public'
ORDER BY
    routine_name;


-- =============================================================================
-- SECCIÓN 7: LISTADO DE TODAS LAS SECUENCIAS
-- Muestra las secuencias, usadas generalmente para generar valores
-- autoincrementales en las columnas de ID.
-- =============================================================================
SELECT
    sequence_name AS "Nombre de la Secuencia",
    data_type AS "Tipo de Dato",
    start_value AS "Valor Inicial",
    minimum_value AS "Valor Mínimo",
    maximum_value AS "Valor Máximo",
    increment AS "Incremento"
FROM
    information_schema.sequences
WHERE
    sequence_schema = 'public'
ORDER BY
    sequence_name;

-- =============================================================================
-- ||                         FIN DEL SCRIPT                                  ||
-- =============================================================================
