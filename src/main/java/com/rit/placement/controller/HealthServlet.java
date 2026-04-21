package com.rit.placement.controller;

import com.rit.placement.service.MetricsService;
import com.rit.placement.util.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Health Check Endpoint
 * URL: /health
 * 
 * Returns system health status including:
 * - Application status
 * - Database connectivity
 * - Uptime
 * - Basic metrics
 */
@WebServlet("/health")
public class HealthServlet extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(HealthServlet.class);
    private final MetricsService metricsService = MetricsService.getInstance();
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        logger.debug("Health check requested from {}", req.getRemoteAddr());
        
        // Check database connectivity
        boolean dbHealthy = checkDatabaseHealth();
        
        // Determine overall status
        String status = dbHealthy ? "UP" : "DOWN";
        int httpStatus = dbHealthy ? HttpServletResponse.SC_OK : HttpServletResponse.SC_SERVICE_UNAVAILABLE;
        
        // Set response
        resp.setStatus(httpStatus);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        
        // Build response
        String healthResponse = buildHealthResponse(status, dbHealthy);
        
        try (PrintWriter out = resp.getWriter()) {
            out.print(healthResponse);
            out.flush();
        }
        
        if (!dbHealthy) {
            logger.error("Health check FAILED - Database connectivity issue");
            metricsService.recordError();
        } else {
            logger.debug("Health check PASSED - All systems operational");
        }
    }
    
    /**
     * Check database connectivity
     */
    private boolean checkDatabaseHealth() {
        try (Connection conn = DBConnection.getConnection()) {
            // Try to execute a simple query
            return conn.isValid(5); // 5 second timeout
        } catch (SQLException e) {
            logger.error("Database health check failed", e);
            return false;
        }
    }
    
    /**
     * Build health response JSON
     */
    private String buildHealthResponse(String status, boolean dbHealthy) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = metricsService.getStartTime();
        long uptimeSeconds = java.time.Duration.between(startTime, now).getSeconds();
        
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"status\": \"").append(status).append("\",\n");
        json.append("  \"timestamp\": \"").append(now).append("\",\n");
        json.append("  \"application\": {\n");
        json.append("    \"name\": \"RIT Placement Portal\",\n");
        json.append("    \"version\": \"1.0-SNAPSHOT\",\n");
        json.append("    \"startTime\": \"").append(startTime).append("\",\n");
        json.append("    \"uptime\": \"").append(formatUptime(uptimeSeconds)).append("\",\n");
        json.append("    \"uptimeSeconds\": ").append(uptimeSeconds).append("\n");
        json.append("  },\n");
        json.append("  \"components\": {\n");
        json.append("    \"database\": {\n");
        json.append("      \"status\": \"").append(dbHealthy ? "UP" : "DOWN").append("\",\n");
        json.append("      \"type\": \"MySQL\"\n");
        json.append("    },\n");
        json.append("    \"application\": {\n");
        json.append("      \"status\": \"UP\"\n");
        json.append("    }\n");
        json.append("  },\n");
        json.append("  \"metrics\": {\n");
        json.append("    \"logins\": ").append(metricsService.getTotalLogins()).append(",\n");
        json.append("    \"applications\": ").append(metricsService.getTotalApplicationsSubmitted()).append(",\n");
        json.append("    \"jobs\": ").append(metricsService.getTotalJobsCreated()).append(",\n");
        json.append("    \"errors\": ").append(metricsService.getTotalErrors()).append("\n");
        json.append("  }\n");
        json.append("}");
        
        return json.toString();
    }
    
    /**
     * Format uptime in human-readable format
     */
    private String formatUptime(long seconds) {
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%dd %dh %dm %ds", days, hours, minutes, secs);
    }
}
