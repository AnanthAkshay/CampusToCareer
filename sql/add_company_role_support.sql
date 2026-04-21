-- SQL Migration: Add Company Role Support
-- This script adds support for COMPANY role users in the placement system

-- 1. Add company_id column to users table (if not exists)
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS company_id INT NULL,
ADD CONSTRAINT fk_users_company 
    FOREIGN KEY (company_id) REFERENCES companies(company_id) 
    ON DELETE SET NULL;

-- 2. Add index for better query performance
CREATE INDEX IF NOT EXISTS idx_users_company_id ON users(company_id);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

-- 3. Example: Create a company user account
-- Replace with actual company details

-- First, ensure the company exists in companies table
-- INSERT INTO companies (company_name, description, company_type) 
-- VALUES ('TechCorp Solutions', 'Leading IT services company', 'IT Services');

-- Then create a user account for the company
-- INSERT INTO users (usn, name, email, password_hash, role, is_active, company_id)
-- VALUES (
--     'COMP001',                          -- USN for company login
--     'TechCorp Solutions',               -- Company name
--     'hr@techcorp.com',                  -- Company email for OTP
--     'temp_password',                    -- Temporary password (will use OTP)
--     'COMPANY',                          -- Role
--     TRUE,                               -- Active
--     (SELECT company_id FROM companies WHERE company_name = 'TechCorp Solutions')
-- );

-- 4. Verify the setup
-- SELECT u.user_id, u.usn, u.name, u.role, u.company_id, c.company_name
-- FROM users u
-- LEFT JOIN companies c ON u.company_id = c.company_id
-- WHERE u.role = 'COMPANY';

-- 5. Grant necessary permissions (if using MySQL user permissions)
-- GRANT SELECT, INSERT, UPDATE ON placement_db.applications TO 'company_user'@'localhost';
-- GRANT SELECT ON placement_db.job_postings TO 'company_user'@'localhost';
-- GRANT SELECT ON placement_db.companies TO 'company_user'@'localhost';

COMMIT;
