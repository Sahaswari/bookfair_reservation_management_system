# Auth Service API Testing Guide

## Check if Service is Running

```powershell
# Check if port 8081 is listening
netstat -ano | findstr :8081

# Or check for Java process
Get-Process -Name java -ErrorAction SilentlyContinue
```

## API Endpoints

Base URL: `http://localhost:8081`

### 1. Register a New User

```powershell
$registerBody = @{
    username = "testuser"
    email = "testuser@example.com"
    password = "Test@1234"
    role = "CUSTOMER"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8081/api/auth/register" `
    -Method POST `
    -Body $registerBody `
    -ContentType "application/json"
```

### 2. Login

```powershell
$loginBody = @{
    username = "testuser"
    password = "Test@1234"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8081/api/auth/login" `
    -Method POST `
    -Body $loginBody `
    -ContentType "application/json"

# Save the token
$token = $response.token
Write-Host "Token: $token" -ForegroundColor Green
```

### 3. Get Current User Info (requires authentication)

```powershell
$headers = @{
    "Authorization" = "Bearer $token"
}

Invoke-RestMethod -Uri "http://localhost:8081/api/users/me" `
    -Method GET `
    -Headers $headers
```

### 4. Refresh Token

```powershell
$headers = @{
    "Authorization" = "Bearer $token"
}

$refreshResponse = Invoke-RestMethod -Uri "http://localhost:8081/api/auth/refresh" `
    -Method POST `
    -Headers $headers

$token = $refreshResponse.token
Write-Host "New Token: $token" -ForegroundColor Green
```

### 5. Logout

```powershell
$headers = @{
    "Authorization" = "Bearer $token"
}

Invoke-RestMethod -Uri "http://localhost:8081/api/auth/logout" `
    -Method POST `
    -Headers $headers
```

## Quick Test Script

Run this after the service starts:

```powershell
# 1. Register
$registerBody = @{
    username = "demo_$(Get-Random -Maximum 9999)"
    email = "demo_$(Get-Random -Maximum 9999)@example.com"
    password = "Demo@1234"
    role = "CUSTOMER"
} | ConvertTo-Json

Write-Host "`n=== Registering User ===" -ForegroundColor Cyan
$registerResponse = Invoke-RestMethod -Uri "http://localhost:8081/api/auth/register" `
    -Method POST -Body $registerBody -ContentType "application/json"
Write-Host "Registration successful!" -ForegroundColor Green
Write-Host "User ID: $($registerResponse.id)" -ForegroundColor Yellow

# 2. Login
$loginBody = @{
    username = ($registerBody | ConvertFrom-Json).username
    password = "Demo@1234"
} | ConvertTo-Json

Write-Host "`n=== Logging In ===" -ForegroundColor Cyan
$loginResponse = Invoke-RestMethod -Uri "http://localhost:8081/api/auth/login" `
    -Method POST -Body $loginBody -ContentType "application/json"
$token = $loginResponse.token
Write-Host "Login successful!" -ForegroundColor Green
Write-Host "Token: $($token.Substring(0,50))..." -ForegroundColor Yellow

# 3. Get user info
Write-Host "`n=== Getting User Info ===" -ForegroundColor Cyan
$headers = @{ "Authorization" = "Bearer $token" }
$userInfo = Invoke-RestMethod -Uri "http://localhost:8081/api/users/me" `
    -Method GET -Headers $headers
Write-Host "User Info Retrieved!" -ForegroundColor Green
Write-Host "Username: $($userInfo.username)" -ForegroundColor Yellow
Write-Host "Email: $($userInfo.email)" -ForegroundColor Yellow
Write-Host "Role: $($userInfo.role)" -ForegroundColor Yellow

Write-Host "`n=== All Tests Passed! ===" -ForegroundColor Green
```

## Swagger UI

Once the service is running, you can also access the interactive API documentation at:

**http://localhost:8081/swagger-ui.html**

This provides a web interface to test all API endpoints.

## Database Creation

Before testing, ensure the database exists:

```powershell
$env:PGPASSWORD="1234"
& "C:\Program Files\PostgreSQL\16\bin\psql.exe" -U postgres -c "CREATE DATABASE IF NOT EXISTS auth_service_db;"
```

Or run the SQL script:

```powershell
$env:PGPASSWORD="1234"
& "C:\Program Files\PostgreSQL\16\bin\psql.exe" -U postgres -f create_databases.sql
```

## Troubleshooting

### Service not starting?
- Check logs in the terminal where you ran `mvnw.cmd spring-boot:run`
- Look for error messages about database connection
- Ensure PostgreSQL is running: `Get-Service postgresql*`

### Database connection failed?
- Verify PostgreSQL is running
- Check password is "1234" in `application.properties`
- Ensure database `auth_service_db` exists

### Port 8081 already in use?
- Check what's using the port: `netstat -ano | findstr :8081`
- Kill the process or change the port in `application.properties`
