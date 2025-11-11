# Stall Service API Test Script
# Run this in PowerShell to test all endpoints

$baseUrl = "http://localhost:8085"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Stall Service API Test Script" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Test 1: Create an Event
Write-Host "1. Creating a new event..." -ForegroundColor Yellow
$createEventBody = @{
    year = 2025
    name = "Colombo International Book Fair 2025"
    startDate = "2025-09-20"
    endDate = "2025-09-30"
    location = "BMICH, Colombo"
    status = "UPCOMING"
} | ConvertTo-Json

$eventResponse = Invoke-RestMethod -Uri "$baseUrl/api/events" -Method Post -Body $createEventBody -ContentType "application/json"
$eventId = $eventResponse.data.id
Write-Host "✓ Event created with ID: $eventId" -ForegroundColor Green
Write-Host ""

# Test 2: Get all events
Write-Host "2. Fetching all events..." -ForegroundColor Yellow
$allEvents = Invoke-RestMethod -Uri "$baseUrl/api/events" -Method Get
Write-Host "✓ Found $($allEvents.data.Count) event(s)" -ForegroundColor Green
Write-Host ""

# Test 3: Create multiple stalls
Write-Host "3. Creating stalls..." -ForegroundColor Yellow

$stallCodes = @("A001", "A002", "A003", "B001", "B002")
$sizes = @("SMALL", "MEDIUM", "LARGE", "MEDIUM", "SMALL")
$prices = @(3000, 5000, 8000, 5000, 3000)

$stallIds = @()

for ($i = 0; $i -lt $stallCodes.Count; $i++) {
    $createStallBody = @{
        eventId = $eventId
        stallCode = $stallCodes[$i]
        sizeCategory = $sizes[$i]
        price = $prices[$i]
        locationX = (Get-Random -Minimum 0 -Maximum 100)
        locationY = (Get-Random -Minimum 0 -Maximum 100)
    } | ConvertTo-Json

    $stallResponse = Invoke-RestMethod -Uri "$baseUrl/api/stalls" -Method Post -Body $createStallBody -ContentType "application/json"
    $stallIds += $stallResponse.data.id
    Write-Host "  ✓ Created stall $($stallCodes[$i]) - $($sizes[$i]) - $$($prices[$i])" -ForegroundColor Green
}
Write-Host ""

# Test 4: Get all stalls for the event
Write-Host "4. Fetching all stalls for the event..." -ForegroundColor Yellow
$eventStalls = Invoke-RestMethod -Uri "$baseUrl/api/stalls/event/$eventId" -Method Get
Write-Host "✓ Found $($eventStalls.data.Count) stall(s) for the event" -ForegroundColor Green
Write-Host ""

# Test 5: Get available stalls
Write-Host "5. Fetching available stalls..." -ForegroundColor Yellow
$availableStalls = Invoke-RestMethod -Uri "$baseUrl/api/stalls/event/$eventId/available" -Method Get
Write-Host "✓ Found $($availableStalls.data.Count) available stall(s)" -ForegroundColor Green
Write-Host ""

# Test 6: Get available medium-sized stalls
Write-Host "6. Fetching available MEDIUM stalls..." -ForegroundColor Yellow
$mediumStalls = Invoke-RestMethod -Uri "$baseUrl/api/stalls/event/$eventId/available/size/MEDIUM" -Method Get
Write-Host "✓ Found $($mediumStalls.data.Count) available MEDIUM stall(s)" -ForegroundColor Green
Write-Host ""

# Test 7: Reserve a stall
Write-Host "7. Reserving a stall..." -ForegroundColor Yellow
$vendorId = [guid]::NewGuid()
$stallToReserve = $stallIds[0]
$reserveResponse = Invoke-RestMethod -Uri "$baseUrl/api/stalls/$stallToReserve/reserve?vendorId=$vendorId" -Method Post
Write-Host "✓ Stall reserved for vendor: $vendorId" -ForegroundColor Green
Write-Host ""

# Test 8: Get available stalls again (should be one less)
Write-Host "8. Fetching available stalls again..." -ForegroundColor Yellow
$availableStallsAfter = Invoke-RestMethod -Uri "$baseUrl/api/stalls/event/$eventId/available" -Method Get
Write-Host "✓ Now $($availableStallsAfter.data.Count) available stall(s)" -ForegroundColor Green
Write-Host ""

# Test 9: Get stalls by vendor
Write-Host "9. Fetching stalls for vendor..." -ForegroundColor Yellow
$vendorStalls = Invoke-RestMethod -Uri "$baseUrl/api/stalls/vendor/$vendorId" -Method Get
Write-Host "✓ Vendor has $($vendorStalls.data.Count) reserved stall(s)" -ForegroundColor Green
Write-Host ""

# Test 10: Update event status
Write-Host "10. Updating event status to ONGOING..." -ForegroundColor Yellow
$updateStatusResponse = Invoke-RestMethod -Uri "$baseUrl/api/events/$eventId/status?status=ONGOING" -Method Patch
Write-Host "✓ Event status updated to: $($updateStatusResponse.data.status)" -ForegroundColor Green
Write-Host ""

# Test 11: Get events by status
Write-Host "11. Fetching ONGOING events..." -ForegroundColor Yellow
$ongoingEvents = Invoke-RestMethod -Uri "$baseUrl/api/events/status/ONGOING" -Method Get
Write-Host "✓ Found $($ongoingEvents.data.Count) ONGOING event(s)" -ForegroundColor Green
Write-Host ""

# Test 12: Unreserve the stall
Write-Host "12. Unreserving the stall..." -ForegroundColor Yellow
$unreserveResponse = Invoke-RestMethod -Uri "$baseUrl/api/stalls/$stallToReserve/unreserve" -Method Post
Write-Host "✓ Stall unreserved successfully" -ForegroundColor Green
Write-Host ""

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "All tests completed successfully! ✓" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Summary:" -ForegroundColor Cyan
Write-Host "  Event ID: $eventId" -ForegroundColor White
Write-Host "  Total Stalls Created: $($stallIds.Count)" -ForegroundColor White
Write-Host "  Available Stalls: $($availableStallsAfter.data.Count)" -ForegroundColor White
Write-Host ""
Write-Host "Service is running at: $baseUrl" -ForegroundColor White
Write-Host ""
