# Quick test script for Auth Service API
# Run this after the service starts on http://localhost:8081

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Auth Service API Quick Test" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Check if service is running
Write-Host "`nChecking if service is running on port 8081..." -ForegroundColor Yellow
$portCheck = netstat -ano | findstr ":8081"
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Service is not running on port 8081!" -ForegroundColor Red
    Write-Host "Please ensure auth-service has started successfully." -ForegroundColor Red
    exit 1
}
Write-Host "✓ Service is running!" -ForegroundColor Green

try {
    # Generate random user data
    $randomNum = Get-Random -Maximum 9999
    $username = "demo_$randomNum"
    $email = "demo_$randomNum@example.com"
    
    # 1. Register
    $registerBody = @{
        username = $username
        email = $email
        password = "Demo@1234"
        role = "CUSTOMER"
    } | ConvertTo-Json

    Write-Host "`n=== 1. Registering User ===" -ForegroundColor Cyan
    Write-Host "Username: $username" -ForegroundColor Gray
    Write-Host "Email: $email" -ForegroundColor Gray
    
    $registerResponse = Invoke-RestMethod -Uri "http://localhost:8081/api/auth/register" `
        -Method POST -Body $registerBody -ContentType "application/json"
    
    Write-Host "✓ Registration successful!" -ForegroundColor Green
    Write-Host "  User ID: $($registerResponse.id)" -ForegroundColor Yellow

    # 2. Login
    $loginBody = @{
        username = $username
        password = "Demo@1234"
    } | ConvertTo-Json

    Write-Host "`n=== 2. Logging In ===" -ForegroundColor Cyan
    
    $loginResponse = Invoke-RestMethod -Uri "http://localhost:8081/api/auth/login" `
        -Method POST -Body $loginBody -ContentType "application/json"
    
    $token = $loginResponse.token
    Write-Host "✓ Login successful!" -ForegroundColor Green
    Write-Host "  Token (first 50 chars): $($token.Substring(0, [Math]::Min(50, $token.Length)))..." -ForegroundColor Yellow

    # 3. Get user info
    Write-Host "`n=== 3. Getting User Info ===" -ForegroundColor Cyan
    
    $headers = @{ "Authorization" = "Bearer $token" }
    $userInfo = Invoke-RestMethod -Uri "http://localhost:8081/api/users/me" `
        -Method GET -Headers $headers
    
    Write-Host "✓ User Info Retrieved!" -ForegroundColor Green
    Write-Host "  Username: $($userInfo.username)" -ForegroundColor Yellow
    Write-Host "  Email: $($userInfo.email)" -ForegroundColor Yellow
    Write-Host "  Role: $($userInfo.role)" -ForegroundColor Yellow

    # 4. Refresh token
    Write-Host "`n=== 4. Refreshing Token ===" -ForegroundColor Cyan
    
    $refreshResponse = Invoke-RestMethod -Uri "http://localhost:8081/api/auth/refresh" `
        -Method POST -Headers $headers
    
    $newToken = $refreshResponse.token
    Write-Host "✓ Token refreshed!" -ForegroundColor Green

    # 5. Logout
    Write-Host "`n=== 5. Logging Out ===" -ForegroundColor Cyan
    
    Invoke-RestMethod -Uri "http://localhost:8081/api/auth/logout" `
        -Method POST -Headers $headers
    
    Write-Host "✓ Logout successful!" -ForegroundColor Green

    Write-Host "`n========================================" -ForegroundColor Cyan
    Write-Host "  ✓ ALL TESTS PASSED!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Cyan
    
    Write-Host "`nYou can now integrate these APIs with your frontend!" -ForegroundColor Yellow
    Write-Host "API Documentation: http://localhost:8081/swagger-ui.html" -ForegroundColor Cyan
    
} catch {
    Write-Host "`n✗ ERROR:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "Response: $responseBody" -ForegroundColor Red
    }
    
    exit 1
}
