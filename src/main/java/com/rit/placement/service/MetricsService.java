package com.rit.placement.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service for tracking application metrics
 * Thread-safe in-memory metrics storage
 */
public class MetricsService {
    
    private static final Logger logger = LoggerFactory.getLogger(MetricsService.class);
    private static final MetricsService INSTANCE = new MetricsService();
    
    // Metrics counters
    private final AtomicLong totalLogins = new AtomicLong(0);
    private final AtomicLong successfulLogins = new AtomicLong(0);
    private final AtomicLong failedLogins = new AtomicLong(0);
    private final AtomicLong totalApplicationsSubmitted = new AtomicLong(0);
    private final AtomicLong totalJobsCreated = new AtomicLong(0);
    private final AtomicLong totalStatusUpdates = new AtomicLong(0);
    private final AtomicLong totalNotificationsSent = new AtomicLong(0);
    private final AtomicLong totalErrors = new AtomicLong(0);
    
    // Per-user metrics
    private final ConcurrentHashMap<String, AtomicLong> loginsByRole = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> applicationsByStatus = new ConcurrentHashMap<>();
    
    // System metrics
    private final LocalDateTime startTime;
    
    private MetricsService() {
        this.startTime = LocalDateTime.now();
        logger.info("MetricsService initialized at {}", startTime);
    }
    
    public static MetricsService getInstance() {
        return INSTANCE;
    }
    
    // Login metrics
    public void recordLogin(String role, boolean success) {
        totalLogins.incrementAndGet();
        if (success) {
            successfulLogins.incrementAndGet();
            loginsByRole.computeIfAbsent(role, k -> new AtomicLong(0)).incrementAndGet();
            logger.debug("Successful login recorded for role: {}", role);
        } else {
            failedLogins.incrementAndGet();
            logger.debug("Failed login attempt recorded");
        }
    }
    
    // Application metrics
    public void recordApplicationSubmitted() {
        totalApplicationsSubmitted.incrementAndGet();
        logger.debug("Application submission recorded. Total: {}", totalApplicationsSubmitted.get());
    }
    
    // Job metrics
    public void recordJobCreated() {
        totalJobsCreated.incrementAndGet();
        logger.debug("Job creation recorded. Total: {}", totalJobsCreated.get());
    }
    
    // Status update metrics
    public void recordStatusUpdate(String newStatus) {
        totalStatusUpdates.incrementAndGet();
        applicationsByStatus.computeIfAbsent(newStatus, k -> new AtomicLong(0)).incrementAndGet();
        logger.debug("Status update recorded: {}. Total updates: {}", newStatus, totalStatusUpdates.get());
    }
    
    // Notification metrics
    public void recordNotificationSent() {
        totalNotificationsSent.incrementAndGet();
        logger.debug("Notification sent recorded. Total: {}", totalNotificationsSent.get());
    }
    
    // Error metrics
    public void recordError() {
        totalErrors.incrementAndGet();
        logger.debug("Error recorded. Total errors: {}", totalErrors.get());
    }
    
    // Getters
    public long getTotalLogins() {
        return totalLogins.get();
    }
    
    public long getSuccessfulLogins() {
        return successfulLogins.get();
    }
    
    public long getFailedLogins() {
        return failedLogins.get();
    }
    
    public long getTotalApplicationsSubmitted() {
        return totalApplicationsSubmitted.get();
    }
    
    public long getTotalJobsCreated() {
        return totalJobsCreated.get();
    }
    
    public long getTotalStatusUpdates() {
        return totalStatusUpdates.get();
    }
    
    public long getTotalNotificationsSent() {
        return totalNotificationsSent.get();
    }
    
    public long getTotalErrors() {
        return totalErrors.get();
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public ConcurrentHashMap<String, AtomicLong> getLoginsByRole() {
        return new ConcurrentHashMap<>(loginsByRole);
    }
    
    public ConcurrentHashMap<String, AtomicLong> getApplicationsByStatus() {
        return new ConcurrentHashMap<>(applicationsByStatus);
    }
    
    /**
     * Get metrics summary as JSON-like string
     */
    public String getMetricsSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"startTime\": \"").append(startTime).append("\",\n");
        sb.append("  \"uptime\": \"").append(getUptime()).append("\",\n");
        sb.append("  \"logins\": {\n");
        sb.append("    \"total\": ").append(totalLogins.get()).append(",\n");
        sb.append("    \"successful\": ").append(successfulLogins.get()).append(",\n");
        sb.append("    \"failed\": ").append(failedLogins.get()).append(",\n");
        sb.append("    \"byRole\": ").append(mapToJson(loginsByRole)).append("\n");
        sb.append("  },\n");
        sb.append("  \"applications\": {\n");
        sb.append("    \"total\": ").append(totalApplicationsSubmitted.get()).append(",\n");
        sb.append("    \"byStatus\": ").append(mapToJson(applicationsByStatus)).append("\n");
        sb.append("  },\n");
        sb.append("  \"jobs\": {\n");
        sb.append("    \"created\": ").append(totalJobsCreated.get()).append("\n");
        sb.append("  },\n");
        sb.append("  \"statusUpdates\": ").append(totalStatusUpdates.get()).append(",\n");
        sb.append("  \"notifications\": ").append(totalNotificationsSent.get()).append(",\n");
        sb.append("  \"errors\": ").append(totalErrors.get()).append("\n");
        sb.append("}");
        return sb.toString();
    }
    
    private String getUptime() {
        LocalDateTime now = LocalDateTime.now();
        long seconds = java.time.Duration.between(startTime, now).getSeconds();
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%dd %dh %dm %ds", days, hours, minutes, secs);
    }
    
    private String mapToJson(ConcurrentHashMap<String, AtomicLong> map) {
        if (map.isEmpty()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder("{");
        map.forEach((key, value) -> {
            sb.append("\"").append(key).append("\": ").append(value.get()).append(", ");
        });
        sb.setLength(sb.length() - 2); // Remove trailing comma
        sb.append("}");
        return sb.toString();
    }
    
    /**
     * Reset all metrics (for testing or maintenance)
     */
    public void reset() {
        totalLogins.set(0);
        successfulLogins.set(0);
        failedLogins.set(0);
        totalApplicationsSubmitted.set(0);
        totalJobsCreated.set(0);
        totalStatusUpdates.set(0);
        totalNotificationsSent.set(0);
        totalErrors.set(0);
        loginsByRole.clear();
        applicationsByStatus.clear();
        logger.info("All metrics reset");
    }
}
