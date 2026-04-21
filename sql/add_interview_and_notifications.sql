-- SQL Migration: Add Interview Scheduling and Notifications
-- This script adds tables for interview scheduling and student notifications

-- 1. Create interviews table
CREATE TABLE IF NOT EXISTS interviews (
    interview_id INT PRIMARY KEY AUTO_INCREMENT,
    application_id INT NOT NULL,
    student_id INT NOT NULL,
    company_id INT NOT NULL,
    job_id INT NOT NULL,
    interview_date DATETIME NOT NULL,
    interview_mode ENUM('ONLINE', 'OFFLINE', 'PHONE') DEFAULT 'ONLINE',
    interview_location VARCHAR(255),
    interview_link VARCHAR(500),
    status ENUM('SCHEDULED', 'COMPLETED', 'CANCELLED', 'RESCHEDULED') DEFAULT 'SCHEDULED',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (application_id) REFERENCES applications(application_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (company_id) REFERENCES companies(company_id) ON DELETE CASCADE,
    FOREIGN KEY (job_id) REFERENCES job_postings(job_id) ON DELETE CASCADE
);

-- 2. Create notifications table
CREATE TABLE IF NOT EXISTS notifications (
    notification_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type ENUM('APPLICATION', 'INTERVIEW', 'STATUS_UPDATE', 'GENERAL') DEFAULT 'GENERAL',
    is_read BOOLEAN DEFAULT FALSE,
    related_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 3. Create application_timeline table for tracking status changes
CREATE TABLE IF NOT EXISTS application_timeline (
    timeline_id INT PRIMARY KEY AUTO_INCREMENT,
    application_id INT NOT NULL,
    status VARCHAR(50) NOT NULL,
    changed_by INT,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (application_id) REFERENCES applications(application_id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by) REFERENCES users(user_id) ON DELETE SET NULL
);

-- 4. Add indexes for better performance
CREATE INDEX idx_interviews_student ON interviews(student_id);
CREATE INDEX idx_interviews_company ON interviews(company_id);
CREATE INDEX idx_interviews_date ON interviews(interview_date);
CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_read ON notifications(is_read);
CREATE INDEX idx_timeline_application ON application_timeline(application_id);

-- 5. Add trigger to create timeline entry when application status changes
DELIMITER //
CREATE TRIGGER IF NOT EXISTS after_application_status_update
AFTER UPDATE ON applications
FOR EACH ROW
BEGIN
    IF OLD.status != NEW.status THEN
        INSERT INTO application_timeline (application_id, status, notes)
        VALUES (NEW.application_id, NEW.status, CONCAT('Status changed from ', OLD.status, ' to ', NEW.status));
    END IF;
END//
DELIMITER ;

-- 6. Sample data for testing (optional)
-- INSERT INTO interviews (application_id, student_id, company_id, job_id, interview_date, interview_mode, interview_link)
-- VALUES (1, 1, 1, 1, '2026-05-15 10:00:00', 'ONLINE', 'https://meet.google.com/abc-defg-hij');

COMMIT;
