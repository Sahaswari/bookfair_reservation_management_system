# Start Backend Services Script
# Run this after databases are set up

Write-Host "=== Starting Backend Services ===" -ForegroundColor Green
Write-Host ""

$services = @(
    @{Name="Auth Service"; Port=8081; Path="backend\auth-service"},
    @{Name="Stall Service"; Port=8082; Path="backend\stall-service"},
    @{Name="Reservation Service"; Port=8083; Path="backend\reservation-service"}
)

Write-Host "This will start the following services:" -ForegroundColor Cyan
foreach ($service in $services) {
    Write-Host "  - $($service.Name) on port $($service.Port)" -ForegroundColor White
}
Write-Host ""

$response = Read-Host "Do you want to start all services? (y/n)"
if ($response -ne 'y') {
    Write-Host "Cancelled" -ForegroundColor Yellow
    exit 0
}

Write-Host ""
Write-Host "Starting services..." -ForegroundColor Yellow
Write-Host "Note: Each service will open in a new PowerShell window" -ForegroundColor Cyan
Write-Host ""

$projectRoot = $PSScriptRoot

foreach ($service in $services) {
    $servicePath = Join-Path $projectRoot $service.Path
    
    Write-Host "Starting $($service.Name)..." -ForegroundColor Green
    
    # Start each service in a new window with local profile
    Start-Process powershell -ArgumentList @(
        "-NoExit",
        "-Command",
        "cd '$servicePath'; Write-Host 'Starting $($service.Name)...' -ForegroundColor Green; .\mvnw.cmd clean spring-boot:run -Dspring-boot.run.profiles=local"
    )
    
    Start-Sleep -Seconds 2
}

Write-Host ""
Write-Host "=== All services started! ===" -ForegroundColor Green
Write-Host ""
Write-Host "Services are running on:" -ForegroundColor Cyan
Write-Host "  - Auth Service: http://localhost:8081" -ForegroundColor White
Write-Host "  - Stall Service: http://localhost:8082" -ForegroundColor White
Write-Host "  - Reservation Service: http://localhost:8083" -ForegroundColor White
Write-Host ""
Write-Host "To stop a service, close its PowerShell window" -ForegroundColor Yellow
Write-Host "First-time startup may take 2-3 minutes to download dependencies" -ForegroundColor Yellow
