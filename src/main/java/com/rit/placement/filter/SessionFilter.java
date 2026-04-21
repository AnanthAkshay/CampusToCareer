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
import java.util.Set;

/**
 * Production-ready session and authorization filter.
 * 
 * SECURITY RULES:
 * - All routes require authentication except public paths
 * - Role-based access control enforced
 * - Session validation on every protected request
 * 
 * ROLE ROUTING:
 * - STUDENT: /student/*, /apply, /companies, /job-postings, /my-applications
 * - COORDINATOR: /admin/*, /companies, /job-postings, /applications
 * - COMPANY: /company/*
 */
@WebFilter("/*")
public class SessionFilter implements Filter {

    // Public paths that don't require authentication
    private static final Set<String> PUBLIC_PATHS = Set.of(
        "/login",
        "/logout",
        "/pages/login.jsp",
        "/pages/login-otp.jsp",
        "/pages/verify-otp.jsp",
        "/otp/send",
        "/otp/verify"
    );

    // Shared paths accessible by multiple roles
    private static final Set<String> SHARED_PATHS = Set.of(
        "/companies",
        "/job-postings"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String path = req.getServletPath();
        if (path == null || path.isEmpty()) {
            path = "/";
        }

        // Allow public paths and static resources
        if (isPublicPath(path) || isStaticResource(path)) {
            chain.doFilter(request, response);
            return;
        }

        // Check authentication
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // Get user role from session
        String role = (String) session.getAttribute("role");
        if (role == null || role.trim().isEmpty()) {
            // Invalid session - force re-login
            session.invalidate();
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // Handle root path "/" - redirect to role-specific dashboard
        if ("/".equals(path)) {
            switch (role.trim().toUpperCase()) {
                case "STUDENT":
                    resp.sendRedirect(req.getContextPath() + "/student/dashboard");
                    return;
                case "PROCTOR":
                    resp.sendRedirect(req.getContextPath() + "/proctor/dashboard");
                    return;
                case "COORDINATOR":
                case "ADMIN":
                    resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
                    return;
                case "COMPANY":
                    resp.sendRedirect(req.getContextPath() + "/company/dashboard");
                    return;
                default:
                    resp.sendRedirect(req.getContextPath() + "/login");
                    return;
            }
        }

        // Enforce role-based access control
        if (!isAuthorizedForPath(role, path)) {
            // Send to custom 403 error page
            req.setAttribute("errorMessage", "Access denied: " + role + " role cannot access " + path);
            req.getRequestDispatcher("/pages/error/403.jsp").forward(req, resp);
            return;
        }

        // All checks passed - proceed
        chain.doFilter(request, response);
    }

    /**
     * Check if path is in public paths (exact match).
     */
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.contains(path);
    }

    /**
     * Check if path is a static resource (CSS, JS, images).
     */
    private boolean isStaticResource(String path) {
        return path.startsWith("/css/") 
            || path.startsWith("/js/") 
            || path.startsWith("/images/")
            || path.startsWith("/components/");
    }

    /**
     * Enforce role-based routing with proper access control.
     * 
     * STUDENT -> /student/*, /apply, /companies, /job-postings, /my-applications
     * PROCTOR -> /proctor/* (proctor dashboard, student management)
     * COORDINATOR/ADMIN -> /admin/*, /companies, /job-postings, /applications
     * COMPANY -> /company/*
     */
    private boolean isAuthorizedForPath(String role, String path) {
        String normalizedRole = role.trim().toUpperCase();

        // Shared paths accessible by multiple roles (read-only for students)
        if (SHARED_PATHS.contains(path)) {
            return true;
        }

        switch (normalizedRole) {
            case "STUDENT":
                // Students can access:
                // - /student/* (profile, dashboard, etc.)
                // - /apply (job application page)
                // - /my-applications (view their applications)
                // - /companies (view companies - read-only)
                // - /job-postings (view jobs - read-only)
                return path.startsWith("/student/") 
                    || path.equals("/apply")
                    || path.equals("/my-applications")
                    || path.startsWith("/eligible-students");

            case "PROCTOR":
                // Proctors can access:
                // - /proctor/* (proctor dashboard, view assigned students)
                return path.startsWith("/proctor/");

            case "COORDINATOR":
            case "ADMIN":
                // Coordinators/Admins can access:
                // - /admin/* (admin panel)
                // - /companies (manage companies)
                // - /job-postings (manage jobs)
                // - /applications (view all applications)
                return path.startsWith("/admin/") 
                    || path.startsWith("/applications");

            case "COMPANY":
                // Companies can access:
                // - /company/* (company dashboard)
                return path.startsWith("/company/");

            default:
                // Unknown role - deny access
                return false;
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No initialization needed
    }

    @Override
    public void destroy() {
        // No cleanup needed
    }
}
