-- Add mobile_no column to user_snapshot table
ALTER TABLE user_snapshot ADD COLUMN IF NOT EXISTS mobile_no VARCHAR(255);