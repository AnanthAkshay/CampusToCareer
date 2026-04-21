-- ============================================================
-- FIX: Add email column to users table
-- This is required for OTP authentication to work properly
-- ============================================================

USE rit_placement;

-- Add email column to users table if it doesn't exist
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS email VARCHAR(150);

-- Update existing users with default email (usn@gmail.com)
-- This ensures all existing users have an email address
UPDATE users 
SET email = CONCAT(LOWER(usn), '@gmail.com')
WHERE email IS NULL OR email = '';

-- Optional: Make email column NOT NULL after populating data
-- Uncomment the line below if you want to enforce email requirement
-- ALTER TABLE users MODIFY COLUMN email VARCHAR(150) NOT NULL;

-- Verify the changes
SELECT user_id, usn, name, email, role FROM users LIMIT 10;
