-- Initial schema for stall-service
-- Contains events, stalls, and user_snapshot tables that back the domain entities

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'event_status') THEN
        CREATE TYPE event_status AS ENUM ('UPCOMING', 'ONGOING', 'ENDED');
    END IF;
END$$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'stall_size') THEN
        CREATE TYPE stall_size AS ENUM ('SMALL', 'MEDIUM', 'LARGE');
    END IF;
END$$;

CREATE TABLE IF NOT EXISTS events (
    id UUID PRIMARY KEY,
    year INTEGER NOT NULL CHECK (year >= 0),
    name VARCHAR(100) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    location VARCHAR(255),
    status event_status NOT NULL DEFAULT 'UPCOMING',
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK (start_date <= end_date)
);

CREATE TABLE IF NOT EXISTS stalls (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    stall_code VARCHAR(10) NOT NULL,
    size_category stall_size NOT NULL,
    price NUMERIC(10, 2),
    location_x DOUBLE PRECISION,
    location_y DOUBLE PRECISION,
    is_reserved BOOLEAN NOT NULL DEFAULT FALSE,
    reserved_by UUID,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_stalls_stall_code ON stalls (stall_code);
CREATE INDEX IF NOT EXISTS idx_stalls_event_id ON stalls (event_id);

CREATE TABLE IF NOT EXISTS user_snapshot (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    email VARCHAR(255),
    role VARCHAR(50),
    status VARCHAR(50),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
