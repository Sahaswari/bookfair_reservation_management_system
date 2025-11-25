# Reservation Service API Testing Documentation

## Overview
This document provides complete step-by-step instructions for testing all reservation-service APIs using Postman.

**Service Details:**
- Base URL: `http://localhost:8084`
- Database: `reservation_db` (PostgreSQL)
- Port: `8084`

---

## Table of Contents
1. [Setup Instructions](#setup-instructions)
2. [API Endpoints](#api-endpoints)
3. [Testing Sequence](#testing-sequence)
4. [Test Cases](#test-cases)
5. [Expected Responses](#expected-responses)
6. [Troubleshooting](#troubleshooting)

---

## Setup Instructions

### Prerequisites
- Docker and Docker Compose installed
- Postman installed
- Reservation-service running on port 8084
- PostgreSQL database `reservation_db` initialized

### Starting the Service

**Using docker-compose.backend.yml (recommended):**
```bash
docker-compose -f docker-compose.backend.yml up -d
```

**Verify service is running:**
```bash
docker ps | findstr "8084"
```

Expected output:
```
0.0.0.0:8084->8084/tcp    bookfair-reservation-service
```

### Postman Setup

1. Open Postman
2. Create new Collection: **"Reservation Service"**
3. Create Environment:
   - Name: **"Reservation Service Local"**
   - Variable: 
     - Key: `base_url`
     - Value: `http://localhost:8084`
4. Select environment (top right)

---

## API Endpoints

### Endpoint Summary

| # | Method | Endpoint | Purpose | Status Code |
|---|--------|----------|---------|-------------|
| 1 | GET | `/api/reservations` | Get all reservations | 200 |
| 2 | POST | `/api/reservations` | Create new reservation | 201 |
| 3 | GET | `/api/reservations/{id}` | Get by ID | 200 |
| 4 | PUT | `/api/reservations/{id}/status` | Update status | 200 |
| 5 | GET | `/api/reservations/status/{status}` | Filter by status | 200 |
| 6 | GET | `/api/reservations/user/{userId}` | Get by user | 200 |
| 7 | GET | `/api/reservations/event/{eventId}` | Get by event | 200 |
| 8 | DELETE | `/api/reservations/{id}` | Delete reservation | 200 |

---

## Testing Sequence

Follow this sequence for proper testing:

1. **Test 1:** GET all reservations (empty list)
2. **Test 2:** POST create first reservation
3. **Test 3:** GET all reservations (verify count = 1)
4. **Test 4:** GET reservation by ID
5. **Test 5:** PUT update status to CONFIRMED
6. **Test 6:** GET reservations by status
7. **Test 7:** GET reservations by user
8. **Test 8:** GET reservations by event
9. **Test 9:** POST create second reservation
10. **Test 10:** DELETE second reservation
11. **Test 11:** GET all reservations (verify count = 1, second deleted)

---

## Test Cases

### TEST 1: Get All Reservations (Empty)

**Request:**
```
Method: GET
URL: {{base_url}}/api/reservations
Headers: None
Body: None
```

**Postman Steps:**
1. Click **"+"** → New Request
2. Name: **"1. Get All Reservations"**
3. Method: **GET**
4. URL: `{{base_url}}/api/reservations`
5. Click **Send**

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Reservations fetched successfully",
  "data": []
}
```

**Verification:**
- Status: **200 OK** ✅
- Response contains empty array ✅

---

### TEST 2: Create First Reservation

**Request:**
```
Method: POST
URL: {{base_url}}/api/reservations
Headers: Content-Type: application/json
```

**Postman Steps:**
1. Click **"+"** → New Request
2. Name: **"2. Create Reservation #1"**
3. Method: **POST**
4. URL: `{{base_url}}/api/reservations`
5. **Headers Tab:**
   - Add: `Content-Type: application/json`
6. **Body Tab:**
   - Select **raw** → **JSON**
   - Paste JSON below

**Body - JSON:**
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "stallId": "660e8400-e29b-41d4-a716-446655440001",
  "eventId": "770e8400-e29b-41d4-a716-446655440002"
}
```

> Only identifiers are required. The reservation date and all user/stall metadata come from existing snapshot tables synchronized via Kafka.

7. Click **Send**

**Expected Response (201 Created):**
```json
{
  "success": true,
  "message": "Reservation created successfully with PENDING status",
  "data": {
    "id": "abc12345-1234-1234-1234-abc123456789",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "stallId": "660e8400-e29b-41d4-a716-446655440001",
    "eventId": "770e8400-e29b-41d4-a716-446655440002",
    "reservationDate": "2025-12-15",
    "status": "PENDING",
    "confirmationCode": "RES-XXXXXXXX",
    "userFirstName": "John",
    "userLastName": "Doe",
    "userEmail": "john@example.com",
    "stallCode": "STALL-001",
    "sizeCategory": "MEDIUM",
    "price": 5000,
    "createdAt": "2025-11-24T10:00:00Z",
    "updatedAt": "2025-11-24T10:00:00Z"
  }
}
```

**Verification:**
- Status: **201 Created** ✅
- Response includes reservation ID ✅
- Status is **PENDING** ✅
- Confirmation code generated ✅

**⚠️ IMPORTANT:**
**Copy the `id` value** for use in next tests.
Example: `abc12345-1234-1234-1234-abc123456789`

---

### TEST 3: Get All Reservations (After Create)

**Request:**
```
Method: GET
URL: {{base_url}}/api/reservations
```

**Postman Steps:**
1. Click **"+"** → New Request
2. Name: **"3. Get All Reservations (After Create)"**
3. Method: **GET**
4. URL: `{{base_url}}/api/reservations`
5. Click **Send**

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Reservations fetched successfully",
  "data": [
    {
      "id": "abc12345-1234-1234-1234-abc123456789",
      "userId": "550e8400-e29b-41d4-a716-446655440000",
      "stallId": "660e8400-e29b-41d4-a716-446655440001",
      "eventId": "770e8400-e29b-41d4-a716-446655440002",
      "status": "PENDING",
      "confirmationCode": "RES-XXXXXXXX"
    }
  ]
}
```

**Verification:**
- Status: **200 OK** ✅
- Data array length = 1 ✅
- Contains the created reservation ✅

---

### TEST 4: Get Reservation by ID

**Request:**
```
Method: GET
URL: {{base_url}}/api/reservations/{id}
(Replace {id} with actual ID from TEST 2)
```

**Postman Steps:**
1. Click **"+"** → New Request
2. Name: **"4. Get Reservation by ID"**
3. Method: **GET**
4. URL: `{{base_url}}/api/reservations/abc12345-1234-1234-1234-abc123456789`
   - **Replace ID with your actual ID from TEST 2**
5. Click **Send**

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Reservation fetched successfully",
  "data": {
    "id": "abc12345-1234-1234-1234-abc123456789",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "stallId": "660e8400-e29b-41d4-a716-446655440001",
    "eventId": "770e8400-e29b-41d4-a716-446655440002",
    "reservationDate": "2025-12-15",
    "status": "PENDING",
    "confirmationCode": "RES-XXXXXXXX",
    "userFirstName": "John",
    "userLastName": "Doe",
    "userEmail": "john@example.com",
    "stallCode": "STALL-001",
    "sizeCategory": "MEDIUM",
    "price": 5000,
    "createdAt": "2025-11-24T10:00:00Z",
    "updatedAt": "2025-11-24T10:00:00Z"
  }
}
```

**Verification:**
- Status: **200 OK** ✅
- Returns correct reservation ✅
- All fields populated ✅

---

### TEST 5: Update Reservation Status (PENDING → CONFIRMED)

**Request:**
```
Method: PUT
URL: {{base_url}}/api/reservations/{id}/status
(Replace {id} with actual ID from TEST 2)
Headers: Content-Type: application/json
```

**Postman Steps:**
1. Click **"+"** → New Request
2. Name: **"5. Update Status to CONFIRMED"**
3. Method: **PUT**
4. URL: `{{base_url}}/api/reservations/abc12345-1234-1234-1234-abc123456789/status`
   - **Replace ID with your actual ID from TEST 2**
5. **Headers Tab:**
   - Add: `Content-Type: application/json`
6. **Body Tab:**
   - Select **raw** → **JSON**
   - Paste:
```json
{
  "status": "CONFIRMED"
}
```
7. Click **Send**

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Reservation status updated successfully",
  "data": {
    "id": "abc12345-1234-1234-1234-abc123456789",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "stallId": "660e8400-e29b-41d4-a716-446655440001",
    "status": "CONFIRMED",
    "confirmationCode": "RES-XXXXXXXX",
    "qrCodeUrl": "/api/reservations/abc12345-1234-1234-1234-abc123456789/qrcode",
    "updatedAt": "2025-11-24T10:05:00Z"
  }
}
```

**Verification:**
- Status: **200 OK** ✅
- Status changed to **CONFIRMED** ✅
- QR Code URL generated ✅
- Updated timestamp changed ✅

---

### TEST 6: Get Reservations by Status (CONFIRMED)

**Request:**
```
Method: GET
URL: {{base_url}}/api/reservations/status/CONFIRMED
```

**Postman Steps:**
1. Click **"+"** → New Request
2. Name: **"6. Get Reservations by Status - CONFIRMED"**
3. Method: **GET**
4. URL: `{{base_url}}/api/reservations/status/CONFIRMED`
5. Click **Send**

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Reservations fetched successfully",
  "data": [
    {
      "id": "abc12345-1234-1234-1234-abc123456789",
      "userId": "550e8400-e29b-41d4-a716-446655440000",
      "status": "CONFIRMED",
      "confirmationCode": "RES-XXXXXXXX"
    }
  ]
}
```

**Verification:**
- Status: **200 OK** ✅
- Returns only CONFIRMED reservations ✅

**Alternative Tests:**
- `/api/reservations/status/PENDING` - For PENDING status
- `/api/reservations/status/CANCELLED` - For CANCELLED status

---

### TEST 7: Get Reservations by User

**Request:**
```
Method: GET
URL: {{base_url}}/api/reservations/user/550e8400-e29b-41d4-a716-446655440000
```

**Postman Steps:**
1. Click **"+"** → New Request
2. Name: **"7. Get Reservations by User"**
3. Method: **GET**
4. URL: `{{base_url}}/api/reservations/user/550e8400-e29b-41d4-a716-446655440000`
5. Click **Send**

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "User reservations fetched successfully",
  "data": [
    {
      "id": "abc12345-1234-1234-1234-abc123456789",
      "userId": "550e8400-e29b-41d4-a716-446655440000",
      "status": "CONFIRMED",
      "confirmationCode": "RES-XXXXXXXX"
    }
  ]
}
```

**Verification:**
- Status: **200 OK** ✅
- Returns only reservations for specified user ✅

---

### TEST 8: Get Reservations by Event

**Request:**
```
Method: GET
URL: {{base_url}}/api/reservations/event/770e8400-e29b-41d4-a716-446655440002
```

**Postman Steps:**
1. Click **"+"** → New Request
2. Name: **"8. Get Reservations by Event"**
3. Method: **GET**
4. URL: `{{base_url}}/api/reservations/event/770e8400-e29b-41d4-a716-446655440002`
5. Click **Send**

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Event reservations fetched successfully",
  "data": [
    {
      "id": "abc12345-1234-1234-1234-abc123456789",
      "eventId": "770e8400-e29b-41d4-a716-446655440002",
      "status": "CONFIRMED",
      "confirmationCode": "RES-XXXXXXXX"
    }
  ]
}
```

**Verification:**
- Status: **200 OK** ✅
- Returns only reservations for specified event ✅

---

### TEST 9: Create Second Reservation

**Request:**
```
Method: POST
URL: {{base_url}}/api/reservations
Headers: Content-Type: application/json
```

**Postman Steps:**
1. Click **"+"** → New Request
2. Name: **"9. Create Reservation #2"**
3. Method: **POST**
4. URL: `{{base_url}}/api/reservations`
5. **Headers Tab:**
   - Add: `Content-Type: application/json`
6. **Body Tab:**
   - Select **raw** → **JSON**
   - Paste:

```json
{
  "userId": "880e8400-e29b-41d4-a716-446655440003",
  "stallId": "990e8400-e29b-41d4-a716-446655440004",
  "eventId": "770e8400-e29b-41d4-a716-446655440002"
}
```

7. Click **Send**

**Expected Response (201 Created):**
```json
{
  "success": true,
  "message": "Reservation created successfully with PENDING status",
  "data": {
    "id": "def67890-5678-5678-5678-def678901234",
    "userId": "880e8400-e29b-41d4-a716-446655440003",
    "stallId": "990e8400-e29b-41d4-a716-446655440004",
    "eventId": "770e8400-e29b-41d4-a716-446655440002",
    "reservationDate": "2025-12-20",
    "status": "PENDING",
    "confirmationCode": "RES-YYYYYYYY"
  }
}
```

**Verification:**
- Status: **201 Created** ✅
- New reservation ID generated ✅
- Status is **PENDING** ✅

---

### TEST 10: Get All Reservations (Final Count)

**Request:**
```
Method: GET
URL: {{base_url}}/api/reservations
```

**Postman Steps:**
1. Click **"+"** → New Request
2. Name: **"10. Get All Reservations (Final)"**
3. Method: **GET**
4. URL: `{{base_url}}/api/reservations`
5. Click **Send**

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Reservations fetched successfully",
  "data": [
    {
      "id": "abc12345-1234-1234-1234-abc123456789",
      "status": "CONFIRMED",
      "confirmationCode": "RES-XXXXXXXX"
    },
    {
      "id": "def67890-5678-5678-5678-def678901234",
      "status": "PENDING",
      "confirmationCode": "RES-YYYYYYYY"
    }
  ]
}
```

**Verification:**
- Status: **200 OK** ✅
- Data array length = 2 ✅
- Contains both reservations ✅
- First is CONFIRMED, second is PENDING ✅

---

### TEST 11: Delete Second Reservation

**Request:**
```
Method: DELETE
URL: {{base_url}}/api/reservations/{reservation_id}
```

**Postman Steps:**
1. Click **"+"** → New Request
2. Name: **"11. Delete Reservation #2"**
3. Method: **DELETE**
4. URL: `{{base_url}}/api/reservations/def67890-5678-5678-5678-def678901234`
   - Replace `def67890-5678-5678-5678-def678901234` with actual ID from TEST 9
5. Click **Send**

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Reservation deleted successfully",
  "data": "Reservation with ID def67890-5678-5678-5678-def678901234 has been deleted"
}
```

**Verification:**
- Status: **200 OK** ✅
- Message confirms deletion ✅
- Returns deleted reservation ID ✅
- Kafka event published: RESERVATION_DELETED ✅

---

### TEST 12: Get All Reservations (After Deletion)

**Request:**
```
Method: GET
URL: {{base_url}}/api/reservations
```

**Postman Steps:**
1. Click **"+"** → New Request
2. Name: **"12. Get All Reservations (After Delete)"**
3. Method: **GET**
4. URL: `{{base_url}}/api/reservations`
5. Click **Send**

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Reservations fetched successfully",
  "data": [
    {
      "id": "abc12345-1234-1234-1234-abc123456789",
      "status": "CONFIRMED",
      "confirmationCode": "RES-XXXXXXXX"
    }
  ]
}
```

**Verification:**
- Status: **200 OK** ✅
- Data array length = 1 ✅
- Only first reservation remains ✅
- Deleted reservation removed from database ✅

---

## Expected Responses

### Response Format

**Success Response:**
```json
{
  "success": true,
  "message": "Description of what happened",
  "data": {
    // Response data here
  }
}
```

**Error Response:**
```json
{
  "success": false,
  "message": "Error description",
  "data": null
}
```

### Status Codes

| Code | Status | Meaning |
|------|--------|---------|
| 200 | OK | Request successful, data returned |
| 201 | Created | Resource successfully created |
| 400 | Bad Request | Invalid request format or data |
| 404 | Not Found | Resource not found |
| 500 | Server Error | Internal server error |

---

## Database Schema Reference

### Reservations Table
```sql
CREATE TABLE reservations (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    stall_id UUID NOT NULL,
    event_id UUID NOT NULL,
    reservation_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED')),
    confirmation_code VARCHAR(255),
    qr_code_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### User Snapshot Table
```sql
CREATE TABLE user_snapshot (
    user_id UUID PRIMARY KEY,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    email VARCHAR(255),
    mobile_no VARCHAR(255),
    role VARCHAR(50),
    status VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Stall Snapshot Table
```sql
CREATE TABLE stall_snapshot (
    stall_id UUID PRIMARY KEY,
    event_id UUID,
    stall_code VARCHAR(10),
    size_category VARCHAR(20),
    price NUMERIC(10, 2),
    location_x DOUBLE PRECISION,
    location_y DOUBLE PRECISION,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## Troubleshooting

### Issue: Connection Refused on Port 8084

**Error:** `Error: connect ECONNREFUSED 127.0.0.1:8084`

**Solution:**
```bash
# 1. Check if service is running
docker ps | findstr "8084"

# 2. If not running, start services
docker-compose -f docker-compose.backend.yml up -d

# 3. Wait 30-40 seconds for startup
# 4. Verify again
docker ps | findstr "8084"

# 5. Check logs if still not working
docker logs bookfair-reservation-service
```

### Issue: 404 Not Found

**Causes:**
- Wrong endpoint path
- Missing path parameters
- Typo in URL

**Check:**
- Verify base URL: `http://localhost:8084`
- Verify endpoint path
- Verify all {placeholders} are replaced with actual values

### Issue: 400 Bad Request

**Causes:**
- Missing required JSON fields
- Wrong data type
- Invalid JSON format

**Check:**
- Verify all required fields are present
- Verify JSON is properly formatted
- Check Content-Type header is `application/json`

### Issue: Database Connection Error

**Error in logs:** `SQLException: Connection refused`

**Solution:**
```bash
# 1. Check if database is running
docker ps | findstr "postgres"

# 2. Verify database name is 'reservation_db'
docker exec bookfair-reservation-db psql -U postgres -l

# 3. Check connection string in logs
docker logs bookfair-reservation-service | findstr "datasource"
```

### Issue: Table Not Found

**Error in logs:** `Table "public.reservations" doesn't exist`

**Solution:**
```bash
# 1. Check if Flyway migrations ran
docker logs bookfair-reservation-service | findstr "Flyway"

# 2. Verify tables exist
docker exec bookfair-reservation-db psql -U postgres -d reservation_db -c "\dt"

# 3. If tables missing, restart service
docker-compose -f docker-compose.backend.yml restart reservation-service
```

---

## Sample UUIDs for Testing

Use these UUIDs if you want different test data:

**User IDs:**
```
550e8400-e29b-41d4-a716-446655440000
660e8400-e29b-41d4-a716-446655440001
770e8400-e29b-41d4-a716-446655440002
880e8400-e29b-41d4-a716-446655440003
```

**Stall IDs:**
```
990e8400-e29b-41d4-a716-446655440004
aa0e8400-e29b-41d4-a716-446655440005
bb0e8400-e29b-41d4-a716-446655440006
cc0e8400-e29b-41d4-a716-446655440007
```

**Event IDs:**
```
770e8400-e29b-41d4-a716-446655440002
dd0e8400-e29b-41d4-a716-446655440008
ee0e8400-e29b-41d4-a716-446655440009
ff0e8400-e29b-41d4-a716-446655440010
```

---

## Summary Checklist

### Pre-Testing
- [ ] Docker-compose backend running
- [ ] Service accessible on port 8084
- [ ] Postman collection created
- [ ] Environment variable set: `base_url = http://localhost:8084`

### API Tests
- [ ] Test 1: GET all (empty) - 200 OK
- [ ] Test 2: POST create - 201 Created
- [ ] Test 3: GET all (count=1) - 200 OK
- [ ] Test 4: GET by ID - 200 OK
- [ ] Test 5: PUT update status - 200 OK
- [ ] Test 6: GET by status - 200 OK
- [ ] Test 7: GET by user - 200 OK
- [ ] Test 8: GET by event - 200 OK
- [ ] Test 9: POST create second - 201 Created
- [ ] Test 10: GET all (count=2) - 200 OK

### Database Verification
- [ ] Tables created in `reservation_db`
- [ ] Reservations table has 2 rows
- [ ] User snapshots created correctly
- [ ] Stall snapshots created correctly

---

**All APIs are now ready for testing!**

