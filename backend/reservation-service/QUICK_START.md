# Quick Start Guide - Reservation Service

## 🚀 Quick Start (5 Steps)

### Step 1: Create the Database
```sql
CREATE DATABASE reservation_service_db;
```

### Step 2: Navigate to Service Directory
```bash
cd backend/reservation-service
```

### Step 3: Build and Run
```bash
mvn clean install
mvn spring-boot:run
```

### Step 4: Verify Service is Running
Open browser: http://localhost:8086/api/reservations

### Step 5: Test the APIs
```bash
.\test-api.ps1
```

## 📌 The 3 Main APIs

### 1️⃣ View All Reservations
```http
GET http://localhost:8086/api/reservations
```

### 2️⃣ Book a Stall (Creates PENDING reservation)
```http
POST http://localhost:8086/api/reservations
Content-Type: application/json

{
  "userId": "uuid",
  "stallId": "uuid",
  "eventId": "uuid"
}
```
> The service derives reservation dates and metadata automatically from snapshot tables.

### 3️⃣ Update Status (Admin: PENDING → CONFIRMED or CANCELLED)
```http
PUT http://localhost:8086/api/reservations/{id}/status
Content-Type: application/json

{
  "status": "CONFIRMED"
}
```

## 💡 Key Points

- ✅ Service runs on **port 8086**
- ✅ Database: **reservation_service_db**
- ✅ Tables are **auto-created** on first run
- ✅ All new reservations start as **PENDING**
- ✅ Only **one active reservation** per stall allowed
- ✅ **QR code** generated when confirmed

## 📁 Important Files

| File | Purpose |
|------|---------|
| `application.properties` | Database configuration |
| `ReservationController.java` | API endpoints |
| `ReservationService.java` | Business logic |
| `test-api.ps1` | Test script |
| `RESERVATION_SERVICE_README.md` | Full documentation |

## 🔍 Troubleshooting

**Port already in use?**
```properties
# Change in application.properties
server.port=8087
```

**Database connection failed?**
```properties
# Update credentials in application.properties
spring.datasource.username=your_username
spring.datasource.password=your_password
```

**Tables not created?**
```properties
# Check in application.properties
spring.jpa.hibernate.ddl-auto=update
```

## ✨ Status Values

- `PENDING` - Awaiting admin approval
- `CONFIRMED` - Approved by admin
- `CANCELLED` - Rejected or cancelled

That's it! You're ready to go! 🎉
