package com.rit.placement.controller;

import com.rit.placement.factory.DAOFactory;

import com.rit.placement.dao.UserDAO;
import com.rit.placement.model.User;
import com.rit.placement.service.MetricsService;
import com.rit.placement.util.EmailUtil;
import com.rit.placement.util.DBConnection;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * OTP Servlet - Handles OTP generation and sending
 * 
 * FLOW:
 * 1. User enters USN
 * 2. System fetches user from database
 * 3. Generates 6-digit OTP
 * 4. Sends OTP to user's email
 * 5. Stores OTP in session with timestamp
 * 6. Redirects to OTP verification page
 */
@WebServlet("/otp/send")
public class OTPServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(OTPServlet.class);
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");
    private final UserDAO userDAO = DAOFactory.getInstance().getUserDAO();
    private final MetricsService metricsService = MetricsService.getInstance();
    private static final int OTP_VALIDITY_MINUTES = 5;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Redirect to login page
        resp.sendRedirect(req.getContextPath() + "/pages/login-otp.jsp");
    }

    private static final java.util.concurrent.ConcurrentHashMap<String, Long> ipCooldowns = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.concurrent.ConcurrentHashMap<String, Long> usnCooldowns = new java.util.concurrent.ConcurrentHashMap<>();
    private static final long COOLDOWN_MS = 60 * 1000; // 60 seconds

    private String generateMockMaskedEmail(String usn) {
        if (usn == null || usn.trim().isEmpty()) {
            return "stu****@rit.edu";
        }
        String cleanUsn = usn.trim().toLowerCase();
        if (cleanUsn.length() >= 5) {
            return cleanUsn.substring(0, 3) + "****@rit.edu";
        }
        return "stu****@rit.edu";
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 1. Get USN from request
        String usn = req.getParameter("usn");
        String clientIp = req.getRemoteAddr();
        long now = System.currentTimeMillis();
        
        logger.info("OTP request received for USN: {} from IP: {}", usn, clientIp);

        // 2. Validate input
        if (usn == null || usn.trim().isEmpty()) {
            logger.warn("OTP request failed: USN is empty from IP: {}", clientIp);
            req.getSession().setAttribute("errorMessage", "USN is required");
            resp.sendRedirect(req.getContextPath() + "/pages/login-otp.jsp");
            return;
        }

        usn = usn.trim().toUpperCase();

        // 3. Cooldown check
        if (ipCooldowns.containsKey(clientIp)) {
            long lastSend = ipCooldowns.get(clientIp);
            if (now - lastSend < COOLDOWN_MS) {
                logger.warn("IP {} is rate limited", clientIp);
                req.getSession().setAttribute("errorMessage", "Too many OTP requests. Please wait a minute.");
                resp.sendRedirect(req.getContextPath() + "/pages/login-otp.jsp");
                return;
            }
        }
        if (usnCooldowns.containsKey(usn)) {
            long lastSend = usnCooldowns.get(usn);
            if (now - lastSend < COOLDOWN_MS) {
                logger.warn("USN {} is rate limited", usn);
                req.getSession().setAttribute("errorMessage", "Too many OTP requests. Please wait a minute.");
                resp.sendRedirect(req.getContextPath() + "/pages/login-otp.jsp");
                return;
            }
        }

        // Update cooldowns
        ipCooldowns.put(clientIp, now);
        usnCooldowns.put(usn, now);

        try {
            // 4. Fetch user from database
            User user = userDAO.getUserByUSN(usn);
            
            // Set up a session for OTP verification
            HttpSession session = req.getSession(true);

            if (user == null || !user.isActive()) {
                // Return generic response to prevent USN enumeration / inactive account enumeration
                String maskedEmail = generateMockMaskedEmail(usn);
                logger.warn("OTP request failed for USN: {} (User not found or inactive). Initiating dummy flow.", usn);
                
                // Setup dummy OTP session attributes so attacker gets redirected to verify page
                // but can never successfully verify any OTP entered.
                session.setAttribute("otp", "DUMMY_" + java.util.UUID.randomUUID().toString());
                session.setAttribute("otp_timestamp", System.currentTimeMillis());
                session.setAttribute("otp_usn", usn);
                session.setAttribute("otp_user_id", -1);
                session.setAttribute("otp_user_name", "Student");
                session.setAttribute("otp_user_role", "STUDENT");
                session.setAttribute("otp_email", maskedEmail);
                session.setAttribute("otp_attempts", 0);
                
                session.setMaxInactiveInterval(OTP_VALIDITY_MINUTES * 60);
                session.setAttribute("successMessage", "If your USN is registered and active, an OTP has been sent.");
                resp.sendRedirect(req.getContextPath() + "/pages/verify-otp.jsp");
                return;
            }

            // Get user's email
            String email = getUserEmail(user.getUserId());
            
            if (email == null || email.trim().isEmpty() || !EmailUtil.isValidEmail(email)) {
                // Even if email is missing or malformed, return generic success to prevent enumeration
                logger.error("OTP request failed: No valid email found for USN: {} (UserID: {}). Initiating dummy flow.", usn, user.getUserId());
                
                String maskedEmail = generateMockMaskedEmail(usn);
                session.setAttribute("otp", "DUMMY_" + java.util.UUID.randomUUID().toString());
                session.setAttribute("otp_timestamp", System.currentTimeMillis());
                session.setAttribute("otp_usn", usn);
                session.setAttribute("otp_user_id", -1);
                session.setAttribute("otp_user_name", "Student");
                session.setAttribute("otp_user_role", "STUDENT");
                session.setAttribute("otp_email", maskedEmail);
                session.setAttribute("otp_attempts", 0);
                
                session.setMaxInactiveInterval(OTP_VALIDITY_MINUTES * 60);
                session.setAttribute("successMessage", "If your USN is registered and active, an OTP has been sent.");
                resp.sendRedirect(req.getContextPath() + "/pages/verify-otp.jsp");
                return;
            }

            // Generate OTP
            String otp = EmailUtil.generateOTP();
            logger.debug("OTP generated for USN: {}", usn);
            
            // Send OTP via email
            boolean emailSent = EmailUtil.sendOTPEmail(email, otp, user.getName());
            
            if (!emailSent) {
                logger.error("OTP email sending failed for USN: {} to email: {}", usn, maskEmail(email));
                metricsService.recordError();
                req.getSession().setAttribute("errorMessage", "Failed to send OTP. Please try again.");
                resp.sendRedirect(req.getContextPath() + "/pages/login-otp.jsp");
                return;
            }

            String maskedEmail = maskEmail(email);
            logger.info("OTP sent successfully to {} for USN: {}", maskedEmail, usn);
            auditLogger.info("OTP_SENT - USN: {}, Email: {}, IP: {}", usn, maskedEmail, clientIp);

            // Store OTP and user info in session
            session.setAttribute("otp", otp);
            session.setAttribute("otp_timestamp", System.currentTimeMillis());
            session.setAttribute("otp_usn", usn);
            session.setAttribute("otp_user_id", user.getUserId());
            session.setAttribute("otp_user_name", user.getName());
            session.setAttribute("otp_user_role", user.getRole());
            session.setAttribute("otp_email", maskedEmail);
            session.setAttribute("otp_attempts", 0);

            // Set session timeout for OTP (5 minutes)
            session.setMaxInactiveInterval(OTP_VALIDITY_MINUTES * 60);

            // Redirect to OTP verification page
            session.setAttribute("successMessage", "If your USN is registered and active, an OTP has been sent.");
            resp.sendRedirect(req.getContextPath() + "/pages/verify-otp.jsp");

        } catch (Exception e) {
            logger.error("Error processing OTP request for USN: {} from IP: {}", usn, clientIp, e);
            metricsService.recordError();
            req.getSession().setAttribute("errorMessage", "Error processing request.");
            resp.sendRedirect(req.getContextPath() + "/pages/login-otp.jsp");
        }
    }

    /**
     * Get user's email from database
     */
    private String getUserEmail(int userId) throws Exception {
        String sql = "SELECT email FROM users WHERE user_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getString("email");
            }
        }
        return null;
    }

    /**
     * Mask email for security (show only first 2 chars and domain)
     * Example: st****@example.com
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***@***.com";
        }
        
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];
        
        if (localPart.length() <= 2) {
            return localPart + "***@" + domain;
        }
        
        return localPart.substring(0, 2) + "****@" + domain;
    }
}
