-- ============================================================
-- Add Profile Fields to Students Table
-- ============================================================

USE rit_placement;

-- Add profile fields to students table
ALTER TABLE students
ADD COLUMN skills TEXT AFTER current_sem,
ADD COLUMN projects TEXT AFTER skills,
ADD COLUMN experience TEXT AFTER projects,
ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER experience;
