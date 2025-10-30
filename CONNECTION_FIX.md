# Database Connection Error - FIXED

## Problem Summary

The application failed to start with the following errors:

1. **Primary Error**: `java.net.UnknownHostException: aws-1-sa-east-1.pooler.supabase.com`
   - The database connection failed because required environment variables were missing

2. **Secondary Error**: `Unable to determine Dialect without JDBC metadata`
   - This occurred because Hibernate couldn't query the database metadata (due to connection failure)
   - The dialect was commented out in the base configuration

## Root Cause

The application requires environment variables that were not set:

- `SUPABASE_DB_PASSWORD` - **REQUIRED** for database connection
- `SUPABASE_SERVICE_ROLE_KEY` - **REQUIRED** for automatic user creation feature
- `SUPABASE_URL` - Has a default value but should be explicitly set

## Solution Applied

### 1. Fixed Hibernate Configuration

**File**: `src/main/resources/application.properties`

**Change**: Uncommented the Hibernate dialect property:
```properties
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

**Why**: This prevents Hibernate from trying to query database metadata to determine the dialect. When the database is unreachable, having an explicit dialect prevents cascading errors.

### 2. Created Environment Setup Script

**File**: `env-setup.sh` (NEW)

This script helps you set up all required environment variables. Copy it and fill in your actual values.

## How to Fix Your Environment

### Option 1: Using the Setup Script (Recommended)

```bash
# 1. Edit the env-setup.sh file with your actual credentials
nano env-setup.sh

# 2. Source it to export variables
source env-setup.sh

# 3. Run the application
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### Option 2: Manual Export

```bash
# Get password from Supabase Dashboard > Settings > Database
export SUPABASE_DB_PASSWORD="your-actual-password"

# Get service role key from Supabase Dashboard > Settings > API
export SUPABASE_SERVICE_ROLE_KEY="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# Supabase project URL
export SUPABASE_URL="https://bdyvzjpkycnekjrlqlfp.supabase.co"

# Optional: Metabase secret (if using Metabase)
export METABASE_SECRET_KEY="your-metabase-secret"

# Run the application
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### Option 3: IDE Configuration (IntelliJ IDEA)

1. Go to **Run** → **Edit Configurations**
2. Select your Spring Boot run configuration
3. Add Environment Variables:
   ```
   SUPABASE_DB_PASSWORD=your-actual-password
   SUPABASE_SERVICE_ROLE_KEY=your-service-role-key
   SUPABASE_URL=https://bdyvzjpkycnekjrlqlfp.supabase.co
   ```
4. Apply and run

## How to Get the Required Credentials

### 1. SUPABASE_DB_PASSWORD

1. Go to [Supabase Dashboard](https://app.supabase.com)
2. Select your project: `bdyvzjpkycnekjrlqlfp`
3. Go to **Settings** → **Database**
4. Copy the database password
5. ⚠️ If you don't have it, you may need to reset it

### 2. SUPABASE_SERVICE_ROLE_KEY

1. Go to [Supabase Dashboard](https://app.supabase.com)
2. Select your project: `bdyvzjpkycnekjrlqlfp`
3. Go to **Settings** → **API**
4. Scroll to **Project API keys**
5. Copy the **service_role** key (NOT the anon key!)
6. ⚠️ **NEVER commit this key to Git** - it has admin privileges

### 3. METABASE_SECRET_KEY (Optional)

This is only needed if you're using Metabase integration:
1. Get it from your Metabase instance settings
2. Or ask your team lead for the secret key

## Verifying the Fix

After setting the environment variables, run:

```bash
# Check that variables are set
echo $SUPABASE_DB_PASSWORD
echo $SUPABASE_SERVICE_ROLE_KEY

# Try to start the application
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

You should see:
```
✅ HikariCP pool starting successfully
✅ Hibernate initializing without dialect errors
✅ Application started on port 8080
```

## Troubleshooting

### Still getting connection errors?

1. **Check network connectivity**:
   ```bash
   ping aws-1-sa-east-1.pooler.supabase.com
   ```

2. **Verify credentials are correct**:
   - Try connecting with psql:
   ```bash
   psql "postgresql://postgres.bdyvzjpkycnekjrlqlfp:$SUPABASE_DB_PASSWORD@aws-1-sa-east-1.pooler.supabase.com:5432/postgres"
   ```

3. **Check Supabase project status**:
   - Go to Supabase Dashboard and verify the project is running
   - Check if there are any service disruptions

### Still getting dialect errors?

This should be fixed now with the explicit dialect. If you still see this error, ensure you're using the latest version of `application.properties` from the repository.

## Security Notes

⚠️ **IMPORTANT SECURITY REMINDERS**:

1. **NEVER commit `env-setup.sh` with real credentials** to Git
2. Add your personal env file to `.gitignore`
3. The `SUPABASE_SERVICE_ROLE_KEY` bypasses Row Level Security - keep it secret
4. For production, use secure environment variable management (Render, AWS Secrets Manager, etc.)

## Files Modified

- ✅ `src/main/resources/application.properties` - Added explicit dialect
- ✅ `env-setup.sh` - NEW - Environment setup helper script
- ✅ `CONNECTION_FIX.md` - NEW - This documentation

## Related Documentation

- [USER_CREATION_FIX.md](./USER_CREATION_FIX.md) - Automatic user creation feature
- [SUPABASE_POOL_FIX.md](./SUPABASE_POOL_FIX.md) - Connection pool configuration

## Next Steps

1. ✅ Set up environment variables
2. ✅ Run the application
3. ⏳ Test the automatic user creation endpoint
4. ⏳ Configure the same environment variables in Render (for production)

