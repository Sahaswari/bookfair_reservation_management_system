# 🚀 Quick Postman Testing Guide

## Step-by-Step Instructions

### ✅ Prerequisites
1. **Start the service:**
   ```bash
   cd backend/reservation-service
   mvn spring-boot:run
   ```
   Wait until you see: `Started ReservationServiceApplication`

2. **Download Postman:** https://www.postman.com/downloads/

---

## 🎯 Method 1: Import Ready-Made Collection (EASIEST!)

### Step 1: Import Collection
1. Open Postman
2. Click **Import** button (top-left)
3. Click **Upload Files**
4. Select: `Reservation-Service.postman_collection.json`
5. Click **Import**

### Step 2: Run Tests
You'll see 12 pre-configured requests ready to test!

**Recommended Test Order:**
1. ✅ Get All Reservations (should be empty)
2. ✅ Create Reservation
3. ✅ Get All Reservations (should show 1)
4. ✅ Get Reservation by ID
5. ✅ Update Status to CONFIRMED
6. ✅ Get Reservations by Status - CONFIRMED
7. ✅ Create Second Reservation
8. ✅ Test Duplicate Reservation (should fail)

---

## 🎯 Method 2: Manual Setup

### Test 1: Get All Reservations

**In Postman:**
```
Method: GET
URL: http://localhost:8086/api/reservations
```

1. Click **+ New** → **HTTP Request**
2. Change dropdown from POST to **GET**
3. Enter URL: `http://localhost:8086/api/reservations`
4. Click **Send**

**You should see:**
```json
{
  "success": true,
  "message": "Reservations fetched successfully",
  "data": []
}
```

---

### Test 2: Create Reservation (Book Stall)

**In Postman:**
```
Method: POST
URL: http://localhost:8086/api/reservations
Headers: Content-Type: application/json
```

**Steps:**
1. Click **+ New** → **HTTP Request**
2. Keep **POST** selected
3. Enter URL: `http://localhost:8086/api/reservations`
4. Click **Headers** tab
   - Key: `Content-Type`
   - Value: `application/json`
5. Click **Body** tab
6. Select **raw**
7. Select **JSON** from dropdown
8. Paste this JSON:

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

9. Click **Send**

**You should see:**
```json
{
  "success": true,
  "message": "Reservation created successfully with PENDING status",
  "data": {
    "id": "some-uuid-here",
    "status": "PENDING",
    "confirmationCode": "RES-XXXXXXXX",
    ...
  }
}
```

**📝 IMPORTANT:** Copy the `id` value from the response!

---

### Test 3: Update Status to CONFIRMED

**In Postman:**
```
Method: PUT
URL: http://localhost:8086/api/reservations/YOUR-ID-HERE/status
Headers: Content-Type: application/json
```

**Steps:**
1. Click **+ New** → **HTTP Request**
2. Change to **PUT**
3. Enter URL: `http://localhost:8086/api/reservations/PASTE-ID-HERE/status`
4. Click **Headers** tab
   - Key: `Content-Type`
   - Value: `application/json`
5. Click **Body** tab → **raw** → **JSON**
6. Paste:

```json
{
  "status": "CONFIRMED"
}
```

7. Click **Send**

**You should see:**
```json
{
  "success": true,
  "message": "Reservation status updated successfully",
  "data": {
    "id": "...",
    "status": "CONFIRMED",
    "qrCodeUrl": "/api/reservations/.../qrcode"
  }
}
```

---

## 🎨 Visual Layout in Postman

```
┌────────────────────────────────────────────────────────┐
│  [GET ▼]  http://localhost:8086/api/reservations  [Send]│
├────────────────────────────────────────────────────────┤
│  Params  │  Authorization  │  Headers  │  Body         │
├────────────────────────────────────────────────────────┤
│                                                         │
│  (Body section - only for POST/PUT requests)           │
│                                                         │
│  [none] [form-data] [x-www-form-urlencoded]            │
│  [raw ●] [binary] [GraphQL]                            │
│                                                         │
│  [Text ▼] → Change to [JSON ▼]                         │
│                                                         │
│  {                                                      │
│    "userId": "...",                                     │
│    "stallId": "...",                                    │
│    ...                                                  │
│  }                                                      │
│                                                         │
└────────────────────────────────────────────────────────┘
```

---

## 📊 Expected Results Summary

| Test | Method | Expected Status | Expected Message |
|------|--------|----------------|------------------|
| Get All | GET | 200 OK | "Reservations fetched successfully" |
| Create | POST | 201 Created | "Reservation created successfully with PENDING status" |
| Update | PUT | 200 OK | "Reservation status updated successfully" |
| Get by ID | GET | 200 OK | "Reservation fetched successfully" |
| Duplicate | POST | 400 Bad Request | "Stall is already reserved" |

---

## 🐛 Common Issues & Solutions

### Issue: "Connection refused"
**Solution:** Service not running
```bash
cd backend/reservation-service
mvn spring-boot:run
```

### Issue: "404 Not Found"
**Solution:** Check URL spelling
- Correct: `http://localhost:8086/api/reservations`
- Wrong: `http://localhost:8086/reservations`

### Issue: "400 Bad Request - Validation error"
**Solution:** Check JSON body
- All required fields present?
- Correct JSON format?
- Content-Type header set?

### Issue: Empty response body
**Solution:** 
- Check **Headers** tab in response
- Look for **Body** tab in bottom panel
- Click **Pretty** or **Raw** to view

---

## 💡 Pro Tips

### Tip 1: Save Your Requests
After testing, click **Save** to keep requests for later use.

### Tip 2: Use Collection Variables
Instead of copy-pasting IDs, use variables:
- `{{reservationId}}`
- `{{baseUrl}}`

The imported collection already has these set up!

### Tip 3: Check Response Time
Look at bottom-right: "Time: 45 ms"
- Good: < 100ms
- Acceptable: 100-500ms
- Slow: > 500ms

### Tip 4: View Different Response Formats
Bottom panel tabs:
- **Pretty** - Formatted JSON
- **Raw** - Unformatted text
- **Preview** - HTML rendering

### Tip 5: Test Status Codes
Look for green "200 OK" or "201 Created" in top-right

---

## 📝 Testing Checklist

Copy this checklist and mark as you test:

```
□ Service is running on port 8086
□ Postman is installed
□ Collection imported (or manual requests created)

Main Tests:
□ GET all reservations (empty initially)
□ POST create reservation (status = PENDING)
□ GET all reservations (shows created reservation)
□ GET by reservation ID
□ PUT update to CONFIRMED (generates QR code)
□ GET by status CONFIRMED

Additional Tests:
□ GET by user ID
□ GET by event ID
□ GET by status PENDING
□ POST duplicate reservation (should fail)
□ Create second reservation with different stall

Bonus Tests:
□ PUT update to CANCELLED
□ Test invalid UUID format
□ Test missing required fields
□ Test invalid JSON format
```

---

## 🎯 Quick Reference Card

**Print this for easy reference:**

```
┌─────────────────────────────────────────────────┐
│         RESERVATION SERVICE API                 │
│         Base: localhost:8086/api/reservations   │
├─────────────────────────────────────────────────┤
│ GET    /                  → All reservations    │
│ POST   /                  → Create (PENDING)    │
│ PUT    /{id}/status       → Update status       │
│ GET    /{id}              → Get by ID           │
│ GET    /status/{status}   → Filter by status    │
│ GET    /user/{userId}     → User's bookings     │
│ GET    /event/{eventId}   → Event's bookings    │
└─────────────────────────────────────────────────┘

Status Values: PENDING | CONFIRMED | CANCELLED
```

---

## 🚀 You're Ready!

**Start testing now:**
1. ✅ Service running? Check!
2. ✅ Postman open? Check!
3. ✅ Collection imported? Check!
4. ✅ Click **Send** on first request!

**Need help?** Check:
- `POSTMAN_TESTING_GUIDE.md` - Full detailed guide
- `RESERVATION_SERVICE_README.md` - API documentation

---

**Happy Testing! 🎉**
