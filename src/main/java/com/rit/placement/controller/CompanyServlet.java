package com.rit.placement.controller;

import com.rit.placement.dao.CompanyDAO;
import com.rit.placement.model.Company;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;
import java.util.List;

/**
 * Company Servlet - Handles company management
 * GET: List all companies
 * POST: Add new company
 */
@WebServlet("/companies")
public class CompanyServlet extends HttpServlet {

    private final CompanyDAO companyDAO = new CompanyDAO();

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
            // 2. Fetch all companies
            List<Company> companies = companyDAO.getAllCompanies();

            // 3. Set attributes for JSP
            req.setAttribute("companies", companies);

            // 4. Forward to companies.jsp
            req.getRequestDispatcher("/pages/companies.jsp").forward(req, resp);

        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Error loading companies: " + e.getMessage());
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

        // 2. Validate role (only COORDINATOR and ADMIN can add companies)
        if (!"COORDINATOR".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }

        try {
            // 3. Get form parameters
            String companyName = req.getParameter("company_name");
            String description = req.getParameter("description");
            String companyType = req.getParameter("company_type");

            // 4. Validate input
            if (isEmpty(companyName)) {
                req.setAttribute("error", "Company name is required");
                doGet(req, resp);
                return;
            }

            // 5. Create company object
            Company company = new Company();
            company.setCompanyName(sanitize(companyName));
            company.setDescription(sanitize(description));
            company.setCompanyType(companyType != null ? companyType : "PRODUCT");

            // 6. Insert into database
            int companyId = companyDAO.insertCompany(company);

            // 7. Set success message and redirect
            session.setAttribute("successMessage", "Company added successfully! (ID: " + companyId + ")");
            resp.sendRedirect(req.getContextPath() + "/companies");

        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", "Error adding company: " + e.getMessage());
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
