package com.rit.placement.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Locale;

/**
 * Central session and authorization guard for the web layer.
 * 
 * Authorization Strategy:
 * - Students: Read access to companies/jobs, write access to applications
 * - Coordinators/Admin: Full access to companies/jobs/applications
 * - Method-level authorization: POST operations restricted by role
 */
@WebFilter("/*")
public class SessionFilter implements Filter {

    private static final String[] STUDENT_PATHS = {
        "/student/dashboard",
        "/student/profile",
        "/apply",
        "/my-applications",
        "/companies",      // Read-only access for students
        "/job-postings"    // Read-only access for students
    };

    private static final String[] PROCTOR_PATHS = {
        "/proctor/dashboard"
    };

    private static final String[] ADMIN_PATHS = {
        "/admin/dashboard",
        "/companies",
        "/job-postings",
        "/eligible-students"
    };

    // Paths that require COORDINATOR/ADMIN role for POST operations
    private static final String[] ADMIN_WRITE_PATHS = {
        "/companies",
        "/job-postings"
    };

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String path = req.getServletPath();
        if (path == null || path.isEmpty()) {
            path = "/";
        }

        if (isPublic(req, path)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String role = String.valueOf(session.getAttribute("role")).toUpperCase(Locale.ROOT);
        String method = req.getMethod().toUpperCase();

        // Check path-level authorization
        if (!isAuthorized(role, path)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied for role: " + role);
            return;
        }

        // Check method-level authorization for write operations
        if ("POST".equals(method) && requiresAdminForWrite(path)) {
            if (!isAdminRole(role)) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, 
                    "Only COORDINATOR or ADMIN can perform this operation");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private boolean isPublic(HttpServletRequest req, String path) {
        // Root should not be a wildcard. We allow it explicitly.
        if ("/".equals(path)) {
            return true;
        }

        // Exact public endpoints
        if ("/login".equals(path) || "/logout".equals(path) || "/pages/login.jsp".equals(path)) {
            return true;
        }

        // Static assets are public by prefix
        return path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/");
    }

    private boolean matchesAny(String path, String[] prefixes) {
        for (String prefix : prefixes) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAuthorized(String role, String path) {
        switch (role) {
            case "STUDENT":
                return matchesAny(path, STUDENT_PATHS);
            case "PROCTOR":
                return matchesAny(path, PROCTOR_PATHS);
            case "FACULTY":
            case "COORDINATOR":
            case "ADMIN":
                return matchesAny(path, ADMIN_PATHS);
            default:
                return false;
        }
    }

    /**
     * Check if path requires admin role for write operations (POST).
     */
    private boolean requiresAdminForWrite(String path) {
        return matchesAny(path, ADMIN_WRITE_PATHS);
    }

    /**
     * Check if role is admin-level (COORDINATOR or ADMIN).
     */
    private boolean isAdminRole(String role) {
        return "COORDINATOR".equals(role) || "ADMIN".equals(role);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }
}
