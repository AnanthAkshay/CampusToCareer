package com.rit.placement.listener;

import com.rit.placement.service.CSVImporterService;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application Startup Listener
 * Runs initialization tasks when the application starts.
 *
 * DB-dependent tasks (CSV import, status check) are offloaded to a background
 * daemon thread with retry logic so that Tomcat finishes deploying immediately
 * even if MySQL is still initializing (common Docker race condition).
 *
 * Retry policy: up to 10 attempts, 3 seconds apart (~30 s total window).
 */
@WebListener
public class ApplicationStartupListener implements ServletContextListener {

    private static final Logger logger    = LoggerFactory.getLogger(ApplicationStartupListener.class);
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");

    private static final int    MAX_RETRIES     = 10;
    private static final long   RETRY_DELAY_MS  = 3_000L;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("=== RIT Placement Portal Starting ===");
        auditLogger.info("APPLICATION_STARTUP - RIT Placement Portal initializing");

        // Deploy completes immediately; DB work runs in background with retries.
        Thread initThread = new Thread(() -> {
            for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
                try {
                    logger.info("DB init attempt {}/{} - connecting...", attempt, MAX_RETRIES);

                    // This will throw if DB is not ready yet
                    CSVImporterService.importStudentsIfNeeded();

                    // Only reached if importStudentsIfNeeded succeeded
                    String status = CSVImporterService.getImportStatus();
                    logger.info("Database status: {}", status);
                    logger.info("=== RIT Placement Portal Ready (attempt {}) ===", attempt);
                    auditLogger.info("APPLICATION_STARTUP_COMPLETE - Application ready after {} attempt(s)", attempt);
                    return; // genuine success — exit retry loop

                } catch (Exception e) {
                    if (attempt < MAX_RETRIES) {
                        logger.warn("DB not ready (attempt {}/{}): {} — retrying in {}s...",
                                attempt, MAX_RETRIES, e.getMessage(), RETRY_DELAY_MS / 1000);
                        try {
                            Thread.sleep(RETRY_DELAY_MS);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            logger.warn("Startup init thread interrupted");
                            return;
                        }
                    } else {
                        logger.error("DB init failed after {} attempts: {}", MAX_RETRIES, e.getMessage());
                        auditLogger.info("APPLICATION_STARTUP_FAILED - Error after {} retries: {}",
                                MAX_RETRIES, e.getMessage());
                    }
                }
            }
        }, "startup-db-init");

        initThread.setDaemon(true);
        initThread.start();

        logger.info("=== RIT Placement Portal Started — DB init running in background ===");
        auditLogger.info("APPLICATION_STARTUP_COMPLETE - Application ready");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("=== RIT Placement Portal Shutting Down ===");
        auditLogger.info("APPLICATION_SHUTDOWN - RIT Placement Portal stopping");
    }
}

