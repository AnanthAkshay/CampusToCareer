package com.rit.placement.controller;

import com.rit.placement.dao.StudentDAO;
import com.rit.placement.dao.UserDAO;
import com.rit.placement.model.Student;
import com.rit.placement.model.User;
import com.rit.placement.util.CGPACalculator;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Admin Students Servlet - Manage all students
 */
@WebServlet("/admin/students")
public class AdminStudentsServlet extends HttpServlet {

    private final StudentDAO studentDAO = new StudentDAO();
    private final UserDAO userDAO = new UserDAO();

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
        if (!"COORDINATOR".equalsIgnoreCase(role)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }

        try {
            // Get all students with details
            List<StudentDetails> students = getAllStudentsWithDetails();

            // Set attributes for JSP
            req.setAttribute("students", students);
            req.setAttribute("totalStudents", students.size());

            // Forward to JSP
            req.getRequestDispatcher("/pages/admin_students.jsp").forward(req, resp);

        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Error loading students: " + e.getMessage());
        }
    }

    /**
     * Get all students with their details
     */
    private List<StudentDetails> getAllStudentsWithDetails() throws SQLException {
        List<StudentDetails> studentsList = new ArrayList<>();

        // Get all users with STUDENT role
        List<User> users = getAllStudentUsers();

        for (User user : users) {
            try {
                Student student = studentDAO.getStudentById(user.getUserId());
                if (student != null) {
                    StudentDetails details = new StudentDetails();
                    details.userId = user.getUserId();
                    details.usn = user.getUsn();
                    details.name = user.getName();
                    details.branch = student.getBranch();
                    details.currentSem = student.getCurrentSem();
                    details.skills = student.getSkills() != null ? student.getSkills() : "Not specified";
                    
                    // Calculate CGPA
                    double cgpa = CGPACalculator.calculateCGPA(user.getUserId());
                    details.cgpa = cgpa >= 0 ? cgpa : 0.0;
                    
                    details.isActive = user.isActive();
                    
                    studentsList.add(details);
                }
            } catch (SQLException e) {
                // Log and continue with next student
                e.printStackTrace();
            }
        }

        return studentsList;
    }

    /**
     * Get all users with STUDENT role
     */
    private List<User> getAllStudentUsers() throws SQLException {
        List<User> students = new ArrayList<>();
        
        try (var conn = com.rit.placement.util.DBConnection.getConnection();
             var ps = conn.prepareStatement(
                 "SELECT * FROM users WHERE role = 'STUDENT' ORDER BY name")) {
            
            var rs = ps.executeQuery();
            while (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsn(rs.getString("usn"));
                user.setName(rs.getString("name"));
                user.setRole(rs.getString("role"));
                user.setActive(rs.getBoolean("is_active"));
                students.add(user);
            }
        }
        
        return students;
    }

    /**
     * Inner class for student details
     */
    public static class StudentDetails {
        public int userId;
        public String usn;
        public String name;
        public String branch;
        public int currentSem;
        public double cgpa;
        public String skills;
        public boolean isActive;

        // Getters for JSP
        public int getUserId() { return userId; }
        public String getUsn() { return usn; }
        public String getName() { return name; }
        public String getBranch() { return branch; }
        public int getCurrentSem() { return currentSem; }
        public double getCgpa() { return cgpa; }
        public String getSkills() { return skills; }
        public boolean isActive() { return isActive; }
    }
}
