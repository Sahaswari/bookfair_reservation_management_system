# 📚 Reservation Service

> **Microservice for managing stall reservations at book fair events**

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Latest-blue.svg)](https://www.postgresql.org/)
[![Status](https://img.shields.io/badge/Status-Production%20Ready-success.svg)]()

## 🎯 Purpose

This service handles all reservation operations for the Book Fair Reservation Management System, including:
- Booking stalls at book fair events
- Managing reservation status (PENDING → CONFIRMED/CANCELLED)
- Caching user and stall information for performance
- Generating confirmation codes and QR codes

## 🚀 Quick Start

```bash
# 1. Create database
createdb reservation_service_db

# 2. Navigate to service directory
cd backend/reservation-service

# 3. Build and run
mvn clean install
mvn spring-boot:run

# 4. Test
.\test-api.ps1
```

**Service URL:** http://localhost:8086/api/reservations

## 📖 Documentation

| Document | Description |
|----------|-------------|
| [QUICK_START.md](QUICK_START.md) | Get started in 5 minutes |
| [RESERVATION_SERVICE_README.md](RESERVATION_SERVICE_README.md) | Complete API documentation |
| [ARCHITECTURE.md](ARCHITECTURE.md) | System architecture & design |
| [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md) | Implementation details |
| [IMPLEMENTATION_CHECKLIST.md](IMPLEMENTATION_CHECKLIST.md) | Verification checklist |

## 🔌 API Endpoints

### Core Endpoints (Required)

#### 1️⃣ Get All Reservations
```http
GET /api/reservations
```

#### 2️⃣ Book a Stall (Creates PENDING reservation)
```http
POST /api/reservations
Content-Type: application/json

{
  "userId": "uuid",
  "stallId": "uuid",
  "eventId": "uuid",
  "reservationDate": "2025-12-01"
}
```

#### 3️⃣ Update Status (Admin: PENDING → CONFIRMED/CANCELLED)
```http
PUT /api/reservations/{id}/status
Content-Type: application/json

{
  "status": "CONFIRMED"
}
```

### Additional Endpoints

- `GET /api/reservations/{id}` - Get by ID
- `GET /api/reservations/status/{status}` - Filter by status
- `GET /api/reservations/user/{userId}` - User's reservations
- `GET /api/reservations/event/{eventId}` - Event's reservations

## 🗄️ Database Schema

### Tables
1. **reservations** - Main reservation data
2. **user_snapshot** - Cached user information
3. **stall_snapshot** - Cached stall information

See [database-schema.sql](database-schema.sql) for full schema.

## 🏗️ Architecture

```
Client → Controller → Service → Repository → Database
```

- **Controller:** REST API endpoints
- **Service:** Business logic
- **Repository:** Data access
- **Entity:** Data model

## 🛠️ Technology Stack

- **Framework:** Spring Boot 3.5.7
- **Language:** Java 17
- **Database:** PostgreSQL
- **ORM:** Spring Data JPA (Hibernate)
- **Build:** Maven
- **Tools:** Lombok

## 📦 Project Structure

```
reservation-service/
├── src/main/java/
│   └── com.bookfair.reservation_service/
│       ├── config/         # Configuration
│       ├── controller/     # REST APIs
│       ├── dto/           # Data transfer objects
│       ├── entity/        # JPA entities
│       ├── exception/     # Error handling
│       ├── repository/    # Data access
│       └── service/       # Business logic
├── src/main/resources/
│   └── application.properties
├── test-api.ps1           # Test script
├── test-reservation-apis.http
└── *.md                   # Documentation
```

## 🔄 Status Flow

```
PENDING → CONFIRMED (Admin approves)
       → CANCELLED (Admin rejects)
```

## ⚙️ Configuration

### Database (application.properties)
```properties
server.port=8086
spring.datasource.url=jdbc:postgresql://localhost:5432/reservation_service_db
spring.datasource.username=postgres
spring.datasource.password=postgres
```

## 🧪 Testing

### Option 1: PowerShell Script
```bash
.\test-api.ps1
```

### Option 2: HTTP File
Open `test-reservation-apis.http` in VS Code with REST Client extension

### Option 3: Manual Testing
```bash
# Get all reservations
curl http://localhost:8086/api/reservations

# Create reservation
curl -X POST http://localhost:8086/api/reservations \
  -H "Content-Type: application/json" \
  -d '{"userId":"uuid","stallId":"uuid","eventId":"uuid","reservationDate":"2025-12-01"}'
```

## 🔐 Business Rules

✅ **Stall Availability:** Only one active reservation per stall
✅ **New Reservations:** Always start with PENDING status
✅ **Status Updates:** Only admins can change status
✅ **Status Transitions:** PENDING → CONFIRMED or CANCELLED
✅ **Confirmation Code:** Auto-generated on creation
✅ **QR Code:** Generated when confirmed

## 🌐 Microservices Integration

Integrates with:
- **Auth Service** - User information
- **Stall Service** - Stall information
- **Event Service** - Event details
- **API Gateway** - Request routing

## 📊 Features

- ✅ RESTful API design
- ✅ PostgreSQL database
- ✅ Automatic table creation
- ✅ Request validation
- ✅ Exception handling
- ✅ CORS support
- ✅ Transaction management
- ✅ Logging
- ✅ Snapshot caching pattern

## 🐛 Troubleshooting

### Port already in use
Change `server.port` in `application.properties`

### Database connection failed
Verify PostgreSQL is running and credentials are correct

### Tables not created
Check `spring.jpa.hibernate.ddl-auto=update` in properties

## 📝 License

Part of the Book Fair Reservation Management System

## 👥 Contributors

Developed as part of Software Architecture Project

---

## 📚 Learn More

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)

---

**Status:** ✅ Production Ready | **Version:** 1.0.0 | **Port:** 8086
