package com.rit.placement.dao;

import com.rit.placement.util.DBConnection;

import java.sql.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * AnalyticsDAO provides aggregated data for placement intelligence dashboard
 */
public class AnalyticsDAO {
    
    /**
     * Get total count of students
     */
    public int countTotalStudents() {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'STUDENT' AND is_active = TRUE";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting total students: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
    
    /**
     * Get count of eligible students (CGPA >= cutoff)
     */
    public int countEligibleStudents(double minCgpa) {
        String sql = "SELECT COUNT(DISTINCT u.user_id) FROM users u " +
                     "JOIN students s ON u.user_id = s.student_id " +
                     "WHERE u.role = 'STUDENT' AND u.is_active = TRUE " +
                     "AND (SELECT AVG(ar.sgpa) FROM academic_records ar WHERE ar.student_id = s.student_id) >= ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, minCgpa);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting eligible students: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
    
    /**
     * Get count of students who have applied to at least one job
     */
    public int countStudentsApplied() {
        String sql = "SELECT COUNT(DISTINCT student_id) FROM applications";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting students applied: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
    
    /**
     * Get count of students shortlisted
     */
    public int countStudentsShortlisted() {
        String sql = "SELECT COUNT(DISTINCT student_id) FROM applications WHERE status = 'SHORTLISTED'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting students shortlisted: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
    
    /**
     * Get count of students placed (status = SELECTED)
     */
    public int countStudentsPlaced() {
        String sql = "SELECT COUNT(DISTINCT student_id) FROM applications WHERE status = 'SELECTED'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting students placed: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
    
    /**
     * Get applications count per company
     * Returns Map<CompanyName, ApplicationCount>
     */
    public Map<String, Integer> getApplicationsByCompany() {
        Map<String, Integer> result = new LinkedHashMap<>();
        String sql = "SELECT c.company_name, COUNT(a.application_id) as app_count " +
                     "FROM applications a " +
                     "JOIN job_postings jp ON a.job_id = jp.job_id " +
                     "JOIN companies c ON jp.company_id = c.company_id " +
                     "GROUP BY c.company_id, c.company_name " +
                     "ORDER BY app_count DESC " +
                     "LIMIT 10";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                result.put(rs.getString("company_name"), rs.getInt("app_count"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching applications by company: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }
    
    /**
     * Get monthly placement trends (last 12 months)
     * Returns Map<Month, PlacementCount>
     */
    public Map<String, Integer> getMonthlyPlacements() {
        Map<String, Integer> result = new LinkedHashMap<>();
        String sql = "SELECT DATE_FORMAT(applied_at, '%Y-%m') as month, " +
                     "COUNT(DISTINCT student_id) as placed_count " +
                     "FROM applications " +
                     "WHERE status = 'SELECTED' " +
                     "AND applied_at >= DATE_SUB(NOW(), INTERVAL 12 MONTH) " +
                     "GROUP BY DATE_FORMAT(applied_at, '%Y-%m') " +
                     "ORDER BY month ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                result.put(rs.getString("month"), rs.getInt("placed_count"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching monthly placements: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }
    
    /**
     * Get placement status breakdown
     * Returns Map<Status, Count>
     */
    public Map<String, Integer> getPlacementStatusBreakdown() {
        Map<String, Integer> result = new HashMap<>();
        String sql = "SELECT status, COUNT(DISTINCT student_id) as count " +
                     "FROM applications " +
                     "GROUP BY status";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                result.put(rs.getString("status"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching placement status breakdown: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }
    
    /**
     * Get total applications count
     */
    public int countTotalApplications() {
        String sql = "SELECT COUNT(*) FROM applications";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting total applications: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
    
    /**
     * Get active job postings count
     */
    public int countActiveJobs() {
        String sql = "SELECT COUNT(*) FROM job_postings WHERE deadline >= CURDATE()";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting active jobs: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
}
