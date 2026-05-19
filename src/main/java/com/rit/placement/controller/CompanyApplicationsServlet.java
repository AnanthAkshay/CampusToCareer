package com.rit.placement.controller;

import com.rit.placement.factory.DAOFactory;

import com.rit.placement.dao.ApplicationDAO;
import com.rit.placement.dao.CompanyDAO;
import com.rit.placement.dao.JobPostingDAO;
import com.rit.placement.dao.StudentDAO;
import com.rit.placement.dao.UserDAO;
import com.rit.placement.model.Application;
import com.rit.placement.model.Company;
import com.rit.placement.model.JobPosting;
import com.rit.placement.model.Student;
import com.rit.placement.model.User;
import com.rit.placement.service.NotificationService;
import com.rit.placement.service.CandidateRankingService;
import com.rit.placement.service.CandidateRankingService.RankedCandidate;
import com.rit.placement.service.MetricsService;
import com.rit.placement.util.CGPACalculator;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Company Applications Management Servlet
 * Handles: /company/applications
 * 
 * Features:
 * - View all applications for company's jobs
 * - View student details (name, CGPA, skills)
 * - Update application status (SHORTLISTED/REJECTED/SELECTED)
 * - Send notifications to students
 */
@WebServlet("/company/applications")
public class CompanyApplicationsServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(CompanyApplicationsServlet.class);
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");
    
    private final UserDAO userDAO = DAOFactory.getInstance().getUserDAO();
    private final CompanyDAO companyDAO = DAOFactory.getInstance().getCompanyDAO();
    private final ApplicationDAO applicationDAO = DAOFactory.getInstance().getApplicationDAO();
    private final StudentDAO studentDAO = DAOFactory.getInstance().getStudentDAO();
    private final JobPostingDAO jobPostingDAO = DAOFactory.getInstance().getJobPostingDAO();
    private final NotificationService notificationService = new NotificationService();
    private final CandidateRankingService rankingService = new CandidateRankingService();
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

            // 4. Fetch company details
            Company company = companyDAO.getCompanyById(companyId);

            // 5. Fetch all applications for this company's jobs
            List<Application> applications = applicationDAO.getApplicationsByCompany(companyId);
            
            // 6. Get job filter parameter
            String filterJobId = req.getParameter("job_id");
            JobPosting selectedJob = null;
            
            if (filterJobId != null && !filterJobId.isEmpty()) {
                try {
                    int jobId = Integer.parseInt(filterJobId);
                    selectedJob = jobPostingDAO.getJobPostingById(jobId);
                } catch (Exception e) {
                    // Invalid job ID, ignore
                }
            }

            // 7. Fetch student details and rank candidates
            Map<Integer, Student> studentMap = new HashMap<>();
            Map<Integer, Double> cgpaMap = new HashMap<>();
            Map<Integer, Integer> rankMap = new HashMap<>();
            Map<Integer, Integer> scoreMap = new HashMap<>();
            
            // If filtering by specific job, rank candidates for that job
            if (selectedJob != null) {
                List<Application> jobApplications = new ArrayList<>();
                for (Application app : applications) {
                    if (app.getJobId() == selectedJob.getJobId()) {
                        jobApplications.add(app);
                    }
                }
                
                // Rank candidates for this specific job
                List<RankedCandidate> rankedCandidates = rankingService.rankCandidates(jobApplications, selectedJob);
                
                for (RankedCandidate ranked : rankedCandidates) {
                    int studentId = ranked.getApplication().getStudentId();
                    studentMap.put(studentId, ranked.getStudent());
                    cgpaMap.put(studentId, ranked.getCgpa());
                    rankMap.put(studentId, ranked.getRank());
                    scoreMap.put(studentId, ranked.getScore());
                }
            } else {
                // No specific job filter - just fetch student details without ranking
                for (Application app : applications) {
                    int studentId = app.getStudentId();
                    
                    if (!studentMap.containsKey(studentId)) {
                        Student student = studentDAO.getStudentById(studentId);
                        if (student != null) {
                            studentMap.put(studentId, student);
                            
                            // Calculate CGPA
                            double cgpa = CGPACalculator.calculateCGPA(studentId);
                            cgpaMap.put(studentId, cgpa);
                        }
                    }
                }
            }

            // 8. Filter by status if requested
            String filterStatus = req.getParameter("status");
            List<Application> filteredApplications = applications;
            
            if (filterStatus != null && !filterStatus.isEmpty() && !"ALL".equals(filterStatus)) {
                filteredApplications = new ArrayList<>();
                for (Application app : applications) {
                    if (filterStatus.equalsIgnoreCase(app.getStatus())) {
                        filteredApplications.add(app);
                    }
                }
            }
            
            // Filter by job if requested
            if (filterJobId != null && !filterJobId.isEmpty()) {
                try {
                    int jobId = Integer.parseInt(filterJobId);
                    List<Application> jobFiltered = new ArrayList<>();
                    for (Application app : filteredApplications) {
                        if (app.getJobId() == jobId) {
                            jobFiltered.add(app);
                        }
                    }
                    filteredApplications = jobFiltered;
                } catch (NumberFormatException e) {
                    // Invalid job ID, ignore
                }
            }
            
            // 9. Pagination
            int page = 1;
            int pageSize = 20; // 20 applications per page
            
            String pageParam = req.getParameter("page");
            if (pageParam != null && !pageParam.isEmpty()) {
                try {
                    page = Integer.parseInt(pageParam);
                    if (page < 1) page = 1;
                } catch (NumberFormatException e) {
                    page = 1;
                }
            }
            
            int totalApplications = filteredApplications.size();
            int totalPages = (int) Math.ceil((double) totalApplications / pageSize);
            if (page > totalPages && totalPages > 0) {
                page = totalPages;
            }
            
            int startIndex = (page - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, totalApplications);
            
            List<Application> paginatedApplications = filteredApplications.subList(startIndex, endIndex);

            // 10. Set attributes
            req.setAttribute("company", company);
            req.setAttribute("applications", paginatedApplications);
            req.setAttribute("allApplications", applications);
            req.setAttribute("studentMap", studentMap);
            req.setAttribute("cgpaMap", cgpaMap);
            req.setAttribute("rankMap", rankMap);
            req.setAttribute("scoreMap", scoreMap);
            req.setAttribute("filterStatus", filterStatus);
            req.setAttribute("filterJobId", filterJobId);
            req.setAttribute("selectedJob", selectedJob);
            req.setAttribute("currentPage", page);
            req.setAttribute("totalPages", totalPages);
            req.setAttribute("totalApplications", totalApplications);
            
            // Get all jobs for filter dropdown
            List<JobPosting> companyJobs = jobPostingDAO.getJobPostingsByCompany(companyId);
            req.setAttribute("companyJobs", companyJobs);

            // 11. Forward to JSP
            req.setAttribute("cgpaMap", cgpaMap);
            req.setAttribute("filterStatus", filterStatus);
            req.setAttribute("currentPage", page);
            req.setAttribute("totalPages", totalPages);
            req.setAttribute("totalApplications", totalApplications);

            // 10. Forward to JSP
            req.getRequestDispatcher("/pages/company_applications.jsp").forward(req, resp);

        } catch (Exception e) {
            logger.error("Error loading applications for user {}", 
                session != null ? session.getAttribute("user_id") : "N/A", e);
            metricsService.recordError();
            
            req.setAttribute("error", "Unable to load applications. Please try again later.");
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
                resp.sendRedirect(req.getContextPath() + "/company/applications");
                return;
            }

            // 4. Get parameters
            String action = req.getParameter("action");
            int applicationId = Integer.parseInt(req.getParameter("application_id"));
            String newStatus = req.getParameter("status");

            // 5. Validate status
            if (!isValidStatus(newStatus)) {
                session.setAttribute("errorMessage", "Invalid status");
                resp.sendRedirect(req.getContextPath() + "/company/applications");
                return;
            }

            // 6. Security: Verify application belongs to this company's jobs
            if (!applicationDAO.belongsToCompany(applicationId, companyId)) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
                return;
            }

            // 7. Get application details for notification
            Application app = applicationDAO.getApplicationsByCompany(companyId).stream()
                .filter(a -> a.getId() == applicationId)
                .findFirst()
                .orElse(null);
            
            if (app == null) {
                session.setAttribute("errorMessage", "Application not found");
                resp.sendRedirect(req.getContextPath() + "/company/applications");
                return;
            }
            
            String oldStatus = app.getStatus();
            
            // 8. Validate status transition
            if (!isValidStatusTransition(oldStatus, newStatus)) {
                session.setAttribute("errorMessage", 
                    "Invalid status transition from " + oldStatus + " to " + newStatus + ". " +
                    "Allowed transitions: PENDING→SHORTLISTED→INTERVIEW→SELECTED or PENDING/SHORTLISTED→REJECTED");
                resp.sendRedirect(req.getContextPath() + "/company/applications");
                return;
            }
            
            // 9. Get company and job details for notification
            Company company = companyDAO.getCompanyById(companyId);
            JobPosting job = jobPostingDAO.getJobPostingById(app.getJobId());
            
            // 9. Update status
            applicationDAO.updateApplicationStatus(applicationId, newStatus);
            
            // Record metrics
            metricsService.recordStatusUpdate(newStatus);
            
            // Audit log
            auditLogger.info("STATUS_UPDATE - ApplicationId: {}, StudentId: {}, OldStatus: {}, NewStatus: {}, CompanyId: {}, UserId: {}", 
                applicationId, app.getStudentId(), oldStatus, newStatus, companyId, userId);
            
            logger.info("Application status updated: ApplicationId {}, {} -> {}", 
                applicationId, oldStatus, newStatus);

            // 10. Send notification to student
            if (company != null && job != null) {
                int studentId = app.getStudentId();
                String companyName = company.getCompanyName();
                String jobTitle = job.getRole();
                
                // Send specific notification based on new status
                if ("SHORTLISTED".equals(newStatus)) {
                    notificationService.notifyShortlisted(studentId, companyName, jobTitle, applicationId);
                } else if ("REJECTED".equals(newStatus)) {
                    notificationService.notifyRejected(studentId, companyName, jobTitle, applicationId);
                } else if ("SELECTED".equals(newStatus)) {
                    notificationService.notifySelected(studentId, companyName, jobTitle, applicationId);
                } else {
                    notificationService.notifyStatusChange(studentId, companyName, jobTitle, 
                        oldStatus, newStatus, applicationId);
                }
            }

            // 11. Success message
            session.setAttribute("successMessage", "Application status updated to " + newStatus + " and student notified");
            resp.sendRedirect(req.getContextPath() + "/company/applications");

        } catch (Exception e) {
            logger.error("Error updating application status for user {}, applicationId: {}", 
                userId, req.getParameter("application_id"), e);
            metricsService.recordError();
            
            HttpSession session2 = req.getSession();
            session2.setAttribute("errorMessage", "Unable to update application status. Please try again.");
            resp.sendRedirect(req.getContextPath() + "/company/applications");
        }
    }

    /**
     * Validate application status
     */
    private boolean isValidStatus(String status) {
        return "PENDING".equals(status) ||
               "SHORTLISTED".equals(status) ||
               "INTERVIEW".equals(status) ||
               "SELECTED".equals(status) ||
               "REJECTED".equals(status);
    }
    
    /**
     * Validate status transition logic
     * Allowed transitions:
     * - PENDING → SHORTLISTED, REJECTED
     * - SHORTLISTED → INTERVIEW, REJECTED
     * - INTERVIEW → SELECTED, REJECTED
     * - REJECTED → (no transitions allowed)
     * - SELECTED → (no transitions allowed)
     */
    private boolean isValidStatusTransition(String oldStatus, String newStatus) {
        // Same status is allowed (no-op)
        if (oldStatus.equals(newStatus)) {
            return true;
        }
        
        switch (oldStatus) {
            case "PENDING":
                return "SHORTLISTED".equals(newStatus) || "REJECTED".equals(newStatus);
            case "SHORTLISTED":
                return "INTERVIEW".equals(newStatus) || "REJECTED".equals(newStatus);
            case "INTERVIEW":
                return "SELECTED".equals(newStatus) || "REJECTED".equals(newStatus);
            case "REJECTED":
            case "SELECTED":
                // Terminal states - no transitions allowed
                return false;
            default:
                return false;
        }
    }
}
