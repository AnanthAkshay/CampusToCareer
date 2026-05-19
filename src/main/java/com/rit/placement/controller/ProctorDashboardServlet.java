package com.rit.placement.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rit.placement.factory.DAOFactory;

import com.rit.placement.dao.ProctorDAO;
import com.rit.placement.model.Student;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * Proctor Dashboard Servlet
 * Displays list of students assigned to the logged-in proctor
 */
@WebServlet("/proctor/dashboard")
public class ProctorDashboardServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(ProctorDashboardServlet.class);

    private final ProctorDAO proctorDAO = DAOFactory.getInstance().getProctorDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        
        // Verify session exists
        if (session == null || session.getAttribute("user_id") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // Verify user is a proctor
        String role = (String) session.getAttribute("role");
        if (!"PROCTOR".equalsIgnoreCase(role)) {
            req.setAttribute("errorMessage", "Access denied: Only proctors can access this page");
            req.getRequestDispatcher("/pages/error/403.jsp").forward(req, resp);
            return;
        }

        // Get proctor ID from session
        Integer proctorId = (Integer) session.getAttribute("user_id");
        String proctorName = (String) session.getAttribute("name");

        try {
            // Fetch students assigned to this proctor
            List<Student> students = proctorDAO.getStudentsByProctorId(proctorId);
            int studentCount = students.size();

            // Set attributes for JSP
            req.setAttribute("students", students);
            req.setAttribute("studentCount", studentCount);
            req.setAttribute("proctorName", proctorName);

            // Forward to JSP
            req.getRequestDispatcher("/pages/proctor_dashboard.jsp").forward(req, resp);

        } catch (Exception e) {
            logger.error("Exception occurred: ", e);
            req.setAttribute("errorMessage", "Error loading dashboard.");
            req.getRequestDispatcher("/pages/error/500.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Redirect POST to GET
        doGet(req, resp);
    }
}
