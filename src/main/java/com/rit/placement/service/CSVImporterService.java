package com.rit.placement.service;

import com.rit.placement.util.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * CSV Importer Service
 * Automatically imports student data from CSV file into database
 * 
 * Features:
 * - Reads students.csv from data/ directory
 * - Creates users with STUDENT role
 * - Generates email addresses (usn@rit.edu)
 * - Inserts academic records (CGPA, SGPA)
 * - Skips duplicates safely
 * - Logs all operations
 */
public class CSVImporterService {
    
    private static final Logger logger = LoggerFactory.getLogger(CSVImporterService.class);
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");
    
    private static final String CSV_FILE_PATH = "/data/students.csv";
    private static final String DEFAULT_PASSWORD = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"; // BCrypt hash of "student123"
    
    /**
     * Import students from CSV file.
     * Only runs if no students exist in the database.
     * Throws on DB failure so the caller's retry loop can react.
     */
    public static void importStudentsIfNeeded() throws Exception {
        // Check if import is needed — will throw if DB not ready
        if (!shouldImport()) {
            logger.info("CSV import skipped - users already exist in database");
            return;
        }

        logger.info("Starting CSV import process...");
        auditLogger.info("CSV_IMPORT_STARTED - Importing students from CSV file");

        int imported = importStudents();

        logger.info("CSV import completed successfully. Imported {} students", imported);
        auditLogger.info("CSV_IMPORT_COMPLETED - Total students imported: {}", imported);
    }
    
    /**
     * Check if import should run
     * Returns true if no students exist (admin users don't prevent import)
     */
    private static boolean shouldImport() throws SQLException {
        int totalUsers = 0;
        int studentCount = 0;
        
        // Get total users count
        String totalSql = "SELECT COUNT(*) FROM users";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(totalSql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                totalUsers = rs.getInt(1);
            }
        }
        
        // Get student count
        String studentSql = "SELECT COUNT(*) FROM users WHERE role = 'STUDENT'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(studentSql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                studentCount = rs.getInt(1);
            }
        }
        
        logger.info("Database status - Total users: {}, Student users: {}", totalUsers, studentCount);
        
        if (studentCount > 0) {
            logger.info("CSV import skipped: {} students already exist in database", studentCount);
            return false;
        }
        
        logger.info("No students found in database. CSV import will proceed.");
        return true;
    }
    
    /**
     * Import students from CSV file
     * Returns number of students imported
     */
    private static int importStudents() throws Exception {
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger skipCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        // Try multiple locations for CSV file
        InputStream inputStream = null;
        
        // 1. Try classpath resource
        inputStream = CSVImporterService.class.getResourceAsStream(CSV_FILE_PATH);
        
        // 2. Try classloader resource
        if (inputStream == null) {
            inputStream = CSVImporterService.class.getClassLoader().getResourceAsStream("data/students.csv");
        }
        
        // 3. Try file system (for Docker volume mount)
        if (inputStream == null) {
            try {
                java.io.File file = new java.io.File("/app/data/students.csv");
                if (file.exists()) {
                    inputStream = new java.io.FileInputStream(file);
                    logger.info("CSV file loaded from file system: /app/data/students.csv");
                }
            } catch (Exception e) {
                logger.debug("Could not load from /app/data/students.csv: {}", e.getMessage());
            }
        }
        
        // 4. Try relative path (for local development)
        if (inputStream == null) {
            try {
                java.io.File file = new java.io.File("data/students.csv");
                if (file.exists()) {
                    inputStream = new java.io.FileInputStream(file);
                    logger.info("CSV file loaded from relative path: data/students.csv");
                }
            } catch (Exception e) {
                logger.debug("Could not load from data/students.csv: {}", e.getMessage());
            }
        }
        
        if (inputStream == null) {
            throw new IllegalStateException("CSV file not found. Tried: classpath, /app/data/students.csv, data/students.csv");
        }
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            
            // Skip header line
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IllegalStateException("CSV file is empty");
            }
            
            logger.info("CSV header: {}", headerLine);
            
            String line;
            int lineNumber = 1;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                if (line.trim().isEmpty()) {
                    continue; // Skip empty lines
                }
                
                try {
                    boolean imported = importStudentRecord(line, lineNumber);
                    if (imported) {
                        successCount.incrementAndGet();
                    } else {
                        skipCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    logger.warn("Failed to import line {}: {} - Error: {}", lineNumber, line, e.getMessage());
                }
            }
            
            logger.info("Import summary - Success: {}, Skipped: {}, Errors: {}", 
                successCount.get(), skipCount.get(), errorCount.get());
            
            return successCount.get();
        }
    }
    
    /**
     * Import a single student record from CSV line
     */
    private static boolean importStudentRecord(String line, int lineNumber) throws SQLException {
        // Parse CSV line
        String[] fields = parseCsvLine(line);
        
        if (fields.length < 2) {
            logger.warn("Line {}: Insufficient fields ({})", lineNumber, fields.length);
            return false;
        }
        
        // Extract fields
        String usn = fields[0].trim();
        String name = fields[1].trim();
        
        // Validate required fields
        if (usn.isEmpty() || name.isEmpty()) {
            logger.warn("Line {}: Missing USN or name", lineNumber);
            return false;
        }
        
        // Parse academic data
        Double sem3Sgpa = parseDouble(fields.length > 2 ? fields[2] : null);
        Double totalCgpa = parseDouble(fields.length > 3 ? fields[3] : null);
        Double sem2Sgpa = parseDouble(fields.length > 4 ? fields[4] : null);
        
        // Generate email
        String email = generateEmail(usn);
        
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // 1. Insert into users table
                int userId = insertUser(conn, usn, name, email);
                
                if (userId == -1) {
                    // User already exists, skip
                    conn.rollback();
                    logger.debug("Skipped duplicate USN: {}", usn);
                    return false;
                }
                
                // 2. Insert into students table
                insertStudent(conn, userId, usn);
                
                // 3. Insert academic records
                if (sem2Sgpa != null && sem2Sgpa > 0) {
                    insertAcademicRecord(conn, userId, 2, sem2Sgpa, totalCgpa);
                }
                if (sem3Sgpa != null && sem3Sgpa > 0) {
                    insertAcademicRecord(conn, userId, 3, sem3Sgpa, totalCgpa);
                }
                
                conn.commit();
                logger.debug("Imported student: {} - {}", usn, name);
                return true;
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }
    
    /**
     * Insert user into users table
     * Returns user_id or -1 if already exists
     */
    private static int insertUser(Connection conn, String usn, String name, String email) throws SQLException {
        // Check if user already exists
        String checkSql = "SELECT user_id FROM users WHERE usn = ?";
        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setString(1, usn);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return -1; // Already exists
            }
        }
        
        // Insert new user
        String insertSql = "INSERT INTO users (usn, name, password_hash, email, role, is_active) VALUES (?, ?, ?, ?, 'STUDENT', TRUE)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, usn);
            ps.setString(2, name);
            ps.setString(3, DEFAULT_PASSWORD);
            ps.setString(4, email);
            
            ps.executeUpdate();
            
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }
            throw new SQLException("Failed to get generated user_id");
        }
    }
    
    /**
     * Insert student into students table
     */
    private static void insertStudent(Connection conn, int userId, String usn) throws SQLException {
        String sql = "INSERT INTO students (student_id, branch, current_sem, projects, experience) " +
                     "VALUES (?, 'ISE', 3, '', '') " +
                     "ON DUPLICATE KEY UPDATE student_id = student_id";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }
    
    /**
     * Insert academic record
     */
    private static void insertAcademicRecord(Connection conn, int userId, int semester, Double sgpa, Double cgpa) throws SQLException {
        if (sgpa == null || sgpa <= 0) {
            return; // Skip invalid SGPA
        }
        
        String sql = "INSERT INTO academic_records (student_id, semester, sgpa, cgpa, marks, max_marks, subject) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE sgpa = VALUES(sgpa), cgpa = VALUES(cgpa)";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, semester);
            ps.setDouble(3, sgpa);
            ps.setDouble(4, cgpa != null ? cgpa : sgpa);
            ps.setDouble(5, sgpa * 10); // Approximate marks
            ps.setDouble(6, 100.0); // Max marks
            ps.setString(7, "Semester " + semester);
            
            ps.executeUpdate();
        }
    }
    
    /**
     * Generate email from USN
     * Format: usn@msrit.edu
     */
    private static String generateEmail(String usn) {
        return usn.toLowerCase() + "@msrit.edu";
    }
    
    /**
     * Parse CSV line handling quoted fields
     */
    private static String[] parseCsvLine(String line) {
        // Simple CSV parser - handles basic cases
        // For production, consider using Apache Commons CSV
        return line.split(",", -1);
    }
    
    /**
     * Parse double value safely
     */
    private static Double parseDouble(String value) {
        if (value == null || value.trim().isEmpty() || "N/A".equalsIgnoreCase(value.trim()) || "TAL".equalsIgnoreCase(value.trim())) {
            return null;
        }
        
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Get import statistics
     */
    public static String getImportStatus() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT COUNT(*) as count FROM users WHERE role = 'STUDENT'";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                
                if (rs.next()) {
                    int count = rs.getInt("count");
                    return String.format("Students in database: %d", count);
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get import status", e);
        }
        return "Status unavailable";
    }
}
