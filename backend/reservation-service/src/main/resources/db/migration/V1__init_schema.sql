-- Initial schema for reservation-service
-- Captures reservations plus supporting snapshot tables shared with other services

CREATE TABLE IF NOT EXISTS user_snapshot (
    user_id UUID PRIMARY KEY,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    email VARCHAR(255),
    role VARCHAR(50),
    status VARCHAR(50),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS stall_snapshot (
    stall_id UUID PRIMARY KEY,
    event_id UUID,
    stall_code VARCHAR(10),
    size_category VARCHAR(20),
    price NUMERIC(10, 2),
    location_x DOUBLE PRECISION,
    location_y DOUBLE PRECISION,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS reservations (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    stall_id UUID NOT NULL,
    event_id UUID NOT NULL,
    reservation_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    confirmation_code VARCHAR(255),
    qr_code_url VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED'))
);

CREATE INDEX IF NOT EXISTS idx_reservations_user_id ON reservations (user_id);
CREATE INDEX IF NOT EXISTS idx_reservations_stall_id ON reservations (stall_id);
CREATE INDEX IF NOT EXISTS idx_reservations_event_id ON reservations (event_id);

-- Enforce at most one active reservation per stall (PENDING or CONFIRMED)
CREATE UNIQUE INDEX IF NOT EXISTS ux_reservations_active_stall
    ON reservations (stall_id)
    WHERE status IN ('PENDING', 'CONFIRMED');
