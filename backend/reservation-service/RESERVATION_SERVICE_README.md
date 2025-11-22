# Reservation Service

## Overview
The Reservation Service is a microservice for managing stall reservations at book fair events. It handles the booking process, status management, and maintains snapshots of user and stall information.

## Technology Stack
- **Java 17**
- **Spring Boot 3.5.7**
- **PostgreSQL** (Database)
- **Spring Data JPA** (ORM)
- **Lombok** (Boilerplate code reduction)
- **Maven** (Build tool)

## Database Schema

### Tables

#### 1. reservations
Main table storing reservation information:
- `id` (UUID, PK) - Reservation ID
- `user_id` (UUID) - Vendor ID from Auth Service
- `stall_id` (UUID) - Stall ID from Stall Service
- `event_id` (UUID) - Event ID
- `reservation_date` (DATE) - Reservation date
- `status` (ENUM: PENDING, CONFIRMED, CANCELLED) - Reservation status
- `confirmation_code` (VARCHAR) - QR code for entry
- `qr_code_url` (VARCHAR) - QR code URL
- `created_at` (TIMESTAMP) - Creation timestamp
- `updated_at` (TIMESTAMP) - Last update timestamp

#### 2. user_snapshot
Cached user information from Auth Service:
- `user_id` (UUID, PK) - User ID from Auth Service
- `first_name` (VARCHAR) - User's first name
- `last_name` (VARCHAR) - User's last name
- `email` (VARCHAR) - Email address
- `role` (VARCHAR) - User role
- `status` (VARCHAR) - User status
- `updated_at` (TIMESTAMP) - Last sync time

#### 3. stall_snapshot
Cached stall information from Stall Service:
- `stall_id` (UUID, PK) - Stall ID from Stall Service
- `event_id` (UUID) - Event ID
- `stall_code` (VARCHAR) - Stall code
- `size_category` (VARCHAR) - Stall size category
- `price` (DECIMAL) - Stall price
- `location_x` (FLOAT) - X coordinate
- `location_y` (FLOAT) - Y coordinate
- `updated_at` (TIMESTAMP) - Last sync time

## API Endpoints

### Base URL
```
http://localhost:8086/api/reservations
```

### 1. Get All Reservations
**Endpoint:** `GET /api/reservations`

**Description:** Retrieves all reservation records from the database.

**Response:**
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
      "reservationDate": "2025-11-19",
      "status": "PENDING",
      "confirmationCode": "RES-ABCD1234",
      "qrCodeUrl": "/api/reservations/uuid/qrcode",
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

### 2. Create Reservation (Book Stall)
**Endpoint:** `POST /api/reservations`

**Description:** Books a stall and creates a reservation with PENDING status.

**Request Body:**
```json
{
  "userId": "uuid",
  "stallId": "uuid",
  "eventId": "uuid",
  "reservationDate": "2025-11-19",
  "userFirstName": "John",
  "userLastName": "Doe",
  "userEmail": "john@example.com",
  "userRole": "VENDOR",
  "userStatus": "ACTIVE",
  "stallCode": "S001",
  "sizeCategory": "MEDIUM",
  "price": "1500.00",
  "locationX": 10.5,
  "locationY": 20.3
}
```

**Response:**
```json
{
  "success": true,
  "message": "Reservation created successfully with PENDING status",
  "data": {
    "id": "uuid",
    "userId": "uuid",
    "stallId": "uuid",
    "eventId": "uuid",
    "reservationDate": "2025-11-19",
    "status": "PENDING",
    "confirmationCode": "RES-ABCD1234",
    "qrCodeUrl": null,
    "createdAt": "2025-11-19T10:00:00",
    "updatedAt": "2025-11-19T10:00:00"
  }
}
```

### 3. Update Reservation Status (Admin Privilege)
**Endpoint:** `PUT /api/reservations/{id}/status`

**Description:** Updates the reservation status. Admins can change PENDING to CONFIRMED or CANCELLED.

**Request Body:**
```json
{
  "status": "CONFIRMED"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Reservation status updated successfully",
  "data": {
    "id": "uuid",
    "userId": "uuid",
    "stallId": "uuid",
    "eventId": "uuid",
    "reservationDate": "2025-11-19",
    "status": "CONFIRMED",
    "confirmationCode": "RES-ABCD1234",
    "qrCodeUrl": "/api/reservations/uuid/qrcode",
    "createdAt": "2025-11-19T10:00:00",
    "updatedAt": "2025-11-19T10:05:00"
  }
}
```

### Additional Endpoints

#### Get Reservation by ID
**Endpoint:** `GET /api/reservations/{id}`

#### Get Reservations by Status
**Endpoint:** `GET /api/reservations/status/{status}`

**Example:** `GET /api/reservations/status/PENDING`

#### Get Reservations by User
**Endpoint:** `GET /api/reservations/user/{userId}`

#### Get Reservations by Event
**Endpoint:** `GET /api/reservations/event/{eventId}`

## Configuration

### Database Setup
Create a PostgreSQL database:
```sql
CREATE DATABASE reservation_service_db;
```

### Application Configuration
Located in `src/main/resources/application.properties`:
```properties
server.port=8086
spring.datasource.url=jdbc:postgresql://localhost:5432/reservation_service_db
spring.datasource.username=postgres
spring.datasource.password=postgres
```

## Running the Service

### Using Maven
```bash
cd backend/reservation-service
mvn clean install
mvn spring-boot:run
```

### Using Docker
```bash
docker-compose up reservation-service
```

## Status Flow

```
PENDING → CONFIRMED (Admin approves)
PENDING → CANCELLED (Admin rejects or user cancels)
```

## Business Rules

1. **Stall Availability**: A stall cannot be reserved if it already has a PENDING or CONFIRMED reservation.
2. **Status Transitions**: Only PENDING reservations can be changed to CONFIRMED or CANCELLED.
3. **Confirmation Code**: Generated automatically when a reservation is created.
4. **QR Code**: Generated when the reservation status is changed to CONFIRMED.
5. **Snapshots**: User and stall data are cached in snapshot tables for performance and data consistency.

## Error Handling

The service uses a global exception handler that returns structured error responses:

```json
{
  "timestamp": "2025-11-19T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Reservation not found with ID: uuid",
  "path": "/api/reservations/uuid"
}
```

## Integration with Other Services

- **Auth Service**: Provides user information (vendors)
- **Stall Service**: Provides stall information
- **Notification Service**: Can be integrated to send confirmation emails

## Testing

Use the provided test files or tools like Postman to test the API endpoints.

Example using curl:
```bash
# Get all reservations
curl http://localhost:8086/api/reservations

# Create a reservation
curl -X POST http://localhost:8086/api/reservations \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "uuid",
    "stallId": "uuid",
    "eventId": "uuid",
    "reservationDate": "2025-11-19"
  }'

# Update status
curl -X PUT http://localhost:8086/api/reservations/{id}/status \
  -H "Content-Type: application/json" \
  -d '{"status": "CONFIRMED"}'
```

## Architecture Notes

This service follows microservices architecture principles:
- **Independent deployment**
- **Database per service pattern**
- **RESTful API design**
- **Loose coupling via HTTP APIs**
- **Data caching via snapshot tables**
