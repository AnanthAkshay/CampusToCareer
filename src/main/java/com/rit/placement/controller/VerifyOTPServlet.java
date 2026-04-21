package com.rit.placement.controller;

import com.rit.placement.service.MetricsService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Verify OTP Servlet - Handles OTP verification and login
 * 
 * FLOW:
 * 1. User enters OTP
 * 2. System validates OTP from session
 * 3. Checks OTP expiry (5 minutes)
 * 4. If valid, creates user session
 * 5. Redirects to role-specific dashboard
 */
@WebServlet("/otp/verify")
public class VerifyOTPServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(VerifyOTPServlet.class);
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");
    private final MetricsService metricsService = MetricsService.getInstance();
    private static final int OTP_VALIDITY_MINUTES = 5;
    private static final int MAX_OTP_ATTEMPTS = 3;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Redirect to OTP verification page
        resp.sendRedirect(req.getContextPath() + "/pages/verify-otp.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);

        // 1. Validate session exists
        if (session == null) {
            resp.sendRedirect(req.getContextPath() + "/pages/login-otp.jsp?error=session_expired");
            return;
        }

        // 2. Get OTP from request
        String enteredOTP = req.getParameter("otp");

        // 3. Validate input
        if (enteredOTP == null || enteredOTP.trim().isEmpty()) {
            session.setAttribute("errorMessage", "Please enter OTP");
            resp.sendRedirect(req.getContextPath() + "/pages/verify-otp.jsp");
            return;
        }

        enteredOTP = enteredOTP.trim();

        // 4. Validate OTP format (6 digits)
        if (!enteredOTP.matches("\\d{6}")) {
            session.setAttribute("errorMessage", "OTP must be 6 digits");
            resp.sendRedirect(req.getContextPath() + "/pages/verify-otp.jsp");
            return;
        }

        // 5. Get stored OTP from session
        String storedOTP = (String) session.getAttribute("otp");
        Long otpTimestamp = (Long) session.getAttribute("otp_timestamp");
        Integer userId = (Integer) session.getAttribute("otp_user_id");
        String userName = (String) session.getAttribute("otp_user_name");
        String userRole = (String) session.getAttribute("otp_user_role");
        String usn = (String) session.getAttribute("otp_usn");

        // 6. Validate OTP exists in session
        if (storedOTP == null || otpTimestamp == null || userId == null) {
            session.setAttribute("errorMessage", "OTP session expired. Please request a new OTP.");
            resp.sendRedirect(req.getContextPath() + "/pages/login-otp.jsp");
            return;
        }

        // 7. Check OTP expiry (5 minutes)
        long currentTime = System.currentTimeMillis();
        long otpAge = (currentTime - otpTimestamp) / 1000; // in seconds
        long maxAge = OTP_VALIDITY_MINUTES * 60; // in seconds

        if (otpAge > maxAge) {
            // OTP expired - clear session
            clearOTPSession(session);
            session.setAttribute("errorMessage", "OTP expired. Please request a new OTP.");
            resp.sendRedirect(req.getContextPath() + "/pages/login-otp.jsp");
            return;
        }

        // 8. Track failed attempts
        Integer attempts = (Integer) session.getAttribute("otp_attempts");
        if (attempts == null) {
            attempts = 0;
        }

        // 9. Verify OTP
        if (!enteredOTP.equals(storedOTP)) {
            attempts++;
            session.setAttribute("otp_attempts", attempts);
            
            String clientIp = req.getRemoteAddr();
            logger.warn("Invalid OTP attempt for USN: {}, Attempt: {}/{}, IP: {}", 
                usn, attempts, MAX_OTP_ATTEMPTS, clientIp);

            // Check if max attempts reached
            if (attempts >= MAX_OTP_ATTEMPTS) {
                auditLogger.info("LOGIN_FAILED - USN: {}, Reason: Max OTP attempts exceeded, IP: {}", 
                    usn, clientIp);
                metricsService.recordLogin(userRole != null ? userRole : "UNKNOWN", false);
                
                clearOTPSession(session);
                session.setAttribute("errorMessage", "Maximum OTP attempts exceeded. Please request a new OTP.");
                resp.sendRedirect(req.getContextPath() + "/pages/login-otp.jsp");
                return;
            }

            int remainingAttempts = MAX_OTP_ATTEMPTS - attempts;
            session.setAttribute("errorMessage", "Invalid OTP. " + remainingAttempts + " attempt(s) remaining.");
            resp.sendRedirect(req.getContextPath() + "/pages/verify-otp.jsp");
            return;
        }

        // 10. OTP is valid - Create user session
        // Clear old session and create new one for security
        clearOTPSession(session);
        
        // Set user session attributes
        session.setAttribute("user_id", userId);
        session.setAttribute("usn", usn);
        session.setAttribute("name", userName);
        session.setAttribute("role", userRole);
        session.setAttribute("login_time", System.currentTimeMillis());
        session.setAttribute("login_method", "OTP");

        // Set session timeout to 15 minutes for logged-in users
        session.setMaxInactiveInterval(15 * 60);

        // 11. Record metrics and audit log
        metricsService.recordLogin(userRole, true);
        
        String clientIp = req.getRemoteAddr();
        auditLogger.info("LOGIN_SUCCESS - USN: {}, UserId: {}, Role: {}, Method: OTP, IP: {}", 
            usn, userId, userRole, clientIp);
        
        logger.info("User logged in successfully via OTP: {} ({}), IP: {}", usn, userRole, clientIp);

        // 12. Redirect to role-specific dashboard
        String dashboardPath = resolveDashboardPath(userRole);
        session.setAttribute("successMessage", "Login successful! Welcome, " + userName);
        resp.sendRedirect(req.getContextPath() + dashboardPath);
    }

    /**
     * Clear OTP-related session attributes
     */
    private void clearOTPSession(HttpSession session) {
        session.removeAttribute("otp");
        session.removeAttribute("otp_timestamp");
        session.removeAttribute("otp_usn");
        session.removeAttribute("otp_user_id");
        session.removeAttribute("otp_user_name");
        session.removeAttribute("otp_user_role");
        session.removeAttribute("otp_email");
        session.removeAttribute("otp_attempts");
    }

    /**
     * Resolve dashboard path based on user role
     */
    private String resolveDashboardPath(String role) {
        if (role == null) {
            return "/pages/login-otp.jsp";
        }

        switch (role.trim().toUpperCase()) {
            case "STUDENT":
                return "/student/dashboard";
            case "PROCTOR":
                return "/proctor/dashboard";
            case "COORDINATOR":
            case "ADMIN":
                return "/admin/dashboard";
            case "COMPANY":
                return "/company/dashboard";
            default:
                return "/pages/login-otp.jsp";
        }
    }
}
