# Local Backend Setup Guide

## Prerequisites Installed
✅ Java 21 (compatible with Java 17 requirement)
✅ Maven Wrapper (included in project)
⏳ PostgreSQL 16 (installing now)

## Step 1: Complete PostgreSQL Installation

When the PostgreSQL installer window appears:
1. **Password**: Set a password for the postgres user (remember this!)
2. **Port**: Keep default 5432
3. **Locale**: Keep default
4. Click through to complete installation

## Step 2: Create Databases

After PostgreSQL installation completes, open PowerShell and run:

```powershell
# Add PostgreSQL to PATH (replace with your actual installation path if different)
$env:PATH += ";C:\Program Files\PostgreSQL\16\bin"

# Login to PostgreSQL (enter the password you set during installation)
psql -U postgres

# In the PostgreSQL prompt, run these commands:
CREATE DATABASE auth_service_db;
CREATE USER auth_service_user WITH PASSWORD 'auth_service_pass';
GRANT ALL PRIVILEGES ON DATABASE auth_service_db TO auth_service_user;

CREATE DATABASE stall_service_db;
CREATE USER stall_service_user WITH PASSWORD 'stall_service_pass';
GRANT ALL PRIVILEGES ON DATABASE stall_service_db TO stall_service_user;

CREATE DATABASE reservation_service_db;
CREATE USER reservation_service_user WITH PASSWORD 'reservation_service_pass';
GRANT ALL PRIVILEGES ON DATABASE reservation_service_db TO reservation_service_user;

CREATE DATABASE genre_service_db;
CREATE USER genre_service_user WITH PASSWORD 'genre_service_pass';
GRANT ALL PRIVILEGES ON DATABASE genre_service_db TO genre_service_user;

CREATE DATABASE notification_service_db;
CREATE USER notification_service_user WITH PASSWORD 'notification_service_pass';
GRANT ALL PRIVILEGES ON DATABASE notification_service_db TO notification_service_user;

# Exit PostgreSQL
\q
```

## Step 3: Run Auth Service (Primary service for login/registration)

```powershell
cd C:\Users\DELL\Downloads\bookfair_reservation_management_system-main\backend\auth-service
.\mvnw.cmd clean install -DskipTests
.\mvnw.cmd spring-boot:run
```

The auth-service will start on **http://localhost:8081**

API Endpoints:
- POST http://localhost:8081/api/auth/register - Register new user
- POST http://localhost:8081/api/auth/login - Login
- POST http://localhost:8081/api/auth/logout - Logout
- POST http://localhost:8081/api/auth/refresh - Refresh token
- GET http://localhost:8081/api/users/me - Get current user

## Step 4: Run Stall Service (Optional - for stall management)

Open a NEW PowerShell window:

```powershell
cd C:\Users\DELL\Downloads\bookfair_reservation_management_system-main\backend\stall-service
.\mvnw.cmd clean install -DskipTests
.\mvnw.cmd spring-boot:run
```

The stall-service will start on **http://localhost:8082**

## Step 5: Run Reservation Service (Optional - for reservations)

Open a NEW PowerShell window:

```powershell
cd C:\Users\DELL\Downloads\bookfair_reservation_management_system-main\backend\reservation-service
.\mvnw.cmd clean install -DskipTests
.\mvnw.cmd spring-boot:run
```

The reservation-service will start on **http://localhost:8083**

## Step 6: Run API Gateway (Optional - routes requests to services)

Open a NEW PowerShell window:

```powershell
cd C:\Users\DELL\Downloads\bookfair_reservation_management_system-main\backend\api-gateway
.\mvnw.cmd clean install -DskipTests
.\mvnw.cmd spring-boot:run
```

The api-gateway will start on **http://localhost:8080**

## Service Ports Summary

- API Gateway: http://localhost:8080 (routes to all services)
- Auth Service: http://localhost:8081
- Stall Service: http://localhost:8082
- Reservation Service: http://localhost:8083
- Genre Service: http://localhost:8084 (if needed)
- Notification Service: http://localhost:8085 (if needed)

## For Frontend Integration

Your frontend should connect to:
- **With API Gateway**: http://localhost:8080/api/auth/...
- **Direct to Auth Service**: http://localhost:8081/api/auth/...

Recommended: Use API Gateway (port 8080) as it provides centralized routing.

## Troubleshooting

### If Maven build fails:
```powershell
# Clean Maven cache
.\mvnw.cmd clean

# Rebuild
.\mvnw.cmd clean install -DskipTests
```

### If database connection fails:
1. Check PostgreSQL is running:
   ```powershell
   Get-Service -Name postgresql*
   ```
2. Verify databases exist:
   ```powershell
   psql -U postgres -c "\l"
   ```

### If port is already in use:
Find and kill the process:
```powershell
netstat -ano | findstr :8081
taskkill /PID <PID_NUMBER> /F
```

## Next Steps

1. Wait for PostgreSQL installation to complete
2. Create the databases (Step 2)
3. Start auth-service (Step 3)
4. Update your frontend .env.local to point to http://localhost:8081 or http://localhost:8080
5. Test the API endpoints
