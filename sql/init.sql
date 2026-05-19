-- ============================================================
-- RIT ISE Placement & Academic Tracking System
-- CONSOLIDATED DATABASE INIT SCRIPT
-- ============================================================
-- This single file replaces all individual SQL migration files.
-- Run this once on a fresh database to set up everything.
-- ============================================================

CREATE DATABASE IF NOT EXISTS rit_placement;
USE rit_placement;

-- ============================================================
-- SECTION 1: CORE TABLES
-- ============================================================

-- 1. Users table (central authentication)
CREATE TABLE IF NOT EXISTS users (
    user_id       INT AUTO_INCREMENT PRIMARY KEY,
    usn           VARCHAR(20) UNIQUE NOT NULL,
    name          VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email         VARCHAR(150),
    role          ENUM('STUDENT','PROCTOR','COORDINATOR','FACULTY','ADMIN','COMPANY') NOT NULL,
    is_active     BOOLEAN DEFAULT TRUE,
    company_id    INT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_users_role (role),
    INDEX idx_users_active (is_active),
    INDEX idx_users_company_id (company_id)
) ENGINE=InnoDB;

-- 2. Students table
CREATE TABLE IF NOT EXISTS students (
    student_id  INT PRIMARY KEY,
    branch      VARCHAR(50) NOT NULL,
    current_sem INT NOT NULL,
    skills      TEXT,
    projects    TEXT,
    experience  TEXT,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 3. Academic records (one row per semester per student)
CREATE TABLE IF NOT EXISTS academic_records (
    record_id   INT AUTO_INCREMENT PRIMARY KEY,
    student_id  INT NOT NULL,
    semester    INT NOT NULL,
    sgpa        DECIMAL(4,2) DEFAULT NULL,
    cgpa        DECIMAL(4,2) DEFAULT NULL,
    marks       DECIMAL(6,2) NOT NULL DEFAULT 0,
    max_marks   DECIMAL(6,2) NOT NULL DEFAULT 100,
    subject     VARCHAR(100),
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    INDEX idx_student_sem (student_id, semester),
    UNIQUE KEY uk_student_semester_subject (student_id, semester, subject)
) ENGINE=InnoDB;

-- 4. Proctors table
CREATE TABLE IF NOT EXISTS proctors (
    proctor_id INT PRIMARY KEY,
    FOREIGN KEY (proctor_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 5. Proctor-Student mapping
CREATE TABLE IF NOT EXISTS proctor_student_map (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    proctor_id    INT NOT NULL,
    student_id    INT NOT NULL,
    assigned_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (proctor_id) REFERENCES proctors(proctor_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    UNIQUE KEY uk_proctor_student (proctor_id, student_id),
    INDEX idx_proctor (proctor_id),
    INDEX idx_student (student_id)
) ENGINE=InnoDB;

-- 6. Companies table
CREATE TABLE IF NOT EXISTS companies (
    company_id   INT AUTO_INCREMENT PRIMARY KEY,
    company_name VARCHAR(200) NOT NULL,
    industry     VARCHAR(100),
    website      VARCHAR(255),
    description  TEXT,
    company_type ENUM('PRODUCT','SERVICE','STARTUP','MNC') DEFAULT 'PRODUCT',
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_company_name (company_name)
) ENGINE=InnoDB;

-- Add FK from users -> companies (after companies table exists)
ALTER TABLE users
ADD CONSTRAINT fk_users_company
    FOREIGN KEY (company_id) REFERENCES companies(company_id)
    ON DELETE SET NULL;

-- 7. Job postings table
CREATE TABLE IF NOT EXISTS job_postings (
    job_id           INT AUTO_INCREMENT PRIMARY KEY,
    company_id       INT NOT NULL,
    role             VARCHAR(100) NOT NULL,
    package          DECIMAL(10,2),
    min_cgpa         DECIMAL(4,2),
    allowed_branches VARCHAR(255),
    required_skills  TEXT,
    deadline         DATE,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES companies(company_id) ON DELETE CASCADE,
    INDEX idx_company_deadline (company_id, deadline),
    INDEX idx_deadline (deadline),
    INDEX idx_min_cgpa (min_cgpa)
) ENGINE=InnoDB;

-- 8. Applications table
CREATE TABLE IF NOT EXISTS applications (
    application_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id     INT NOT NULL,
    job_id         INT NOT NULL,
    status         ENUM('PENDING','SHORTLISTED','INTERVIEW','SELECTED','REJECTED') DEFAULT 'PENDING',
    applied_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (job_id) REFERENCES job_postings(job_id) ON DELETE CASCADE,
    UNIQUE KEY uk_student_job (student_id, job_id),
    INDEX idx_student_status (student_id, status),
    INDEX idx_job_status (job_id, status)
) ENGINE=InnoDB;

-- 9. Interviews table
CREATE TABLE IF NOT EXISTS interviews (
    interview_id       INT AUTO_INCREMENT PRIMARY KEY,
    application_id     INT NOT NULL,
    student_id         INT NOT NULL,
    company_id         INT NOT NULL,
    job_id             INT NOT NULL,
    interview_date     DATETIME NOT NULL,
    interview_mode     ENUM('ONLINE','OFFLINE','HYBRID','PHONE') DEFAULT 'ONLINE',
    interview_location VARCHAR(255),
    interview_link     VARCHAR(500),
    status             ENUM('SCHEDULED','COMPLETED','CANCELLED','RESCHEDULED') DEFAULT 'SCHEDULED',
    notes              TEXT,
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (application_id) REFERENCES applications(application_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (company_id) REFERENCES companies(company_id) ON DELETE CASCADE,
    FOREIGN KEY (job_id) REFERENCES job_postings(job_id) ON DELETE CASCADE,
    INDEX idx_student_date (student_id, interview_date),
    INDEX idx_company_date (company_id, interview_date)
) ENGINE=InnoDB;

-- 10. Notifications table
CREATE TABLE IF NOT EXISTS notifications (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id         INT NOT NULL,
    title           VARCHAR(255) NOT NULL,
    message         TEXT NOT NULL,
    type            ENUM('APPLICATION','INTERVIEW','STATUS_UPDATE','JOB_POSTED','GENERAL') DEFAULT 'GENERAL',
    is_read         BOOLEAN DEFAULT FALSE,
    related_id      INT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user_read (user_id, is_read),
    INDEX idx_user_created (user_id, created_at)
) ENGINE=InnoDB;

-- 11. Documents table
CREATE TABLE IF NOT EXISTS documents (
    document_id   INT AUTO_INCREMENT PRIMARY KEY,
    student_id    INT NOT NULL,
    document_type ENUM('RESUME','MARKSHEET','CERTIFICATE','OTHER') NOT NULL,
    file_name     VARCHAR(255) NOT NULL,
    file_path     VARCHAR(500) NOT NULL,
    uploaded_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    INDEX idx_student_type (student_id, document_type)
) ENGINE=InnoDB;

-- 12. Proctor remarks table
CREATE TABLE IF NOT EXISTS proctor_remarks (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    student_id  INT NOT NULL,
    proctor_id  INT NOT NULL,
    remark      TEXT NOT NULL,
    category    ENUM('ACADEMIC','PLACEMENT','BEHAVIOR','GENERAL') DEFAULT 'GENERAL',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_remark_student FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_remark_proctor FOREIGN KEY (proctor_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_student_id (student_id),
    INDEX idx_proctor_id (proctor_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB;

-- 13. Company requests table (admin approval workflow)
CREATE TABLE IF NOT EXISTS company_requests (
    request_id   INT AUTO_INCREMENT PRIMARY KEY,
    company_name VARCHAR(200) NOT NULL,
    email        VARCHAR(150) NOT NULL,
    phone        VARCHAR(20),
    website      VARCHAR(255),
    industry     VARCHAR(100),
    description  TEXT,
    company_type ENUM('PRODUCT','SERVICE','STARTUP','MNC') DEFAULT 'PRODUCT',
    status       ENUM('PENDING','APPROVED','REJECTED') DEFAULT 'PENDING',
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reviewed_at  TIMESTAMP NULL,
    reviewed_by  INT NULL,
    INDEX idx_status (status),
    INDEX idx_created (created_at)
) ENGINE=InnoDB;

-- 14. Application timeline table (status change audit trail)
CREATE TABLE IF NOT EXISTS application_timeline (
    timeline_id    INT AUTO_INCREMENT PRIMARY KEY,
    application_id INT NOT NULL,
    status         VARCHAR(50) NOT NULL,
    changed_by     INT,
    notes          TEXT,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (application_id) REFERENCES applications(application_id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_timeline_application (application_id)
) ENGINE=InnoDB;

-- ============================================================
-- SECTION 2: TRIGGERS
-- ============================================================

-- Auto-log application status changes to timeline
DELIMITER //
CREATE TRIGGER IF NOT EXISTS after_application_status_update
AFTER UPDATE ON applications
FOR EACH ROW
BEGIN
    IF OLD.status != NEW.status THEN
        INSERT INTO application_timeline (application_id, status, notes)
        VALUES (NEW.application_id, NEW.status,
                CONCAT('Status changed from ', OLD.status, ' to ', NEW.status));
    END IF;
END//
DELIMITER ;

-- ============================================================
-- SECTION 3: DEFAULT SEED DATA
-- ============================================================

-- Default Admin user (password: admin123)
INSERT INTO users (usn, name, password_hash, email, role, is_active)
VALUES ('ADMIN001', 'System Administrator',
        '$2a$10$Agr2p2S4Y.hLb38UPmHrcuBs1sFKLet.Fac1daBZMzFhDDxLpMKw.',
        'admin@msrit.edu', 'ADMIN', TRUE)
ON DUPLICATE KEY UPDATE usn = usn;

-- Default Coordinator users (password: admin123 — change immediately in production!)
INSERT INTO users (usn, name, password_hash, email, role, is_active)
VALUES
    ('COORD001', 'Placement Coordinator',
     '$2a$10$Agr2p2S4Y.hLb38UPmHrcuBs1sFKLet.Fac1daBZMzFhDDxLpMKw.',
     'coordinator1@msrit.edu', 'COORDINATOR', TRUE),
    ('COORD002', 'Training Coordinator',
     '$2a$10$Agr2p2S4Y.hLb38UPmHrcuBs1sFKLet.Fac1daBZMzFhDDxLpMKw.',
     'coordinator2@msrit.edu', 'COORDINATOR', TRUE)
ON DUPLICATE KEY UPDATE role = 'COORDINATOR', is_active = TRUE;

-- ============================================================
-- NOTE: Student data is loaded automatically from data/students.csv
--       by the CSVImporterService on first application boot.
--       Do NOT add sample students here.
-- ============================================================
SELECT 'Database initialized successfully!' AS Status;
-- ============================================================
