# Reservation Service API - Quick Reference

## Base Information
- **Service Name:** Reservation Service
- **Base URL:** `http://localhost:8084`
- **Port:** 8084
- **Database:** `reservation_db` (PostgreSQL)

---

## All 7 API Endpoints

### 1. GET All Reservations
```
GET http://localhost:8084/api/reservations
Headers: None
Body: None
Expected Status: 200
```

### 2. CREATE Reservation
```
POST http://localhost:8084/api/reservations
Headers: Content-Type: application/json
Body: {
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "stallId": "660e8400-e29b-41d4-a716-446655440001",
  "eventId": "770e8400-e29b-41d4-a716-446655440002",
  "reservationDate": "2025-12-15",
  "userFirstName": "John",
  "userLastName": "Doe",
  "userEmail": "john@example.com",
  "userRole": "CUSTOMER",
  "userStatus": "ACTIVE",
  "stallCode": "STALL-001",
  "sizeCategory": "MEDIUM",
  "price": "5000",
  "locationX": 10,
  "locationY": 20
}
Expected Status: 201
```

### 3. GET Reservation by ID
```
GET http://localhost:8084/api/reservations/{reservation-id}
Headers: None
Body: None
Expected Status: 200
```

### 4. UPDATE Status
```
PUT http://localhost:8084/api/reservations/{reservation-id}/status
Headers: Content-Type: application/json
Body: {
  "status": "CONFIRMED"
}
Expected Status: 200
Valid Status Values: PENDING, CONFIRMED, CANCELLED
```

### 5. GET by Status
```
GET http://localhost:8084/api/reservations/status/{status}
Headers: None
Body: None
Expected Status: 200
Valid Status Values: PENDING, CONFIRMED, CANCELLED
```

### 6. GET by User
```
GET http://localhost:8084/api/reservations/user/{userId}
Headers: None
Body: None
Expected Status: 200
```

### 7. GET by Event
```
GET http://localhost:8084/api/reservations/event/{eventId}
Headers: None
Body: None
Expected Status: 200
```

---

## Testing Sequence (10 Steps)

| Step | Method | Endpoint | Purpose |
|------|--------|----------|---------|
| 1 | GET | /api/reservations | Get all (should be empty) |
| 2 | POST | /api/reservations | Create reservation #1 |
| 3 | GET | /api/reservations | Get all (count=1) |
| 4 | GET | /api/reservations/{id} | Get by ID |
| 5 | PUT | /api/reservations/{id}/status | Update to CONFIRMED |
| 6 | GET | /api/reservations/status/CONFIRMED | Filter by status |
| 7 | GET | /api/reservations/user/{userId} | Filter by user |
| 8 | GET | /api/reservations/event/{eventId} | Filter by event |
| 9 | POST | /api/reservations | Create reservation #2 |
| 10 | GET | /api/reservations | Get all (count=2) |

---

## Response Examples

### Success Response
```json
{
  "success": true,
  "message": "Reservations fetched successfully",
  "data": [...]
}
```

### Error Response
```json
{
  "success": false,
  "message": "Error message here",
  "data": null
}
```

---

## Request Headers

### For GET Requests
No headers needed (but can add `Accept: application/json`)

### For POST/PUT Requests
```
Content-Type: application/json
```

---

## Status Codes Reference

| Code | Meaning |
|------|---------|
| 200 | OK - Request successful |
| 201 | Created - Resource created |
| 400 | Bad Request - Invalid data |
| 404 | Not Found - Resource not found |
| 500 | Server Error |

---

## Database Tables

### reservations
- `id` - UUID primary key
- `user_id` - Foreign key to user
- `stall_id` - Foreign key to stall
- `event_id` - Event identifier
- `reservation_date` - Date of reservation
- `status` - PENDING/CONFIRMED/CANCELLED
- `confirmation_code` - Unique code
- `qr_code_url` - QR code URL (generated on confirm)
- `created_at` - Creation timestamp
- `updated_at` - Last update timestamp

### user_snapshot
- `user_id` - UUID primary key
- `first_name` - User's first name
- `last_name` - User's last name
- `email` - Email address
- `mobile_no` - Phone number
- `role` - User role
- `status` - User status
- `updated_at` - Timestamp

### stall_snapshot
- `stall_id` - UUID primary key
- `event_id` - Event identifier
- `stall_code` - Stall code
- `size_category` - SMALL/MEDIUM/LARGE
- `price` - Booking price
- `location_x` - X coordinate
- `location_y` - Y coordinate
- `updated_at` - Timestamp

---

## Sample Test Data

### User IDs
- `550e8400-e29b-41d4-a716-446655440000`
- `880e8400-e29b-41d4-a716-446655440003`

### Stall IDs
- `660e8400-e29b-41d4-a716-446655440001`
- `990e8400-e29b-41d4-a716-446655440004`

### Event IDs
- `770e8400-e29b-41d4-a716-446655440002`

---

## Common Issues & Fixes

### Connection Refused (Port 8084)
```bash
docker ps | findstr "8084"
docker-compose -f docker-compose.backend.yml up -d
```

### 404 Not Found
- Check URL spelling
- Verify path parameters
- Confirm base URL: `http://localhost:8084`

### 400 Bad Request
- Verify all required fields present
- Check JSON format
- Confirm Content-Type header

### Database Connection Error
```bash
docker logs bookfair-reservation-service
docker exec bookfair-reservation-db psql -U postgres -d reservation_db -c "\dt"
```

---

## Postman Environment Variables

```
Variable: base_url
Value: http://localhost:8084
```

Use in URLs as: `{{base_url}}/api/reservations`

---

## Quick Commands

### Start Service
```bash
docker-compose -f docker-compose.backend.yml up -d
```

### Check Service Status
```bash
docker ps | findstr "8084"
```

### View Logs
```bash
docker logs bookfair-reservation-service
```

### Stop Service
```bash
docker-compose -f docker-compose.backend.yml down
```

### Connect to Database
```bash
docker exec -it bookfair-reservation-db psql -U postgres -d reservation_db
```

### Query Reservations
```sql
SELECT id, status, confirmation_code FROM reservations;
```

---

**Complete documentation available in: RESERVATION_API_TESTING.md**

