package com.rit.placement.controller;

import com.rit.placement.dao.NotificationDAO;
import com.rit.placement.model.Notification;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

/**
 * Notifications Page Servlet
 * Handles: /notifications
 */
@WebServlet("/notifications")
public class NotificationsPageServlet extends HttpServlet {
    
    private final NotificationDAO notificationDAO = new NotificationDAO();
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        // Validate session
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            resp.sendRedirect(req.getContextPath() + "/pages/login-otp.jsp");
            return;
        }
        
        Integer userId = (Integer) session.getAttribute("user_id");
        
        try {
            // Get all notifications
            List<Notification> notifications = notificationDAO.getNotificationsByUser(userId);
            
            // Get unread count
            int unreadCount = notificationDAO.getUnreadCount(userId);
            
            // Set attributes
            req.setAttribute("notifications", notifications);
            req.setAttribute("unreadCount", unreadCount);
            
            // Forward to JSP
            req.getRequestDispatcher("/pages/notifications.jsp").forward(req, resp);
            
        } catch (Exception e) {
            System.err.println("Error loading notifications page: " + e.getMessage());
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to load notifications");
        }
    }
}
