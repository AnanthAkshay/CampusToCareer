package com.rit.placement.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rit.placement.factory.DAOFactory;

import com.rit.placement.dao.CompanyDAO;
import com.rit.placement.dao.CompanyRequestDAO;
import com.rit.placement.model.Company;
import com.rit.placement.model.CompanyRequest;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;
import java.util.List;

/**
 * Admin Companies Servlet - Manage companies and company requests
 */
@WebServlet("/admin/companies")
public class AdminCompaniesServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(AdminCompaniesServlet.class);

    private final CompanyDAO companyDAO = DAOFactory.getInstance().getCompanyDAO();
    private final CompanyRequestDAO requestDAO = DAOFactory.getInstance().getCompanyRequestDAO();

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
            // Get all companies
            List<Company> companies = companyDAO.getAllCompanies();
            
            // Get pending company requests
            List<CompanyRequest> pendingRequests = requestDAO.getPendingRequests();

            // Set attributes for JSP
            req.setAttribute("companies", companies);
            req.setAttribute("pendingRequests", pendingRequests);
            req.setAttribute("totalCompanies", companies.size());
            req.setAttribute("pendingCount", pendingRequests.size());

            // Forward to JSP
            req.getRequestDispatcher("/pages/admin_companies.jsp").forward(req, resp);

        } catch (Exception e) {
            logger.error("Exception occurred: ", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Error loading companies.");
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

        Integer userId = (Integer) session.getAttribute("user_id");

        try {
            String action = req.getParameter("action");

            if ("add".equals(action)) {
                // Add new company directly
                String companyName = req.getParameter("company_name");
                String description = req.getParameter("description");
                String companyType = req.getParameter("company_type");

                if (companyName != null && !companyName.trim().isEmpty()) {
                    Company company = new Company();
                    company.setCompanyName(companyName.trim());
                    company.setDescription(description != null ? description.trim() : null);
                    company.setCompanyType(companyType != null ? companyType : "PRODUCT");

                    companyDAO.insertCompany(company);
                    session.setAttribute("successMessage", "Company added successfully");
                } else {
                    session.setAttribute("errorMessage", "Company name is required");
                }
            } else if ("update".equals(action)) {
                // Update existing company
                String companyIdStr = req.getParameter("company_id");
                String companyName = req.getParameter("company_name");
                String description = req.getParameter("description");
                String companyType = req.getParameter("company_type");

                if (companyIdStr != null && companyName != null && !companyName.trim().isEmpty()) {
                    Company company = new Company();
                    company.setCompanyId(Integer.parseInt(companyIdStr));
                    company.setCompanyName(companyName.trim());
                    company.setDescription(description != null ? description.trim() : null);
                    company.setCompanyType(companyType != null ? companyType : "PRODUCT");

                    companyDAO.updateCompany(company);
                    session.setAttribute("successMessage", "Company updated successfully");
                } else {
                    session.setAttribute("errorMessage", "Company name is required");
                }
            } else if ("delete".equals(action)) {
                // Delete company
                String companyIdStr = req.getParameter("company_id");
                if (companyIdStr != null) {
                    int companyId = Integer.parseInt(companyIdStr);
                    companyDAO.deleteCompany(companyId);
                    session.setAttribute("successMessage", "Company deleted successfully");
                }
            } else if ("approve_request".equals(action)) {
                // Approve company request
                String requestIdStr = req.getParameter("request_id");
                if (requestIdStr != null) {
                    int requestId = Integer.parseInt(requestIdStr);
                    CompanyRequest request = requestDAO.getRequestById(requestId);
                    
                    if (request != null && "PENDING".equals(request.getStatus())) {
                        // Create company from request
                        Company company = new Company();
                        company.setCompanyName(request.getCompanyName());
                        company.setDescription(request.getDescription());
                        company.setCompanyType(request.getCompanyType());
                        
                        companyDAO.insertCompany(company);
                        
                        // Update request status
                        requestDAO.updateStatus(requestId, "APPROVED", userId);
                        
                        session.setAttribute("successMessage", "Company request approved and added");
                    }
                }
            } else if ("reject_request".equals(action)) {
                // Reject company request
                String requestIdStr = req.getParameter("request_id");
                if (requestIdStr != null) {
                    int requestId = Integer.parseInt(requestIdStr);
                    requestDAO.updateStatus(requestId, "REJECTED", userId);
                    session.setAttribute("successMessage", "Company request rejected");
                }
            }

            resp.sendRedirect(req.getContextPath() + "/admin/companies");

        } catch (Exception e) {
            logger.error("Exception occurred: ", e);
            session.setAttribute("errorMessage", "Error.");
            resp.sendRedirect(req.getContextPath() + "/admin/companies");
        }
    }
}
