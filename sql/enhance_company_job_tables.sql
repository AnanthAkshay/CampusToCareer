-- ============================================================
-- Enhance Companies and Job Postings Tables
-- ============================================================

USE rit_placement;

-- Add company_type to companies table
ALTER TABLE companies
ADD COLUMN company_type ENUM('PRODUCT','SERVICE','STARTUP','MNC') DEFAULT 'PRODUCT' AFTER description,
ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP AFTER company_type;

-- Add additional fields to job_postings table
ALTER TABLE job_postings
ADD COLUMN allowed_branches VARCHAR(255) AFTER min_cgpa,
ADD COLUMN deadline DATE AFTER required_skills,
ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP AFTER deadline;
