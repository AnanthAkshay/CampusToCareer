package com.rit.placement.controller;

import com.rit.placement.dao.ApplicationDAO;
import com.rit.placement.dao.CompanyDAO;
import com.rit.placement.dao.JobPostingDAO;
import com.rit.placement.dao.StudentDAO;
import com.rit.placement.util.DBConnection;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;
import java.sql.*;

/**
 * Admin Dashboard Servlet - Main control panel for coordinators
 * Displays key statistics and analytics
 */
@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {

    private final StudentDAO studentDAO = new StudentDAO();
    private final ApplicationDAO applicationDAO = new ApplicationDAO();
    private final CompanyDAO companyDAO = new CompanyDAO();
    private final JobPostingDAO jobPostingDAO = new JobPostingDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Session validation (SessionFilter already handles this)
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String role = (String) session.getAttribute("role");
        if (!"COORDINATOR".equalsIgnoreCase(role)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }

        try {
            // Fetch dashboard statistics
            DashboardStats stats = getDashboardStats();

            // Set attributes for JSP
            req.setAttribute("totalStudents", stats.totalStudents);
            req.setAttribute("totalApplications", stats.totalApplications);
            req.setAttribute("totalCompanies", stats.totalCompanies);
            req.setAttribute("totalJobs", stats.totalJobs);
            req.setAttribute("selectedStudents", stats.selectedStudents);
            req.setAttribute("shortlistedStudents", stats.shortlistedStudents);
            req.setAttribute("pendingApplications", stats.pendingApplications);
            req.setAttribute("activeJobs", stats.activeJobs);

            // Branch-wise placement data for charts
            req.setAttribute("branchData", getBranchWisePlacements());

            // Forward to JSP
            req.getRequestDispatcher("/pages/admin_dashboard.jsp").forward(req, resp);

        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Error loading dashboard: " + e.getMessage());
        }
    }

    /**
     * Get dashboard statistics
     */
    private DashboardStats getDashboardStats() throws SQLException {
        DashboardStats stats = new DashboardStats();

        try (Connection conn = DBConnection.getConnection()) {
            // Total students
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM students")) {
                ResultSet rs = ps.executeQuery();
                if (rs.next()) stats.totalStudents = rs.getInt(1);
            }

            // Total applications
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM applications")) {
                ResultSet rs = ps.executeQuery();
                if (rs.next()) stats.totalApplications = rs.getInt(1);
            }

            // Total companies
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM companies")) {
                ResultSet rs = ps.executeQuery();
                if (rs.next()) stats.totalCompanies = rs.getInt(1);
            }

            // Total job postings
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM job_postings")) {
                ResultSet rs = ps.executeQuery();
                if (rs.next()) stats.totalJobs = rs.getInt(1);
            }

            // Selected students
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(DISTINCT student_id) FROM applications WHERE status = 'SELECTED'")) {
                ResultSet rs = ps.executeQuery();
                if (rs.next()) stats.selectedStudents = rs.getInt(1);
            }

            // Shortlisted students
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(DISTINCT student_id) FROM applications WHERE status = 'SHORTLISTED'")) {
                ResultSet rs = ps.executeQuery();
                if (rs.next()) stats.shortlistedStudents = rs.getInt(1);
            }

            // Pending applications
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM applications WHERE status = 'APPLIED'")) {
                ResultSet rs = ps.executeQuery();
                if (rs.next()) stats.pendingApplications = rs.getInt(1);
            }

            // Active jobs (deadline not passed)
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM job_postings WHERE deadline >= CURDATE()")) {
                ResultSet rs = ps.executeQuery();
                if (rs.next()) stats.activeJobs = rs.getInt(1);
            }
        }

        return stats;
    }

    /**
     * Get branch-wise placement statistics for charts
     */
    private String getBranchWisePlacements() throws SQLException {
        StringBuilder json = new StringBuilder("[");
        
        String sql = "SELECT s.branch, COUNT(DISTINCT a.student_id) as placed_count " +
                     "FROM students s " +
                     "LEFT JOIN applications a ON s.student_id = a.student_id AND a.status = 'SELECTED' " +
                     "GROUP BY s.branch " +
                     "ORDER BY s.branch";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            boolean first = true;
            while (rs.next()) {
                if (!first) json.append(",");
                json.append("{\"branch\":\"").append(rs.getString("branch"))
                    .append("\",\"count\":").append(rs.getInt("placed_count"))
                    .append("}");
                first = false;
            }
        }
        
        json.append("]");
        return json.toString();
    }

    /**
     * Inner class for dashboard statistics
     */
    private static class DashboardStats {
        int totalStudents = 0;
        int totalApplications = 0;
        int totalCompanies = 0;
        int totalJobs = 0;
        int selectedStudents = 0;
        int shortlistedStudents = 0;
        int pendingApplications = 0;
        int activeJobs = 0;
    }
}
