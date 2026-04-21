package com.rit.placement.controller;

import com.rit.placement.dao.ApplicationDAO;
import com.rit.placement.dao.CompanyDAO;
import com.rit.placement.dao.JobPostingDAO;
import com.rit.placement.dao.UserDAO;
import com.rit.placement.model.Application;
import com.rit.placement.model.Company;
import com.rit.placement.model.JobPosting;
import com.rit.placement.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

/**
 * Company Dashboard Servlet
 * Handles: /company/dashboard
 * 
 * Displays:
 * - Company information
 * - Posted job listings
 * - Applications received for their jobs
 */
@WebServlet("/company/dashboard")
public class CompanyDashboardServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final CompanyDAO companyDAO = new CompanyDAO();
    private final JobPostingDAO jobPostingDAO = new JobPostingDAO();
    private final ApplicationDAO applicationDAO = new ApplicationDAO();

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

        // 2. Validate role - only COMPANY can access
        if (!"COMPANY".equalsIgnoreCase(role)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied. Only companies can access this page.");
            return;
        }

        try {
            // 3. Fetch user details
            User user = userDAO.getUserById(userId);
            if (user == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "User not found");
                return;
            }

            // 4. Get company_id from user
            Integer companyId = user.getCompanyId();
            if (companyId == null) {
                req.setAttribute("error", "No company associated with this account. Please contact admin.");
                req.getRequestDispatcher("/pages/error/company_not_found.jsp").forward(req, resp);
                return;
            }

            // 5. Fetch company details
            Company company = companyDAO.getCompanyById(companyId);
            if (company == null) {
                req.setAttribute("error", "Company not found. Please contact admin.");
                req.getRequestDispatcher("/pages/error/company_not_found.jsp").forward(req, resp);
                return;
            }

            // 6. Fetch job postings for this company
            List<JobPosting> jobPostings = jobPostingDAO.getJobPostingsByCompany(companyId);

            // 7. Fetch applications for all jobs of this company
            List<Application> applications = applicationDAO.getApplicationsByCompany(companyId);

            // 8. Calculate statistics
            int totalJobs = jobPostings.size();
            int activeJobs = (int) jobPostings.stream()
                .filter(job -> job.getDeadline() != null && 
                              !job.getDeadline().before(new java.sql.Date(System.currentTimeMillis())))
                .count();
            int totalApplications = applications.size();
            
            // Count applications by status
            long pendingApplications = applications.stream()
                .filter(app -> "PENDING".equals(app.getStatus()))
                .count();
            long shortlistedApplications = applications.stream()
                .filter(app -> "SHORTLISTED".equals(app.getStatus()))
                .count();
            long selectedApplications = applications.stream()
                .filter(app -> "SELECTED".equals(app.getStatus()))
                .count();

            // 9. Set attributes for JSP
            req.setAttribute("company", company);
            req.setAttribute("jobPostings", jobPostings);
            req.setAttribute("applications", applications);
            req.setAttribute("totalJobs", totalJobs);
            req.setAttribute("activeJobs", activeJobs);
            req.setAttribute("totalApplications", totalApplications);
            req.setAttribute("pendingApplications", pendingApplications);
            req.setAttribute("shortlistedApplications", shortlistedApplications);
            req.setAttribute("selectedApplications", selectedApplications);

            // 10. Forward to company dashboard JSP
            req.getRequestDispatcher("/pages/company_dashboard.jsp").forward(req, resp);

        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Error loading company dashboard: " + e.getMessage());
        }
    }
}
