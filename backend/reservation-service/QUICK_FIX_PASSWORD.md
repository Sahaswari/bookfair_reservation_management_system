# Quick Fix: PostgreSQL Password Update

## Current Error
```
FATAL: password authentication failed for user "postgres"
```

## What to Do

### Step 1: Find Your PostgreSQL Password
Open **pgAdmin 4** and check the connection settings for the "postgres" user. Your password is NOT "postgres" - it's whatever you set during PostgreSQL installation.

### Step 2: Update the Password in application.properties

Edit this file:
```
backend/reservation-service/src/main/resources/application.properties
```

Find this line:
```properties
spring.datasource.password=postgres
```

Change it to:
```properties
spring.datasource.password=YOUR_ACTUAL_PASSWORD
```

Example (if your password is "mydbpass123"):
```properties
spring.datasource.password=mydbpass123
```

### Step 3: Restart the Application

Stop the current running application (Ctrl+C in terminal), then run:
```bash
cd "E:\8th sem\software architecture project\bookfair_reservation_management_system\backend\reservation-service"
mvn spring-boot:run
```

### Step 4: Verify Success

You should see in the logs:
```
HikariPool-1 - Start completed.
```

And application should start successfully on port 8086:
```
Tomcat started on port 8086 (http) with context path '/'
```

### Step 5: Test API

All 3 endpoints are now ready to use:
- GET http://localhost:8086/api/reservations
- POST http://localhost:8086/api/reservations  
- PUT http://localhost:8086/api/reservations/{id}/status

---

## All Fixes Applied ✅

1. ✅ compose.yaml - Fixed database credentials
2. ✅ ReservationServiceApplication.java - Added @EntityScan and component scanning
3. ✅ application.properties - Set validate mode, disabled Docker Compose
4. ⏳ PostgreSQL password - **YOU NEED TO UPDATE THIS**

Once you provide the correct password, your database will connect successfully!
