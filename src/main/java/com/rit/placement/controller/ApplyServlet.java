package com.rit.placement.controller;

import com.rit.placement.dao.ApplicationDAO;
import com.rit.placement.dao.JobPostingDAO;
import com.rit.placement.model.Application;
import com.rit.placement.model.JobPosting;
import com.rit.placement.model.JobApplicationStatus;
import com.rit.placement.service.EligibilityService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Apply Servlet - Handles job applications
 * GET: Display available jobs with eligibility and application status
 * POST: Submit application
 */
@WebServlet("/apply")
public class ApplyServlet extends HttpServlet {

    private final JobPostingDAO jobPostingDAO = new JobPostingDAO();
    private final ApplicationDAO applicationDAO = new ApplicationDAO();
    private final EligibilityService eligibilityService = new EligibilityService();

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
            // 3. Fetch all active job postings
            List<JobPosting> allJobs = jobPostingDAO.getAllJobPostings();

            // 4. Create JobApplicationStatus for each job
            List<JobApplicationStatus> jobStatuses = new ArrayList<>();
            
            for (JobPosting job : allJobs) {
                boolean hasApplied = applicationDAO.hasApplied(userId, job.getJobId());
                boolean eligible = eligibilityService.isEligible(userId, job.getJobId());
                
                JobApplicationStatus status = new JobApplicationStatus(job, hasApplied, eligible);
                jobStatuses.add(status);
            }

            // 5. Set attributes for JSP
            req.setAttribute("jobStatuses", jobStatuses);

            // 6. Forward to apply.jsp
            req.getRequestDispatcher("/pages/apply.jsp").forward(req, resp);

        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Error loading jobs: " + e.getMessage());
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

            // 8. Set success message and redirect
            session.setAttribute("successMessage", "Application submitted successfully! (ID: " + applicationId + ")");
            resp.sendRedirect(req.getContextPath() + "/my-applications");

        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "Invalid job ID format");
            resp.sendRedirect(req.getContextPath() + "/apply");
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "Error submitting application: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/apply");
        }
    }
}
