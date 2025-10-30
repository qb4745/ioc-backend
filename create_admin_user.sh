#!/bin/bash
# =====================================================
# Script para crear usuario ADMIN completo
# Versión: Solo API REST (sin psql)
# =====================================================

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

print_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
print_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
print_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# --- VARIABLES ---
SUPABASE_URL="https://bdyvzjpkycnekjrlqlfp.supabase.co"
SUPABASE_SERVICE_ROLE_KEY="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJkeXZ6anBreWNuZWtqcmxxbGZwIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc1NzIxMDQwNywiZXhwIjoyMDcyNzg2NDA3fQ.zpAwZYOKqFBLPSlfr2k4Ky9cNOrWjj_RPD6DZtK6IV0"
SUPABASE_ANON_KEY="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJkeXZ6anBreWNuZWtqcmxxbGZwIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTcyMTA0MDcsImV4cCI6MjA3Mjc4NjQwN30.xvaRNhVi_6SeJhH7fXbBHcdLZSbDrWVE-nzi2gqJPxI"

# Solicitar datos
read -p "Email del admin [admin@example.com]: " ADMIN_EMAIL
ADMIN_EMAIL=${ADMIN_EMAIL:-admin@example.com}

read -sp "Password del admin: " ADMIN_PASSWORD
echo
if [ -z "$ADMIN_PASSWORD" ]; then
    print_error "Password no puede estar vacío"
    exit 1
fi

read -p "Nombre [Admin]: " ADMIN_NOMBRE
ADMIN_NOMBRE=${ADMIN_NOMBRE:-Admin}

read -p "Apellido [Sistema]: " ADMIN_APELLIDO
ADMIN_APELLIDO=${ADMIN_APELLIDO:-Sistema}

read -p "Planta ID [1]: " PLANTA_ID
PLANTA_ID=${PLANTA_ID:-1}

print_info "Creando usuario admin en Supabase..."

RESPONSE=$(curl -s -X POST "$SUPABASE_URL/auth/v1/admin/users" \
  -H "apikey: $SUPABASE_SERVICE_ROLE_KEY" \
  -H "Authorization: Bearer $SUPABASE_SERVICE_ROLE_KEY" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"$ADMIN_EMAIL\",
    \"password\": \"$ADMIN_PASSWORD\",
    \"email_confirm\": true
  }")

SUPABASE_USER_ID=$(echo "$RESPONSE" | grep -o '"id":"[^"]*"' | cut -d'"' -f4)

if [ -z "$SUPABASE_USER_ID" ]; then
    print_error "No se pudo crear el usuario en Supabase"
    echo "Respuesta: $RESPONSE"
    exit 1
fi

print_info "✓ Usuario creado en Supabase con ID: $SUPABASE_USER_ID"

# ============================================
# Insertar usando API REST de Supabase
# ============================================
print_info "Insertando roles usando API REST..."

# Insertar roles
curl -s -X POST "$SUPABASE_URL/rest/v1/roles" \
  -H "apikey: $SUPABASE_SERVICE_ROLE_KEY" \
  -H "Authorization: Bearer $SUPABASE_SERVICE_ROLE_KEY" \
  -H "Content-Type: application/json" \
  -H "Prefer: resolution=merge-duplicates" \
  -d '[
    {"name": "ADMIN", "description": "Administrator with full access"},
    {"name": "USER", "description": "Regular user with limited access"}
  ]' > /dev/null

print_info "Insertando usuario en app_users usando API REST..."

# Insertar usuario
USER_INSERT_RESPONSE=$(curl -s -X POST "$SUPABASE_URL/rest/v1/app_users" \
  -H "apikey: $SUPABASE_SERVICE_ROLE_KEY" \
  -H "Authorization: Bearer $SUPABASE_SERVICE_ROLE_KEY" \
  -H "Content-Type: application/json" \
  -H "Prefer: return=representation" \
  -d "{
    \"supabase_user_id\": \"$SUPABASE_USER_ID\",
    \"email\": \"$ADMIN_EMAIL\",
    \"primer_nombre\": \"$ADMIN_NOMBRE\",
    \"primer_apellido\": \"$ADMIN_APELLIDO\",
    \"planta_id\": $PLANTA_ID,
    \"is_active\": true
  }")

APP_USER_ID=$(echo "$USER_INSERT_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

if [ -z "$APP_USER_ID" ]; then
    print_warn "Usuario podría ya existir, obteniendo ID..."
    
    GET_USER_RESPONSE=$(curl -s -X GET "$SUPABASE_URL/rest/v1/app_users?supabase_user_id=eq.$SUPABASE_USER_ID&select=id" \
      -H "apikey: $SUPABASE_SERVICE_ROLE_KEY" \
      -H "Authorization: Bearer $SUPABASE_SERVICE_ROLE_KEY")
    
    APP_USER_ID=$(echo "$GET_USER_RESPONSE" | grep -o '"id":[0-9]*' | cut -d':' -f2)
fi

if [ -z "$APP_USER_ID" ]; then
    print_error "No se pudo obtener el ID del usuario en app_users"
    exit 1
fi

print_info "✓ Usuario en app_users con ID: $APP_USER_ID"

print_info "Obteniendo rol ADMIN..."

# Obtener ID del rol ADMIN
ADMIN_ROLE_RESPONSE=$(curl -s -X GET "$SUPABASE_URL/rest/v1/roles?name=eq.ADMIN&select=id" \
  -H "apikey: $SUPABASE_SERVICE_ROLE_KEY" \
  -H "Authorization: Bearer $SUPABASE_SERVICE_ROLE_KEY")

ADMIN_ROLE_ID=$(echo "$ADMIN_ROLE_RESPONSE" | grep -o '"id":[0-9]*' | cut -d':' -f2)

if [ -z "$ADMIN_ROLE_ID" ]; then
    print_error "No se pudo obtener el ID del rol ADMIN"
    exit 1
fi

print_info "Asignando rol ADMIN (ID: $ADMIN_ROLE_ID) al usuario..."

# Asignar rol
curl -s -X POST "$SUPABASE_URL/rest/v1/user_roles" \
  -H "apikey: $SUPABASE_SERVICE_ROLE_KEY" \
  -H "Authorization: Bearer $SUPABASE_SERVICE_ROLE_KEY" \
  -H "Content-Type: application/json" \
  -H "Prefer: resolution=ignore-duplicates" \
  -d "{
    \"user_id\": $APP_USER_ID,
    \"role_id\": $ADMIN_ROLE_ID
  }" > /dev/null

print_info "✓ Rol asignado exitosamente"

print_info "Obteniendo token de acceso..."

TOKEN_RESPONSE=$(curl -s -X POST "$SUPABASE_URL/auth/v1/token?grant_type=password" \
  -H "apikey: $SUPABASE_ANON_KEY" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"$ADMIN_EMAIL\",
    \"password\": \"$ADMIN_PASSWORD\"
  }")

ACCESS_TOKEN=$(echo "$TOKEN_RESPONSE" | grep -o '"access_token":"[^"]*"' | cut -d'"' -f4)

if [ -z "$ACCESS_TOKEN" ]; then
    print_error "No se pudo obtener el token"
    echo "Respuesta: $TOKEN_RESPONSE"
    exit 1
fi

print_info "✓ Token obtenido exitosamente"

echo ""
echo "======================================"
echo "✅ Usuario ADMIN creado exitosamente"
echo "======================================"
echo ""
echo "Email: $ADMIN_EMAIL"
echo "Supabase ID: $SUPABASE_USER_ID"
echo "App User ID: $APP_USER_ID"
echo "Role ID: $ADMIN_ROLE_ID"
echo ""
echo "export TOKEN='$ACCESS_TOKEN'"
echo ""
