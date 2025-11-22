# Testing Reservation Service with Postman

## 🚀 Quick Setup

### Step 1: Start the Service
```bash
cd backend/reservation-service
mvn spring-boot:run
```

**Service runs on:** http://localhost:8086

### Step 2: Open Postman
Download from: https://www.postman.com/downloads/

---

## 📋 API Endpoints to Test

### Base URL
```
http://localhost:8086/api/reservations
```

---

## 1️⃣ Get All Reservations

**Method:** `GET`  
**URL:** `http://localhost:8086/api/reservations`  
**Headers:** 
- `Accept: application/json`

**Steps in Postman:**
1. Create new request
2. Select `GET` method
3. Enter URL: `http://localhost:8086/api/reservations`
4. Click **Send**

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Reservations fetched successfully",
  "data": [
    {
      "id": "uuid",
      "userId": "uuid",
      "stallId": "uuid",
      "eventId": "uuid",
      "reservationDate": "2025-12-01",
      "status": "PENDING",
      "confirmationCode": "RES-ABCD1234",
      "qrCodeUrl": null,
      "createdAt": "2025-11-19T10:00:00",
      "updatedAt": "2025-11-19T10:00:00",
      "userFirstName": "John",
      "userLastName": "Doe",
      "userEmail": "john@example.com",
      "stallCode": "S001",
      "sizeCategory": "MEDIUM",
      "price": 1500.00,
      "locationX": 10.5,
      "locationY": 20.3
    }
  ]
}
```

---

## 2️⃣ Create New Reservation (Book Stall)

**Method:** `POST`  
**URL:** `http://localhost:8086/api/reservations`  
**Headers:**
- `Content-Type: application/json`

**Steps in Postman:**
1. Create new request
2. Select `POST` method
3. Enter URL: `http://localhost:8086/api/reservations`
4. Click **Body** tab
5. Select **raw** and **JSON**
6. Paste the JSON body below
7. Click **Send**

**Request Body:**
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440001",
  "stallId": "550e8400-e29b-41d4-a716-446655440002",
  "eventId": "550e8400-e29b-41d4-a716-446655440003",
  "reservationDate": "2025-12-01",
  "userFirstName": "John",
  "userLastName": "Doe",
  "userEmail": "john.doe@example.com",
  "userRole": "VENDOR",
  "userStatus": "ACTIVE",
  "stallCode": "S001",
  "sizeCategory": "MEDIUM",
  "price": "1500.00",
  "locationX": 10.5,
  "locationY": 20.3
}
```

**Expected Response (201 Created):**
```json
{
  "success": true,
  "message": "Reservation created successfully with PENDING status",
  "data": {
    "id": "generated-uuid",
    "userId": "550e8400-e29b-41d4-a716-446655440001",
    "stallId": "550e8400-e29b-41d4-a716-446655440002",
    "eventId": "550e8400-e29b-41d4-a716-446655440003",
    "reservationDate": "2025-12-01",
    "status": "PENDING",
    "confirmationCode": "RES-ABCD1234",
    "qrCodeUrl": null,
    "createdAt": "2025-11-19T10:00:00",
    "updatedAt": "2025-11-19T10:00:00"
  }
}
```

**⚠️ Note:** Copy the `id` from the response - you'll need it for the next request!

---

## 3️⃣ Update Reservation Status (Admin)

### Option A: Confirm Reservation

**Method:** `PUT`  
**URL:** `http://localhost:8086/api/reservations/{id}/status`  
**Headers:**
- `Content-Type: application/json`

**Steps in Postman:**
1. Create new request
2. Select `PUT` method
3. Enter URL: `http://localhost:8086/api/reservations/YOUR-RESERVATION-ID-HERE/status`
4. Click **Body** tab
5. Select **raw** and **JSON**
6. Paste the JSON body below
7. Click **Send**

**Request Body:**
```json
{
  "status": "CONFIRMED"
}
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Reservation status updated successfully",
  "data": {
    "id": "uuid",
    "userId": "550e8400-e29b-41d4-a716-446655440001",
    "stallId": "550e8400-e29b-41d4-a716-446655440002",
    "status": "CONFIRMED",
    "confirmationCode": "RES-ABCD1234",
    "qrCodeUrl": "/api/reservations/uuid/qrcode",
    "createdAt": "2025-11-19T10:00:00",
    "updatedAt": "2025-11-19T10:05:00"
  }
}
```

### Option B: Cancel Reservation

**Request Body:**
```json
{
  "status": "CANCELLED"
}
```

---

## 4️⃣ Get Reservation by ID

**Method:** `GET`  
**URL:** `http://localhost:8086/api/reservations/{id}`  

**Steps in Postman:**
1. Create new request
2. Select `GET` method
3. Enter URL: `http://localhost:8086/api/reservations/YOUR-ID-HERE`
4. Click **Send**

---

## 5️⃣ Get Reservations by Status

**Method:** `GET`  
**URL:** `http://localhost:8086/api/reservations/status/{status}`

**Examples:**
- `http://localhost:8086/api/reservations/status/PENDING`
- `http://localhost:8086/api/reservations/status/CONFIRMED`
- `http://localhost:8086/api/reservations/status/CANCELLED`

---

## 6️⃣ Get Reservations by User

**Method:** `GET`  
**URL:** `http://localhost:8086/api/reservations/user/{userId}`

**Example:**
```
http://localhost:8086/api/reservations/user/550e8400-e29b-41d4-a716-446655440001
```

---

## 7️⃣ Get Reservations by Event

**Method:** `GET`  
**URL:** `http://localhost:8086/api/reservations/event/{eventId}`

**Example:**
```
http://localhost:8086/api/reservations/event/550e8400-e29b-41d4-a716-446655440003
```

---

## 🧪 Complete Test Scenario

### Test Flow:
1. ✅ GET all reservations (should be empty initially)
2. ✅ POST create first reservation (status = PENDING)
3. ✅ GET all reservations (should show 1 reservation)
4. ✅ GET by status PENDING (should show the reservation)
5. ✅ PUT update status to CONFIRMED
6. ✅ GET by status CONFIRMED (should show the reservation)
7. ✅ POST create another reservation with same stall (should fail)
8. ✅ GET by user ID (should show user's reservations)
9. ✅ GET by event ID (should show event's reservations)

---

## 📸 Postman Collection Setup

### Create a Collection:
1. Click **Collections** in Postman
2. Click **+ New Collection**
3. Name it: "Reservation Service API"
4. Add all 7 requests to this collection

### Set Variables:
1. Click on your collection
2. Go to **Variables** tab
3. Add these variables:

| Variable | Initial Value | Current Value |
|----------|--------------|---------------|
| `baseUrl` | `http://localhost:8086/api/reservations` | Same |
| `reservationId` | (empty - will be set dynamically) | (empty) |
| `userId` | `550e8400-e29b-41d4-a716-446655440001` | Same |
| `stallId` | `550e8400-e29b-41d4-a716-446655440002` | Same |
| `eventId` | `550e8400-e29b-41d4-a716-446655440003` | Same |

### Use Variables in Requests:
Instead of full URLs, use: `{{baseUrl}}`

Example: `{{baseUrl}}/{{reservationId}}/status`

---

## 📋 Sample Test Data

### User IDs (Copy these for testing):
```
550e8400-e29b-41d4-a716-446655440001
550e8400-e29b-41d4-a716-446655440004
550e8400-e29b-41d4-a716-446655440006
```

### Stall IDs:
```
550e8400-e29b-41d4-a716-446655440002
550e8400-e29b-41d4-a716-446655440005
550e8400-e29b-41d4-a716-446655440007
```

### Event ID:
```
550e8400-e29b-41d4-a716-446655440003
```

---

## ❌ Error Scenarios to Test

### Test 1: Duplicate Reservation
Try booking the same stall twice - should get 400 Bad Request

### Test 2: Invalid Status Update
Try updating a cancelled reservation - should fail

### Test 3: Non-existent Reservation
GET with invalid ID - should get 404 Not Found

### Test 4: Invalid Request Body
POST with missing required fields - should get validation error

---

## 🎯 Expected Status Codes

| Scenario | Status Code |
|----------|-------------|
| Get all reservations | 200 OK |
| Create reservation | 201 Created |
| Update status | 200 OK |
| Get by ID (found) | 200 OK |
| Get by ID (not found) | 404 Not Found |
| Duplicate reservation | 400 Bad Request |
| Invalid operation | 400 Bad Request |
| Validation error | 400 Bad Request |

---

## 🔍 Troubleshooting

### Service not responding?
```bash
# Check if service is running
netstat -ano | findstr :8086

# Restart service
mvn spring-boot:run
```

### Connection refused?
- Verify service is running on port 8086
- Check firewall settings
- Ensure PostgreSQL is running

### 404 Not Found?
- Check the URL spelling
- Verify base URL: `http://localhost:8086/api/reservations`

### 400 Bad Request?
- Check JSON syntax in body
- Verify all required fields are present
- Check Content-Type header is set

---

## 📦 Import Ready-Made Collection

I can provide you with a Postman collection JSON file if needed. Save this to a file and import it:

### File: `Reservation-Service.postman_collection.json`

```json
{
  "info": {
    "name": "Reservation Service API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Get All Reservations",
      "request": {
        "method": "GET",
        "header": [],
        "url": {
          "raw": "http://localhost:8086/api/reservations",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8086",
          "path": ["api", "reservations"]
        }
      }
    },
    {
      "name": "Create Reservation",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"userId\": \"550e8400-e29b-41d4-a716-446655440001\",\n  \"stallId\": \"550e8400-e29b-41d4-a716-446655440002\",\n  \"eventId\": \"550e8400-e29b-41d4-a716-446655440003\",\n  \"reservationDate\": \"2025-12-01\",\n  \"userFirstName\": \"John\",\n  \"userLastName\": \"Doe\",\n  \"userEmail\": \"john.doe@example.com\",\n  \"userRole\": \"VENDOR\",\n  \"userStatus\": \"ACTIVE\",\n  \"stallCode\": \"S001\",\n  \"sizeCategory\": \"MEDIUM\",\n  \"price\": \"1500.00\",\n  \"locationX\": 10.5,\n  \"locationY\": 20.3\n}"
        },
        "url": {
          "raw": "http://localhost:8086/api/reservations",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8086",
          "path": ["api", "reservations"]
        }
      }
    },
    {
      "name": "Update Status to CONFIRMED",
      "request": {
        "method": "PUT",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"status\": \"CONFIRMED\"\n}"
        },
        "url": {
          "raw": "http://localhost:8086/api/reservations/{id}/status",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8086",
          "path": ["api", "reservations", "{id}", "status"]
        }
      }
    }
  ]
}
```

**To import:**
1. Open Postman
2. Click **Import** button
3. Select the JSON file
4. Click **Import**

---

## 💡 Pro Tips

1. **Use Environment Variables** for different environments (dev, test, prod)
2. **Save Responses** to compare with future tests
3. **Use Tests Tab** to write automated assertions
4. **Use Pre-request Scripts** to auto-generate UUIDs
5. **Organize by Folders** within the collection

---

## ✅ Testing Checklist

- [ ] Service is running on port 8086
- [ ] PostgreSQL database is created
- [ ] Postman is installed
- [ ] All 7 main endpoints tested
- [ ] Error scenarios validated
- [ ] Different status transitions tested
- [ ] Multiple reservations created
- [ ] Filter endpoints tested

---

**Happy Testing! 🚀**
