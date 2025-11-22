# Bookfair Docker Management Script
# This script helps manage Docker containers for the Bookfair system

param(
    [Parameter(Mandatory=$false)]
    [ValidateSet('up', 'down', 'restart', 'logs', 'build', 'clean', 'backend-only', 'status')]
    [string]$Command = 'status'
)

function Show-Banner {
    Write-Host "===============================================" -ForegroundColor Cyan
    Write-Host "   Bookfair Reservation Management System" -ForegroundColor Cyan
    Write-Host "   Docker Management Tool" -ForegroundColor Cyan
    Write-Host "===============================================" -ForegroundColor Cyan
    Write-Host ""
}

function Start-AllServices {
    Write-Host "Starting all services..." -ForegroundColor Green
    docker-compose up -d --build
    Write-Host "All services started successfully!" -ForegroundColor Green
    Show-ServiceStatus
}

function Stop-AllServices {
    Write-Host "Stopping all services..." -ForegroundColor Yellow
    docker-compose down
    Write-Host "All services stopped successfully!" -ForegroundColor Green
}

function Restart-AllServices {
    Write-Host "Restarting all services..." -ForegroundColor Yellow
    docker-compose restart
    Write-Host "All services restarted successfully!" -ForegroundColor Green
}

function Show-Logs {
    Write-Host "Showing logs (Press Ctrl+C to exit)..." -ForegroundColor Cyan
    docker-compose logs -f
}

function Build-AllServices {
    Write-Host "Building all services..." -ForegroundColor Green
    docker-compose build --no-cache
    Write-Host "All services built successfully!" -ForegroundColor Green
}

function Clear-Docker {
    Write-Host "Cleaning up Docker resources..." -ForegroundColor Yellow
    $response = Read-Host "This will remove all containers, images, and volumes. Continue? (y/n)"
    if ($response -eq 'y') {
        docker-compose down -v
        docker system prune -a -f
        Write-Host "Docker cleanup completed!" -ForegroundColor Green
    } else {
        Write-Host "Cleanup cancelled." -ForegroundColor Yellow
    }
}

function Start-BackendOnly {
    Write-Host "Starting backend services only..." -ForegroundColor Green
    docker-compose -f docker-compose.backend.yml up -d --build
    Write-Host "Backend services started successfully!" -ForegroundColor Green
    Show-ServiceStatus
}

function Show-ServiceStatus {
    Write-Host ""
    Write-Host "Service Status:" -ForegroundColor Cyan
    Write-Host "===============================================" -ForegroundColor Cyan
    docker-compose ps
    Write-Host ""
    Write-Host "Access URLs:" -ForegroundColor Cyan
    Write-Host "  API Gateway:         http://localhost:8080" -ForegroundColor White
    Write-Host "  Auth Service:        http://localhost:8081" -ForegroundColor White
    Write-Host "  Genre Service:       http://localhost:8082" -ForegroundColor White
    Write-Host "  Notification Service: http://localhost:8083" -ForegroundColor White
    Write-Host "  Reservation Service: http://localhost:8084" -ForegroundColor White
    Write-Host "  Stall Service:       http://localhost:8085" -ForegroundColor White
    Write-Host "  Employee Portal:     http://localhost:3000" -ForegroundColor White
    Write-Host "  User Portal:         http://localhost:3001" -ForegroundColor White
    Write-Host ""
}

# Main script execution
Show-Banner

switch ($Command) {
    'up' {
        Start-AllServices
    }
    'down' {
        Stop-AllServices
    }
    'restart' {
        Restart-AllServices
    }
    'logs' {
        Show-Logs
    }
    'build' {
        Build-AllServices
    }
    'clean' {
        Clear-Docker
    }
    'backend-only' {
        Start-BackendOnly
    }
    'status' {
        Show-ServiceStatus
    }
    default {
        Write-Host "Usage: .\docker-manage.ps1 [command]" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "Available commands:" -ForegroundColor Cyan
        Write-Host "  up            - Start all services" -ForegroundColor White
        Write-Host "  down          - Stop all services" -ForegroundColor White
        Write-Host "  restart       - Restart all services" -ForegroundColor White
        Write-Host "  logs          - View logs" -ForegroundColor White
        Write-Host "  build         - Rebuild all services" -ForegroundColor White
        Write-Host "  clean         - Clean up Docker resources" -ForegroundColor White
        Write-Host "  backend-only  - Start only backend services" -ForegroundColor White
        Write-Host "  status        - Show service status" -ForegroundColor White
        Write-Host ""
    }
}
