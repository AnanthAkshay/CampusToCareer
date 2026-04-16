package com.rit.placement.controller;

import com.rit.placement.dao.UserDAO;
import com.rit.placement.model.User;
import com.rit.placement.util.PasswordUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;

/**
 * Handles login POST requests.
 * Validates USN + password via UserDAO, creates session on success, and
 * redirects users to the correct dashboard for their role.
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String usn      = req.getParameter("usn");
        String password = req.getParameter("password");

        try {
            User user = userDAO.getUserByUSN(usn);
            if (user != null && user.isActive() && isAuthenticated(user, password)) {

                HttpSession old = req.getSession(false);
                if (old != null) {
                    old.invalidate();
                }

                HttpSession session = req.getSession(true);
                session.setAttribute("user_id", user.getUserId());
                session.setAttribute("usn",     user.getUsn());
                session.setAttribute("name",    user.getName());
                session.setAttribute("role",    user.getRole());

                resp.sendRedirect(req.getContextPath() + resolveDashboardPath(user.getRole()));
            } else {
                resp.sendRedirect(req.getContextPath()
                        + "/pages/login.jsp?error=1");
            }

        } catch (Exception e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath()
                    + "/pages/login.jsp?error=1");
        }
    }

    /** GET on /login redirects to the login page */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.sendRedirect(req.getContextPath() + "/pages/login.jsp");
    }

    private boolean isAuthenticated(User user, String plainPassword) throws Exception {
        return PasswordUtil.verifyPassword(plainPassword, user.getPasswordHash());
    }

    private String resolveDashboardPath(String role) {
        if (role == null) {
            return "/login";
        }

        switch (role.trim().toUpperCase()) {
            case "STUDENT":
                return "/student/dashboard";
            case "PROCTOR":
                return "/proctor/dashboard";
            case "FACULTY":
            case "COORDINATOR":
            case "ADMIN":
                return "/admin/dashboard";
            default:
                return "/login";
        }
    }
}
