# Stall Service API Guide

This document describes the event and stall management endpoints exposed by the **Stall Service**.

## Base URLs

| Environment | Gateway URL | Direct Service URL | Notes |
|-------------|-------------|--------------------|-------|
| Local (Docker) | `http://localhost:8080` | `http://localhost:8085` | Use the gateway URL (`/api/...`) so CORS, filters, and routing rules stay consistent with higher environments. |

## Response Format

The Stall Service uses a consistent `ApiResponse<T>` envelope:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "timestamp": "2025-11-22T08:15:30.123Z"
}
```

## Endpoints Summary

### Event Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/events` | POST | Create a new event |
| `/api/events` | GET | Get all events |
| `/api/events/{id}` | GET | Get event by ID |
| `/api/events/year/{year}` | GET | Get events by year |
| `/api/events/status/{status}` | GET | Get events by status (UPCOMING, ONGOING, ENDED) |
| `/api/events/{id}` | PUT | Update an existing event |
| `/api/events/{id}/status` | PATCH | Update only the event status |
| `/api/events/{id}` | DELETE | Delete an event |

### Stall Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/stalls` | POST | Create a new stall |
| `/api/stalls` | GET | Get all stalls |
| `/api/stalls/{id}` | GET | Get stall by ID |
| `/api/stalls/event/{eventId}` | GET | Get all stalls for a specific event |
| `/api/stalls/event/{eventId}/available` | GET | Get available stalls for an event |
| `/api/stalls/event/{eventId}/available/size/{size}` | GET | Get available stalls by size |
| `/api/stalls/vendor/{vendorId}` | GET | Get stalls reserved by a specific vendor |
| `/api/stalls/{id}` | PUT | Update a stall |
| `/api/stalls/{id}/reserve` | POST | Reserve a stall |
| `/api/stalls/{id}/unreserve` | POST | Unreserve a stall |
| `/api/stalls/{id}` | DELETE | Delete a stall |

---

## 1. Create Event — `POST /api/events`

Creates a new book fair event.

### Request Body

```json
{
  "year": 2025,
  "name": "Colombo International Book Fair 2025",
  "startDate": "2025-09-20",
  "endDate": "2025-09-29",
  "location": "BMICH, Colombo",
  "status": "UPCOMING"
}
```

### Successful Response (`201 Created`)

```json
{
  "success": true,
  "message": "Event created successfully",
  "data": {
    "id": "c473562d-123a-456b-789c-0123456789ab",
    "year": 2025,
    "name": "Colombo International Book Fair 2025",
    "startDate": "2025-09-20",
    "endDate": "2025-09-29",
    "location": "BMICH, Colombo",
    "status": "UPCOMING",
    "totalStalls": 0,
    "availableStalls": 0,
    "createdAt": "2025-11-22T10:00:00Z",
    "updatedAt": "2025-11-22T10:00:00Z"
  }
}
```

---

## 2. Create Stall — `POST /api/stalls`

Adds a new stall to an existing event.

### Request Body

```json
{
  "eventId": "c473562d-123a-456b-789c-0123456789ab",
  "stallCode": "A001",
  "sizeCategory": "MEDIUM",
  "price": 50000.00,
  "locationX": 10.5,
  "locationY": 20.3
}
```

### Successful Response (`201 Created`)

```json
{
  "success": true,
  "message": "Stall created successfully",
  "data": {
    "id": "d584673e-234b-567c-890d-1234567890bc",
    "eventId": "c473562d-123a-456b-789c-0123456789ab",
    "eventName": "Colombo International Book Fair 2025",
    "stallCode": "A001",
    "sizeCategory": "MEDIUM",
    "price": 50000.00,
    "locationX": 10.5,
    "locationY": 20.3,
    "isReserved": false,
    "createdAt": "2025-11-22T10:05:00Z",
    "updatedAt": "2025-11-22T10:05:00Z"
  }
}
```

---

## 3. Get Available Stalls — `GET /api/stalls/event/{eventId}/available`

Retrieves all unreserved stalls for a specific event.

### Successful Response (`200 OK`)

```json
{
  "success": true,
  "message": "Available stalls fetched successfully",
  "data": [
    {
      "id": "d584673e-234b-567c-890d-1234567890bc",
      "eventId": "c473562d-123a-456b-789c-0123456789ab",
      "eventName": "Colombo International Book Fair 2025",
      "stallCode": "A001",
      "sizeCategory": "MEDIUM",
      "price": 50000.00,
      "locationX": 10.5,
      "locationY": 20.3,
      "isReserved": false
    }
  ]
}
```

---

## 4. Reserve Stall — `POST /api/stalls/{id}/reserve`

Reserves a stall for a specific vendor.

### Query Parameters
- `vendorId` (UUID, required): The ID of the vendor reserving the stall.

### Successful Response (`200 OK`)

```json
{
  "success": true,
  "message": "Stall reserved successfully",
  "data": {
    "id": "d584673e-234b-567c-890d-1234567890bc",
    "stallCode": "A001",
    "isReserved": true,
    "reservedBy": "e695784f-345c-678d-901e-2345678901cd",
    "reservedByName": "John Doe" // Populated if User Snapshot exists
  }
}
```

---

## 5. User Snapshot Endpoints

Used to cache user information from the Auth Service to reduce inter-service calls.

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/user-snapshots` | POST | Create or update a user snapshot |
| `/api/user-snapshots/user/{userId}` | GET | Get snapshot by original User ID |

### Create/Update Snapshot Request

```json
{
  "userId": "e695784f-345c-678d-901e-2345678901cd",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "role": "VENDOR",
  "status": "ACTIVE"
}
```

## Integration Notes

1. **User Snapshots**: The Auth Service should call the User Snapshot endpoint whenever a user is created or updated to keep the Stall Service data in sync.
2. **Reservation Flow**: When a stall is reserved, the system checks if it is already reserved. If not, it marks it as reserved by the provided vendor ID.
3. **Stall Map**: The `locationX` and `locationY` coordinates are intended for rendering the stall map on the frontend.
