package com.rit.placement.model;

import java.sql.Timestamp;

/**
 * JavaBean representing a row in the 'notifications' table
 */
public class Notification {
    private int notificationId;
    private int userId;
    private String title;
    private String message;
    private String type;  // APPLICATION, INTERVIEW, STATUS_UPDATE, GENERAL
    private boolean isRead;
    private Integer relatedId;
    private Timestamp createdAt;
    
    // Getters and Setters
    public int getNotificationId() { return notificationId; }
    public void setNotificationId(int notificationId) { this.notificationId = notificationId; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public boolean isRead() { return isRead; }
    public void setRead(boolean isRead) { this.isRead = isRead; }
    
    public Integer getRelatedId() { return relatedId; }
    public void setRelatedId(Integer relatedId) { this.relatedId = relatedId; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
