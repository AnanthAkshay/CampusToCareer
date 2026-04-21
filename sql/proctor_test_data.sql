-- ============================================================
-- Proctor Management System - Test Data
-- ============================================================

USE rit_placement;

-- Insert a test proctor user
INSERT INTO users (usn, name, password_hash, email, role, is_active) 
VALUES ('PROCTOR001', 'Dr. Rajesh Kumar', '$2a$10$dummyhash', 'rajesh.kumar@rit.edu', 'PROCTOR', TRUE);

-- Get the proctor's user_id (assuming it's the last inserted)
SET @proctor_id = LAST_INSERT_ID();

-- Insert proctor into proctors table
INSERT INTO proctors (proctor_id) VALUES (@proctor_id);

-- Insert test students (if not already exist)
INSERT IGNORE INTO users (usn, name, password_hash, email, role, is_active) VALUES
('1RI21IS001', 'Amit Sharma', '$2a$10$dummyhash', '1ri21is001@gmail.com', 'STUDENT', TRUE),
('1RI21IS002', 'Priya Patel', '$2a$10$dummyhash', '1ri21is002@gmail.com', 'STUDENT', TRUE),
('1RI21IS003', 'Rahul Verma', '$2a$10$dummyhash', '1ri21is003@gmail.com', 'STUDENT', TRUE),
('1RI21IS004', 'Sneha Reddy', '$2a$10$dummyhash', '1ri21is004@gmail.com', 'STUDENT', TRUE),
('1RI21IS005', 'Karthik Rao', '$2a$10$dummyhash', '1ri21is005@gmail.com', 'STUDENT', TRUE);

-- Insert students into students table
INSERT IGNORE INTO students (student_id, branch, current_sem) 
SELECT user_id, 'ISE', 6 FROM users WHERE usn IN ('1RI21IS001', '1RI21IS002', '1RI21IS003', '1RI21IS004', '1RI21IS005');

-- Insert academic records for CGPA calculation
INSERT IGNORE INTO academic_records (student_id, semester, sgpa) VALUES
((SELECT user_id FROM users WHERE usn = '1RI21IS001'), 1, 8.5),
((SELECT user_id FROM users WHERE usn = '1RI21IS001'), 2, 8.7),
((SELECT user_id FROM users WHERE usn = '1RI21IS001'), 3, 8.9),
((SELECT user_id FROM users WHERE usn = '1RI21IS001'), 4, 9.0),
((SELECT user_id FROM users WHERE usn = '1RI21IS001'), 5, 8.8),

((SELECT user_id FROM users WHERE usn = '1RI21IS002'), 1, 7.5),
((SELECT user_id FROM users WHERE usn = '1RI21IS002'), 2, 7.8),
((SELECT user_id FROM users WHERE usn = '1RI21IS002'), 3, 8.0),
((SELECT user_id FROM users WHERE usn = '1RI21IS002'), 4, 8.2),
((SELECT user_id FROM users WHERE usn = '1RI21IS002'), 5, 8.1),

((SELECT user_id FROM users WHERE usn = '1RI21IS003'), 1, 6.5),
((SELECT user_id FROM users WHERE usn = '1RI21IS003'), 2, 6.8),
((SELECT user_id FROM users WHERE usn = '1RI21IS003'), 3, 7.0),
((SELECT user_id FROM users WHERE usn = '1RI21IS003'), 4, 7.2),
((SELECT user_id FROM users WHERE usn = '1RI21IS003'), 5, 7.1),

((SELECT user_id FROM users WHERE usn = '1RI21IS004'), 1, 9.0),
((SELECT user_id FROM users WHERE usn = '1RI21IS004'), 2, 9.2),
((SELECT user_id FROM users WHERE usn = '1RI21IS004'), 3, 9.1),
((SELECT user_id FROM users WHERE usn = '1RI21IS004'), 4, 9.3),
((SELECT user_id FROM users WHERE usn = '1RI21IS004'), 5, 9.2),

((SELECT user_id FROM users WHERE usn = '1RI21IS005'), 1, 5.5),
((SELECT user_id FROM users WHERE usn = '1RI21IS005'), 2, 5.8),
((SELECT user_id FROM users WHERE usn = '1RI21IS005'), 3, 6.0),
((SELECT user_id FROM users WHERE usn = '1RI21IS005'), 4, 6.2),
((SELECT user_id FROM users WHERE usn = '1RI21IS005'), 5, 6.1);

-- Assign students to the proctor
INSERT INTO proctor_student_map (proctor_id, student_id) VALUES
(@proctor_id, (SELECT user_id FROM users WHERE usn = '1RI21IS001')),
(@proctor_id, (SELECT user_id FROM users WHERE usn = '1RI21IS002')),
(@proctor_id, (SELECT user_id FROM users WHERE usn = '1RI21IS003')),
(@proctor_id, (SELECT user_id FROM users WHERE usn = '1RI21IS004')),
(@proctor_id, (SELECT user_id FROM users WHERE usn = '1RI21IS005'));

-- Verification queries
SELECT '=== PROCTOR CREATED ===' AS Status;
SELECT user_id, usn, name, email, role FROM users WHERE role = 'PROCTOR';

SELECT '=== STUDENTS ASSIGNED ===' AS Status;
SELECT COUNT(*) as student_count FROM proctor_student_map WHERE proctor_id = @proctor_id;

SELECT '=== PROCTOR-STUDENT MAPPING ===' AS Status;
SELECT 
    u1.usn as proctor_usn,
    u1.name as proctor_name,
    u2.usn as student_usn,
    u2.name as student_name,
    s.branch,
    COALESCE(AVG(ar.sgpa), 0.0) as cgpa
FROM proctor_student_map psm
JOIN users u1 ON psm.proctor_id = u1.user_id
JOIN users u2 ON psm.student_id = u2.user_id
JOIN students s ON psm.student_id = s.student_id
LEFT JOIN academic_records ar ON s.student_id = ar.student_id
WHERE psm.proctor_id = @proctor_id
GROUP BY u1.usn, u1.name, u2.usn, u2.name, s.branch
ORDER BY u2.usn;

SELECT 'Test data inserted successfully!' AS Status;
