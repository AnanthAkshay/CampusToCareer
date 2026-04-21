-- ============================================================
-- RIT ISE Placement & Academic Tracking System - Docker Init
-- ============================================================
-- This file is used by Docker to initialize the database
-- Note: CREATE DATABASE is handled by MYSQL_DATABASE env var
-- ============================================================

USE placement_system;

-- 1. Users table (central authentication table)
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

-- 5. Companies table
CREATE TABLE IF NOT EXISTS companies (
    company_id   INT AUTO_INCREMENT PRIMARY KEY,
    company_name VARCHAR(200) NOT NULL,
    industry     VARCHAR(100),
    website      VARCHAR(255),
    description  TEXT,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_company_name (company_name)
) ENGINE=InnoDB;

-- 6. Job postings table
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
    INDEX idx_deadline (deadline)
) ENGINE=InnoDB;

-- 7. Applications table
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

-- 8. Interviews table
CREATE TABLE IF NOT EXISTS interviews (
    interview_id      INT AUTO_INCREMENT PRIMARY KEY,
    application_id    INT NOT NULL,
    student_id        INT NOT NULL,
    company_id        INT NOT NULL,
    job_id            INT NOT NULL,
    interview_date    DATETIME NOT NULL,
    interview_mode    ENUM('ONLINE','OFFLINE','HYBRID') DEFAULT 'ONLINE',
    interview_location VARCHAR(255),
    interview_link    VARCHAR(500),
    notes             TEXT,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (application_id) REFERENCES applications(application_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (company_id) REFERENCES companies(company_id) ON DELETE CASCADE,
    FOREIGN KEY (job_id) REFERENCES job_postings(job_id) ON DELETE CASCADE,
    INDEX idx_student_date (student_id, interview_date),
    INDEX idx_company_date (company_id, interview_date)
) ENGINE=InnoDB;

-- 9. Notifications table
CREATE TABLE IF NOT EXISTS notifications (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id         INT NOT NULL,
    title           VARCHAR(255) NOT NULL,
    message         TEXT NOT NULL,
    type            ENUM('APPLICATION','INTERVIEW','STATUS_UPDATE','JOB_POSTED','GENERAL') DEFAULT 'GENERAL',
    is_read         BOOLEAN DEFAULT FALSE,
    related_id      INT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user_read (user_id, is_read),
    INDEX idx_user_created (user_id, created_at)
) ENGINE=InnoDB;

-- 10. Documents table
CREATE TABLE IF NOT EXISTS documents (
    document_id   INT AUTO_INCREMENT PRIMARY KEY,
    student_id    INT NOT NULL,
    document_type ENUM('RESUME','MARKSHEET','CERTIFICATE','OTHER') NOT NULL,
    file_name     VARCHAR(255) NOT NULL,
    file_path     VARCHAR(500) NOT NULL,
    uploaded_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    INDEX idx_student_type (student_id, document_type)
) ENGINE=InnoDB;

-- 11. Proctor remarks table
CREATE TABLE IF NOT EXISTS proctor_remarks (
    remark_id   INT AUTO_INCREMENT PRIMARY KEY,
    student_id  INT NOT NULL,
    proctor_id  INT NOT NULL,
    remark_text TEXT NOT NULL,
    category    ENUM('ACADEMIC','PLACEMENT','BEHAVIOR','GENERAL') DEFAULT 'GENERAL',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (proctor_id) REFERENCES proctors(proctor_id) ON DELETE CASCADE,
    INDEX idx_student_proctor (student_id, proctor_id),
    INDEX idx_created (created_at)
) ENGINE=InnoDB;

-- 12. Company requests table
CREATE TABLE IF NOT EXISTS company_requests (
    request_id   INT AUTO_INCREMENT PRIMARY KEY,
    company_name VARCHAR(200) NOT NULL,
    email        VARCHAR(150) NOT NULL,
    phone        VARCHAR(20),
    website      VARCHAR(255),
    industry     VARCHAR(100),
    description  TEXT,
    status       ENUM('PENDING','APPROVED','REJECTED') DEFAULT 'PENDING',
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reviewed_at  TIMESTAMP NULL,
    reviewed_by  INT NULL,
    INDEX idx_status (status),
    INDEX idx_created (created_at)
) ENGINE=InnoDB;

-- Insert default admin user (password: admin123)
-- Password hash generated using BCrypt
INSERT INTO users (usn, name, password_hash, email, role, is_active) 
VALUES ('ADMIN001', 'System Administrator', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'admin@rit.edu', 'ADMIN', TRUE)
ON DUPLICATE KEY UPDATE usn=usn;

-- Success message
SELECT 'Database initialized successfully!' AS Status;
