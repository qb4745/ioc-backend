-- init-h2.sql: create a citext domain so H2 accepts "citext" columnDefinition used in entities
CREATE DOMAIN IF NOT EXISTS citext AS VARCHAR;

