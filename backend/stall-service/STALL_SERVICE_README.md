# Stall Service - Complete Guide

## 📋 Overview

The Stall Service manages book fair events, stalls, and user information for the International Bookfair Reservation Management System. It provides REST APIs for creating, managing, and reserving stalls for different events.

## 🗄️ Database Schema

### Tables

#### 1. **events**
Stores information about book fair events.

| Field | Type | Description |
|-------|------|-------------|
| id | UUID (PK) | Unique event ID |
| year | INT | Year (e.g., 2025) |
| name | VARCHAR(100) | Event name (e.g., "Colombo Book Fair 2025") |
| start_date | DATE | Event start date |
| end_date | DATE | Event end date |
| location | VARCHAR(255) | Location of event |
| status | ENUM | Event lifecycle (UPCOMING, ONGOING, ENDED) |
| created_at | TIMESTAMP | Record creation time |
| updated_at | TIMESTAMP | Last update time |

#### 2. **stalls**
Stores stall information for each event.

| Field | Type | Description |
|-------|------|-------------|
| id | UUID (PK) | Stall ID |
| event_id | UUID (FK → events.id) | Which event the stall belongs to |
| stall_code | VARCHAR(10), UNIQUE | Unique stall identifier |
| size_category | ENUM | Stall size (SMALL, MEDIUM, LARGE) |
| price | DECIMAL(10,2) | Stall rental cost |
| location_x | FLOAT | Map X-coordinate |
| location_y | FLOAT | Map Y-coordinate |
| is_reserved | BOOLEAN | True if reserved |
| reserved_by | UUID | Vendor ID |
| created_at | TIMESTAMP | Created timestamp |
| updated_at | TIMESTAMP | Updated timestamp |

#### 3. **user_snapshot**
Cached user information from Auth Service.

| Field | Type | Description |
|-------|------|-------------|
| id | UUID (PK) | Snapshot ID |
| user_id | UUID (PK) | User ID from Auth Service |
| first_name | VARCHAR(100) | Cached name |
| last_name | VARCHAR(100) | Cached name |
| email | VARCHAR(255) | Cached email |
| role | VARCHAR(50) | Cached user role |
| status | VARCHAR(50) | Cached status |
| updated_at | TIMESTAMP | Last sync time |

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 16+
- Docker & Docker Compose (for containerized deployment)

### Local Development Setup

#### 1. **Create PostgreSQL Database**

```sql
-- Connect to PostgreSQL
psql -U postgres

-- Create database
CREATE DATABASE stall_service_db;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE stall_service_db TO postgres;
```

#### 2. **Configure Application Properties**

The `application.properties` file is already configured:

```properties
spring.application.name=stall-service
server.port=8085

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/stall_service_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

#### 3. **Build and Run**

```bash
# Navigate to stall-service directory
cd backend/stall-service

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run

# Or run the JAR file
java -jar target/stall-service-0.0.1-SNAPSHOT.jar
```

The service will start on **http://localhost:8085**

### Docker Deployment

#### 1. **Using Docker Compose (Recommended)**

From the project root directory:

```bash
# Start only stall service with database
docker-compose up -d stall-service stall-postgres

# Or start all services
docker-compose up -d

# Check logs
docker-compose logs -f stall-service

# Check database logs
docker-compose logs -f stall-postgres
```

#### 2. **Database Access**

The PostgreSQL database is exposed on **port 5433** (to avoid conflicts with local PostgreSQL on 5432):

```bash
# Connect to the database
psql -h localhost -p 5433 -U postgres -d stall_service_db

# Password: postgres
```

## 📡 API Endpoints

### Base URL
- **Local**: `http://localhost:8085`
- **Docker**: `http://localhost:8085`

### Event APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/events` | Create a new event |
| GET | `/api/events` | Get all events |
| GET | `/api/events/{id}` | Get event by ID |
| GET | `/api/events/year/{year}` | Get events by year |
| GET | `/api/events/status/{status}` | Get events by status |
| PUT | `/api/events/{id}` | Update event |
| DELETE | `/api/events/{id}` | Delete event |

### Stall APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/stalls` | Create a new stall |
| GET | `/api/stalls` | Get all stalls |
| GET | `/api/stalls/{id}` | Get stall by ID |
| GET | `/api/stalls/event/{eventId}` | Get stalls by event |
| GET | `/api/stalls/event/{eventId}/available` | Get available stalls |
| GET | `/api/stalls/size/{sizeCategory}` | Get stalls by size |
| GET | `/api/stalls/code/{stallCode}` | Get stall by code |
| PUT | `/api/stalls/{id}` | Update stall |
| POST | `/api/stalls/{stallId}/reserve?userId={userId}` | Reserve a stall |
| POST | `/api/stalls/{stallId}/unreserve` | Unreserve a stall |
| DELETE | `/api/stalls/{id}` | Delete stall |

### User Snapshot APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/user-snapshots` | Create/Update user snapshot |
| GET | `/api/user-snapshots` | Get all snapshots |
| GET | `/api/user-snapshots/{id}` | Get snapshot by ID |
| GET | `/api/user-snapshots/user/{userId}` | Get snapshot by user ID |
| DELETE | `/api/user-snapshots/{id}` | Delete snapshot |

## 🧪 API Testing Examples

### 1. Create an Event

```bash
curl -X POST http://localhost:8085/api/events \
  -H "Content-Type: application/json" \
  -d '{
    "year": 2025,
    "name": "Colombo International Book Fair 2025",
    "startDate": "2025-09-15",
    "endDate": "2025-09-25",
    "location": "BMICH, Colombo",
    "status": "UPCOMING"
  }'
```

### 2. Create a Stall

```bash
curl -X POST http://localhost:8085/api/stalls \
  -H "Content-Type: application/json" \
  -d '{
    "eventId": "<EVENT_ID>",
    "stallCode": "A001",
    "sizeCategory": "MEDIUM",
    "price": 50000.00,
    "locationX": 10.5,
    "locationY": 20.3
  }'
```

### 3. Get Available Stalls for an Event

```bash
curl -X GET http://localhost:8085/api/stalls/event/<EVENT_ID>/available
```

### 4. Reserve a Stall

```bash
curl -X POST "http://localhost:8085/api/stalls/<STALL_ID>/reserve?userId=<USER_ID>"
```

### 5. Get All Events

```bash
curl -X GET http://localhost:8085/api/events
```

## 🛠️ Development

### Project Structure

```
stall-service/
├── src/
│   ├── main/
│   │   ├── java/com/bookfair/stall_service/
│   │   │   ├── entity/          # Database entities
│   │   │   │   ├── Event.java
│   │   │   │   ├── Stall.java
│   │   │   │   └── UserSnapshot.java
│   │   │   ├── repository/      # JPA repositories
│   │   │   │   ├── EventRepository.java
│   │   │   │   ├── StallRepository.java
│   │   │   │   └── UserSnapshotRepository.java
│   │   │   ├── dto/            # Data Transfer Objects
│   │   │   │   ├── EventDTO.java
│   │   │   │   ├── StallDTO.java
│   │   │   │   └── UserSnapshotDTO.java
│   │   │   ├── service/        # Business logic
│   │   │   │   ├── EventService.java
│   │   │   │   ├── StallService.java
│   │   │   │   └── UserSnapshotService.java
│   │   │   ├── controller/     # REST controllers
│   │   │   │   ├── EventController.java
│   │   │   │   ├── StallController.java
│   │   │   │   └── UserSnapshotController.java
│   │   │   └── StallServiceApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── pom.xml
├── Dockerfile
└── STALL_SERVICE_README.md
```

### Key Technologies

- **Spring Boot 3.5.7** - Application framework
- **Spring Data JPA** - Database access
- **PostgreSQL** - Database
- **Hibernate** - ORM
- **Maven** - Build tool
- **Docker** - Containerization

## 🔍 Testing

### Using PowerShell Script

A PowerShell script `test-api.ps1` is provided for quick API testing:

```powershell
# Run all tests
.\test-api.ps1

# The script will:
# 1. Create an event
# 2. Create multiple stalls
# 3. List all stalls
# 4. Reserve a stall
# 5. List available stalls
```

### Manual Testing with Postman

1. Import the API collection from the `/docs` folder (if available)
2. Set base URL: `http://localhost:8085`
3. Test each endpoint

## 🐛 Troubleshooting

### Database Connection Issues

```bash
# Check if PostgreSQL is running
docker-compose ps stall-postgres

# Check database logs
docker-compose logs stall-postgres

# Connect to database
psql -h localhost -p 5433 -U postgres -d stall_service_db
```

### Application Not Starting

```bash
# Check application logs
docker-compose logs -f stall-service

# Common issues:
# 1. Port 8085 already in use
# 2. Database not ready
# 3. Wrong database credentials
```

### Clean Start

```bash
# Stop all containers
docker-compose down

# Remove volumes (WARNING: This deletes all data)
docker-compose down -v

# Rebuild and start
docker-compose up -d --build stall-service
```

## 📊 Database Migrations

The application uses `spring.jpa.hibernate.ddl-auto=update` which automatically creates/updates tables. For production, consider:

1. Using Flyway or Liquibase for database migrations
2. Setting `ddl-auto=validate` in production
3. Manual schema management

## 🔐 Security Considerations

**TODO for Production:**

1. Add Spring Security
2. Implement JWT authentication
3. Add authorization checks
4. Use environment variables for credentials
5. Enable HTTPS
6. Add rate limiting
7. Implement input validation

## 📝 Notes

- Database is automatically created when the application starts
- Tables are auto-generated from entity classes
- Use UUIDs for all primary keys
- Foreign key relationships are managed by JPA
- Timestamps are automatically handled

## 🤝 Integration with Other Services

### Auth Service
- User snapshots cached from Auth Service
- User IDs reference Auth Service users

### Reservation Service
- Stall IDs used in reservations
- Status updates synced

### API Gateway
- All requests routed through gateway
- Gateway handles authentication

## 📧 Support

For issues or questions:
- Check logs: `docker-compose logs -f stall-service`
- Check database: `psql -h localhost -p 5433 -U postgres -d stall_service_db`
- Contact: Your Team Lead

---

**Last Updated**: November 2025  
**Service Version**: 0.0.1-SNAPSHOT  
**Database**: PostgreSQL 16

## 📊 Database Schema

The Stall Service uses **PostgreSQL** database with three main tables:

### 1. **events** Table
Stores information about book fair events.

| Column | Type | Description |
|--------|------|-------------|
| id | UUID (PK) | Unique event ID |
| year | INT | Event year (e.g., 2025) |
| name | VARCHAR(100) | Event name |
| start_date | DATE | Event start date |
| end_date | DATE | Event end date |
| location | VARCHAR(255) | Event location |
| status | ENUM | Event lifecycle (UPCOMING, ONGOING, ENDED) |
| created_at | TIMESTAMP | Record creation time |
| updated_at | TIMESTAMP | Last update time |

### 2. **stalls** Table
Stores information about individual stalls at events.

| Column | Type | Description |
|--------|------|-------------|
| id | UUID (PK) | Unique stall ID |
| event_id | UUID (FK) | Reference to events table |
| stall_code | VARCHAR(10) UNIQUE | Unique stall identifier |
| size_category | ENUM | Stall size (SMALL, MEDIUM, LARGE) |
| price | DECIMAL(10,2) | Rental cost |
| location_x | FLOAT | Map X-coordinate |
| location_y | FLOAT | Map Y-coordinate |
| is_reserved | BOOLEAN | Reservation status |
| reserved_by | UUID | Vendor ID from Auth Service |
| created_at | TIMESTAMP | Record creation time |
| updated_at | TIMESTAMP | Last update time |

### 3. **user_snapshot** Table
Cached user information from Auth Service.

| Column | Type | Description |
|--------|------|-------------|
| user_id | UUID (PK) | User ID from Auth Service |
| first_name | VARCHAR(100) | Cached first name |
| last_name | VARCHAR(100) | Cached last name |
| email | VARCHAR(255) | Cached email |
| role | VARCHAR(50) | Cached user role |
| status | VARCHAR(50) | Cached user status |
| updated_at | TIMESTAMP | Last sync time |

## 🚀 Quick Start

### Prerequisites
- Java 17
- PostgreSQL 16+ installed locally OR use Docker
- Maven 3.8+

### Option 1: Using Docker (Recommended)

```bash
# Start stall-service with its database
docker-compose up stall-service stall-postgres -d

# Check logs
docker-compose logs -f stall-service

# Access database
docker exec -it bookfair-stall-postgres psql -U postgres -d stall_service_db
```

### Option 2: Local PostgreSQL Setup

1. **Create Database**:
```sql
CREATE DATABASE stall_service_db;
```

2. **Update application.properties** (if using different credentials):
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/stall_service_db
spring.datasource.username=your_username
spring.datasource.password=your_password
```

3. **Run the service**:
```bash
cd backend/stall-service
mvn spring-boot:run
```

The tables will be created automatically by Hibernate based on the JPA entities.

## 📡 API Endpoints

### Event APIs

#### Create Event
```http
POST /api/events
Content-Type: application/json

{
  "year": 2025,
  "name": "Colombo Book Fair 2025",
  "startDate": "2025-09-20",
  "endDate": "2025-09-30",
  "location": "BMICH, Colombo",
  "status": "UPCOMING"
}
```

#### Get All Events
```http
GET /api/events
```

#### Get Event by ID
```http
GET /api/events/{eventId}
```

#### Get Events by Year
```http
GET /api/events/year/2025
```

#### Get Events by Status
```http
GET /api/events/status/UPCOMING
```

#### Update Event Status
```http
PATCH /api/events/{eventId}/status?status=ONGOING
```

#### Delete Event
```http
DELETE /api/events/{eventId}
```

### Stall APIs

#### Create Stall
```http
POST /api/stalls
Content-Type: application/json

{
  "eventId": "uuid-here",
  "stallCode": "A001",
  "sizeCategory": "MEDIUM",
  "price": 5000.00,
  "locationX": 10.5,
  "locationY": 20.3
}
```

#### Get All Stalls
```http
GET /api/stalls
```

#### Get Stall by ID
```http
GET /api/stalls/{stallId}
```

#### Get Stalls by Event
```http
GET /api/stalls/event/{eventId}
```

#### Get Available Stalls by Event
```http
GET /api/stalls/event/{eventId}/available
```

#### Get Available Stalls by Size
```http
GET /api/stalls/event/{eventId}/available/size/MEDIUM
```

#### Reserve a Stall
```http
POST /api/stalls/{stallId}/reserve?vendorId={vendorUUID}
```

#### Unreserve a Stall
```http
POST /api/stalls/{stallId}/unreserve
```

#### Get Stalls by Vendor
```http
GET /api/stalls/vendor/{vendorId}
```

#### Delete Stall
```http
DELETE /api/stalls/{stallId}
```

## 🧪 Testing the APIs

### Using cURL

**Create an Event**:
```bash
curl -X POST http://localhost:8085/api/events \
  -H "Content-Type: application/json" \
  -d '{
    "year": 2025,
    "name": "Colombo Book Fair 2025",
    "startDate": "2025-09-20",
    "endDate": "2025-09-30",
    "location": "BMICH, Colombo"
  }'
```

**Create a Stall**:
```bash
curl -X POST http://localhost:8085/api/stalls \
  -H "Content-Type: application/json" \
  -d '{
    "eventId": "event-uuid-from-above",
    "stallCode": "A001",
    "sizeCategory": "MEDIUM",
    "price": 5000.00
  }'
```

**Get Available Stalls**:
```bash
curl http://localhost:8085/api/stalls/event/{eventId}/available
```

### Using Postman

1. Import the collection (if available) or create requests manually
2. Base URL: `http://localhost:8085`
3. All endpoints return responses in this format:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... }
}
```

## 🔧 Database Connection Details

### Local Development
- **Host**: localhost
- **Port**: 5432
- **Database**: stall_service_db
- **Username**: postgres
- **Password**: postgres

### Docker Environment
- **Host**: stall-postgres (container name)
- **Port**: 5432 (internal), 5433 (exposed to host)
- **Database**: stall_service_db
- **Username**: postgres
- **Password**: postgres

### Accessing the Database

**Using psql (Docker)**:
```bash
docker exec -it bookfair-stall-postgres psql -U postgres -d stall_service_db
```

**Using psql (Local)**:
```bash
psql -U postgres -d stall_service_db
```

**Common SQL Queries**:
```sql
-- View all tables
\dt

-- View all events
SELECT * FROM events;

-- View all stalls
SELECT * FROM stalls;

-- View available stalls for an event
SELECT * FROM stalls WHERE event_id = 'uuid' AND is_reserved = false;

-- View reserved stalls with vendor info
SELECT s.*, u.first_name, u.last_name 
FROM stalls s 
LEFT JOIN user_snapshot u ON s.reserved_by = u.user_id 
WHERE s.is_reserved = true;
```

## 📂 Project Structure

```
stall-service/
├── src/main/java/com/bookfair/stall_service/
│   ├── entity/           # JPA Entities (Database models)
│   │   ├── Event.java
│   │   ├── EventStatus.java
│   │   ├── Stall.java
│   │   ├── StallSize.java
│   │   └── UserSnapshot.java
│   ├── repository/       # Data access layer
│   │   ├── EventRepository.java
│   │   ├── StallRepository.java
│   │   └── UserSnapshotRepository.java
│   ├── service/          # Business logic
│   │   ├── EventService.java
│   │   └── StallService.java
│   ├── controller/       # REST API controllers
│   │   ├── EventController.java
│   │   └── StallController.java
│   ├── dto/              # Data Transfer Objects
│   │   ├── EventDTO.java
│   │   ├── CreateEventRequest.java
│   │   ├── StallDTO.java
│   │   ├── CreateStallRequest.java
│   │   └── ApiResponse.java
│   └── StallServiceApplication.java
└── src/main/resources/
    └── application.properties
```

## 🔗 Integration with Other Services

### Auth Service Integration
- Stall service stores `reserved_by` as UUID referencing users in Auth Service
- User information is cached in `user_snapshot` table to reduce API calls
- When displaying stall information, cached user data is used

### Reservation Service Integration
- Reservation service can call Stall Service APIs to:
  - Get available stalls for an event
  - Reserve stalls for vendors
  - Check stall availability

### Future Enhancements
- Add API Gateway routes for this service
- Implement JWT authentication
- Add WebSocket for real-time stall availability updates
- Implement stall map visualization APIs

## 🐛 Troubleshooting

### Database Connection Issues
```
Error: org.postgresql.util.PSQLException: Connection refused
```
**Solution**: Make sure PostgreSQL is running and credentials are correct.

### Port Already in Use
```
Error: Web server failed to start. Port 8085 was already in use.
```
**Solution**: Change the port in `application.properties` or kill the process using port 8085.

### Tables Not Created
```
Error: relation "events" does not exist
```
**Solution**: 
- Check `spring.jpa.hibernate.ddl-auto=update` in application.properties
- Or manually create tables using SQL scripts

## 📝 Notes for Team Members

1. **Each microservice has its own database** - Don't access other services' databases directly
2. **Use REST APIs for inter-service communication**
3. **UUID is used for all IDs** - Ensures uniqueness across distributed systems
4. **Hibernate auto-creates tables** - No manual SQL needed for table creation
5. **Docker exposes stall-postgres on port 5433** - To avoid conflicts with other PostgreSQL instances

## 🎯 Next Steps

1. Test all API endpoints using Postman or cURL
2. Add validation for business rules (e.g., stall availability)
3. Implement authentication and authorization
4. Add comprehensive error handling
5. Write unit and integration tests
6. Document API with Swagger/OpenAPI
7. Set up monitoring and logging
