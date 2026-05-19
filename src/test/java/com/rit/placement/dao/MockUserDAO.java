package com.rit.placement.dao;

import com.rit.placement.model.User;
import com.rit.placement.util.MockDBConnection;

import java.sql.*;

/**
 * Mock version of UserDAO that uses MockDBConnection
 */
public class MockUserDAO {
    
    public User getUserByUSN(String usn) {
        String sql = "SELECT user_id, usn, name, password_hash, role, is_active, created_at, company_id " +
                     "FROM users WHERE usn = ?";
        
        try (Connection conn = MockDBConnection.getConnection();
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
                    
                    int companyId = rs.getInt("company_id");
                    if (!rs.wasNull()) {
                        user.setCompanyId(companyId);
                    }
                    
                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user by USN " + usn + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    public User getUserById(int userId) {
        String sql = "SELECT user_id, usn, name, password_hash, role, is_active, created_at, company_id " +
                     "FROM users WHERE user_id = ?";
        
        try (Connection conn = MockDBConnection.getConnection();
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
                    
                    int companyId = rs.getInt("company_id");
                    if (!rs.wasNull()) {
                        user.setCompanyId(companyId);
                    }
                    
                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user " + userId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    public int insertUser(User user) throws SQLException {
        String sql = "INSERT INTO users (usn, name, password_hash, role, is_active, company_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = MockDBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getUsn());
            stmt.setString(2, user.getName());
            stmt.setString(3, user.getPasswordHash());
            stmt.setString(4, user.getRole());
            stmt.setBoolean(5, user.isActive());
            
            if (user.getCompanyId() != null) {
                stmt.setInt(6, user.getCompanyId());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }
            
            stmt.executeUpdate();
            
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new SQLException("Failed to retrieve generated user_id");
        }
    }
}
