package com.rit.placement;

import com.rit.placement.util.DBConnection;
import java.sql.*;

public class CheckDB {
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE users ADD COLUMN failed_login_attempts INT DEFAULT 0");
                System.out.println("Added failed_login_attempts column!");
            } catch (Exception e) {
                System.out.println("Column might already exist: " + e.getMessage());
            }
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE users ADD COLUMN account_locked_until TIMESTAMP NULL");
                System.out.println("Added account_locked_until column!");
            } catch (Exception e) {
                System.out.println("Column might already exist: " + e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBConnection.closePool();
        }
    }
}
