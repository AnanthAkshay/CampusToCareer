-- ============================================================
-- Fix COMPANY Role in Database Schema
-- ============================================================
-- This migration adds COMPANY role to the existing ENUM
-- Run this ONLY if you already have the database created
-- ============================================================

USE rit_placement;

-- Add COMPANY role to existing ENUM
ALTER TABLE users 
MODIFY COLUMN role ENUM('STUDENT','PROCTOR','COORDINATOR','FACULTY','ADMIN','COMPANY') NOT NULL;

-- Verify the change
DESCRIBE users;

-- Check current role distribution
SELECT role, COUNT(*) as count 
FROM users 
GROUP BY role 
ORDER BY role;

-- ============================================================
-- NOTES
-- ============================================================
-- 1. This is safe to run on existing data
-- 2. No data will be lost
-- 3. COMPANY role is now available for new users
-- 4. Existing users remain unchanged
-- ============================================================
