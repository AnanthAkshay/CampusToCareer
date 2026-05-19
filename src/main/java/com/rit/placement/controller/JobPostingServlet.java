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
 * Job Posting Servlet - Handles job posting management
 * GET: List all job postings
 * POST: Create new job posting
 */
@WebServlet("/job-postings")
public class JobPostingServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(JobPostingServlet.class);

    private final JobPostingDAO jobPostingDAO = DAOFactory.getInstance().getJobPostingDAO();
    private final CompanyDAO companyDAO = DAOFactory.getInstance().getCompanyDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 1. Validate session
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
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

            // 2. Fetch all job postings
            List<JobPosting> jobPostings = jobPostingDAO.getAllJobPostings(limit, offset);
            req.setAttribute("currentPage", page);
            req.setAttribute("limit", limit);

            // 3. Fetch all companies (for dropdown in form)
            List<Company> companies = companyDAO.getAllCompanies();

            // 4. Set attributes for JSP
            req.setAttribute("jobPostings", jobPostings);
            req.setAttribute("companies", companies);

            // 5. Forward to job_postings.jsp
            req.getRequestDispatcher("/pages/job_postings.jsp").forward(req, resp);

        } catch (Exception e) {
            logger.error("Exception occurred: ", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Error loading job postings.");
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

        String role = (String) session.getAttribute("role");

        // 2. Validate role (only COORDINATOR and ADMIN can add job postings)
        if (!"COORDINATOR".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }

        try {
            // 3. Get form parameters
            String companyIdStr = req.getParameter("company_id");
            String roleParam = req.getParameter("role");
            String packageStr = req.getParameter("package");
            String minCgpaStr = req.getParameter("min_cgpa");
            String allowedBranches = req.getParameter("allowed_branches");
            String requiredSkills = req.getParameter("required_skills");
            String deadlineStr = req.getParameter("deadline");

            // 4. Validate required fields
            if (isEmpty(companyIdStr) || isEmpty(roleParam) || isEmpty(minCgpaStr) || isEmpty(deadlineStr)) {
                req.setAttribute("error", "Company, Role, Min CGPA, and Deadline are required");
                doGet(req, resp);
                return;
            }

            // 5. Parse and validate data
            int companyId = Integer.parseInt(companyIdStr);
            BigDecimal packageAmount = !isEmpty(packageStr) ? new BigDecimal(packageStr) : null;
            BigDecimal minCgpa = new BigDecimal(minCgpaStr);
            Date deadline = Date.valueOf(deadlineStr);

            // Validate CGPA range
            if (minCgpa.compareTo(BigDecimal.ZERO) < 0 || minCgpa.compareTo(new BigDecimal("10.0")) > 0) {
                req.setAttribute("error", "Min CGPA must be between 0 and 10");
                doGet(req, resp);
                return;
            }

            // 6. Create job posting object
            JobPosting job = new JobPosting();
            job.setCompanyId(companyId);
            job.setRole(sanitize(roleParam));
            job.setPackageAmount(packageAmount);
            job.setMinCgpa(minCgpa);
            job.setAllowedBranches(sanitize(allowedBranches));
            job.setRequiredSkills(sanitize(requiredSkills));
            job.setDeadline(deadline);

            // 7. Insert into database
            int jobId = jobPostingDAO.insertJobPosting(job);

            // 8. Set success message and redirect
            session.setAttribute("successMessage", "Job posting created successfully! (ID: " + jobId + ")");
            resp.sendRedirect(req.getContextPath() + "/job-postings");

        } catch (NumberFormatException e) {
            req.setAttribute("error", "Invalid number format.");
            doGet(req, resp);
        } catch (IllegalArgumentException e) {
            req.setAttribute("error", "Invalid date format. Use YYYY-MM-DD");
            doGet(req, resp);
        } catch (Exception e) {
            logger.error("Exception occurred: ", e);
            req.setAttribute("error", "Error creating job posting.");
            doGet(req, resp);
        }
    }

    /**
     * Sanitize input: trim whitespace and return null if empty
     */
    private String sanitize(String input) {
        if (input == null) return null;
        input = input.trim();
        return input.isEmpty() ? null : input;
    }

    /**
     * Check if string is null or empty after trimming
     */
    private boolean isEmpty(String input) {
        return input == null || input.trim().isEmpty();
    }
}
