package com.rit.placement.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rit.placement.factory.DAOFactory;

import com.rit.placement.dao.NotificationDAO;
import com.rit.placement.model.Notification;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Notifications API Servlet
 * Handles: /api/notifications
 * 
 * Features:
 * - GET: Fetch user's notifications
 * - POST: Mark notifications as read
 */
@WebServlet("/api/notifications")
public class NotificationsServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(NotificationsServlet.class);
    
    private final NotificationDAO notificationDAO = DAOFactory.getInstance().getNotificationDAO();
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        // Validate session
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"error\": \"Unauthorized\"}");
            return;
        }
        
        Integer userId = (Integer) session.getAttribute("user_id");
        
        try {
            // Get action parameter
            String action = req.getParameter("action");
            
            if ("count".equals(action)) {
                // Get unread count
                int unreadCount = notificationDAO.getUnreadCount(userId);
                
                resp.setContentType("application/json");
                resp.getWriter().write("{\"unreadCount\": " + unreadCount + "}");
                
            } else {
                // Get all notifications
                List<Notification> notifications = notificationDAO.getNotificationsByUser(userId);
                
                // Convert to JSON
                resp.setContentType("application/json");
                PrintWriter out = resp.getWriter();
                
                out.write("{\"notifications\": [");
                for (int i = 0; i < notifications.size(); i++) {
                    Notification notif = notifications.get(i);
                    out.write("{");
                    out.write("\"id\": " + notif.getNotificationId() + ",");
                    out.write("\"title\": \"" + escapeJson(notif.getTitle()) + "\",");
                    out.write("\"message\": \"" + escapeJson(notif.getMessage()) + "\",");
                    out.write("\"type\": \"" + escapeJson(notif.getType()) + "\",");
                    out.write("\"isRead\": " + notif.isRead() + ",");
                    out.write("\"createdAt\": \"" + notif.getCreatedAt() + "\"");
                    out.write("}");
                    if (i < notifications.size() - 1) {
                        out.write(",");
                    }
                }
                out.write("]}");
            }
            
        } catch (Exception e) {
            logger.error("Error fetching notifications.");
            logger.error("Exception occurred: ", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Failed to fetch notifications\"}");
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        // Validate session
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"error\": \"Unauthorized\"}");
            return;
        }
        
        Integer userId = (Integer) session.getAttribute("user_id");
        
        try {
            String action = req.getParameter("action");
            
            if ("markRead".equals(action)) {
                String notifIdStr = req.getParameter("id");
                
                if (notifIdStr != null && !notifIdStr.isEmpty()) {
                    int notificationId = Integer.parseInt(notifIdStr);
                    notificationDAO.markAsRead(notificationId);
                } else {
                    // Mark all as read
                    notificationDAO.markAllAsRead(userId);
                }
                
                resp.setContentType("application/json");
                resp.getWriter().write("{\"success\": true}");
            }
            
        } catch (Exception e) {
            logger.error("Error marking notification as read.");
            logger.error("Exception occurred: ", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Failed to update notification\"}");
        }
    }
    
    /**
     * Escape special characters for JSON
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
