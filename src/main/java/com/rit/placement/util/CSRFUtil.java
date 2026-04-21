package com.rit.placement.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * CSRF Protection Utility
 * Generates and validates CSRF tokens to prevent Cross-Site Request Forgery attacks
 */
public class CSRFUtil {
    
    private static final String CSRF_TOKEN_ATTRIBUTE = "csrf_token";
    private static final int TOKEN_LENGTH = 32;
    private static final SecureRandom secureRandom = new SecureRandom();
    
    /**
     * Generate a new CSRF token and store it in session
     */
    public static String generateToken(HttpSession session) {
        byte[] tokenBytes = new byte[TOKEN_LENGTH];
        secureRandom.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        session.setAttribute(CSRF_TOKEN_ATTRIBUTE, token);
        return token;
    }
    
    /**
     * Get existing CSRF token from session, or generate new one if not exists
     */
    public static String getToken(HttpSession session) {
        String token = (String) session.getAttribute(CSRF_TOKEN_ATTRIBUTE);
        if (token == null) {
            token = generateToken(session);
        }
        return token;
    }
    
    /**
     * Validate CSRF token from request against session token
     */
    public static boolean validateToken(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        
        String sessionToken = (String) session.getAttribute(CSRF_TOKEN_ATTRIBUTE);
        String requestToken = request.getParameter("csrf_token");
        
        if (sessionToken == null || requestToken == null) {
            return false;
        }
        
        // Use constant-time comparison to prevent timing attacks
        return constantTimeEquals(sessionToken, requestToken);
    }
    
    /**
     * Constant-time string comparison to prevent timing attacks
     */
    private static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        
        return result == 0;
    }
}
