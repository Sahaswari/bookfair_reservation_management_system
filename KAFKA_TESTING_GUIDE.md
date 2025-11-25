# Quick Reference: Kafka Event Testing

## Service Startup

### Start All Services
```powershell
cd "E:\8th sem\software architecture project\bookfair_reservation_management_system"
docker-compose up -d
```

### Check Service Health
```bash
# Reservation Service
curl http://localhost:8086/actuator/health

# Notification Service
curl http://localhost:8083/actuator/health

# Kafka (check logs)
docker logs bookfair-kafka
```

### View Logs
```powershell
# Reservation Service
docker logs -f bookfair-reservation-service

# Notification Service
docker logs -f bookfair-notification-service

# Kafka
docker logs -f bookfair-kafka
```

## Kafka Monitoring

### Access Kafka UI
http://localhost:19000

### Check Topics
```bash
docker exec bookfair-kafka kafka-topics --list --bootstrap-server localhost:9092
```

### View Topic Messages
```bash
docker exec bookfair-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic reservation-events \
  --from-beginning
```

## Database Verification

### Connect to Reservation Database
```bash
# Using psql in Docker
docker exec -it bookfair-reservation-db psql -U postgres -d reservation_service_db

# Queries
SELECT COUNT(*) FROM reservations;
SELECT COUNT(*) FROM user_snapshot;
SELECT COUNT(*) FROM stall_snapshot;
```

### Connect to Notification Database
```bash
# Using psql in Docker
docker exec -it bookfair-notification-db psql -U postgres -d notification_service_db

# Queries
SELECT COUNT(*) FROM reservation_snapshot;
SELECT * FROM reservation_snapshot ORDER BY updated_at DESC LIMIT 5;
```

### View All Reservations with Snapshots
```sql
-- In reservation_service_db
SELECT 
    r.id,
    r.status,
    r.confirmation_code,
    u.first_name,
    u.last_name,
    s.stall_code
FROM reservations r
LEFT JOIN user_snapshot u ON r.user_id = u.user_id
LEFT JOIN stall_snapshot s ON r.stall_id = s.stall_id
ORDER BY r.created_at DESC;
```

### View Synchronized Snapshots
```sql
-- In notification_service_db
SELECT 
    id,
    reservation_id,
    status,
    user_email,
    stall_code,
    updated_at
FROM reservation_snapshot
ORDER BY updated_at DESC
LIMIT 10;
```

## API Testing

### 1. Create a Reservation
```bash
curl -X POST http://localhost:8086/api/reservations \
  -H "Content-Type: application/json" \
  -d '{
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
  }'
```

**Expected Response** (201 Created):
```json
{
  "id": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "stallId": "660e8400-e29b-41d4-a716-446655440001",
  "status": "PENDING",
  "confirmationCode": "RES-xxxxxxxx",
  "createdAt": "2025-12-01T10:00:00Z"
}
```

### 2. Get All Reservations
```bash
curl -X GET http://localhost:8086/api/reservations
```

### 3. Get Reservation by ID
```bash
curl -X GET http://localhost:8086/api/reservations/{reservation-id}
```

### 4. Update Reservation Status
```bash
RESERVATION_ID="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"

curl -X PUT http://localhost:8086/api/reservations/$RESERVATION_ID/status \
  -H "Content-Type: application/json" \
  -d '{"status": "CONFIRMED"}'
```

**Expected Response** (200 OK):
```json
{
  "id": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "status": "CONFIRMED",
  "qrCodeUrl": "/api/reservations/xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx/qrcode",
  "updatedAt": "2025-12-01T10:05:00Z"
}
```

### 5. Get Reservations by Status
```bash
curl -X GET http://localhost:8086/api/reservations/status/CONFIRMED
```

### 6. Get Reservations by User
```bash
curl -X GET http://localhost:8086/api/reservations/user/550e8400-e29b-41d4-a716-446655440000
```

### 7. Get Reservations by Event
```bash
curl -X GET http://localhost:8086/api/reservations/event/770e8400-e29b-41d4-a716-446655440002
```

## Event Flow Verification Steps

### Step 1: Verify Event Published
After creating/updating a reservation:
1. Go to Kafka UI: http://localhost:19000
2. Navigate to "Topics" → "reservation-events"
3. View messages - should see the event

**Event Structure**:
```json
{
  "eventId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "eventType": "RESERVATION_CREATED",
  "occurredAt": "2025-12-01T10:00:00.000000Z",
  "reservationId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "stallId": "660e8400-e29b-41d4-a716-446655440001",
  "status": "PENDING",
  "confirmationCode": "RES-xxxxxxxx",
  "userFirstName": "John",
  "userLastName": "Doe",
  "userEmail": "john@example.com",
  "stallCode": "STALL-001"
}
```

### Step 2: Verify Event Consumed
After event is published:
1. Connect to notification database
2. Run query: `SELECT * FROM reservation_snapshot WHERE reservation_id = 'xxx'`
3. Should have record with matching reservation_id
4. All snapshot data should be present

### Step 3: Verify Status Change Event
After updating reservation status:
1. Check Kafka UI again
2. Should see new event with `RESERVATION_CONFIRMED` or `RESERVATION_CANCELLED` type
3. Check notification database
4. `reservation_snapshot.status` should match the new status
5. `updated_at` timestamp should be recent

## Troubleshooting

### Issue: Services Won't Start
**Solution**: 
```bash
# Check Docker logs
docker logs bookfair-reservation-service
docker logs bookfair-kafka

# Verify network exists
docker network ls | grep bookfair

# Rebuild images
docker-compose build --no-cache
docker-compose up -d
```

### Issue: Kafka Connection Failed
**Solution**:
```bash
# Check if Kafka is running
docker ps | grep kafka

# Check Kafka health
docker exec bookfair-kafka kafka-broker-api-versions --bootstrap-server localhost:9092

# Restart Kafka
docker-compose restart kafka
```

### Issue: Database Connection Failed
**Solution**:
```bash
# Check if databases are running
docker ps | grep postgres

# Check database logs
docker logs bookfair-reservation-db
docker logs bookfair-notification-db

# Verify connection
docker exec -it bookfair-reservation-db psql -U postgres -c "SELECT 1"
```

### Issue: Event Not Consumed
**Solution**:
```bash
# Check consumer logs
docker logs bookfair-notification-service | grep -i "kafka\|listener\|error"

# Verify topic exists
docker exec bookfair-kafka kafka-topics --list --bootstrap-server localhost:9092

# Check consumer group
docker exec bookfair-kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --list

# Describe consumer group
docker exec bookfair-kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --describe \
  --group notification-service-reservation-sync
```

### Issue: Messages in Dead Letter Topic
**Solution**:
```bash
# Check DLT messages
docker exec bookfair-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic reservation-events.DLT \
  --from-beginning

# Check error logs
docker logs bookfair-notification-service | grep -i "error\|exception"
```

## Performance Monitoring

### Check Kafka Consumer Lag
```bash
docker exec bookfair-kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --describe \
  --group notification-service-reservation-sync
```

### Monitor Message Throughput
```bash
# Use Kafka UI to visualize metrics
# http://localhost:19000 → Consumer Groups → View lag and throughput
```

### Database Query Performance
```sql
-- Check reservation query performance
EXPLAIN ANALYZE
SELECT COUNT(*) FROM reservations WHERE status = 'CONFIRMED';

-- Check snapshot sync performance
EXPLAIN ANALYZE
SELECT COUNT(*) FROM reservation_snapshot WHERE updated_at > NOW() - INTERVAL '5 minutes';
```

## Cleanup Commands

### Stop Services (Keep Volumes)
```bash
docker-compose stop
```

### Stop and Remove Containers
```bash
docker-compose down
```

### Remove Everything (Including Volumes)
```bash
docker-compose down -v
```

### Rebuild from Scratch
```bash
docker-compose down -v
docker-compose build --no-cache
docker-compose up -d
```

### View Container Resource Usage
```bash
docker stats bookfair-reservation-service bookfair-notification-service bookfair-kafka
```

## Sample Test Data

### User ID (use any UUID)
```
550e8400-e29b-41d4-a716-446655440000
```

### Stall ID (use any UUID)
```
660e8400-e29b-41d4-a716-446655440001
```

### Event ID (use any UUID)
```
770e8400-e29b-41d4-a716-446655440002
```

### Generate Random UUID (PowerShell)
```powershell
[guid]::NewGuid()
```

### Generate Random UUID (Bash)
```bash
python3 -c "import uuid; print(uuid.uuid4())"
```

## Key Endpoints Summary

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | /api/reservations | Get all reservations |
| POST | /api/reservations | Create new reservation |
| GET | /api/reservations/{id} | Get by ID |
| PUT | /api/reservations/{id}/status | Update status |
| GET | /api/reservations/status/{status} | Filter by status |
| GET | /api/reservations/user/{userId} | Get user's reservations |
| GET | /api/reservations/event/{eventId} | Get event's reservations |

## Success Criteria Checklist

- [ ] Docker Compose starts without errors
- [ ] All services are healthy (health check passing)
- [ ] Can create reservation via API
- [ ] Reservation saved in reservation_service_db
- [ ] Kafka event appears in reservation-events topic
- [ ] ReservationSnapshot created in notification_service_db
- [ ] Status update triggers new event
- [ ] Event is consumed and snapshot updated
- [ ] No errors in service logs
- [ ] Database data is consistent across services

