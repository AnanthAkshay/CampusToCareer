package com.rit.placement.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * JDBC connection utility (Docker-ready + production-safe).
 * Uses HikariCP for connection pooling.
 * Requires environment variables: DB_URL, DB_USER, DB_PASSWORD
 */
public class DBConnection {

    private static final HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(getEnv("DB_URL"));
        config.setUsername(getEnv("DB_USER"));
        config.setPassword(getEnv("DB_PASSWORD"));
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        
        // HikariCP recommended settings
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setIdleTimeout(300000);
        config.setConnectionTimeout(20000);

        dataSource = new HikariDataSource(config);
    }

    private DBConnection() {
        // prevent instantiation
    }

    private static String getEnv(String key) {
        String value = System.getenv(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Missing required environment variable: " + key);
        }
        return value.trim();
    }

    /**
     * Get a connection from the HikariCP pool.
     * Always use try-with-resources while calling.
     */
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Shut down the connection pool (call during application shutdown)
     */
    public static void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    /**
     * Quick test (optional)
     */
    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            System.out.println("✅ DB Connected Successfully via HikariCP");
        } catch (Exception e) {
            System.out.println("❌ DB Connection Failed: " + e.getMessage());
        } finally {
            closePool();
        }
    }
}