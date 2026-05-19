package com.rit.placement.controller;

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

    private final JobPostingDAO jobPostingDAO = new JobPostingDAO();
    private final CompanyDAO companyDAO = new CompanyDAO();

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
            // Get all job postings
            List<JobPosting> jobs = jobPostingDAO.getAllJobPostings();
            
            // Get all companies for dropdown
            List<Company> companies = companyDAO.getAllCompanies();

            // Set attributes for JSP
            req.setAttribute("jobs", jobs);
            req.setAttribute("companies", companies);
            req.setAttribute("totalJobs", jobs.size());

            // Forward to JSP
            req.getRequestDispatcher("/pages/admin_jobs.jsp").forward(req, resp);

        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Error loading jobs: " + e.getMessage());
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
            e.printStackTrace();
            session.setAttribute("errorMessage", "Error: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/admin/jobs");
        }
    }
}
