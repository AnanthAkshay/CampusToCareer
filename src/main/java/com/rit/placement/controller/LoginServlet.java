package com.rit.placement.controller;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;

/**
 * Login Servlet - Redirects to OTP-based login
 * 
 * This servlet now redirects all login requests to the OTP login page.
 * Password-based authentication has been replaced with OTP authentication.
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    /** GET on /login redirects to OTP login page */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // Redirect to OTP login page
        resp.sendRedirect(req.getContextPath() + "/pages/login-otp.jsp");
    }

    /** POST on /login also redirects to OTP login page */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Redirect to OTP login page
        resp.sendRedirect(req.getContextPath() + "/pages/login-otp.jsp");
    }
}
