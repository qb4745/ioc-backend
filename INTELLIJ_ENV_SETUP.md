# IntelliJ IDEA - Environment Variables Setup

## Your Environment Variables

Copy these values to configure your IntelliJ run configuration:

```
METABASE_SECRET_KEY=42cc18c4d202ab992899ba0bcc1ecb61c2f8601d388da11c25a25ef62b1a10e6
SUPABASE_DB_PASSWORD=HANWVh91w8ZdZvFZ
SUPABASE_JWT_ISSUER_URI=https://bdyvzjpkycnekjrlqlfp.supabase.co/auth/v1
SUPABASE_SERVICE_ROLE_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJkeXZ6anBreWNuZWtqcmxxbGZwIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc1NzIxMDQwNywiZXhwIjoyMDcyNzg2NDA3fQ.zpAwZYOKqFBLPSlfr2k4Ky9cNOrWjj_RPD6DZtK6IV0
SUPABASE_URL=https://bdyvzjpkycnekjrlqlfp.supabase.co
```

## Step-by-Step Setup in IntelliJ IDEA

### Method 1: Using Run Configuration (Recommended)

1. **Open Run Configurations**
   - Click the dropdown next to the Run button (▶️) in the toolbar
   - Select **Edit Configurations...**
   - OR: Go to **Run** → **Edit Configurations...**

2. **Find Your Spring Boot Configuration**
   - In the left panel, expand **Spring Boot**
   - Select **IocbackendApplication** (or your main application configuration)

3. **Add Environment Variables**
   - Find the **Environment variables** field
   - Click the **folder icon** (📁) or **Edit** button next to it
   
4. **Add Each Variable**
   In the popup dialog, click the **+** button and add each variable:
   
   ```
   Name: METABASE_SECRET_KEY
   Value: 42cc18c4d202ab992899ba0bcc1ecb61c2f8601d388da11c25a25ef62b1a10e6
   ```
   
   ```
   Name: SUPABASE_DB_PASSWORD
   Value: HANWVh91w8ZdZvFZ
   ```
   
   ```
   Name: SUPABASE_JWT_ISSUER_URI
   Value: https://bdyvzjpkycnekjrlqlfp.supabase.co/auth/v1
   ```
   
   ```
   Name: SUPABASE_SERVICE_ROLE_KEY
   Value: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJkeXZ6anBreWNuZWtqcmxxbGZwIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc1NzIxMDQwNywiZXhwIjoyMDcyNzg2NDA3fQ.zpAwZYOKqFBLPSlfr2k4Ky9cNOrWjj_RPD6DZtK6IV0
   ```
   
   ```
   Name: SUPABASE_URL
   Value: https://bdyvzjpkycnekjrlqlfp.supabase.co
   ```

5. **Apply and Save**
   - Click **OK** in the environment variables dialog
   - Click **OK** or **Apply** in the Run Configurations dialog

6. **Run the Application**
   - Click the Run button (▶️) or press `Shift + F10`
   - The application should now start successfully! ✅

### Method 2: Quick Copy-Paste Format

If IntelliJ has a text field for environment variables, paste this (semicolon-separated):

```
METABASE_SECRET_KEY=42cc18c4d202ab992899ba0bcc1ecb61c2f8601d388da11c25a25ef62b1a10e6;SUPABASE_DB_PASSWORD=HANWVh91w8ZdZvFZ;SUPABASE_JWT_ISSUER_URI=https://bdyvzjpkycnekjrlqlfp.supabase.co/auth/v1;SUPABASE_SERVICE_ROLE_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJkeXZ6anBreWNuZWtqcmxxbGZwIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc1NzIxMDQwNywiZXhwIjoyMDcyNzg2NDA3fQ.zpAwZYOKqFBLPSlfr2k4Ky9cNOrWjj_RPD6DZtK6IV0;SUPABASE_URL=https://bdyvzjpkycnekjrlqlfp.supabase.co
```

### Method 3: Using EnvFile Plugin (Alternative)

If you have the **EnvFile** plugin installed:

1. Create a file `.env` in the project root:
   ```bash
   # DO NOT COMMIT THIS FILE TO GIT!
   METABASE_SECRET_KEY=42cc18c4d202ab992899ba0bcc1ecb61c2f8601d388da11c25a25ef62b1a10e6
   SUPABASE_DB_PASSWORD=HANWVh91w8ZdZvFZ
   SUPABASE_JWT_ISSUER_URI=https://bdyvzjpkycnekjrlqlfp.supabase.co/auth/v1
   SUPABASE_SERVICE_ROLE_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJkeXZ6anBreWNuZWtqcmxxbGZwIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc1NzIxMDQwNywiZXhwIjoyMDcyNzg2NDA3fQ.zpAwZYOKqFBLPSlfr2k4Ky9cNOrWjj_RPD6DZtK6IV0
   SUPABASE_URL=https://bdyvzjpkycnekjrlqlfp.supabase.co
   ```

2. In Run Configuration, enable the EnvFile plugin and point it to `.env`

3. **⚠️ IMPORTANT**: Add `.env` to `.gitignore`!

## Visual Guide

```
┌─────────────────────────────────────────────────────────────┐
│ Run/Debug Configurations                                     │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│ Spring Boot                                                  │
│  └─ IocbackendApplication                                    │
│                                                              │
│ Configuration:                                               │
│   Main class: com.cambiaso.ioc.IocbackendApplication        │
│   Active profiles: local                                     │
│                                                              │
│   Environment variables: [📁 Edit]  ← CLICK HERE            │
│   ┌──────────────────────────────────────────────────────┐  │
│   │ Name                        │ Value                  │  │
│   ├─────────────────────────────┼────────────────────────┤  │
│   │ METABASE_SECRET_KEY         │ 42cc18c4d202ab99...   │  │
│   │ SUPABASE_DB_PASSWORD        │ HANWVh91w8ZdZvFZ       │  │
│   │ SUPABASE_JWT_ISSUER_URI     │ https://bdyvzjpky...  │  │
│   │ SUPABASE_SERVICE_ROLE_KEY   │ eyJhbGciOiJIUzI1...   │  │
│   │ SUPABASE_URL                │ https://bdyvzjpky...  │  │
│   └──────────────────────────────────────────────────────┘  │
│                                                              │
│   [OK]  [Cancel]  [Apply]                                   │
└─────────────────────────────────────────────────────────────┘
```

## Verify Setup

After setting up the environment variables, you should see successful startup logs:

```
✅ HikariDataSource - SupabaseHikariPool-Local - Starting...
✅ HikariDataSource - SupabaseHikariPool-Local - Start completed.
✅ Hibernate: ...
✅ Started IocbackendApplication in X.XXX seconds
✅ Tomcat started on port 8080
```

## Troubleshooting

### ❌ Still getting connection errors?

1. **Double-check the values** - Make sure there are no extra spaces or line breaks
2. **Restart IntelliJ** - Sometimes IntelliJ needs a restart to pick up configuration changes
3. **Check Active Profile** - Make sure `local` profile is active in your run configuration

### ❌ Variables not being read?

1. **Verify in Run Configuration**: The environment variables should show in the "Environment variables" field
2. **Check for typos**: Variable names are case-sensitive
3. **Try Method 2**: Use the semicolon-separated format instead

### ❌ Database still unreachable?

Check network connectivity:
```bash
ping aws-1-sa-east-1.pooler.supabase.com
```

If ping fails, you might have network/firewall issues.

## Security Reminder

⚠️ **IMPORTANT**: 
- These credentials are **SENSITIVE** - do not share them publicly
- Do not commit them to Git
- The `SUPABASE_SERVICE_ROLE_KEY` has admin privileges - keep it secret
- Consider using IntelliJ's password manager or the EnvFile plugin for better security

## Next Steps

1. ✅ Set up environment variables in IntelliJ (follow steps above)
2. ✅ Run the application
3. ✅ Test the endpoints at http://localhost:8080
4. ⏳ Test the automatic user creation feature at `POST /api/admin/users`

## Quick Reference Card

Save this for quick access:

```
Profile: local
Port: 8080
Database: Supabase PostgreSQL (aws-1-sa-east-1)
Auth: Supabase Auth (JWT)
```

---

**Happy coding! 🚀**

If you encounter any issues, check the [CONNECTION_FIX.md](./CONNECTION_FIX.md) for detailed troubleshooting steps.

