package com.rit.placement.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * JDBC connection utility (Docker-ready + production-safe).
 *
 * Requires environment variables:
 * DB_URL, DB_USER, DB_PASSWORD
 */
public class DBConnection {

    private static final String URL;
    private static final String USER;
    private static final String PASS;

    static {
        // 🔒 Strict environment-based configuration (no fallback)
        URL = getEnv("DB_URL");
        USER = getEnv("DB_USER");
        PASS = getEnv("DB_PASSWORD");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found.", e);
        }
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
     * Get a new DB connection.
     * Always use try-with-resources while calling.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    /**
     * Quick test (optional)
     */
    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            System.out.println("✅ DB Connected Successfully");
        } catch (Exception e) {
            System.out.println("❌ DB Connection Failed: " + e.getMessage());
        }
    }
}