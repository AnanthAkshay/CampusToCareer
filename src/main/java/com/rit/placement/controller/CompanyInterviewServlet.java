package com.rit.placement.controller;

import com.rit.placement.dao.*;
import com.rit.placement.model.*;
import com.rit.placement.service.NotificationService;
import com.rit.placement.service.MetricsService;
import com.rit.placement.util.CSRFUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Company Interview Management Servlet
 * Handles: /company/interviews
 */
@WebServlet("/company/interviews")
public class CompanyInterviewServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(CompanyInterviewServlet.class);
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");
    
    private final UserDAO userDAO = new UserDAO();
    private final CompanyDAO companyDAO = new CompanyDAO();
    private final InterviewDAO interviewDAO = new InterviewDAO();
    private final ApplicationDAO applicationDAO = new ApplicationDAO();
    private final JobPostingDAO jobPostingDAO = new JobPostingDAO();
    private final NotificationService notificationService = new NotificationService();
    private final MetricsService metricsService = MetricsService.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            resp.sendRedirect(req.getContextPath() + "/pages/login-otp.jsp");
            return;
        }

        Integer userId = (Integer) session.getAttribute("user_id");
        String role = (String) session.getAttribute("role");

        if (!"COMPANY".equalsIgnoreCase(role)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }

        try {
            User user = userDAO.getUserById(userId);
            Integer companyId = user.getCompanyId();
            
            if (companyId == null) {
                req.setAttribute("error", "No company associated");
                req.getRequestDispatcher("/pages/error/company_not_found.jsp").forward(req, resp);
                return;
            }

            Company company = companyDAO.getCompanyById(companyId);
            List<Interview> interviews = interviewDAO.getInterviewsByCompany(companyId);
            List<Application> shortlistedApps = getShortlistedApplications(companyId);

            req.setAttribute("company", company);
            req.setAttribute("interviews", interviews);
            req.setAttribute("shortlistedApps", shortlistedApps);
            req.getRequestDispatcher("/pages/company_interviews.jsp").forward(req, resp);

        } catch (Exception e) {
            logger.error("Error loading interviews for user {}", userId, e);
            metricsService.recordError();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            resp.sendRedirect(req.getContextPath() + "/pages/login-otp.jsp");
            return;
        }

        Integer userId = (Integer) session.getAttribute("user_id");
        String role = (String) session.getAttribute("role");

        if (!"COMPANY".equalsIgnoreCase(role)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }
        
        // Validate CSRF token
        if (!CSRFUtil.validateToken(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF token. Please refresh the page and try again.");
            return;
        }

        try {
            User user = userDAO.getUserById(userId);
            Integer companyId = user.getCompanyId();
            
            if (companyId == null) {
                session.setAttribute("errorMessage", "No company associated");
                resp.sendRedirect(req.getContextPath() + "/company/interviews");
                return;
            }

            // Schedule interview
            int applicationId = Integer.parseInt(req.getParameter("application_id"));
            String dateTimeStr = req.getParameter("interview_datetime");
            String mode = req.getParameter("interview_mode");
            String location = req.getParameter("interview_location");
            String link = req.getParameter("interview_link");
            String notes = req.getParameter("notes");
            
            // Validate interview date/time
            Timestamp interviewTimestamp;
            try {
                interviewTimestamp = Timestamp.valueOf(dateTimeStr.replace("T", " ") + ":00");
            } catch (IllegalArgumentException e) {
                session.setAttribute("errorMessage", "Invalid date/time format");
                resp.sendRedirect(req.getContextPath() + "/company/interviews");
                return;
            }
            
            // Check if interview date is in the past
            if (interviewTimestamp.before(new Timestamp(System.currentTimeMillis()))) {
                session.setAttribute("errorMessage", "Interview date cannot be in the past");
                resp.sendRedirect(req.getContextPath() + "/company/interviews");
                return;
            }

            // Get application details
            List<Application> apps = applicationDAO.getApplicationsByCompany(companyId);
            Application app = apps.stream()
                .filter(a -> a.getId() == applicationId)
                .findFirst()
                .orElse(null);

            if (app == null) {
                session.setAttribute("errorMessage", "Application not found");
                resp.sendRedirect(req.getContextPath() + "/company/interviews");
                return;
            }

            JobPosting job = jobPostingDAO.getJobPostingById(app.getJobId());
            Company company = companyDAO.getCompanyById(companyId);
            
            // Check for duplicate interview at same time for this student
            if (interviewDAO.hasConflictingInterview(app.getStudentId(), interviewTimestamp)) {
                session.setAttribute("errorMessage", 
                    "This student already has an interview scheduled at this time. Please choose a different time.");
                resp.sendRedirect(req.getContextPath() + "/company/interviews");
                return;
            }

            // Create interview
            Interview interview = new Interview();
            interview.setApplicationId(applicationId);
            interview.setStudentId(app.getStudentId());
            interview.setCompanyId(companyId);
            interview.setJobId(app.getJobId());
            interview.setInterviewDate(interviewTimestamp);
            interview.setInterviewMode(mode);
            interview.setInterviewLocation(location);
            interview.setInterviewLink(link);
            interview.setNotes(notes);

            int interviewId = interviewDAO.scheduleInterview(interview);
            
            // Audit log
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a");
            String formattedDate = sdf.format(interview.getInterviewDate());
            
            auditLogger.info("INTERVIEW_SCHEDULED - InterviewId: {}, StudentId: {}, CompanyId: {}, JobId: {}, DateTime: {}, Mode: {}, UserId: {}", 
                interviewId, app.getStudentId(), companyId, app.getJobId(), formattedDate, mode, userId);
            
            logger.info("Interview scheduled: InterviewId {}, StudentId {}, DateTime: {}, Mode: {}", 
                interviewId, app.getStudentId(), formattedDate, mode);

            // Send notification
            if (company != null && job != null) {
                notificationService.notifyInterviewScheduled(
                    app.getStudentId(), 
                    company.getCompanyName(), 
                    job.getRole(), 
                    formattedDate, 
                    mode, 
                    interviewId
                );
            }

            session.setAttribute("successMessage", "Interview scheduled and student notified");
            resp.sendRedirect(req.getContextPath() + "/company/interviews");

        } catch (Exception e) {
            logger.error("Error scheduling interview for user {}, applicationId: {}", 
                userId, req.getParameter("application_id"), e);
            metricsService.recordError();
            
            HttpSession session2 = req.getSession();
            session2.setAttribute("errorMessage", "Error: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/company/interviews");
        }
    }

    private List<Application> getShortlistedApplications(int companyId) throws Exception {
        List<Application> all = applicationDAO.getApplicationsByCompany(companyId);
        List<Application> shortlisted = new java.util.ArrayList<>();
        for (Application app : all) {
            if ("SHORTLISTED".equals(app.getStatus())) {
                shortlisted.add(app);
            }
        }
        return shortlisted;
    }
}
