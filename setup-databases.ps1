# PostgreSQL Database Setup Script
# Run this after PostgreSQL installation completes

Write-Host "=== PostgreSQL Database Setup ===" -ForegroundColor Green

# Check if PostgreSQL is installed
$postgresPath = "C:\Program Files\PostgreSQL\16\bin\psql.exe"
if (-not (Test-Path $postgresPath)) {
    Write-Host "ERROR: PostgreSQL not found at $postgresPath" -ForegroundColor Red
    Write-Host "Please complete PostgreSQL installation first" -ForegroundColor Yellow
    exit 1
}

Write-Host "PostgreSQL found!" -ForegroundColor Green
Write-Host ""

# Prompt for postgres password
$postgresPassword = Read-Host "Enter the postgres user password (set during installation)" -AsSecureString
$BSTR = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($postgresPassword)
$password = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($BSTR)

# Create SQL commands
$sqlCommands = @"
-- Create databases
CREATE DATABASE auth_service_db;
CREATE DATABASE stall_service_db;
CREATE DATABASE reservation_service_db;
CREATE DATABASE genre_service_db;
CREATE DATABASE notification_service_db;

-- Grant permissions to postgres user
GRANT ALL PRIVILEGES ON DATABASE auth_service_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE stall_service_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE reservation_service_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE genre_service_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE notification_service_db TO postgres;
"@

# Save SQL to temp file
$tempSqlFile = "$env:TEMP\setup_databases.sql"
$sqlCommands | Out-File -FilePath $tempSqlFile -Encoding UTF8

Write-Host "Creating databases..." -ForegroundColor Yellow

# Set environment variable for password
$env:PGPASSWORD = $password

# Execute SQL file
& $postgresPath -U postgres -f $tempSqlFile

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "=== Databases created successfully! ===" -ForegroundColor Green
    Write-Host ""
    Write-Host "Created databases:" -ForegroundColor Cyan
    Write-Host "  - auth_service_db" -ForegroundColor White
    Write-Host "  - stall_service_db" -ForegroundColor White
    Write-Host "  - reservation_service_db" -ForegroundColor White
    Write-Host "  - genre_service_db" -ForegroundColor White
    Write-Host "  - notification_service_db" -ForegroundColor White
    Write-Host ""
    Write-Host "You can now start the backend services!" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "ERROR: Failed to create databases" -ForegroundColor Red
    Write-Host "Please check the error messages above" -ForegroundColor Yellow
}

# Clean up
Remove-Item $tempSqlFile -ErrorAction SilentlyContinue
$env:PGPASSWORD = $null
