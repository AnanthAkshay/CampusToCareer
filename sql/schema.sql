-- ============================================================
-- RIT ISE Placement & Academic Tracking System - Database Schema
-- ============================================================

CREATE DATABASE IF NOT EXISTS rit_placement;
USE rit_placement;

-- 1. Users table (central authentication table)
CREATE TABLE users (
    user_id       INT AUTO_INCREMENT PRIMARY KEY,
    usn           VARCHAR(20) UNIQUE NOT NULL,
    name          VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role          ENUM('STUDENT','PROCTOR','COORDINATOR','FACULTY','ADMIN') NOT NULL,
    is_active     BOOLEAN DEFAULT TRUE,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. Students table
CREATE TABLE students (
    student_id  INT PRIMARY KEY,
    branch      VARCHAR(50) NOT NULL,
    current_sem INT NOT NULL,
    FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 3. Academic records (one row per semester per student)
CREATE TABLE academic_records (
    record_id   INT AUTO_INCREMENT PRIMARY KEY,
    student_id  INT NOT NULL,
    semester    INT NOT NULL,
    sgpa        DECIMAL(4,2) NOT NULL,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    UNIQUE KEY uk_student_sem (student_id, semester),
    INDEX idx_student_sem (student_id, semester)
) ENGINE=InnoDB;

-- 4. Proctors table
CREATE TABLE proctors (
    proctor_id INT PRIMARY KEY,
    FOREIGN KEY (proctor_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 5. Proctor-Student mapping
CREATE TABLE proctor_student_map (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    proctor_id  INT NOT NULL,
    student_id  INT NOT NULL,
    FOREIGN KEY (proctor_id) REFERENCES proctors(proctor_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    UNIQUE KEY uk_proctor_student (proctor_id, student_id)
) ENGINE=InnoDB;

-- 6. Companies
CREATE TABLE companies (
    company_id   INT AUTO_INCREMENT PRIMARY KEY,
    company_name VARCHAR(150) NOT NULL,
    description  TEXT,
    company_type ENUM('PRODUCT','SERVICE','STARTUP','MNC') DEFAULT 'PRODUCT',
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 7. Job postings
CREATE TABLE job_postings (
    job_id          INT AUTO_INCREMENT PRIMARY KEY,
    company_id      INT NOT NULL,
    role            VARCHAR(100) NOT NULL,
    package         DECIMAL(10,2),
    min_cgpa        DECIMAL(4,2),
    allowed_branches VARCHAR(255),
    required_skills TEXT,
    deadline        DATE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES companies(company_id) ON DELETE CASCADE,
    INDEX idx_min_cgpa (min_cgpa)
) ENGINE=InnoDB;

-- 8. Applications
CREATE TABLE applications (
    application_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id     INT NOT NULL,
    job_id         INT NOT NULL,
    status         ENUM('APPLIED','SHORTLISTED','SELECTED','REJECTED') DEFAULT 'APPLIED',
    applied_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (job_id) REFERENCES job_postings(job_id) ON DELETE CASCADE,
    UNIQUE KEY uk_student_job (student_id, job_id)
) ENGINE=InnoDB;

-- 9. Documents
CREATE TABLE documents (
    document_id       INT AUTO_INCREMENT PRIMARY KEY,
    student_id        INT NOT NULL,
    resume_path       VARCHAR(255),
    certificates_path VARCHAR(255),
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 10. Notifications
CREATE TABLE notifications (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id         INT NOT NULL,
    message         TEXT NOT NULL,
    status          ENUM('UNREAD','READ') DEFAULT 'UNREAD',
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user_status (user_id, status)
) ENGINE=InnoDB;

-- 11. Remarks (proctor → student)
CREATE TABLE remarks (
    remark_id   INT AUTO_INCREMENT PRIMARY KEY,
    student_id  INT NOT NULL,
    proctor_id  INT NOT NULL,
    comment     TEXT NOT NULL,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (proctor_id) REFERENCES proctors(proctor_id) ON DELETE CASCADE
) ENGINE=InnoDB;
