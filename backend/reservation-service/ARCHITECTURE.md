# Reservation Service Architecture

## 📊 System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                     RESERVATION SERVICE                          │
│                      Port: 8086                                  │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                      CLIENT / API GATEWAY                        │
│                    (HTTP Requests / JSON)                        │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                    CONTROLLER LAYER                              │
│                 ReservationController.java                       │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │  GET  /api/reservations            → getAllReservations() │ │
│  │  POST /api/reservations            → createReservation()  │ │
│  │  PUT  /api/reservations/{id}/status → updateStatus()      │ │
│  │  GET  /api/reservations/{id}       → getById()            │ │
│  │  GET  /api/reservations/status/{s} → getByStatus()        │ │
│  │  GET  /api/reservations/user/{id}  → getByUser()          │ │
│  │  GET  /api/reservations/event/{id} → getByEvent()         │ │
│  └───────────────────────────────────────────────────────────┘ │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                      SERVICE LAYER                               │
│                  ReservationService.java                         │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │  • Business Logic                                          │ │
│  │  • Validation Rules                                        │ │
│  │  • Status Management                                       │ │
│  │  • Confirmation Code Generation                            │ │
│  │  • QR Code URL Generation                                  │ │
│  │  • Snapshot Data Management                                │ │
│  │  • Duplicate Prevention                                    │ │
│  └───────────────────────────────────────────────────────────┘ │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                    REPOSITORY LAYER                              │
│                  (Spring Data JPA)                               │
│  ┌────────────────────┬──────────────────┬────────────────────┐ │
│  │ ReservationRepo    │ UserSnapshotRepo │ StallSnapshotRepo  │ │
│  │ .findAll()         │ .findById()      │ .findById()        │ │
│  │ .save()            │ .save()          │ .save()            │ │
│  │ .findByStatus()    │                  │                    │ │
│  │ .findByUserId()    │                  │                    │ │
│  └────────────────────┴──────────────────┴────────────────────┘ │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                      ENTITY LAYER                                │
│                   (JPA Entities)                                 │
│  ┌────────────────────┬──────────────────┬────────────────────┐ │
│  │   Reservation      │   UserSnapshot   │  StallSnapshot     │ │
│  ├────────────────────┼──────────────────┼────────────────────┤ │
│  │ - id               │ - userId         │ - stallId          │ │
│  │ - userId           │ - firstName      │ - eventId          │ │
│  │ - stallId          │ - lastName       │ - stallCode        │ │
│  │ - eventId          │ - email          │ - sizeCategory     │ │
│  │ - reservationDate  │ - role           │ - price            │ │
│  │ - status           │ - status         │ - locationX        │ │
│  │ - confirmationCode │ - updatedAt      │ - locationY        │ │
│  │ - qrCodeUrl        │                  │ - updatedAt        │ │
│  │ - createdAt        │                  │                    │ │
│  │ - updatedAt        │                  │                    │ │
│  └────────────────────┴──────────────────┴────────────────────┘ │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                   DATABASE LAYER                                 │
│                PostgreSQL - reservation_service_db               │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │  Tables: reservations, user_snapshot, stall_snapshot      │ │
│  │  Indexes: status, userId, stallId, eventId                │ │
│  │  Constraints: Foreign Keys, NOT NULL, CHECK               │ │
│  └───────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

## 🔄 Request Flow Example

### Creating a Reservation

```
1. Client Request
   POST /api/reservations
   {
     "userId": "uuid",
     "stallId": "uuid",
       "eventId": "uuid"
   }
   
2. Controller Layer
   ↓ Validates request
   ↓ Calls service layer
   
3. Service Layer
   ↓ Checks if stall already reserved
   ↓ Resolves user & stall snapshots via relations
   ↓ Creates reservation with PENDING status
   ↓ Generates confirmation code
   
4. Repository Layer
   ↓ Saves to database
   
5. Database
   ↓ Inserts records
   ↓ Returns saved entity
   
6. Response
   {
     "success": true,
     "message": "Reservation created successfully",
     "data": { reservation details }
   }
```

## 🎯 Status Flow

```
┌─────────────┐
│   CREATE    │
│ Reservation │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│   PENDING   │  ← New reservations start here
└──────┬──────┘
       │
       ├─────────────────┐
       │                 │
       ▼                 ▼
┌─────────────┐   ┌─────────────┐
│  CONFIRMED  │   │  CANCELLED  │
│ (by Admin)  │   │ (by Admin)  │
└─────────────┘   └─────────────┘
```

## 🗂️ Package Structure

```
com.bookfair.reservation_service
├── config/
│   └── WebConfig.java           (CORS, Web Config)
├── controller/
│   └── ReservationController.java    (REST APIs)
├── dto/
│   ├── ApiResponse.java         (Response wrapper)
│   ├── CreateReservationRequest.java
│   ├── ReservationDTO.java
│   └── UpdateReservationStatusRequest.java
├── entity/
│   ├── Reservation.java         (Main entity)
│   ├── ReservationStatus.java   (Enum)
│   ├── StallSnapshot.java       (Cache)
│   └── UserSnapshot.java        (Cache)
├── exception/
│   ├── DuplicateResourceException.java
│   ├── GlobalExceptionHandler.java
│   ├── InvalidOperationException.java
│   └── ResourceNotFoundException.java
├── repository/
│   ├── ReservationRepository.java
│   ├── StallSnapshotRepository.java
│   └── UserSnapshotRepository.java
├── service/
│   └── ReservationService.java  (Business logic)
└── ReservationServiceApplication.java (Main)
```

## 🔌 Microservices Integration

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│ Auth Service │────▶│ User Data    │────▶│ User         │
│   (8080)     │     │              │     │ Snapshot     │
└──────────────┘     └──────────────┘     └──────────────┘
                                                   │
                                                   ▼
┌──────────────┐                           ┌──────────────┐
│ API Gateway  │──────────────────────────▶│ Reservation  │
│              │                           │   Service    │
└──────────────┘                           │   (8086)     │
                                           └──────────────┘
                                                   │
                                                   ▼
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│ Stall Service│────▶│ Stall Data   │────▶│ Stall        │
│   (8085)     │     │              │     │ Snapshot     │
└──────────────┘     └──────────────┘     └──────────────┘
```

## 📝 Key Components

### 1. Controller (HTTP Interface)
- Handles HTTP requests/responses
- Request validation
- Delegates to service layer

### 2. Service (Business Logic)
- Core business rules
- Status management
- Snapshot synchronization
- Validation logic

### 3. Repository (Data Access)
- JPA interface
- CRUD operations
- Custom queries

### 4. Entity (Data Model)
- JPA entities
- Database mapping
- Relationships

### 5. DTO (Data Transfer)
- API contracts
- Data transformation
- Validation rules

### 6. Exception Handling
- Custom exceptions
- Global handler
- Structured errors

## 🚀 Deployment Architecture

```
┌──────────────────────────────────────────┐
│           Load Balancer                  │
└────────────┬─────────────────────────────┘
             │
    ┌────────┴────────┐
    │                 │
    ▼                 ▼
┌─────────┐      ┌─────────┐
│Instance1│      │Instance2│
│  :8086  │      │  :8086  │
└────┬────┘      └────┬────┘
     │                │
     └────────┬───────┘
              │
              ▼
      ┌──────────────┐
      │  PostgreSQL  │
      │   Database   │
      └──────────────┘
```

## 💾 Data Flow

```
User Action → HTTP Request → Controller → Service → Repository
                                                         ↓
Response ← JSON ← DTO ← Business Logic ← Entity ← Database
```

## 🎯 Three Main APIs

```
1. GET /api/reservations
   └─→ View all reservations
   
2. POST /api/reservations
   └─→ Book stall (PENDING)
   
3. PUT /api/reservations/{id}/status
   └─→ Update status (CONFIRMED/CANCELLED)
```

---

**Architecture Pattern:** Layered Architecture + Repository Pattern
**Communication:** REST API (JSON)
**Database:** PostgreSQL
**Framework:** Spring Boot 3.5.7
