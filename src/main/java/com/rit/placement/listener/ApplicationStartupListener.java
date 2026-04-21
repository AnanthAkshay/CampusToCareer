package com.rit.placement.listener;

import com.rit.placement.service.CSVImporterService;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application Startup Listener
 * Runs initialization tasks when the application starts
 * 
 * Tasks:
 * - Import students from CSV if database is empty
 * - Initialize application metrics
 * - Log startup information
 */
@WebListener
public class ApplicationStartupListener implements ServletContextListener {
    
    private static final Logger logger = LoggerFactory.getLogger(ApplicationStartupListener.class);
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("=== RIT Placement Portal Starting ===");
        auditLogger.info("APPLICATION_STARTUP - RIT Placement Portal initializing");
        
        try {
            // Import students from CSV
            logger.info("Checking if CSV import is needed...");
            CSVImporterService.importStudentsIfNeeded();
            
            // Log import status
            String status = CSVImporterService.getImportStatus();
            logger.info("Database status: {}", status);
            
            logger.info("=== RIT Placement Portal Started Successfully ===");
            auditLogger.info("APPLICATION_STARTUP_COMPLETE - Application ready");
            
        } catch (Exception e) {
            logger.error("Application startup failed", e);
            auditLogger.info("APPLICATION_STARTUP_FAILED - Error: {}", e.getMessage());
        }
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("=== RIT Placement Portal Shutting Down ===");
        auditLogger.info("APPLICATION_SHUTDOWN - RIT Placement Portal stopping");
    }
}
