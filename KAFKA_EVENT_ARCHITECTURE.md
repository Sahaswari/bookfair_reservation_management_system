# Kafka Event-Driven Architecture Implementation

## Overview
Successfully implemented Kafka-based event-driven microservice architecture for the Book Fair Reservation Management System. Reservation service now publishes lifecycle events that are consumed by notification service for data synchronization.

## Changes Implemented

### 1. Event Model Classes

#### ReservationLifecycleEvent.java (Both Services)
**Location**: 
- `backend/reservation-service/src/main/java/com/bookfair/reservation_service/messaging/ReservationLifecycleEvent.java`
- `backend/notification-service/src/main/java/com/bookfair/notification_service/messaging/ReservationLifecycleEvent.java`

**Purpose**: Kafka event DTO published when reservations change (create/confirm/cancel)

**Fields**:
- Event metadata: `eventId`, `eventType` (RESERVATION_CREATED|CONFIRMED|CANCELLED), `occurredAt`
- Reservation data: `reservationId`, `userId`, `stallId`, `bookFairEventId`, `reservationDate`, `status`, `confirmationCode`, `qrCodeUrl`
- User snapshot: `userFirstName`, `userLastName`, `userEmail`, `userRole`, `userStatus`
- Stall snapshot: `stallCode`, `sizeCategory`, `price` (BigDecimal), `locationX`, `locationY` (Float)

### 2. Producer (Reservation Service)

#### ReservationEventProducer.java
**Location**: `backend/reservation-service/src/main/java/com/bookfair/reservation_service/messaging/ReservationEventProducer.java`

**Features**:
- Uses `@Qualifier` to inject the correct KafkaTemplate bean
- Publishes events to configurable topic (`app.kafka.reservation-events-topic`)
- Async event publishing with completion callbacks
- Graceful error handling with logging
- Respects `app.kafka.enabled` configuration flag

**Method**: `publishReservationEvent(ReservationLifecycleEvent event)`

### 3. Consumer (Notification Service)

#### ReservationEventListener.java
**Location**: `backend/notification-service/src/main/java/com/bookfair/notification_service/messaging/ReservationEventListener.java`

**Features**:
- Listens to `reservation-events` topic
- Updates `ReservationSnapshot` table when events arrive
- Uses containerFactory `kafkaListenerContainerFactory` for proper deserialization
- Handles errors gracefully without stopping message consumption
- Configurable consumer group ID

**Method**: `handleReservationEvent(ReservationLifecycleEvent event)`

### 4. Entity Models

#### ReservationSnapshot.java (Notification Service)
**Location**: `backend/notification-service/src/main/java/com/bookfair/notification_service/entity/ReservationSnapshot.java`

**Purpose**: Stores snapshot of reservation data for notification generation

**Fields**:
- `id` (UUID PK) - Primary key
- `reservationId` (UUID, unique) - Reference to source reservation
- Complete reservation and snapshot data matching the event
- `updatedAt` timestamp for tracking sync time

### 5. Service Layer Integration

#### ReservationService.java (Enhanced)
**Location**: `backend/reservation-service/src/main/java/com/bookfair/reservation_service/service/ReservationService.java`

**Changes**:
- Added `ReservationEventProducer` dependency
- Injected producer via constructor with `@RequiredArgsConstructor`
- Integrated event publishing in:
  - `createReservation()` → Publishes `RESERVATION_CREATED` event
  - `updateReservationStatus()` → Publishes `RESERVATION_CONFIRMED` or `RESERVATION_CANCELLED` event
- Added helper methods:
  - `publishReservationCreatedEvent()` - Builds and publishes creation events
  - `publishReservationStatusChangedEvent()` - Builds and publishes status change events
- Events include complete snapshot data from request/repository

### 6. Kafka Configuration

#### ReservationEventKafkaConfig.java (Reservation Service)
**Location**: `backend/reservation-service/src/main/java/com/bookfair/reservation_service/config/ReservationEventKafkaConfig.java`

**Purpose**: Producer configuration for event publishing

**Beans**:
- `reservationEventProducerFactory` - Configures String/ReservationLifecycleEvent serialization
- `reservationEventKafkaTemplate` - KafkaTemplate for sending events
- `reservationEventsTopic` - Topic creation bean (3 partitions, 1 replica, 7 days retention)

#### ReservationKafkaConfig.java (Notification Service)
**Location**: `backend/notification-service/src/main/java/com/bookfair/notification_service/config/ReservationKafkaConfig.java`

**Purpose**: Consumer configuration for event consumption

**Beans**:
- `reservationEventConsumerFactory` - JsonDeserializer for ReservationLifecycleEvent
- `kafkaListenerContainerFactory` - Container factory with:
  - Concurrency: 3
  - Error handling with dead letter topics (.DLT suffix)
  - Exponential backoff retry (2 max attempts, 1s initial, 2x multiplier, 10s max)
  - Manual acknowledgment (RECORD mode)
- `reservationEventsTopic` & `reservationEventsDltTopic` - Topic creation beans

### 7. Repository

#### ReservationSnapshotRepository.java (Notification Service)
**Location**: `backend/notification-service/src/main/java/com/bookfair/notification_service/repository/ReservationSnapshotRepository.java`

**Methods**:
- `findById(UUID)` - Get snapshot by ID
- `findByUserId(UUID)` - Query by user
- `findByEventId(UUID)` - Query by event
- `findByStatus(String)` - Query by status

### 8. Database Migrations

#### V1__init_schema.sql (Updated - Notification Service)
**Location**: `backend/notification-service/src/main/resources/db/migration/V1__init_schema.sql`

**Changes**:
- Updated `reservation_snapshot` table with complete field mapping:
  - Added `id` UUID PK for JPA compatibility
  - Added all user snapshot fields (first_name, last_name, email, role, status)
  - Added all stall snapshot fields (code, size_category, price, location_x, location_y)
  - Added `qr_code_url` and `updated_at` timestamp
- Maintains 7-day retention, 1 replica configuration

### 9. Application Configuration

#### application.properties (Reservation Service)
**Updates**:
- Added `app.kafka.reservation-events-topic=reservation-events`
- Updated Kafka bootstrap servers: `kafka:9092` (Docker Compose)
- Added producer serialization: `JsonSerializer` for values
- Updated database URL to use dedicated container name

#### application.properties (Notification Service)
**Updates**:
- Added `app.kafka.reservation-events-topic=reservation-events`
- Updated database URL to use dedicated container name: `notification-db:5432/notification_service_db`
- Updated Kafka bootstrap servers: `kafka:9092` (Docker Compose)

### 10. Docker Compose Configuration

#### docker-compose.yml (Updated)
**Reservation Service Changes**:
- Fixed port mapping: `8086:8086` (was 8084:8086)
- Fixed PORT env variable: `SERVER_PORT=8086` (was 8084)
- Fixed database name: `reservation_service_db` (was `reservation_db`)
- Added Kafka dependency: `kafka` service with health check
- Added Kafka env variables including `APP_KAFKA_RESERVATION_EVENTS_TOPIC`

**Notification Service Changes**:
- Fixed database name: `notification_service_db` (was `notification_db`)
- Added Kafka dependency: `kafka` service with health check
- Added Kafka env variable: `APP_KAFKA_RESERVATION_EVENTS_TOPIC=reservation-events`

## Event Flow

### Reservation Creation Flow
1. User creates reservation via POST `/api/reservations`
2. `ReservationService.createReservation()` saves to database
3. Calls `publishReservationCreatedEvent()` which:
   - Gathers user and stall snapshot data from request/repositories
   - Builds `ReservationLifecycleEvent` with type `RESERVATION_CREATED`
   - Calls `ReservationEventProducer.publishReservationEvent()`
4. Event published to Kafka `reservation-events` topic
5. `ReservationEventListener` in notification-service consumes event
6. Updates/creates `ReservationSnapshot` record in `notification_db`
7. Notification service can now generate notifications using snapshot data

### Reservation Status Change Flow
1. Admin updates reservation status via PUT `/api/reservations/{id}/status`
2. `ReservationService.updateReservationStatus()` updates database
3. If status is CONFIRMED, generates QR code URL
4. Calls `publishReservationStatusChangedEvent()` which:
   - Fetches latest user and stall snapshots from repositories
   - Builds `ReservationLifecycleEvent` with type `RESERVATION_CONFIRMED` or `RESERVATION_CANCELLED`
   - Publishes to Kafka
5. Consumer updates `ReservationSnapshot` record
6. Notification service detects status change and sends appropriate notification

## Technology Stack

### Kafka Infrastructure
- **Apache Kafka**: 7.6.1 on port 9092
- **Zookeeper**: 7.6.1 for cluster coordination
- **Schema Registry**: 7.6.1 for schema management
- **Kafka UI**: 19000 for monitoring and debugging

### Serialization
- **Keys**: String serialization
- **Values**: JSON serialization/deserialization
- **Trust all packages** enabled for trusted JSON deserialization

### Error Handling
- **Retry Policy**: Exponential backoff (2 attempts, 1s initial, 2x multiplier, 10s max)
- **Dead Letter Topic**: `.DLT` suffix for failed messages (14 days retention)
- **Missing Topics**: Non-fatal (missingTopicsFatal = false)

## Configuration Properties

### Reservation Service
```properties
app.kafka.enabled=true
app.kafka.reservation-events-topic=reservation-events
spring.kafka.bootstrap-servers=kafka:9092
spring.kafka.producer.key-serializer=StringSerializer
spring.kafka.producer.value-serializer=JsonSerializer
```

### Notification Service
```properties
app.kafka.enabled=true
app.kafka.reservation-events-topic=reservation-events
spring.kafka.bootstrap-servers=kafka:9092
spring.kafka.consumer.auto-offset-reset=latest
spring.kafka.listener.ack-mode=record
```

## Testing Event Flow

### 1. Start Docker Compose
```bash
docker-compose up -d
```

### 2. Verify Services are Ready
- Reservation Service: http://localhost:8086/actuator/health
- Notification Service: http://localhost:8083/actuator/health
- Kafka UI: http://localhost:19000

### 3. Create a Reservation
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

### 4. Verify Event Published
- Check Kafka UI (localhost:19000) → Topics → reservation-events
- Should see event message with `RESERVATION_CREATED` type

### 5. Verify Event Consumed
- Query notification database: `SELECT * FROM reservation_snapshot`
- Should have record with matching reservation_id and all snapshot data

### 6. Update Reservation Status
```bash
curl -X PUT http://localhost:8086/api/reservations/{reservation-id}/status \
  -H "Content-Type: application/json" \
  -d '{"status": "CONFIRMED"}'
```

### 7. Verify Status Change Event
- Check Kafka UI → reservation-events topic
- Should see new event with `RESERVATION_CONFIRMED` type
- Check notification database → reservation_snapshot.status should be CONFIRMED

## Database Schema

### Reservation Service (reservation_service_db)
- **reservations**: Core reservation records
- **user_snapshot**: User data captured at reservation time
- **stall_snapshot**: Stall data captured at reservation time

### Notification Service (notification_service_db)
- **reservation_snapshot**: Synchronized from Kafka events
- **user_snapshot**: User data from user-events Kafka topic
- **notifications**: Generated notifications for users

## Files Modified Summary

| File | Change Type | Purpose |
|------|------------|---------|
| ReservationLifecycleEvent.java (x2) | Created | Event DTO for Kafka |
| ReservationEventProducer.java | Created | Event publisher |
| ReservationEventListener.java | Created | Event consumer |
| ReservationService.java | Enhanced | Integrated event publishing |
| ReservationKafkaConfig.java | Created | Consumer configuration |
| ReservationEventKafkaConfig.java | Created | Producer configuration |
| ReservationSnapshotRepository.java | Created | Data access for snapshots |
| ReservationSnapshot.java | Created | Snapshot entity |
| application.properties (x2) | Updated | Kafka configuration |
| docker-compose.yml | Updated | Service & topic configuration |
| V1__init_schema.sql | Updated | Database schema |

## Next Steps

1. **User Event Integration**: Create `UserEventPublisher` in auth-service
2. **Stall Event Integration**: Create `StallEventPublisher` in stall-service
3. **Notification Generation**: Create notification records based on events
4. **Email Notifications**: Integrate email service for event-driven notifications
5. **Monitoring**: Set up alerts for Kafka consumer lag and dead letter topics
6. **Testing**: Complete end-to-end integration testing with all services

## Success Criteria

✅ ReservationLifecycleEvent class created with proper structure
✅ ReservationEventProducer integrated into ReservationService
✅ Events published on reservation creation and status changes
✅ ReservationEventListener consumes events in notification-service
✅ ReservationSnapshot table updated via Kafka events
✅ Kafka topics created with proper retention and error handling
✅ Docker Compose properly configured for event flow
✅ Database migrations updated with new columns
✅ All configuration properties aligned with Docker Compose
✅ Dead letter topic support for failed messages

