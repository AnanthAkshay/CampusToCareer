package com.rit.placement.controller;

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

    private final ProctorDAO proctorDAO = new ProctorDAO();

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
            e.printStackTrace();
            req.setAttribute("errorMessage", "Error loading dashboard: " + e.getMessage());
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
