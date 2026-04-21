package com.rit.placement.dao;

import com.rit.placement.BaseTest;
import com.rit.placement.model.Notification;
import com.rit.placement.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for NotificationDAO
 */
@DisplayName("NotificationDAO Tests")
class NotificationDAOTest extends BaseTest {
    
    private TestNotificationDAO notificationDAO;
    private TestUserDAO userDAO;
    private int testUserId;
    
    @BeforeEach
    public void setUpDAO() throws SQLException {
        notificationDAO = new TestNotificationDAO();
        userDAO = new TestUserDAO();
        
        // Create test user
        User user = new User();
        user.setUsn("1RI21IS200");
        user.setName("Test User");
        user.setPasswordHash("password");
        user.setRole("STUDENT");
        user.setActive(true);
        testUserId = userDAO.insertUser(user);
    }
    
    @Test
    @DisplayName("Should create notification successfully")
    void testCreateNotification() throws SQLException {
        // Given: A notification
        Notification notification = new Notification();
        notification.setUserId(testUserId);
        notification.setTitle("Test Notification");
        notification.setMessage("This is a test message");
        notification.setType("GENERAL");
        
        // When: Creating notification
        int notificationId = notificationDAO.createNotification(notification);
        
        // Then: Notification should be created
        assertThat(notificationId).isGreaterThan(0);
        
        // And: Notification should be retrievable
        List<Notification> notifications = notificationDAO.getNotificationsByUser(testUserId);
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).getTitle()).isEqualTo("Test Notification");
    }
    
    @Test
    @DisplayName("Should get notifications by user")
    void testGetNotificationsByUser() throws SQLException {
        // Given: Multiple notifications for user
        createTestNotification(testUserId, "Notification 1", "APPLICATION");
        createTestNotification(testUserId, "Notification 2", "STATUS_UPDATE");
        createTestNotification(testUserId, "Notification 3", "INTERVIEW");
        
        // When: Getting notifications
        List<Notification> notifications = notificationDAO.getNotificationsByUser(testUserId);
        
        // Then: Should return all notifications
        assertThat(notifications).hasSize(3);
        assertThat(notifications).extracting(Notification::getTitle)
            .contains("Notification 1", "Notification 2", "Notification 3");
    }
    
    @Test
    @DisplayName("Should get unread count")
    void testGetUnreadCount() throws SQLException {
        // Given: Multiple notifications, some read
        int id1 = createTestNotification(testUserId, "Unread 1", "GENERAL");
        int id2 = createTestNotification(testUserId, "Unread 2", "GENERAL");
        int id3 = createTestNotification(testUserId, "Read 1", "GENERAL");
        
        notificationDAO.markAsRead(id3);
        
        // When: Getting unread count
        int unreadCount = notificationDAO.getUnreadCount(testUserId);
        
        // Then: Should return correct count
        assertThat(unreadCount).isEqualTo(2);
    }
    
    @Test
    @DisplayName("Should mark notification as read")
    void testMarkAsRead() throws SQLException {
        // Given: An unread notification
        int notificationId = createTestNotification(testUserId, "Test", "GENERAL");
        assertThat(notificationDAO.getUnreadCount(testUserId)).isEqualTo(1);
        
        // When: Marking as read
        notificationDAO.markAsRead(notificationId);
        
        // Then: Unread count should decrease
        assertThat(notificationDAO.getUnreadCount(testUserId)).isEqualTo(0);
    }
    
    @Test
    @DisplayName("Should mark all notifications as read")
    void testMarkAllAsRead() throws SQLException {
        // Given: Multiple unread notifications
        createTestNotification(testUserId, "Unread 1", "GENERAL");
        createTestNotification(testUserId, "Unread 2", "GENERAL");
        createTestNotification(testUserId, "Unread 3", "GENERAL");
        
        assertThat(notificationDAO.getUnreadCount(testUserId)).isEqualTo(3);
        
        // When: Marking all as read
        notificationDAO.markAllAsRead(testUserId);
        
        // Then: All should be marked as read
        assertThat(notificationDAO.getUnreadCount(testUserId)).isEqualTo(0);
    }
    
    @Test
    @DisplayName("Should handle notification with related_id")
    void testNotificationWithRelatedId() throws SQLException {
        // Given: A notification with related_id
        Notification notification = new Notification();
        notification.setUserId(testUserId);
        notification.setTitle("Application Update");
        notification.setMessage("Your application has been updated");
        notification.setType("STATUS_UPDATE");
        notification.setRelatedId(123);
        
        // When: Creating notification
        int notificationId = notificationDAO.createNotification(notification);
        
        // Then: Related ID should be stored
        List<Notification> notifications = notificationDAO.getNotificationsByUser(testUserId);
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).getRelatedId()).isEqualTo(123);
    }
    
    @Test
    @DisplayName("Should order notifications by created_at DESC")
    void testNotificationOrdering() throws SQLException, InterruptedException {
        // Given: Notifications created at different times
        createTestNotification(testUserId, "First", "GENERAL");
        Thread.sleep(10); // Small delay to ensure different timestamps
        createTestNotification(testUserId, "Second", "GENERAL");
        Thread.sleep(10);
        createTestNotification(testUserId, "Third", "GENERAL");
        
        // When: Getting notifications
        List<Notification> notifications = notificationDAO.getNotificationsByUser(testUserId);
        
        // Then: Should be ordered newest first
        assertThat(notifications).hasSize(3);
        assertThat(notifications.get(0).getTitle()).isEqualTo("Third");
        assertThat(notifications.get(1).getTitle()).isEqualTo("Second");
        assertThat(notifications.get(2).getTitle()).isEqualTo("First");
    }
    
    private int createTestNotification(int userId, String title, String type) throws SQLException {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage("Test message");
        notification.setType(type);
        return notificationDAO.createNotification(notification);
    }
}
