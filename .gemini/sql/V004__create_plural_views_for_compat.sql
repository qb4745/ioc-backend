-- Migration: V004__create_plural_views_for_compat.sql
-- Objetivo: crear vistas con nombres plurales para compatibilidad con consultas/artefactos
-- que esperan tablas con nombres en plural (roles, permissions, app_users, user_roles, role_permissions, plantas).

BEGIN;

-- Drop views if exist to be idempotent
DROP VIEW IF EXISTS roles CASCADE;
DROP VIEW IF EXISTS permissions CASCADE;
DROP VIEW IF EXISTS app_users CASCADE;
DROP VIEW IF EXISTS user_roles CASCADE;
DROP VIEW IF EXISTS role_permissions CASCADE;
DROP VIEW IF EXISTS plantas CASCADE;

-- Create views mapping to singular tables
CREATE VIEW roles AS SELECT * FROM role;
CREATE VIEW permissions AS SELECT * FROM permission;
CREATE VIEW app_users AS SELECT * FROM app_user;
CREATE VIEW user_roles AS SELECT * FROM user_role;
CREATE VIEW role_permissions AS SELECT * FROM role_permission;
CREATE VIEW plantas AS SELECT * FROM planta;

COMMIT;

-- Nota: estas vistas preservan compatibilidad de lectura. Si existen procedimientos que intentan
-- escribir directamente en tablas plurales, ajustar dichos procedimientos o reemplazar por
-- funciones/API que usen la nueva estructura.

