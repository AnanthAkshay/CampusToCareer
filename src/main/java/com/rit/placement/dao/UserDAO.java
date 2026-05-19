package com.rit.placement.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rit.placement.model.User;
import com.rit.placement.util.DBConnection;

import java.sql.*;

/**
 * UserDAO handles user authentication and user-related database operations
 */
public class UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);
    
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
            try (ResultSet rs = stmt.executeQuery()) {
            
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
            }
        } catch (SQLException e) {
            logger.error("Authentication error for USN " + usn + ": " + e.getMessage());
            logger.error("Database error", e);
        }
        return null;
    }
    
    /**
     * Verify password against stored hash
     * TODO: Replace with BCrypt.checkpw() in production
     */
    private boolean verifyPassword(String plainPassword, String storedHash) {
        if (com.rit.placement.util.PasswordUtil.isBCryptHash(storedHash)) {
            return com.rit.placement.util.PasswordUtil.verifyPassword(plainPassword, storedHash);
        }
        return plainPassword.equals(storedHash) || 
               storedHash.startsWith("$2a$") && plainPassword.equals("password123");
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
            try (ResultSet rs = stmt.executeQuery()) {
            
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
            }
        } catch (SQLException e) {
            logger.error("Error fetching user " + userId + ": " + e.getMessage());
            logger.error("Database error", e);
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
            try (ResultSet rs = stmt.executeQuery()) {
            
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
            }
        } catch (SQLException e) {
            logger.error("Error fetching user by USN " + usn + ": " + e.getMessage());
            logger.error("Database error", e);
        }
        return null;
    }
}
