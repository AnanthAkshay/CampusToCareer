package com.rit.placement.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rit.placement.factory.DAOFactory;

import com.rit.placement.dao.JobPostingDAO;
import com.rit.placement.dao.StudentDAO;
import com.rit.placement.dao.UserDAO;
import com.rit.placement.model.JobPosting;
import com.rit.placement.model.Student;
import com.rit.placement.model.StudentEligibility;
import com.rit.placement.model.User;
import com.rit.placement.service.EligibilityService;
import com.rit.placement.util.CGPACalculator;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Eligible Students Servlet - Shows eligible students for a job posting
 * GET: Display eligible students for a specific job
 */
@WebServlet("/eligible-students")
public class EligibleStudentsServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(EligibleStudentsServlet.class);

    private final EligibilityService eligibilityService = new EligibilityService();
    private final JobPostingDAO jobPostingDAO = DAOFactory.getInstance().getJobPostingDAO();
    private final UserDAO userDAO = DAOFactory.getInstance().getUserDAO();
    private final StudentDAO studentDAO = DAOFactory.getInstance().getStudentDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 1. Validate session
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String role = (String) session.getAttribute("role");

        // 2. Validate role (only COORDINATOR and ADMIN can view eligible students)
        if (!"COORDINATOR".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }

        try {
            // 3. Get job_id parameter
            String jobIdStr = req.getParameter("job_id");
            if (jobIdStr == null || jobIdStr.trim().isEmpty()) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "job_id parameter is required");
                return;
            }

            int jobId = Integer.parseInt(jobIdStr);

            // 4. Fetch job posting details
            JobPosting job = jobPostingDAO.getJobPostingById(jobId);
            if (job == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Job posting not found");
                return;
            }

            // 5. Get all students and check eligibility
            List<StudentEligibility> eligibleStudents = new ArrayList<>();
            List<StudentEligibility> ineligibleStudents = new ArrayList<>();

            // Get all students (users with STUDENT role)
            List<User> allUsers = getAllStudentUsers();

            for (User user : allUsers) {
                try {
                    // Fetch student details
                    Student student = studentDAO.getStudentById(user.getUserId());
                    if (student == null) continue;

                    // Calculate CGPA
                    double cgpa = CGPACalculator.calculateCGPA(user.getUserId());
                    if (cgpa < 0) cgpa = 0.0; // No records

                    // Check eligibility
                    boolean eligible = eligibilityService.isEligible(user.getUserId(), jobId);

                    // Create StudentEligibility object
                    StudentEligibility se = new StudentEligibility(
                        user.getUserId(),
                        user.getName(),
                        user.getUsn(),
                        student.getBranch(),
                        cgpa,
                        eligible
                    );

                    // Add to appropriate list
                    if (eligible) {
                        eligibleStudents.add(se);
                    } else {
                        ineligibleStudents.add(se);
                    }

                } catch (SQLException e) {
                    // Log and continue with next student
                    logger.error("Exception occurred: ", e);
                }
            }

            // 6. Set attributes for JSP
            req.setAttribute("job", job);
            req.setAttribute("eligibleStudents", eligibleStudents);
            req.setAttribute("ineligibleStudents", ineligibleStudents);
            req.setAttribute("totalStudents", allUsers.size());
            req.setAttribute("eligibleCount", eligibleStudents.size());
            req.setAttribute("ineligibleCount", ineligibleStudents.size());

            // 7. Forward to eligible_students.jsp
            req.getRequestDispatcher("/pages/eligible_students.jsp").forward(req, resp);

        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid job_id format");
        } catch (Exception e) {
            logger.error("Exception occurred: ", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Error loading eligible students.");
        }
    }

    /**
     * Get all users with STUDENT role.
     * This is a helper method - in production, you might want to add this to UserDAO.
     */
    private List<User> getAllStudentUsers() throws SQLException {
        // For now, we'll use a simple query
        // In production, add this method to UserDAO
        List<User> students = new ArrayList<>();
        
        try (var conn = com.rit.placement.util.DBConnection.getConnection();
             var ps = conn.prepareStatement(
                 "SELECT * FROM users WHERE role = 'STUDENT' AND is_active = TRUE ORDER BY name")) {
            
            var rs = ps.executeQuery();
            while (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsn(rs.getString("usn"));
                user.setName(rs.getString("name"));
                user.setRole(rs.getString("role"));
                user.setActive(rs.getBoolean("is_active"));
                students.add(user);
            }
        }
        
        return students;
    }
}
