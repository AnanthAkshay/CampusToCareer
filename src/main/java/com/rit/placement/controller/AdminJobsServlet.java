package com.rit.placement.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rit.placement.factory.DAOFactory;

import com.rit.placement.dao.CompanyDAO;
import com.rit.placement.dao.JobPostingDAO;
import com.rit.placement.model.Company;
import com.rit.placement.model.JobPosting;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

/**
 * Admin Jobs Servlet - Manage job postings
 */
@WebServlet("/admin/jobs")
public class AdminJobsServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(AdminJobsServlet.class);

    private final JobPostingDAO jobPostingDAO = DAOFactory.getInstance().getJobPostingDAO();
    private final CompanyDAO companyDAO = DAOFactory.getInstance().getCompanyDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Session validation
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String role = (String) session.getAttribute("role");
        if (!"COORDINATOR".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }

        try {
            int page = 1;
            int limit = 10;
            String pageParam = req.getParameter("page");
            if (pageParam != null && !pageParam.isEmpty()) {
                page = Integer.parseInt(pageParam);
            }
            int offset = (page - 1) * limit;

            // Get all job postings
            List<JobPosting> jobs = jobPostingDAO.getAllJobPostings(limit, offset);
            req.setAttribute("currentPage", page);
            req.setAttribute("limit", limit);
            
            // Get all companies for dropdown
            List<Company> companies = companyDAO.getAllCompanies();

            // Set attributes for JSP
            req.setAttribute("jobs", jobs);
            req.setAttribute("companies", companies);
            req.setAttribute("totalJobs", jobs.size());

            // Forward to JSP
            req.getRequestDispatcher("/pages/admin_jobs.jsp").forward(req, resp);

        } catch (Exception e) {
            logger.error("Exception occurred: ", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Error loading jobs.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Session validation
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String role = (String) session.getAttribute("role");
        if (!"COORDINATOR".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }

        try {
            String action = req.getParameter("action");

            if ("add".equals(action)) {
                // Add new job posting
                String companyIdStr = req.getParameter("company_id");
                String roleParam = req.getParameter("role");
                String packageStr = req.getParameter("package");
                String minCgpaStr = req.getParameter("min_cgpa");
                String allowedBranches = req.getParameter("allowed_branches");
                String requiredSkills = req.getParameter("required_skills");
                String deadlineStr = req.getParameter("deadline");

                if (companyIdStr != null && roleParam != null && minCgpaStr != null && deadlineStr != null) {
                    JobPosting job = new JobPosting();
                    job.setCompanyId(Integer.parseInt(companyIdStr));
                    job.setRole(roleParam.trim());
                    job.setPackageAmount(packageStr != null && !packageStr.isEmpty() ? 
                        new BigDecimal(packageStr) : null);
                    job.setMinCgpa(new BigDecimal(minCgpaStr));
                    job.setAllowedBranches(allowedBranches != null ? allowedBranches.trim() : null);
                    job.setRequiredSkills(requiredSkills != null ? requiredSkills.trim() : null);
                    job.setDeadline(Date.valueOf(deadlineStr));

                    jobPostingDAO.insertJobPosting(job);
                    session.setAttribute("successMessage", "Job posting created successfully");
                } else {
                    session.setAttribute("errorMessage", "All required fields must be filled");
                }
            } else if ("update".equals(action)) {
                // Update existing job posting
                String jobIdStr = req.getParameter("job_id");
                String companyIdStr = req.getParameter("company_id");
                String roleParam = req.getParameter("role");
                String packageStr = req.getParameter("package");
                String minCgpaStr = req.getParameter("min_cgpa");
                String allowedBranches = req.getParameter("allowed_branches");
                String requiredSkills = req.getParameter("required_skills");
                String deadlineStr = req.getParameter("deadline");

                if (jobIdStr != null && companyIdStr != null && roleParam != null && minCgpaStr != null && deadlineStr != null) {
                    JobPosting job = new JobPosting();
                    job.setJobId(Integer.parseInt(jobIdStr));
                    job.setCompanyId(Integer.parseInt(companyIdStr));
                    job.setRole(roleParam.trim());
                    job.setPackageAmount(packageStr != null && !packageStr.isEmpty() ? 
                        new BigDecimal(packageStr) : null);
                    job.setMinCgpa(new BigDecimal(minCgpaStr));
                    job.setAllowedBranches(allowedBranches != null ? allowedBranches.trim() : null);
                    job.setRequiredSkills(requiredSkills != null ? requiredSkills.trim() : null);
                    job.setDeadline(Date.valueOf(deadlineStr));

                    jobPostingDAO.updateJobPosting(job);
                    session.setAttribute("successMessage", "Job posting updated successfully");
                } else {
                    session.setAttribute("errorMessage", "All required fields must be filled");
                }
            } else if ("delete".equals(action)) {
                // Delete job posting
                String jobIdStr = req.getParameter("job_id");
                if (jobIdStr != null) {
                    int jobId = Integer.parseInt(jobIdStr);
                    jobPostingDAO.deleteJobPosting(jobId);
                    session.setAttribute("successMessage", "Job posting deleted successfully");
                }
            }

            resp.sendRedirect(req.getContextPath() + "/admin/jobs");

        } catch (Exception e) {
            logger.error("Exception occurred: ", e);
            session.setAttribute("errorMessage", "Error.");
            resp.sendRedirect(req.getContextPath() + "/admin/jobs");
        }
    }
}
