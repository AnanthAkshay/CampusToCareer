package com.rit.placement.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rit.placement.service.JobRecommendationService;
import com.rit.placement.service.JobRecommendationService.RecommendedJob;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

/**
 * Job Recommendations Servlet
 * Provides personalized job recommendations to students
 */
@WebServlet("/student/recommendations")
public class JobRecommendationsServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(JobRecommendationsServlet.class);
    
    private final JobRecommendationService recommendationService = new JobRecommendationService();
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        // 1. Validate session
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            resp.sendRedirect(req.getContextPath() + "/pages/login-otp.jsp");
            return;
        }
        
        Integer userId = (Integer) session.getAttribute("user_id");
        String role = (String) session.getAttribute("role");
        
        // 2. Validate role - only students can access
        if (!"STUDENT".equalsIgnoreCase(role)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied. Only students can view recommendations.");
            return;
        }
        
        try {
            // 3. Get limit parameter (default: 10)
            int limit = 10;
            String limitParam = req.getParameter("limit");
            if (limitParam != null && !limitParam.isEmpty()) {
                try {
                    limit = Integer.parseInt(limitParam);
                    if (limit < 1) limit = 10;
                    if (limit > 50) limit = 50; // Max 50 recommendations
                } catch (NumberFormatException e) {
                    limit = 10;
                }
            }
            
            // 4. Get recommendations
            List<RecommendedJob> recommendations = recommendationService.getRecommendedJobs(userId, limit);
            
            // 5. Set attributes
            req.setAttribute("recommendations", recommendations);
            req.setAttribute("totalRecommendations", recommendations.size());
            
            // 6. Forward to JSP
            req.getRequestDispatcher("/pages/job_recommendations.jsp").forward(req, resp);
            
        } catch (Exception e) {
            logger.error("ERROR in JobRecommendationsServlet.");
            logger.error("User ID: " + userId);
            logger.error("Exception occurred: ", e);
            
            req.setAttribute("error", "Unable to load recommendations. Please try again later.");
            req.getRequestDispatcher("/pages/error/generic_error.jsp").forward(req, resp);
        }
    }
}
