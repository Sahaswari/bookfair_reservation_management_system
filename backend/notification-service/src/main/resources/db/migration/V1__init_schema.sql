-- Initial schema for notification-service
-- Tracks outbound notifications plus the cached reservation and user data required to render messages

CREATE TABLE IF NOT EXISTS user_snapshot (
    user_id UUID PRIMARY KEY,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    email VARCHAR(255),
    phone VARCHAR(30),
    role VARCHAR(50),
    status VARCHAR(50),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS reservation_snapshot (
    reservation_id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    stall_id UUID,
    event_id UUID,
    status VARCHAR(20),
    reservation_date DATE,
    confirmation_code VARCHAR(255),
    qr_code_url VARCHAR(255),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    reservation_id UUID,
    channel VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    template_code VARCHAR(50),
    subject VARCHAR(255),
    message TEXT NOT NULL,
    metadata JSONB,
    retry_count INTEGER NOT NULL DEFAULT 0,
    max_retries INTEGER NOT NULL DEFAULT 3,
    scheduled_for TIMESTAMP WITHOUT TIME ZONE,
    sent_at TIMESTAMP WITHOUT TIME ZONE,
    error_reason TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK (status IN ('PENDING', 'SENT', 'FAILED', 'CANCELLED')),
    CHECK (channel IN ('EMAIL', 'SMS', 'IN_APP'))
);

CREATE INDEX IF NOT EXISTS idx_notifications_user_status ON notifications (user_id, status);
CREATE INDEX IF NOT EXISTS idx_notifications_reservation ON notifications (reservation_id);
CREATE INDEX IF NOT EXISTS idx_notifications_scheduled_for ON notifications (scheduled_for) WHERE scheduled_for IS NOT NULL;
