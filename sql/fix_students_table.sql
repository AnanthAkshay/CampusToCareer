-- ============================================================
-- FIX STUDENTS TABLE - Add Missing Columns
-- ============================================================
-- This script adds the missing columns to the students table
-- that are required for the profile functionality
-- ============================================================

USE rit_placement;

-- Add skills column if it doesn't exist
ALTER TABLE students 
ADD COLUMN IF NOT EXISTS skills TEXT;

-- Add projects column if it doesn't exist
ALTER TABLE students 
ADD COLUMN IF NOT EXISTS projects TEXT;

-- Add experience column if it doesn't exist
ALTER TABLE students 
ADD COLUMN IF NOT EXISTS experience TEXT;

-- Add updated_at column if it doesn't exist
ALTER TABLE students 
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- Verify the changes
DESCRIBE students;

-- Show sample data
SELECT * FROM students LIMIT 5;
