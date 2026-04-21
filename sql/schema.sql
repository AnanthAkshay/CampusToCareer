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
    email         VARCHAR(150),
    role          ENUM('STUDENT','PROCTOR','COORDINATOR','FACULTY','ADMIN','COMPANY') NOT NULL,
    is_active     BOOLEAN DEFAULT TRUE,
    company_id    INT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_users_role (role),
    INDEX idx_users_company_id (company_id)
) ENGINE=InnoDB;

-- 2. Students table
CREATE TABLE students (
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

-- Add foreign key constraint for users.company_id
ALTER TABLE users
ADD CONSTRAINT fk_users_company 
    FOREIGN KEY (company_id) REFERENCES companies(company_id) 
    ON DELETE SET NULL;

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
    status         ENUM('PENDING','SHORTLISTED','INTERVIEW','SELECTED','REJECTED') DEFAULT 'PENDING',
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
    title           VARCHAR(255) NOT NULL,
    message         TEXT NOT NULL,
    type            VARCHAR(50) DEFAULT 'INFO',
    is_read         BOOLEAN DEFAULT FALSE,
    related_id      INT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user_status (user_id, is_read)
) ENGINE=InnoDB;

-- 11. Interviews
CREATE TABLE interviews (
    interview_id       INT AUTO_INCREMENT PRIMARY KEY,
    application_id     INT NOT NULL,
    student_id         INT NOT NULL,
    company_id         INT NOT NULL,
    job_id             INT NOT NULL,
    interview_date     TIMESTAMP NOT NULL,
    interview_mode     VARCHAR(50),
    interview_location VARCHAR(255),
    interview_link     VARCHAR(500),
    status             VARCHAR(50) DEFAULT 'SCHEDULED',
    notes              TEXT,
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (application_id) REFERENCES applications(application_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (company_id) REFERENCES companies(company_id) ON DELETE CASCADE,
    FOREIGN KEY (job_id) REFERENCES job_postings(job_id) ON DELETE CASCADE,
    INDEX idx_student_id (student_id),
    INDEX idx_company_id (company_id),
    INDEX idx_interview_date (interview_date)
) ENGINE=InnoDB;

-- 12. Remarks (proctor → student)
CREATE TABLE remarks (
    remark_id   INT AUTO_INCREMENT PRIMARY KEY,
    student_id  INT NOT NULL,
    proctor_id  INT NOT NULL,
    comment     TEXT NOT NULL,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (proctor_id) REFERENCES proctors(proctor_id) ON DELETE CASCADE
) ENGINE=InnoDB;
