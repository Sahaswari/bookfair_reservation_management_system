-- Add reservation state columns to stall_snapshot so snapshots mirror stall availability
ALTER TABLE stall_snapshot
    ADD COLUMN IF NOT EXISTS is_reserved BOOLEAN;

ALTER TABLE stall_snapshot
    ADD COLUMN IF NOT EXISTS reserved_by UUID;

UPDATE stall_snapshot SET is_reserved = FALSE WHERE is_reserved IS NULL;

ALTER TABLE stall_snapshot
    ALTER COLUMN is_reserved SET DEFAULT FALSE;

ALTER TABLE stall_snapshot
    ALTER COLUMN is_reserved SET NOT NULL;
