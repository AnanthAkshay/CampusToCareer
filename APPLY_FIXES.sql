-- ============================================================
-- APPLY ALL FIXES - Module 3 OTP Authentication
-- Run this script to fix existing database
-- ============================================================

USE rit_placement;

-- Fix #1: Add email column to users table (if not exists)
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS email VARCHAR(150);

-- Fix #2: Populate email for existing users
UPDATE users 
SET email = CONCAT(LOWER(usn), '@gmail.com')
WHERE email IS NULL OR email = '';

-- Verification: Show updated users
SELECT user_id, usn, name, email, role, is_active 
FROM users 
LIMIT 10;

-- Success message
SELECT 'Database fixes applied successfully!' AS Status;
