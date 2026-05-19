package com.rit.placement.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rit.placement.model.Notification;
import com.rit.placement.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for the 'notifications' table
 */
public class NotificationDAO {
    private static final Logger logger = LoggerFactory.getLogger(NotificationDAO.class);
    
    /**
     * Create a new notification
     */
    public int createNotification(Notification notification) throws SQLException {
        String sql = "INSERT INTO notifications (user_id, title, message, type, related_id) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
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
            
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }
            throw new SQLException("Failed to retrieve generated notification_id");
        }
    }
    
    /**
     * Get notifications for a user
     */
    public List<Notification> getNotificationsByUser(int userId) throws SQLException {
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC LIMIT 50";
        List<Notification> notifications = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
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
    
    /**
     * Get unread notifications count
     */
    public int getUnreadCount(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = FALSE";
        
        try (Connection conn = DBConnection.getConnection();
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
    
    /**
     * Mark notification as read
     */
    public void markAsRead(int notificationId) throws SQLException {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE notification_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, notificationId);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Mark all notifications as read for a user
     */
    public void markAllAsRead(int userId) throws SQLException {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE user_id = ? AND is_read = FALSE";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Map ResultSet to Notification object
     */
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
