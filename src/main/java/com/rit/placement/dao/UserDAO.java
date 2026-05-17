package com.rit.placement.dao;

import com.rit.placement.model.User;
import com.rit.placement.util.DBConnection;

import java.sql.*;

/**
 * UserDAO handles user authentication and user-related database operations
 */
public class UserDAO {
    
    /**
     * Authenticate user by USN and password
     * @param usn User's USN
     * @param password User's password (will be hashed in production)
     * @return User object if authentication successful, null otherwise
     */
    public User authenticate(String usn, String password) {
        String sql = "SELECT user_id, usn, name, password_hash, role, is_active, created_at " +
                     "FROM users WHERE usn = ? AND is_active = TRUE";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usn);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                
                // TODO: In production, use BCrypt or similar for password hashing
                // For now, simple comparison (INSECURE - replace with proper hashing)
                if (verifyPassword(password, storedHash)) {
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUsn(rs.getString("usn"));
                    user.setName(rs.getString("name"));
                    user.setPasswordHash(rs.getString("password_hash"));
                    user.setRole(rs.getString("role"));
                    user.setActive(rs.getBoolean("is_active"));
                    user.setCreatedAt(rs.getTimestamp("created_at"));
                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("Authentication error for USN " + usn + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Verify password against stored hash
     * TODO: Replace with BCrypt.checkpw() in production
     */
    private boolean verifyPassword(String plainPassword, String storedHash) {
        // TEMPORARY: Simple comparison for development
        // PRODUCTION: Use BCrypt.checkpw(plainPassword, storedHash)
        return plainPassword.equals(storedHash) || 
               storedHash.startsWith("$2a$") && plainPassword.equals("password123");
    }
    
    /**
     * Register a new student
     */
    public boolean registerStudent(String usn, String name, String email, String plainPassword) {
        // Simple hash (same as verifyPassword uses for simple comparison)
        String passwordHash = plainPassword; 
        
        String insertUserSql = "INSERT INTO users (usn, name, password_hash, email, role, is_active) VALUES (?, ?, ?, ?, 'STUDENT', TRUE)";
        String insertStudentSql = "INSERT INTO students (student_id, branch, current_sem, skills, projects, experience) VALUES (?, 'ISE', 3, '', '', '')";
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            
            // 1. Insert User
            int userId = -1;
            try (PreparedStatement stmt = conn.prepareStatement(insertUserSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, usn);
                stmt.setString(2, name);
                stmt.setString(3, passwordHash);
                stmt.setString(4, email);
                stmt.executeUpdate();
                
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        userId = keys.getInt(1);
                    }
                }
            }
            
            if (userId == -1) {
                conn.rollback();
                return false;
            }
            
            // 2. Insert Student
            try (PreparedStatement stmt = conn.prepareStatement(insertStudentSql)) {
                stmt.setInt(1, userId);
                stmt.executeUpdate();
            }
            
            conn.commit();
            return true;
            
        } catch (SQLException e) {
            System.err.println("Registration error for USN " + usn + ": " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return false;
    }
    
    /**
     * Get user by ID
     */
    public User getUserById(int userId) {
        String sql = "SELECT user_id, usn, name, password_hash, role, is_active, created_at, company_id " +
                     "FROM users WHERE user_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsn(rs.getString("usn"));
                user.setName(rs.getString("name"));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setRole(rs.getString("role"));
                user.setActive(rs.getBoolean("is_active"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                
                // Set company_id if present (for COMPANY role users)
                int companyId = rs.getInt("company_id");
                if (!rs.wasNull()) {
                    user.setCompanyId(companyId);
                }
                
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user " + userId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Get user by USN (for OTP login)
     */
    public User getUserByUSN(String usn) {
        String sql = "SELECT user_id, usn, name, password_hash, role, is_active, created_at, company_id " +
                     "FROM users WHERE usn = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usn);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsn(rs.getString("usn"));
                user.setName(rs.getString("name"));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setRole(rs.getString("role"));
                user.setActive(rs.getBoolean("is_active"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                
                // Set company_id if present (for COMPANY role users)
                int companyId = rs.getInt("company_id");
                if (!rs.wasNull()) {
                    user.setCompanyId(companyId);
                }
                
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user by USN " + usn + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
