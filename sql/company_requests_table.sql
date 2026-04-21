-- ============================================================
-- Company Request Approval System
-- ============================================================
-- This table stores company registration requests that require
-- admin approval before being added to the companies table
-- ============================================================

USE rit_placement;

CREATE TABLE IF NOT EXISTS company_requests (
    request_id INT AUTO_INCREMENT PRIMARY KEY,
    company_name VARCHAR(150) NOT NULL,
    description TEXT,
    email VARCHAR(150),
    company_type ENUM('PRODUCT','SERVICE','STARTUP','MNC') DEFAULT 'PRODUCT',
    status ENUM('PENDING','APPROVED','REJECTED') DEFAULT 'PENDING',
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP NULL,
    reviewed_by INT NULL,
    INDEX idx_status (status),
    INDEX idx_requested_at (requested_at)
) ENGINE=InnoDB;

-- Verify table creation
DESCRIBE company_requests;

-- Sample query to check pending requests
SELECT * FROM company_requests WHERE status = 'PENDING' ORDER BY requested_at DESC;
