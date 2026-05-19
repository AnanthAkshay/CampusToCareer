package com.rit.placement.dao;

import com.rit.placement.model.Notification;
import com.rit.placement.util.MockDBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Mock version of NotificationDAO that uses MockDBConnection
 */
public class MockNotificationDAO {
    
    public int createNotification(Notification notification) throws SQLException {
        String sql = "INSERT INTO notifications (user_id, title, message, type, related_id) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = MockDBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, notification.getUserId());
            stmt.setString(2, notification.getTitle());
            stmt.setString(3, notification.getMessage());
            stmt.setString(4, notification.getType());
            
            if (notification.getRelatedId() != null) {
                stmt.setInt(5, notification.getRelatedId());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
            
            stmt.executeUpdate();
            
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new SQLException("Failed to retrieve generated notification_id");
        }
    }
    
    public List<Notification> getNotificationsByUser(int userId) throws SQLException {
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC LIMIT 50";
        List<Notification> notifications = new ArrayList<>();
        
        try (Connection conn = MockDBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    notifications.add(mapRow(rs));
                }
            }
        }
        
        return notifications;
    }
    
    public int getUnreadCount(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = FALSE";
        
        try (Connection conn = MockDBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        
        return 0;
    }
    
    public void markAsRead(int notificationId) throws SQLException {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE notification_id = ?";
        
        try (Connection conn = MockDBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, notificationId);
            stmt.executeUpdate();
        }
    }
    
    public void markAllAsRead(int userId) throws SQLException {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE user_id = ? AND is_read = FALSE";
        
        try (Connection conn = MockDBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }
    
    private Notification mapRow(ResultSet rs) throws SQLException {
        Notification notification = new Notification();
        notification.setNotificationId(rs.getInt("notification_id"));
        notification.setUserId(rs.getInt("user_id"));
        notification.setTitle(rs.getString("title"));
        notification.setMessage(rs.getString("message"));
        notification.setType(rs.getString("type"));
        notification.setRead(rs.getBoolean("is_read"));
        
        int relatedId = rs.getInt("related_id");
        if (!rs.wasNull()) {
            notification.setRelatedId(relatedId);
        }
        
        notification.setCreatedAt(rs.getTimestamp("created_at"));
        return notification;
    }
}
