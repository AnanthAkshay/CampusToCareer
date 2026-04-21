package com.rit.placement.controller;

import com.rit.placement.service.MetricsService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Metrics Endpoint
 * URL: /metrics
 * 
 * Returns detailed application metrics including:
 * - Login statistics
 * - Application submissions
 * - Job creations
 * - Status updates
 * - Notifications sent
 * - Error counts
 */
@WebServlet("/metrics")
public class MetricsServlet extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(MetricsServlet.class);
    private final MetricsService metricsService = MetricsService.getInstance();
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        logger.debug("Metrics requested from {}", req.getRemoteAddr());
        
        // Set response
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        
        // Get metrics summary
        String metricsJson = metricsService.getMetricsSummary();
        
        try (PrintWriter out = resp.getWriter()) {
            out.print(metricsJson);
            out.flush();
        }
        
        logger.debug("Metrics response sent successfully");
    }
}
