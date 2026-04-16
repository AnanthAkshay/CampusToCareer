package com.rit.placement.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * JDBC connection utility backed by environment variables.
 */
public class DBConnection {

    private static final String URL;
    private static final String USER;
    private static final String PASS;

    static {
    URL = "jdbc:mysql://localhost:3306/rit_placement";
    USER = "root";
    PASS = "Akshay@2006"; // 🔴 change this

    try {
        Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException e) {
        throw new IllegalStateException("MySQL JDBC Driver not found.", e);
    }
}

    private DBConnection() {
    }

    private static String requireEnv(String key) {
        String value = System.getenv(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Missing required environment variable: " + key);
        }
        return value.trim();
    }

    /**
     * Returns a new database connection.
     * Caller is responsible for closing the connection (use try-with-resources).
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    /** Quick connectivity test — run this standalone to verify config. */
    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            System.out.println("Connected to database successfully!");
        } catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
        }
    }
}
