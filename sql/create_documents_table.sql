-- ============================================================
-- DOCUMENTS TABLE - Enhanced Version
-- ============================================================
-- This table stores document paths for students
-- Supports resume and certificate uploads
-- ============================================================

USE rit_placement;

-- Drop existing documents table if it exists (to recreate with proper structure)
DROP TABLE IF EXISTS documents;

-- Create documents table
CREATE TABLE documents (
    document_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    resume_path VARCHAR(255),
    certificates_path VARCHAR(255),
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    INDEX idx_student (student_id)
) ENGINE=InnoDB;

-- Verify table creation
DESCRIBE documents;
