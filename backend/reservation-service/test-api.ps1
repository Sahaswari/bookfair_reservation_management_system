# Reservation Service API Test Script
# This script tests all the reservation-service API endpoints

$baseUrl = "http://localhost:8086/api/reservations"

Write-Host "======================================" -ForegroundColor Cyan
Write-Host "Reservation Service API Test Script" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""

# Test 1: Get All Reservations
Write-Host "1. Testing GET All Reservations..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri $baseUrl -Method Get -ContentType "application/json"
    Write-Host "SUCCESS: Retrieved all reservations" -ForegroundColor Green
    Write-Host "Total reservations: $($response.data.Count)" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
}

# Test 2: Create a New Reservation
Write-Host "2. Testing POST Create Reservation (Book Stall)..." -ForegroundColor Yellow
$newReservation = @{
    userId = "550e8400-e29b-41d4-a716-446655440001"
    stallId = "550e8400-e29b-41d4-a716-446655440002"
    eventId = "550e8400-e29b-41d4-a716-446655440003"
    reservationDate = "2025-12-01"
    userFirstName = "John"
    userLastName = "Doe"
    userEmail = "john.doe@example.com"
    userRole = "VENDOR"
    userStatus = "ACTIVE"
    stallCode = "S001"
    sizeCategory = "MEDIUM"
    price = "1500.00"
    locationX = 10.5
    locationY = 20.3
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri $baseUrl -Method Post -Body $newReservation -ContentType "application/json"
    Write-Host "SUCCESS: Reservation created" -ForegroundColor Green
    Write-Host "Reservation ID: $($response.data.id)" -ForegroundColor Green
    Write-Host "Status: $($response.data.status)" -ForegroundColor Green
    Write-Host "Confirmation Code: $($response.data.confirmationCode)" -ForegroundColor Green
    $reservationId = $response.data.id
    Write-Host ""
} catch {
    Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
}

# Wait a moment
Start-Sleep -Seconds 1

# Test 3: Get Reservation by ID
if ($reservationId) {
    Write-Host "3. Testing GET Reservation by ID..." -ForegroundColor Yellow
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/$reservationId" -Method Get -ContentType "application/json"
        Write-Host "SUCCESS: Retrieved reservation" -ForegroundColor Green
        Write-Host "ID: $($response.data.id)" -ForegroundColor Green
        Write-Host "Status: $($response.data.status)" -ForegroundColor Green
        Write-Host ""
    } catch {
        Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host ""
    }
}

# Test 4: Update Reservation Status to CONFIRMED
if ($reservationId) {
    Write-Host "4. Testing PUT Update Status to CONFIRMED (Admin)..." -ForegroundColor Yellow
    $statusUpdate = @{
        status = "CONFIRMED"
    } | ConvertTo-Json

    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/$reservationId/status" -Method Put -Body $statusUpdate -ContentType "application/json"
        Write-Host "SUCCESS: Reservation status updated" -ForegroundColor Green
        Write-Host "New Status: $($response.data.status)" -ForegroundColor Green
        Write-Host "QR Code URL: $($response.data.qrCodeUrl)" -ForegroundColor Green
        Write-Host ""
    } catch {
        Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host ""
    }
}

# Test 5: Get Reservations by Status
Write-Host "5. Testing GET Reservations by Status (PENDING)..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/status/PENDING" -Method Get -ContentType "application/json"
    Write-Host "SUCCESS: Retrieved PENDING reservations" -ForegroundColor Green
    Write-Host "Count: $($response.data.Count)" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
}

# Test 6: Get Reservations by Status (CONFIRMED)
Write-Host "6. Testing GET Reservations by Status (CONFIRMED)..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/status/CONFIRMED" -Method Get -ContentType "application/json"
    Write-Host "SUCCESS: Retrieved CONFIRMED reservations" -ForegroundColor Green
    Write-Host "Count: $($response.data.Count)" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
}

# Test 7: Get Reservations by User
Write-Host "7. Testing GET Reservations by User..." -ForegroundColor Yellow
$testUserId = "550e8400-e29b-41d4-a716-446655440001"
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/user/$testUserId" -Method Get -ContentType "application/json"
    Write-Host "SUCCESS: Retrieved user reservations" -ForegroundColor Green
    Write-Host "Count: $($response.data.Count)" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
}

# Test 8: Get Reservations by Event
Write-Host "8. Testing GET Reservations by Event..." -ForegroundColor Yellow
$testEventId = "550e8400-e29b-41d4-a716-446655440003"
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/event/$testEventId" -Method Get -ContentType "application/json"
    Write-Host "SUCCESS: Retrieved event reservations" -ForegroundColor Green
    Write-Host "Count: $($response.data.Count)" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
}

# Test 9: Try to book already reserved stall (Should fail)
Write-Host "9. Testing Duplicate Reservation (Should Fail)..." -ForegroundColor Yellow
$duplicateReservation = @{
    userId = "550e8400-e29b-41d4-a716-446655440006"
    stallId = "550e8400-e29b-41d4-a716-446655440002"
    eventId = "550e8400-e29b-41d4-a716-446655440003"
    reservationDate = "2025-12-01"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri $baseUrl -Method Post -Body $duplicateReservation -ContentType "application/json"
    Write-Host "UNEXPECTED: Duplicate reservation was created (should have failed)" -ForegroundColor Red
    Write-Host ""
} catch {
    Write-Host "EXPECTED: Duplicate reservation prevented" -ForegroundColor Green
    Write-Host "Error Message: $($_.Exception.Message)" -ForegroundColor Yellow
    Write-Host ""
}

# Test 10: Update Status to CANCELLED
if ($reservationId) {
    Write-Host "10. Testing PUT Update Status to CANCELLED..." -ForegroundColor Yellow
    $cancelUpdate = @{
        status = "CANCELLED"
    } | ConvertTo-Json

    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/$reservationId/status" -Method Put -Body $cancelUpdate -ContentType "application/json"
        Write-Host "WARNING: Status updated (Note: Cannot update CONFIRMED to CANCELLED)" -ForegroundColor Yellow
        Write-Host "New Status: $($response.data.status)" -ForegroundColor Yellow
        Write-Host ""
    } catch {
        Write-Host "EXPECTED: Cannot update status of confirmed/cancelled reservation" -ForegroundColor Yellow
        Write-Host ""
    }
}

Write-Host "======================================" -ForegroundColor Cyan
Write-Host "API Testing Complete!" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
