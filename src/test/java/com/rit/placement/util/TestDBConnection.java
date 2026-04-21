package com.rit.placement.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Test database connection utility using H2 in-memory database
 */
public class TestDBConnection {
    
    private static final String URL = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL";
    private static final String USER = "sa";
    private static final String PASS = "";
    
    private static boolean initialized = false;
    
    static {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("H2 JDBC Driver not found.", e);
        }
    }
    
    /**
     * Get test database connection
     */
    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(URL, USER, PASS);
        
        // Initialize schema on first connection
        if (!initialized) {
            initializeSchema(conn);
            initialized = true;
        }
        
        return conn;
    }
    
    /**
     * Initialize test database schema
     */
    private static void initializeSchema(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Users table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS users (" +
                "user_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "usn VARCHAR(20) UNIQUE NOT NULL, " +
                "name VARCHAR(100) NOT NULL, " +
                "password_hash VARCHAR(255) NOT NULL, " +
                "email VARCHAR(150), " +
                "role VARCHAR(20) NOT NULL, " +
                "is_active BOOLEAN DEFAULT TRUE, " +
                "company_id INT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );
            
            // Students table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS students (" +
                "student_id INT PRIMARY KEY, " +
                "branch VARCHAR(50) NOT NULL, " +
                "current_sem INT NOT NULL, " +
                "skills TEXT, " +
                "projects TEXT, " +
                "experience TEXT, " +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE" +
                ")"
            );
            
            // Academic records table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS academic_records (" +
                "record_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "student_id INT NOT NULL, " +
                "semester INT NOT NULL, " +
                "marks DECIMAL(6,2) NOT NULL, " +
                "max_marks DECIMAL(6,2) NOT NULL, " +
                "subject VARCHAR(100), " +
                "FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE" +
                ")"
            );
            
            // Companies table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS companies (" +
                "company_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "company_name VARCHAR(200) NOT NULL, " +
                "industry VARCHAR(100), " +
                "website VARCHAR(255), " +
                "description TEXT, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );
            
            // Job postings table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS job_postings (" +
                "job_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "company_id INT NOT NULL, " +
                "role VARCHAR(100) NOT NULL, " +
                "package DECIMAL(10,2), " +
                "min_cgpa DECIMAL(4,2), " +
                "allowed_branches VARCHAR(255), " +
                "required_skills TEXT, " +
                "deadline DATE, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (company_id) REFERENCES companies(company_id) ON DELETE CASCADE" +
                ")"
            );
            
            // Applications table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS applications (" +
                "application_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "student_id INT NOT NULL, " +
                "job_id INT NOT NULL, " +
                "status VARCHAR(20) DEFAULT 'PENDING', " +
                "applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (job_id) REFERENCES job_postings(job_id) ON DELETE CASCADE, " +
                "UNIQUE (student_id, job_id)" +
                ")"
            );
            
            // Notifications table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS notifications (" +
                "notification_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "user_id INT NOT NULL, " +
                "title VARCHAR(255) NOT NULL, " +
                "message TEXT NOT NULL, " +
                "type VARCHAR(20) DEFAULT 'GENERAL', " +
                "is_read BOOLEAN DEFAULT FALSE, " +
                "related_id INT, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE" +
                ")"
            );
        }
    }
    
    /**
     * Clear all data from tables (for test cleanup)
     */
    public static void clearAllData() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");
            stmt.execute("TRUNCATE TABLE notifications");
            stmt.execute("TRUNCATE TABLE applications");
            stmt.execute("TRUNCATE TABLE job_postings");
            stmt.execute("TRUNCATE TABLE companies");
            stmt.execute("TRUNCATE TABLE academic_records");
            stmt.execute("TRUNCATE TABLE students");
            stmt.execute("TRUNCATE TABLE users");
            stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");
        }
    }
    
    /**
     * Reset auto-increment counters
     */
    public static void resetSequences() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE users ALTER COLUMN user_id RESTART WITH 1");
            stmt.execute("ALTER TABLE companies ALTER COLUMN company_id RESTART WITH 1");
            stmt.execute("ALTER TABLE job_postings ALTER COLUMN job_id RESTART WITH 1");
            stmt.execute("ALTER TABLE applications ALTER COLUMN application_id RESTART WITH 1");
            stmt.execute("ALTER TABLE notifications ALTER COLUMN notification_id RESTART WITH 1");
            stmt.execute("ALTER TABLE academic_records ALTER COLUMN record_id RESTART WITH 1");
        }
    }
}
