-- ============================================================
-- Multi-Role Authentication System Upgrade
-- ============================================================
-- This script upgrades the existing role system to support
-- STUDENT, COORDINATOR, and COMPANY roles with strict access control.
-- ============================================================

USE rit_placement;

-- Step 1: Check if role column needs modification
-- The existing schema already has role as ENUM, but we need to ensure
-- it supports the three required roles

-- Step 2: Update existing users to have proper roles
-- All existing users without explicit role should default to STUDENT
UPDATE users 
SET role = 'STUDENT' 
WHERE role IS NULL OR role = '';

-- Step 3: Insert sample COORDINATOR users
-- Password: 'coord123' (BCrypt hash)
-- NOTE: Default credentials MUST be changed in production
-- Use strong passwords and proper BCrypt hashing
INSERT INTO users (usn, name, password_hash, role, is_active) 
VALUES 
    ('COORD001', 'Placement Coordinator', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'COORDINATOR', TRUE),
    ('COORD002', 'Training Coordinator', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'COORDINATOR', TRUE)
ON DUPLICATE KEY UPDATE 
    role = 'COORDINATOR',
    is_active = TRUE;

-- Step 4: Verify role distribution
SELECT 
    role,
    COUNT(*) as user_count,
    SUM(CASE WHEN is_active = TRUE THEN 1 ELSE 0 END) as active_count
FROM users
GROUP BY role
ORDER BY role;

-- Step 5: Create indexes for performance (if not exists)
-- This helps with role-based queries
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_active ON users(is_active);

-- ============================================================
-- VERIFICATION QUERIES
-- ============================================================

-- Check all coordinators
SELECT user_id, usn, name, role, is_active 
FROM users 
WHERE role = 'COORDINATOR';

-- Check all students
SELECT COUNT(*) as student_count 
FROM users 
WHERE role = 'STUDENT';

-- Check for any users without roles
SELECT user_id, usn, name, role 
FROM users 
WHERE role IS NULL OR role = '';

-- ============================================================
-- NOTES FOR PRODUCTION
-- ============================================================
-- 1. Change default coordinator passwords immediately after deployment
-- 2. Use proper BCrypt hashing for all passwords
-- 3. Monitor role distribution regularly
-- 4. Ensure all new users get appropriate roles on creation
-- 5. COMPANY role users will be added later via admin interface
-- ============================================================
