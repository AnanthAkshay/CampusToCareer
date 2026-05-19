package com.rit.placement.controller;

import com.rit.placement.factory.DAOFactory;

import com.rit.placement.dao.ApplicationDAO;
import com.rit.placement.dao.JobPostingDAO;
import com.rit.placement.model.Application;
import com.rit.placement.model.JobPosting;
import com.rit.placement.model.JobApplicationStatus;
import com.rit.placement.service.EligibilityService;
import com.rit.placement.service.NotificationService;
import com.rit.placement.service.MetricsService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import com.rit.placement.model.Student;
import com.rit.placement.dao.StudentDAO;
import com.rit.placement.util.CGPACalculator;

/**
 * Apply Servlet - Handles job applications
 * GET: Display available jobs with eligibility and application status
 * POST: Submit application
 */
@WebServlet("/apply")
public class ApplyServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(ApplyServlet.class);
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");
    
    private final JobPostingDAO jobPostingDAO = DAOFactory.getInstance().getJobPostingDAO();
    private final ApplicationDAO applicationDAO = DAOFactory.getInstance().getApplicationDAO();
    private final EligibilityService eligibilityService = new EligibilityService();
    private final NotificationService notificationService = new NotificationService();
    private final MetricsService metricsService = MetricsService.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 1. Validate session
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        Integer userId = (Integer) session.getAttribute("user_id");
        String role = (String) session.getAttribute("role");

        // 2. Validate role (only STUDENT can apply)
        if (!"STUDENT".equalsIgnoreCase(role)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Only students can apply for jobs");
            return;
        }

        try {
            // Pagination setup
            int page = 1;
            int limit = 10;
            String pageParam = req.getParameter("page");
            if (pageParam != null && !pageParam.isEmpty()) {
                page = Integer.parseInt(pageParam);
            }
            int offset = (page - 1) * limit;

            // Optimize N+1 query: Single LEFT JOIN query with pagination
            List<JobApplicationStatus> jobStatuses = jobPostingDAO.getJobsWithApplicationStatus(userId, limit, offset);

            // Cache CGPA in Session to prevent repeated DB calculation
            Double cgpa = (Double) session.getAttribute("cached_cgpa");
            if (cgpa == null) {
                cgpa = CGPACalculator.calculateCGPA(userId);
                session.setAttribute("cached_cgpa", cgpa);
            }
            
            Student student = DAOFactory.getInstance().getStudentDAO().getStudentById(userId);

            // Compute eligibility once per job in memory
            for (JobApplicationStatus status : jobStatuses) {
                boolean eligible = eligibilityService.isEligible(student, cgpa, status.getJob());
                status.setEligible(eligible);
            }

            req.setAttribute("jobStatuses", jobStatuses);
            req.setAttribute("currentPage", page);
            req.setAttribute("limit", limit);

            // 6. Forward to apply.jsp
            req.getRequestDispatcher("/pages/apply.jsp").forward(req, resp);

        } catch (Exception e) {
            logger.error("Error loading jobs for student {}", userId, e);
            metricsService.recordError();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Error loading jobs.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 1. Validate session
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        Integer userId = (Integer) session.getAttribute("user_id");
        String role = (String) session.getAttribute("role");

        // 2. Validate role
        if (!"STUDENT".equalsIgnoreCase(role)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Only students can apply for jobs");
            return;
        }

        try {
            // 3. Get job_id parameter
            String jobIdStr = req.getParameter("job_id");
            if (jobIdStr == null || jobIdStr.trim().isEmpty()) {
                session.setAttribute("errorMessage", "Invalid job ID");
                resp.sendRedirect(req.getContextPath() + "/apply");
                return;
            }

            int jobId = Integer.parseInt(jobIdStr);

            // 4. Check if already applied
            if (applicationDAO.hasApplied(userId, jobId)) {
                session.setAttribute("errorMessage", "You have already applied for this job");
                resp.sendRedirect(req.getContextPath() + "/apply");
                return;
            }

            // 5. Check eligibility
            boolean eligible = eligibilityService.isEligible(userId, jobId);
            if (!eligible) {
                session.setAttribute("errorMessage", "You are not eligible for this job");
                resp.sendRedirect(req.getContextPath() + "/apply");
                return;
            }

            // 6. Create application
            Application application = new Application();
            application.setStudentId(userId);
            application.setJobId(jobId);
            application.setStatus("APPLIED");

            // 7. Insert application
            int applicationId = applicationDAO.insertApplication(application);
            
            // Record metrics
            metricsService.recordApplicationSubmitted();
            
            // Audit log
            auditLogger.info("APPLICATION_SUBMITTED - StudentId: {}, JobId: {}, ApplicationId: {}", 
                userId, jobId, applicationId);
            
            logger.info("Application submitted: StudentId {}, JobId {}, ApplicationId {}", 
                userId, jobId, applicationId);
            
            // 8. Get job details for notification
            JobPosting job = jobPostingDAO.getJobPostingById(jobId);
            
            // 9. Send notification to student
            if (job != null) {
                notificationService.notifyJobApplication(
                    userId, 
                    job.getCompanyName(), 
                    job.getRole(), 
                    applicationId
                );
                
                // 10. Send notification to company
                String studentName = (String) session.getAttribute("name");
                if (studentName == null) studentName = "A student";
                notificationService.notifyCompanyOfApplication(
                    job.getCompanyId(),
                    studentName,
                    job.getRole(),
                    applicationId
                );
            }

            // 11. Set success message and redirect
            session.setAttribute("successMessage", "Application submitted successfully! (ID: " + applicationId + ")");
            resp.sendRedirect(req.getContextPath() + "/my-applications");

        } catch (NumberFormatException e) {
            logger.warn("Invalid job ID format for student {}: {}", userId, req.getParameter("job_id"));
            session.setAttribute("errorMessage", "Invalid job ID format");
            resp.sendRedirect(req.getContextPath() + "/apply");
        } catch (Exception e) {
            logger.error("Error submitting application for student {}, jobId: {}", 
                userId, req.getParameter("job_id"), e);
            metricsService.recordError();
            session.setAttribute("errorMessage", "Error submitting application.");
            resp.sendRedirect(req.getContextPath() + "/apply");
        }
    }
}
