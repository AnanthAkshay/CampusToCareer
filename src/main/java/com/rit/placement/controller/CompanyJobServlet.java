package com.rit.placement.controller;

import com.rit.placement.factory.DAOFactory;

import com.rit.placement.dao.CompanyDAO;
import com.rit.placement.dao.JobPostingDAO;
import com.rit.placement.dao.UserDAO;
import com.rit.placement.dao.StudentDAO;
import com.rit.placement.model.Company;
import com.rit.placement.model.JobPosting;
import com.rit.placement.model.User;
import com.rit.placement.model.Student;
import com.rit.placement.service.NotificationService;
import com.rit.placement.service.EligibilityService;
import com.rit.placement.service.MetricsService;

import com.rit.placement.util.CGPACalculator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

/**
 * Company Job Management Servlet
 * Handles: /company/jobs
 * 
 * Features:
 * - List all jobs for company
 * - Add new job posting
 * - Edit existing job
 * - Delete job
 */
@WebServlet("/company/jobs")
public class CompanyJobServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(CompanyJobServlet.class);
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");
    
    private final UserDAO userDAO = DAOFactory.getInstance().getUserDAO();
    private final CompanyDAO companyDAO = DAOFactory.getInstance().getCompanyDAO();
    private final JobPostingDAO jobPostingDAO = DAOFactory.getInstance().getJobPostingDAO();
    private final StudentDAO studentDAO = DAOFactory.getInstance().getStudentDAO();
    private final NotificationService notificationService = new NotificationService();
    private final EligibilityService eligibilityService = new EligibilityService();
    private final MetricsService metricsService = MetricsService.getInstance();

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

        // 2. Validate role
        if (!"COMPANY".equalsIgnoreCase(role)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }

        try {
            // 3. Get company_id
            User user = userDAO.getUserById(userId);
            Integer companyId = user.getCompanyId();
            
            if (companyId == null) {
                req.setAttribute("error", "No company associated with this account");
                req.getRequestDispatcher("/pages/error/company_not_found.jsp").forward(req, resp);
                return;
            }

            // 4. Get action parameter
            String action = req.getParameter("action");
            
            if ("edit".equals(action)) {
                handleEdit(req, resp, companyId);
            } else if ("delete".equals(action)) {
                handleDelete(req, resp, companyId);
            } else {
                handleList(req, resp, companyId);
            }

        } catch (Exception e) {
            logger.error("Error loading jobs for user {}", userId, e);
            metricsService.recordError();
            
            req.setAttribute("error", "Unable to load jobs. Please try again later.");
            req.getRequestDispatcher("/pages/error/generic_error.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 1. Validate session
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            resp.sendRedirect(req.getContextPath() + "/pages/login-otp.jsp");
            return;
        }

        Integer userId = (Integer) session.getAttribute("user_id");
        String role = (String) session.getAttribute("role");

        // 2. Validate role
        if (!"COMPANY".equalsIgnoreCase(role)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }
        


        try {
            // 4. Get company_id
            User user = userDAO.getUserById(userId);
            Integer companyId = user.getCompanyId();
            
            if (companyId == null) {
                session.setAttribute("errorMessage", "No company associated with this account");
                resp.sendRedirect(req.getContextPath() + "/company/jobs");
                return;
            }

            // 4. Get action
            String action = req.getParameter("action");
            
            if ("add".equals(action)) {
                handleAdd(req, resp, companyId);
            } else if ("update".equals(action)) {
                handleUpdate(req, resp, companyId);
            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
            }

        } catch (Exception e) {
            logger.error("Error processing job operation for user {}, action: {}", userId, req.getParameter("action"), e);
            metricsService.recordError();
            
            HttpSession session2 = req.getSession();
            session2.setAttribute("errorMessage", "Unable to process job operation. Please try again.");
            resp.sendRedirect(req.getContextPath() + "/company/jobs");
        }
    }

    /**
     * List all jobs for company
     */
    private void handleList(HttpServletRequest req, HttpServletResponse resp, int companyId)
            throws Exception {
        
        Company company = companyDAO.getCompanyById(companyId);
        List<JobPosting> jobs = jobPostingDAO.getJobPostingsByCompany(companyId);
        
        req.setAttribute("company", company);
        req.setAttribute("jobs", jobs);
        req.getRequestDispatcher("/pages/company_jobs.jsp").forward(req, resp);
    }

    /**
     * Show edit form for a job
     */
    private void handleEdit(HttpServletRequest req, HttpServletResponse resp, int companyId)
            throws Exception {
        
        int jobId = Integer.parseInt(req.getParameter("id"));
        JobPosting job = jobPostingDAO.getJobPostingById(jobId);
        
        // Security: Verify job belongs to this company
        if (job == null || job.getCompanyId() != companyId) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }
        
        Company company = companyDAO.getCompanyById(companyId);
        List<JobPosting> jobs = jobPostingDAO.getJobPostingsByCompany(companyId);
        
        req.setAttribute("company", company);
        req.setAttribute("jobs", jobs);
        req.setAttribute("editJob", job);
        req.getRequestDispatcher("/pages/company_jobs.jsp").forward(req, resp);
    }

    /**
     * Delete a job
     */
    private void handleDelete(HttpServletRequest req, HttpServletResponse resp, int companyId)
            throws Exception {
        
        int jobId = Integer.parseInt(req.getParameter("id"));
        JobPosting job = jobPostingDAO.getJobPostingById(jobId);
        
        // Security: Verify job belongs to this company
        if (job == null || job.getCompanyId() != companyId) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }
        
        // Check if job has applications
        if (jobPostingDAO.hasApplications(jobId)) {
            HttpSession session = req.getSession();
            session.setAttribute("errorMessage", 
                "Cannot delete job posting. This job has existing applications. " +
                "Please contact administrator if you need to remove this job.");
            resp.sendRedirect(req.getContextPath() + "/company/jobs");
            return;
        }
        
        jobPostingDAO.deleteJobPosting(jobId);
        
        // Audit log
        auditLogger.info("JOB_DELETED - CompanyId: {}, JobId: {}, UserId: {}", 
            companyId, jobId, req.getSession().getAttribute("user_id"));
        
        logger.info("Job deleted successfully: JobId {}", jobId);
        
        HttpSession session = req.getSession();
        session.setAttribute("successMessage", "Job deleted successfully");
        resp.sendRedirect(req.getContextPath() + "/company/jobs");
    }

    /**
     * Add new job
     */
    private void handleAdd(HttpServletRequest req, HttpServletResponse resp, int companyId)
            throws Exception {
        
        // Get form parameters
        String role = req.getParameter("role");
        String packageStr = req.getParameter("package");
        String minCgpaStr = req.getParameter("min_cgpa");
        String allowedBranches = req.getParameter("allowed_branches");
        String requiredSkills = req.getParameter("required_skills");
        String deadlineStr = req.getParameter("deadline");
        
        // Validate
        if (role == null || role.trim().isEmpty() ||
            packageStr == null || minCgpaStr == null || deadlineStr == null) {
            HttpSession session = req.getSession();
            session.setAttribute("errorMessage", "All fields are required");
            resp.sendRedirect(req.getContextPath() + "/company/jobs");
            return;
        }
        
        // Create job posting
        JobPosting job = new JobPosting();
        job.setCompanyId(companyId);
        job.setRole(role.trim());
        job.setPackageAmount(new BigDecimal(packageStr));
        job.setMinCgpa(new BigDecimal(minCgpaStr));
        job.setAllowedBranches(allowedBranches != null ? allowedBranches.trim() : null);
        job.setRequiredSkills(requiredSkills != null ? requiredSkills.trim() : null);
        job.setDeadline(Date.valueOf(deadlineStr));
        
        int jobId = jobPostingDAO.insertJobPosting(job);
        
        // Record metrics
        metricsService.recordJobCreated();
        
        // Get company details for notification
        Company company = companyDAO.getCompanyById(companyId);
        
        // Audit log
        auditLogger.info("JOB_CREATED - CompanyId: {}, JobId: {}, Role: {}, Package: {}, MinCGPA: {}, UserId: {}", 
            companyId, jobId, role.trim(), packageStr, minCgpaStr, 
            req.getSession().getAttribute("user_id"));
        
        logger.info("Job created successfully: {} at company {} (JobId: {})", role.trim(), companyId, jobId);
        
        // Notify eligible students about new job posting
        if (company != null) {
            try {
                List<Student> allStudents = studentDAO.getAllStudents();
                int notifiedCount = 0;
                
                for (Student student : allStudents) {
                    // Check if student is eligible for this job
                    boolean eligible = eligibilityService.isEligible(student.getStudentId(), jobId);
                    
                    if (eligible) {
                        // Create notification for eligible student
                        notificationService.notifyNewJobPosted(
                            student.getStudentId(),
                            role.trim(),
                            company.getCompanyName(),
                            jobId
                        );
                        notifiedCount++;
                    }
                }
                
                logger.info("New job posted: {} at {}. Notified {} eligible students", 
                    role, company.getCompanyName(), notifiedCount);
            } catch (Exception e) {
                logger.error("Error notifying students about new job {}", jobId, e);
                metricsService.recordError();
                // Don't fail the job creation if notification fails
            }
        }
        
        HttpSession session = req.getSession();
        session.setAttribute("successMessage", "Job posted successfully");
        resp.sendRedirect(req.getContextPath() + "/company/jobs");
    }

    /**
     * Update existing job
     */
    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp, int companyId)
            throws Exception {
        
        int jobId = Integer.parseInt(req.getParameter("job_id"));
        
        // Security: Verify job belongs to this company
        JobPosting existingJob = jobPostingDAO.getJobPostingById(jobId);
        if (existingJob == null || existingJob.getCompanyId() != companyId) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }
        
        // Get form parameters
        String role = req.getParameter("role");
        String packageStr = req.getParameter("package");
        String minCgpaStr = req.getParameter("min_cgpa");
        String allowedBranches = req.getParameter("allowed_branches");
        String requiredSkills = req.getParameter("required_skills");
        String deadlineStr = req.getParameter("deadline");
        
        // Update job
        JobPosting job = new JobPosting();
        job.setJobId(jobId);
        job.setCompanyId(companyId);
        job.setRole(role.trim());
        job.setPackageAmount(new BigDecimal(packageStr));
        job.setMinCgpa(new BigDecimal(minCgpaStr));
        job.setAllowedBranches(allowedBranches != null ? allowedBranches.trim() : null);
        job.setRequiredSkills(requiredSkills != null ? requiredSkills.trim() : null);
        job.setDeadline(Date.valueOf(deadlineStr));
        
        jobPostingDAO.updateJobPosting(job);
        
        // Audit log
        auditLogger.info("JOB_UPDATED - CompanyId: {}, JobId: {}, Role: {}, UserId: {}", 
            companyId, jobId, role.trim(), req.getSession().getAttribute("user_id"));
        
        logger.info("Job updated successfully: JobId {}, Role: {}", jobId, role.trim());
        
        HttpSession session = req.getSession();
        session.setAttribute("successMessage", "Job updated successfully");
        resp.sendRedirect(req.getContextPath() + "/company/jobs");
    }
}
