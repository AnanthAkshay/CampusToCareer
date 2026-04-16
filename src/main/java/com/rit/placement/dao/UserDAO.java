package com.rit.placement.dao;

import com.rit.placement.model.User;
import com.rit.placement.util.DBConnection;
import java.sql.*;

/**
 * DAO for the 'users' table.
 *
 * CONTRACT: This DAO stores the password value already present on the User
 * object. Hashing must be performed by the caller before invoking
 * insertUser(), so the DAO never risks double-hashing credentials.
 *
 * Correct usage:
 *   String hash = PasswordUtil.hashPassword(plainPassword);
 *   user.setPasswordHash(hash);
 *   userDAO.insertUser(user);
 */
public class UserDAO {

    /**
     * Inserts a new user and returns the generated user_id.
     *
     * @param u User object with the password value already prepared for storage.
     *          Call PasswordUtil.hashPassword() before passing the User here.
     * @return the generated user_id (auto-increment PK)
     */
    public int insertUser(User u) throws SQLException {
        String sql = "INSERT INTO users (usn, name, password_hash, role, is_active) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            String storedPasswordValue = u.getPasswordHash();
            if (storedPasswordValue == null || storedPasswordValue.trim().isEmpty()) {
                throw new SQLException("User password value must be prepared before DAO insert.");
            }

            ps.setString(1, u.getUsn());
            ps.setString(2, u.getName());
            ps.setString(3, storedPasswordValue);
            ps.setString(4, u.getRole());
            ps.setBoolean(5, u.isActive());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
            throw new SQLException("Failed to retrieve generated user_id.");
        }
    }

    /** Fetch a user by USN (unique). Returns null if not found. */
    public User getUserByUSN(String usn) throws SQLException {
        String sql = "SELECT * FROM users WHERE usn = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usn);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapRow(rs) : null;
        }
    }

    /** Fetch a user by primary key. Returns null if not found. */
    public User getUserById(int userId) throws SQLException {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapRow(rs) : null;
        }
    }

    /** Updates the stored password hash for an existing user. */
    public void updatePasswordHash(int userId, String passwordHash) throws SQLException {
        String sql = "UPDATE users SET password_hash = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    /** Maps a ResultSet row to a User object. */
    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setUsn(rs.getString("usn"));
        u.setName(rs.getString("name"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRole(rs.getString("role"));
        u.setActive(rs.getBoolean("is_active"));
        u.setCreatedAt(rs.getTimestamp("created_at"));
        return u;
    }
}
