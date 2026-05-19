package com.rit.placement.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rit.placement.factory.DAOFactory;

import com.rit.placement.dao.ApplicationDAO;
import com.rit.placement.model.Application;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;
import java.util.List;

/**
 * Admin Applications Servlet - Manage all applications
 * Allows coordinators to view and update application status
 */
@WebServlet("/admin/applications")
public class AdminApplicationsServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(AdminApplicationsServlet.class);

    private final ApplicationDAO applicationDAO = DAOFactory.getInstance().getApplicationDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Session validation
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String role = (String) session.getAttribute("role");
        if (!"COORDINATOR".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }

        try {
            // Get all applications
            List<Application> applications = applicationDAO.getAllApplications();

            // Get statistics
            ApplicationDAO.ApplicationStats globalStats = getGlobalStats();

            // Set attributes for JSP
            req.setAttribute("applications", applications);
            req.setAttribute("totalApplications", applications.size());
            req.setAttribute("stats", globalStats);

            // Forward to JSP
            req.getRequestDispatcher("/pages/admin_applications.jsp").forward(req, resp);

        } catch (Exception e) {
            logger.error("Exception occurred: ", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Error loading applications.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Session validation
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String role = (String) session.getAttribute("role");
        if (!"COORDINATOR".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }

        try {
            // Get parameters
            String action = req.getParameter("action");
            String applicationIdStr = req.getParameter("application_id");
            String newStatus = req.getParameter("status");

            if ("update_status".equals(action) && applicationIdStr != null && newStatus != null) {
                int applicationId = Integer.parseInt(applicationIdStr);
                
                // Validate status
                if (isValidStatus(newStatus)) {
                    applicationDAO.updateStatus(applicationId, newStatus);
                    session.setAttribute("successMessage", "Application status updated successfully");
                } else {
                    session.setAttribute("errorMessage", "Invalid status value");
                }
            }

            // Redirect back to applications page
            resp.sendRedirect(req.getContextPath() + "/admin/applications");

        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "Invalid application ID");
            resp.sendRedirect(req.getContextPath() + "/admin/applications");
        } catch (Exception e) {
            logger.error("Exception occurred: ", e);
            session.setAttribute("errorMessage", "Error updating application.");
            resp.sendRedirect(req.getContextPath() + "/admin/applications");
        }
    }

    /**
     * Validate application status
     */
    private boolean isValidStatus(String status) {
        return "APPLIED".equals(status) || 
               "SHORTLISTED".equals(status) || 
               "SELECTED".equals(status) || 
               "REJECTED".equals(status);
    }

    /**
     * Get global application statistics
     */
    private ApplicationDAO.ApplicationStats getGlobalStats() throws Exception {
        try (var conn = com.rit.placement.util.DBConnection.getConnection();
             var ps = conn.prepareStatement(
                 "SELECT " +
                 "COUNT(*) as total, " +
                 "SUM(CASE WHEN status = 'APPLIED' THEN 1 ELSE 0 END) as applied, " +
                 "SUM(CASE WHEN status = 'SHORTLISTED' THEN 1 ELSE 0 END) as shortlisted, " +
                 "SUM(CASE WHEN status = 'SELECTED' THEN 1 ELSE 0 END) as selected, " +
                 "SUM(CASE WHEN status = 'REJECTED' THEN 1 ELSE 0 END) as rejected " +
                 "FROM applications")) {
            
            var rs = ps.executeQuery();
            if (rs.next()) {
                return new ApplicationDAO.ApplicationStats(
                    rs.getInt("total"),
                    rs.getInt("applied"),
                    rs.getInt("shortlisted"),
                    rs.getInt("selected"),
                    rs.getInt("rejected")
                );
            }
        }
        return new ApplicationDAO.ApplicationStats(0, 0, 0, 0, 0);
    }
}
