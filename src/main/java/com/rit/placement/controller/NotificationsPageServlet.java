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
import java.util.List;

/**
 * Notifications Page Servlet
 * Handles: /notifications
 */
@WebServlet("/notifications")
public class NotificationsPageServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(NotificationsPageServlet.class);
    
    private final NotificationDAO notificationDAO = DAOFactory.getInstance().getNotificationDAO();
    
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
            logger.error("Error loading notifications page.");
            logger.error("Exception occurred: ", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to load notifications");
        }
    }
}
