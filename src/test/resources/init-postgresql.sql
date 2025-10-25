-- ===================================================================
-- POSTGRESQL TESTCONTAINERS INITIALIZATION SCRIPT
-- ===================================================================
-- This script runs automatically when PostgreSQL container starts
-- It enables extensions needed by the application entities

-- Enable citext extension (case-insensitive text type)
-- Used in: AppUser.email
CREATE EXTENSION IF NOT EXISTS citext;

-- Enable UUID generation functions
-- Used in: Various entities with UUID columns
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Enable pg_trgm for similarity searches (optional, if needed)
-- CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Enable hstore for key-value storage (optional)
-- CREATE EXTENSION IF NOT EXISTS hstore;

-- Verify extensions are installed
SELECT extname, extversion FROM pg_extension
WHERE extname IN ('citext', 'uuid-ossp')
ORDER BY extname;