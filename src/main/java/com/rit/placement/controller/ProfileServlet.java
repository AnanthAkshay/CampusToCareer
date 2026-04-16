package com.rit.placement.controller;

import com.rit.placement.dao.StudentDAO;
import com.rit.placement.dao.UserDAO;
import com.rit.placement.model.Student;
import com.rit.placement.model.User;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;

/**
 * Profile Servlet - Handles student profile view and updates
 * GET: Display profile form with current data
 * POST: Update profile (skills, projects, experience)
 */
@WebServlet("/student/profile")
public class ProfileServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final StudentDAO studentDAO = new StudentDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 1. Validate session
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        Integer userId = (Integer) session.getAttribute("user_id");
        String role = (String) session.getAttribute("role");

        // 2. Validate role (only STUDENT can access)
        if (!"STUDENT".equalsIgnoreCase(role)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }

        try {
            // 3. Fetch user details
            User user = userDAO.getUserById(userId);
            if (user == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "User not found");
                return;
            }

            // 4. Fetch student profile
            Student student = studentDAO.getStudentById(userId);
            if (student == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Student profile not found");
                return;
            }

            // 5. Set attributes for JSP
            req.setAttribute("name", user.getName());
            req.setAttribute("usn", user.getUsn());
            req.setAttribute("branch", student.getBranch());
            req.setAttribute("currentSem", student.getCurrentSem());
            req.setAttribute("skills", student.getSkills());
            req.setAttribute("projects", student.getProjects());
            req.setAttribute("experience", student.getExperience());

            // 6. Forward to profile.jsp
            req.getRequestDispatcher("/pages/profile.jsp").forward(req, resp);

        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Error loading profile: " + e.getMessage());
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

        Integer userId = (Integer) session.getAttribute("user_id");
        String role = (String) session.getAttribute("role");

        // 2. Validate role
        if (!"STUDENT".equalsIgnoreCase(role)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }

        try {
            // 3. Get form parameters
            String skills = req.getParameter("skills");
            String projects = req.getParameter("projects");
            String experience = req.getParameter("experience");

            // 4. Validate and sanitize input
            skills = sanitize(skills);
            projects = sanitize(projects);
            experience = sanitize(experience);

            // Validation: At least one field must be non-empty
            if (isEmpty(skills) && isEmpty(projects) && isEmpty(experience)) {
                req.setAttribute("error", "Please fill at least one field");
                doGet(req, resp);
                return;
            }

            // 5. Update profile in database
            studentDAO.updateProfile(userId, skills, projects, experience);

            // 6. Set success message and redirect
            session.setAttribute("successMessage", "Profile updated successfully!");
            resp.sendRedirect(req.getContextPath() + "/student/profile");

        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", "Error updating profile: " + e.getMessage());
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
