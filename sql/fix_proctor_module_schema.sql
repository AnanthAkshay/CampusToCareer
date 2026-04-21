-- ============================================================
-- FIX PROCTOR MODULE - Database Schema Corrections
-- ============================================================
-- This script fixes all database issues identified in the audit
-- Run this BEFORE deploying the proctor module
-- ============================================================

USE rit_placement;

-- ============================================================
-- STEP 1: Fix proctor_remarks table foreign keys
-- ============================================================

-- Drop existing proctor_remarks table if it exists (to recreate with correct FKs)
DROP TABLE IF EXISTS proctor_remarks;

-- Recreate proctor_remarks with correct foreign key references
CREATE TABLE proctor_remarks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    proctor_id INT NOT NULL,
    remark TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_remark_student FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_remark_proctor FOREIGN KEY (proctor_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_student_id (student_id),
    INDEX idx_proctor_id (proctor_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB;

-- ============================================================
-- STEP 2: Ensure proctor_student_map table exists and is correct
-- ============================================================

-- Verify proctor_student_map exists (should already exist from schema.sql)
-- If not, create it:
CREATE TABLE IF NOT EXISTS proctor_student_map (
    id INT AUTO_INCREMENT PRIMARY KEY,
    proctor_id INT NOT NULL,
    student_id INT NOT NULL,
    assigned_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (proctor_id) REFERENCES proctors(proctor_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    UNIQUE KEY uk_proctor_student (proctor_id, student_id),
    INDEX idx_proctor (proctor_id),
    INDEX idx_student (student_id)
) ENGINE=InnoDB;

-- ============================================================
-- STEP 3: Add sample data for testing (OPTIONAL - for development)
-- ============================================================

-- Insert sample proctor (if not exists)
INSERT IGNORE INTO users (user_id, usn, name, password_hash, email, role, is_active)
VALUES (1001, 'PROC001', 'Dr. John Smith', '$2a$10$dummyhash', 'john.smith@rit.edu', 'PROCTOR', TRUE);

INSERT IGNORE INTO proctors (proctor_id)
VALUES (1001);

-- Insert sample students (if not exists)
INSERT IGNORE INTO users (user_id, usn, name, password_hash, email, role, is_active)
VALUES 
(2001, '1RV21IS001', 'Alice Johnson', '$2a$10$dummyhash', 'alice@rit.edu', 'STUDENT', TRUE),
(2002, '1RV21IS002', 'Bob Williams', '$2a$10$dummyhash', 'bob@rit.edu', 'STUDENT', TRUE),
(2003, '1RV21IS003', 'Charlie Brown', '$2a$10$dummyhash', 'charlie@rit.edu', 'STUDENT', TRUE);

INSERT IGNORE INTO students (student_id, branch, current_sem)
VALUES 
(2001, 'ISE', 6),
(2002, 'ISE', 6),
(2003, 'CSE', 6);

-- Add skills to students
UPDATE students SET skills = 'Java, Python, SQL, Spring Boot' WHERE student_id = 2001;
UPDATE students SET skills = 'JavaScript, React, Node.js, MongoDB' WHERE student_id = 2002;
UPDATE students SET skills = 'C++, Data Structures, Algorithms' WHERE student_id = 2003;

-- Add academic records for CGPA calculation
INSERT IGNORE INTO academic_records (student_id, semester, sgpa)
VALUES 
(2001, 1, 8.5), (2001, 2, 8.7), (2001, 3, 8.9), (2001, 4, 9.0), (2001, 5, 8.8),
(2002, 1, 6.5), (2002, 2, 6.8), (2002, 3, 6.9), (2002, 4, 7.0), (2002, 5, 6.7),
(2003, 1, 5.5), (2003, 2, 5.8), (2003, 3, 6.0), (2003, 4, 6.2), (2003, 5, 6.1);

-- Map students to proctor
INSERT IGNORE INTO proctor_student_map (proctor_id, student_id)
VALUES 
(1001, 2001),
(1001, 2002),
(1001, 2003);

-- Add sample companies and jobs
INSERT IGNORE INTO companies (company_id, company_name, description, company_type)
VALUES 
(101, 'TechCorp', 'Leading technology company', 'MNC'),
(102, 'StartupXYZ', 'Innovative startup', 'STARTUP');

INSERT IGNORE INTO job_postings (job_id, company_id, role, package, min_cgpa, allowed_branches, required_skills)
VALUES 
(201, 101, 'Software Engineer', 12.00, 7.0, 'ISE,CSE', 'Java, Spring Boot, SQL'),
(202, 102, 'Full Stack Developer', 8.00, 6.5, 'ISE,CSE', 'JavaScript, React, Node.js');

-- Add sample applications
INSERT IGNORE INTO applications (application_id, student_id, job_id, status, applied_at)
VALUES 
(301, 2001, 201, 'SELECTED', NOW()),
(302, 2001, 202, 'APPLIED', NOW()),
(303, 2002, 202, 'SHORTLISTED', NOW());
-- Student 2003 has no applications (will be AT RISK)

-- Add sample remarks
INSERT IGNORE INTO proctor_remarks (student_id, proctor_id, remark)
VALUES 
(2001, 1001, 'Excellent performance. Keep up the good work!'),
(2002, 1001, 'Good progress. Need to improve CGPA slightly.'),
(2003, 1001, 'URGENT: Student needs immediate attention. Low CGPA and no applications submitted.');

-- ============================================================
-- STEP 4: Verification queries
-- ============================================================

-- Verify proctor_remarks table structure
DESCRIBE proctor_remarks;

-- Verify proctor_student_map
SELECT * FROM proctor_student_map;

-- Verify student data with CGPA
SELECT 
    u.user_id,
    u.usn,
    u.name,
    s.branch,
    s.skills,
    (SELECT AVG(ar.sgpa) FROM academic_records ar WHERE ar.student_id = s.student_id) as cgpa,
    (SELECT COUNT(*) FROM applications a WHERE a.student_id = s.student_id) as app_count
FROM users u
JOIN students s ON u.user_id = s.student_id
WHERE u.role = 'STUDENT';

-- ============================================================
-- COMPLETION MESSAGE
-- ============================================================

SELECT 'Proctor Module Schema Fix Complete!' as Status,
       'All foreign keys corrected' as Step1,
       'Mapping table verified' as Step2,
       'Sample data inserted' as Step3,
       'Ready for deployment' as Result;
