package com.rit.placement.controller;

import com.rit.placement.dao.AcademicDAO;
import com.rit.placement.dao.ApplicationDAO;
import com.rit.placement.dao.StudentDAO;
import com.rit.placement.dao.UserDAO;
import com.rit.placement.model.AcademicRecord;
import com.rit.placement.model.Student;
import com.rit.placement.model.User;
import com.rit.placement.service.ReadinessService;
import com.rit.placement.service.JobRecommendationService;
import com.rit.placement.service.JobRecommendationService.RecommendedJob;
import com.rit.placement.util.CGPACalculator;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;
import java.util.List;

/**
 * Student Dashboard Servlet - Fetches real data from database
 * Handles: /student/dashboard
 */
@WebServlet("/student/dashboard")
public class StudentDashboardServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final AcademicDAO academicDAO = new AcademicDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final ApplicationDAO applicationDAO = new ApplicationDAO();
    private final ReadinessService readinessService = new ReadinessService();
    private final JobRecommendationService recommendationService = new JobRecommendationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 1. Read and validate session
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        Integer userId = (Integer) session.getAttribute("user_id");
        String role = (String) session.getAttribute("role");

        // 2. Validate role (only STUDENT can access)
        if (!"STUDENT".equalsIgnoreCase(role)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }

        try {
            // 3. Fetch student details from database
            User user = userDAO.getUserById(userId);
            if (user == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "User not found");
                return;
            }

            String name = user.getName();
            String usn = user.getUsn();

            // 4. Fetch academic records
            List<AcademicRecord> records = academicDAO.getRecordsByStudentId(userId);

            // 5. Extract SGPA for semester 2 and 3
            Double sem2Sgpa = null;
            Double sem3Sgpa = null;

            for (AcademicRecord record : records) {
                if (record.getSemester() == 2) {
                    sem2Sgpa = record.getSgpa();
                } else if (record.getSemester() == 3) {
                    sem3Sgpa = record.getSgpa();
                }
            }

            // 6. Calculate CGPA using CGPACalculator
            double cgpa = CGPACalculator.calculateCGPA(userId);

            // 7. Fetch student profile for readiness calculation
            Student student = studentDAO.getStudentById(userId);
            if (student == null) {
                // Create a minimal student object if not found
                student = new Student();
                student.setStudentId(userId);
                student.setSkills("");
                student.setProjects("");
                student.setExperience("");
            }
            student.setCgpa(cgpa >= 0 ? cgpa : 0.0);

            // 8. Count applications
            int applicationsCount = applicationDAO.getApplicationsByStudent(userId).size();

            // 9. Calculate readiness score and get recommendations
            int readinessScore = readinessService.calculateScore(student, applicationsCount);
            List<String> recommendations = readinessService.getRecommendations(student, applicationsCount);
            String readinessLevel = readinessService.getReadinessLevel(readinessScore);
            String readinessColor = readinessService.getReadinessColor(readinessScore);
            
            // 10. Get top 5 job recommendations
            List<RecommendedJob> jobRecommendations = recommendationService.getRecommendedJobs(userId, 5);

            // 11. Set request attributes for JSP
            req.setAttribute("name", name);
            req.setAttribute("usn", usn);
            req.setAttribute("sem2_sgpa", sem2Sgpa);
            req.setAttribute("sem3_sgpa", sem3Sgpa);
            req.setAttribute("cgpa", cgpa >= 0 ? cgpa : null);
            
            // Readiness attributes
            req.setAttribute("readinessScore", readinessScore);
            req.setAttribute("readinessLevel", readinessLevel);
            req.setAttribute("readinessColor", readinessColor);
            req.setAttribute("recommendations", recommendations);
            req.setAttribute("applicationsCount", applicationsCount);
            
            // Job recommendations
            req.setAttribute("jobRecommendations", jobRecommendations);

            // 12. Forward to dashboard.jsp
            req.getRequestDispatcher("/pages/dashboard.jsp").forward(req, resp);

        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Error loading dashboard: " + e.getMessage());
        }
    }
}
