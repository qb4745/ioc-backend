#!/bin/bash
# ===================================================================
# Environment Variables Setup Script for IOC Backend
# ===================================================================
# This script helps you set up the required environment variables
# for running the application locally.
#
# Usage:
#   1. Copy this file and fill in your actual values
#   2. Source it: source env-setup.sh
#   OR
#   3. Export variables manually in your shell
# ===================================================================

# ===================================================================
# DATABASE CONNECTION (REQUIRED)
# ===================================================================
# Get this password from Supabase Dashboard > Settings > Database
export SUPABASE_DB_PASSWORD="HANWVh91w8ZdZvFZ"
export DATABASE_URL="postgresql://postgres.bdyvzjpkycnekjrlqlfp:${SUPABASE_DB_PASSWORD}@aws-0-sa-east-1.pooler.supabase.com:5432/postgres"

# Optional: Override the default database URL
# export SPRING_DATASOURCE_URL="jdbc:postgresql://aws-1-sa-east-1.pooler.supabase.com:5432/postgres?reWriteBatchedInserts=true&prepareThreshold=0&preferQueryMode=simple&sslmode=require"

# ===================================================================
# SUPABASE AUTH API (REQUIRED for user creation feature)
# ===================================================================
# Get these from Supabase Dashboard > Settings > API
export SUPABASE_URL="https://bdyvzjpkycnekjrlqlfp.supabase.co"
export SUPABASE_SERVICE_ROLE_KEY="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJkeXZ6anBreWNuZWtqcmxxbGZwIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc1NzIxMDQwNywiZXhwIjoyMDcyNzg2NDA3fQ.zpAwZYOKqFBLPSlfr2k4Ky9cNOrWjj_RPD6DZtK6IV0"
export SUPABASE_ANON_KEY="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJkeXZ6anBreWNuZWtqcmxxbGZwIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTcyMTA0MDcsImV4cCI6MjA3Mjc4NjQwN30.xvaRNhVi_6SeJhH7fXbBHcdLZSbDrWVE-nzi2gqJPxI"

# ===================================================================
# METABASE (OPTIONAL - only if using Metabase integration)
# ===================================================================
export METABASE_SECRET_KEY="42cc18c4d202ab992899ba0bcc1ecb61c2f8601d388da11c25a25ef62b1a10e6"
export METABASE_URL="https://treated-paste-eos-memo.trycloudflare.com"


# ===================================================================
# Verify the variables are set
# ===================================================================
echo "====================================================================="
echo "Environment Variables Status:"
echo "====================================================================="
echo "SUPABASE_DB_PASSWORD: ${SUPABASE_DB_PASSWORD:+✓ SET}"
echo "SUPABASE_URL: ${SUPABASE_URL:+✓ SET}"
echo "SUPABASE_SERVICE_ROLE_KEY: ${SUPABASE_SERVICE_ROLE_KEY:+✓ SET}"
echo "SUPABASE_ANON_KEY: ${SUPABASE_ANON_KEY:+✓ SET}"
echo "METABASE_SECRET_KEY: ${METABASE_SECRET_KEY:+✓ SET (optional)}"
echo "====================================================================="

if [ -z "$SUPABASE_DB_PASSWORD" ]; then
    echo "⚠️  WARNING: SUPABASE_DB_PASSWORD is not set!"
    echo "   Get it from: Supabase Dashboard > Settings > Database > Password"
fi

if [ -z "$SUPABASE_SERVICE_ROLE_KEY" ]; then
    echo "⚠️  WARNING: SUPABASE_SERVICE_ROLE_KEY is not set!"
    echo "   Get it from: Supabase Dashboard > Settings > API > service_role key"
fi

echo ""
echo "To use these variables in your current shell, run:"
echo "  source env-setup.sh"
echo ""

