package com.rit.placement.controller;

import com.rit.placement.factory.DAOFactory;

import com.rit.placement.dao.UserDAO;
import com.rit.placement.model.User;
import com.rit.placement.service.MetricsService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Login Servlet - Handles password-based authentication for Staff (Admin, Coordinator, Proctor, Company)
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(LoginServlet.class);
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");
    private final UserDAO userDAO = DAOFactory.getInstance().getUserDAO();
    private final MetricsService metricsService = MetricsService.getInstance();

    /** GET redirects to unified login page */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.sendRedirect(req.getContextPath() + "/pages/login-otp.jsp");
    }

    /** POST handles USN & password authentication */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String usn = req.getParameter("usn");
        String password = req.getParameter("password");
        String clientIp = req.getRemoteAddr();

        if (usn == null || usn.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            req.getSession().setAttribute("errorMessage", "Username and Password are required");
            resp.sendRedirect(req.getContextPath() + "/pages/login-otp.jsp");
            return;
        }

        usn = usn.trim().toUpperCase();

        try {
            logger.info("Password login attempt for USN: {} from IP: {}", usn, clientIp);
            User user = userDAO.authenticate(usn, password);

            if (user == null) {
                logger.warn("Password login failed: Invalid credentials for USN: {} from IP: {}", usn, clientIp);
                auditLogger.info("LOGIN_ATTEMPT_FAILED - USN: {}, Reason: Invalid credentials, IP: {}", usn, clientIp);
                metricsService.recordLogin("UNKNOWN", false);
                req.getSession().setAttribute("errorMessage", "Invalid username or password");
                resp.sendRedirect(req.getContextPath() + "/pages/login-otp.jsp");
                return;
            }

            // Check if user is active
            if (!user.isActive()) {
                logger.warn("Password login failed: Inactive account for USN: {} from IP: {}", usn, clientIp);
                auditLogger.info("LOGIN_ATTEMPT_FAILED - USN: {}, Reason: Account inactive, IP: {}", usn, clientIp);
                metricsService.recordLogin(user.getRole(), false);
                req.getSession().setAttribute("errorMessage", "Your account is inactive. Please contact admin.");
                resp.sendRedirect(req.getContextPath() + "/pages/login-otp.jsp");
                return;
            }

            // Prevent Session Fixation
            HttpSession oldSession = req.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }
            
            // Create user session
            HttpSession session = req.getSession(true);
            session.setAttribute("user_id", user.getUserId());
            session.setAttribute("usn", user.getUsn());
            session.setAttribute("name", user.getName());
            session.setAttribute("role", user.getRole());
            session.setAttribute("login_time", System.currentTimeMillis());
            session.setAttribute("login_method", "PASSWORD");

            // Set session timeout (15 minutes)
            session.setMaxInactiveInterval(15 * 60);

            // Record metrics
            metricsService.recordLogin(user.getRole(), true);
            auditLogger.info("LOGIN_SUCCESS - USN: {}, UserId: {}, Role: {}, Method: PASSWORD, IP: {}", 
                usn, user.getUserId(), user.getRole(), clientIp);
            logger.info("User logged in successfully via Password: {} ({}), IP: {}", usn, user.getRole(), clientIp);

            // Redirect based on role
            String dashboardPath = resolveDashboardPath(user.getRole());
            session.setAttribute("successMessage", "Login successful! Welcome, " + user.getName());
            resp.sendRedirect(req.getContextPath() + dashboardPath);

        } catch (SecurityException se) {
            logger.warn("Account lockout for USN: {} from IP: {}", usn, clientIp);
            req.getSession().setAttribute("errorMessage", se.getMessage());
            resp.sendRedirect(req.getContextPath() + "/pages/login-otp.jsp");
        } catch (Exception e) {
            logger.error("Error during password login for USN: {} from IP: {}", usn, clientIp, e);
            metricsService.recordError();
            req.getSession().setAttribute("errorMessage", "Error processing login.");
            resp.sendRedirect(req.getContextPath() + "/pages/login-otp.jsp");
        }
    }

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
