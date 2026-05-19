package com.rit.placement.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rit.placement.factory.DAOFactory;

import com.rit.placement.dao.ApplicationDAO;
import com.rit.placement.dao.ApplicationDAO.ApplicationStats;
import com.rit.placement.model.Application;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;
import java.util.List;

/**
 * My Applications Servlet - Shows student's applications
 * GET: Display all applications for logged-in student
 */
@WebServlet("/my-applications")
public class MyApplicationsServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(MyApplicationsServlet.class);

    private final ApplicationDAO applicationDAO = DAOFactory.getInstance().getApplicationDAO();

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

        // 2. Validate role (only STUDENT can view their applications)
        if (!"STUDENT".equalsIgnoreCase(role)) {
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

            // 3. Fetch applications for this student
            List<Application> applications = applicationDAO.getApplicationsByStudent(userId, limit, offset);
            req.setAttribute("currentPage", page);
            req.setAttribute("limit", limit);

            // 4. Get application statistics
            ApplicationStats stats = applicationDAO.getStudentStats(userId);

            // 5. Set attributes for JSP
            req.setAttribute("applications", applications);
            req.setAttribute("stats", stats);

            // 6. Forward to my_applications.jsp
            req.getRequestDispatcher("/pages/my_applications.jsp").forward(req, resp);

        } catch (Exception e) {
            logger.error("Exception occurred: ", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Error loading applications.");
        }
    }
}
