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

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 1. Get USN from request
        String usn = req.getParameter("usn");
        String clientIp = req.getRemoteAddr();
        
        logger.info("OTP request received for USN: {} from IP: {}", usn, clientIp);

        // 2. Validate input
        if (usn == null || usn.trim().isEmpty()) {
            logger.warn("OTP request failed: USN is empty from IP: {}", clientIp);
            req.getSession().setAttribute("errorMessage", "USN is required");
            resp.sendRedirect(req.getContextPath() + "/pages/login-otp.jsp");
            return;
        }

        usn = usn.trim().toUpperCase();

        try {
            // 3. Fetch user from database
            User user = userDAO.getUserByUSN(usn);

            if (user == null) {
                logger.warn("OTP request failed: User not found for USN: {} from IP: {}", usn, clientIp);
                auditLogger.info("LOGIN_ATTEMPT_FAILED - USN: {}, Reason: User not found, IP: {}", usn, clientIp);
                metricsService.recordLogin("UNKNOWN", false);
                req.getSession().setAttribute("errorMessage", "User not found with USN: " + usn);
                resp.sendRedirect(req.getContextPath() + "/pages/login-otp.jsp");
                return;
            }

            // 4. Check if user is active
            if (!user.isActive()) {
                logger.warn("OTP request failed: Inactive account for USN: {} from IP: {}", usn, clientIp);
                auditLogger.info("LOGIN_ATTEMPT_FAILED - USN: {}, Reason: Account inactive, IP: {}", usn, clientIp);
                metricsService.recordLogin(user.getRole(), false);
                req.getSession().setAttribute("errorMessage", "Your account is inactive. Please contact admin.");
                resp.sendRedirect(req.getContextPath() + "/pages/login-otp.jsp");
                return;
            }

            // 5. Get user's email
            String email = getUserEmail(user.getUserId());
            
            if (email == null || email.trim().isEmpty()) {
                logger.error("OTP request failed: No email found for USN: {} (UserID: {})", usn, user.getUserId());
                req.getSession().setAttribute("errorMessage", "No email found for this user. Please contact admin.");
                resp.sendRedirect(req.getContextPath() + "/pages/login-otp.jsp");
                return;
            }

            // 6. Validate email format
            if (!EmailUtil.isValidEmail(email)) {
                logger.error("OTP request failed: Invalid email format for USN: {} (Email: {})", usn, email);
                req.getSession().setAttribute("errorMessage", "Invalid email format. Please contact admin.");
                resp.sendRedirect(req.getContextPath() + "/pages/login-otp.jsp");
                return;
            }

            // 7. Generate OTP
            String otp = EmailUtil.generateOTP();
            logger.debug("OTP generated for USN: {}", usn);
            
            // 8. Send OTP via email
            boolean emailSent = EmailUtil.sendOTPEmail(email, otp, user.getName());
            
            if (!emailSent) {
                logger.error("OTP email sending failed for USN: {} to email: {}", usn, maskEmail(email));
                metricsService.recordError();
                req.getSession().setAttribute("errorMessage", "Failed to send OTP. Please try again.");
                resp.sendRedirect(req.getContextPath() + "/pages/login-otp.jsp");
                return;
            }

            logger.info("OTP sent successfully to {} for USN: {}", maskEmail(email), usn);
            auditLogger.info("OTP_SENT - USN: {}, Email: {}, IP: {}", usn, maskEmail(email), clientIp);

            // 9. Store OTP and user info in session
            HttpSession session = req.getSession(true);
            session.setAttribute("otp", otp);
            session.setAttribute("otp_timestamp", System.currentTimeMillis());
            session.setAttribute("otp_usn", usn);
            session.setAttribute("otp_user_id", user.getUserId());
            session.setAttribute("otp_user_name", user.getName());
            session.setAttribute("otp_user_role", user.getRole());
            session.setAttribute("otp_email", maskEmail(email));

            // 10. Set session timeout for OTP (5 minutes)
            session.setMaxInactiveInterval(OTP_VALIDITY_MINUTES * 60);

            // 11. Redirect to OTP verification page
            session.setAttribute("successMessage", "OTP sent to " + maskEmail(email));
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
