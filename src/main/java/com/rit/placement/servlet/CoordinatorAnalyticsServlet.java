package com.rit.placement.servlet;

import com.rit.placement.dao.AnalyticsDAO;
import com.rit.placement.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Map;

/**
 * CoordinatorAnalyticsServlet provides placement intelligence dashboard
 */
@WebServlet("/admin/analytics")
public class CoordinatorAnalyticsServlet extends HttpServlet {
    private AnalyticsDAO analyticsDAO = new AnalyticsDAO();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }
        
        User user = (User) session.getAttribute("user");
        // Allow ADMIN, COORDINATOR, and FACULTY to access analytics
        if (!"ADMIN".equals(user.getRole()) && 
            !"COORDINATOR".equals(user.getRole()) && 
            !"FACULTY".equals(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/dashboard.jsp");
            return;
        }
        
        try {
            // Fetch KPI metrics
            int totalStudents = analyticsDAO.countTotalStudents();
            int eligibleStudents = analyticsDAO.countEligibleStudents(6.0); // CGPA >= 6.0
            int studentsApplied = analyticsDAO.countStudentsApplied();
            int studentsShortlisted = analyticsDAO.countStudentsShortlisted();
            int studentsPlaced = analyticsDAO.countStudentsPlaced();
            int totalApplications = analyticsDAO.countTotalApplications();
            int activeJobs = analyticsDAO.countActiveJobs();
            
            // Calculate placement percentage
            double placementPercentage = totalStudents > 0 
                ? (studentsPlaced * 100.0 / totalStudents) 
                : 0.0;
            
            // Calculate application rate
            double applicationRate = eligibleStudents > 0
                ? (studentsApplied * 100.0 / eligibleStudents)
                : 0.0;
            
            // Fetch chart data
            Map<String, Integer> applicationsByCompany = analyticsDAO.getApplicationsByCompany();
            Map<String, Integer> monthlyPlacements = analyticsDAO.getMonthlyPlacements();
            Map<String, Integer> statusBreakdown = analyticsDAO.getPlacementStatusBreakdown();
            
            // Set attributes for JSP
            request.setAttribute("totalStudents", totalStudents);
            request.setAttribute("eligibleStudents", eligibleStudents);
            request.setAttribute("studentsApplied", studentsApplied);
            request.setAttribute("studentsShortlisted", studentsShortlisted);
            request.setAttribute("studentsPlaced", studentsPlaced);
            request.setAttribute("totalApplications", totalApplications);
            request.setAttribute("activeJobs", activeJobs);
            request.setAttribute("placementPercentage", placementPercentage);
            request.setAttribute("applicationRate", applicationRate);
            
            request.setAttribute("applicationsByCompany", applicationsByCompany);
            request.setAttribute("monthlyPlacements", monthlyPlacements);
            request.setAttribute("statusBreakdown", statusBreakdown);
            
            request.getRequestDispatcher("/WEB-INF/views/admin/admin_analytics.jsp").forward(request, response);
            
        } catch (Exception e) {
            System.err.println("Error loading analytics dashboard: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("error", "Failed to load analytics data. Please try again.");
            request.getRequestDispatcher("/WEB-INF/views/admin/admin_analytics.jsp").forward(request, response);
        }
    }
}
