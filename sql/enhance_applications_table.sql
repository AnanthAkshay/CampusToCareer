-- ============================================================
-- Enhance Applications Table
-- ============================================================

USE rit_placement;

-- Add applied_at timestamp to applications table
ALTER TABLE applications
ADD COLUMN applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP AFTER status;
