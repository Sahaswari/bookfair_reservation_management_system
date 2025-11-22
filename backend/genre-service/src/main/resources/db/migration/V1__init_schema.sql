-- Initial schema for genre-service
-- Provides canonical storage for genres plus cached user metadata

CREATE TABLE IF NOT EXISTS user_snapshot (
    user_id UUID PRIMARY KEY,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    email VARCHAR(255),
    role VARCHAR(50),
    status VARCHAR(50),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS genres (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    display_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by UUID,
    updated_by UUID,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK (char_length(code) > 0)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_genres_code ON genres (code);
CREATE UNIQUE INDEX IF NOT EXISTS ux_genres_name ON genres (name);
