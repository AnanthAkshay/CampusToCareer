package com.rit.placement.dao;

import com.rit.placement.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO for generating placement reports
 */
public class ReportDAO {
    
    /**
     * Student report data structure
     */
    public static class StudentReportData {
        private String name;
        private String usn;
        private double cgpa;
        private int applications;
        private String status;
        private String branch;
        
        public StudentReportData(String name, String usn, double cgpa, int applications, String status, String branch) {
            this.name = name;
            this.usn = usn;
            this.cgpa = cgpa;
            this.applications = applications;
            this.status = status;
            this.branch = branch;
        }
        
        public String getName() { return name; }
        public String getUsn() { return usn; }
        public double getCgpa() { return cgpa; }
        public int getApplications() { return applications; }
        public String getStatus() { return status; }
        public String getBranch() { return branch; }
    }
    
    /**
     * Fetch all students with their placement data for reports
     */
    public List<StudentReportData> getStudentReportData() {
        List<StudentReportData> students = new ArrayList<>();
        
        String sql = "SELECT " +
                     "u.name, u.usn, s.branch, " +
                     "COALESCE((SELECT AVG(ar.sgpa) FROM academic_records ar WHERE ar.student_id = u.user_id), 0) as cgpa, " +
                     "COALESCE((SELECT COUNT(*) FROM applications a WHERE a.student_id = u.user_id), 0) as app_count, " +
                     "CASE " +
                     "  WHEN EXISTS(SELECT 1 FROM applications a WHERE a.student_id = u.user_id AND a.status = 'SELECTED') THEN 'PLACED' " +
                     "  WHEN EXISTS(SELECT 1 FROM applications a WHERE a.student_id = u.user_id AND a.status = 'SHORTLISTED') THEN 'SHORTLISTED' " +
                     "  WHEN EXISTS(SELECT 1 FROM applications a WHERE a.student_id = u.user_id) THEN 'APPLIED' " +
                     "  ELSE 'NOT APPLIED' " +
                     "END as status " +
                     "FROM users u " +
                     "JOIN students s ON u.user_id = s.student_id " +
                     "WHERE u.role = 'STUDENT' AND u.is_active = TRUE " +
                     "ORDER BY u.name";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                StudentReportData data = new StudentReportData(
                    rs.getString("name"),
                    rs.getString("usn"),
                    rs.getDouble("cgpa"),
                    rs.getInt("app_count"),
                    rs.getString("status"),
                    rs.getString("branch")
                );
                students.add(data);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching student report data: " + e.getMessage());
            e.printStackTrace();
        }
        
        return students;
    }
    
    /**
     * Get summary statistics for reports
     */
    public Map<String, Object> getSummaryStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try (Connection conn = DBConnection.getConnection()) {
            // Total students
            String sql1 = "SELECT COUNT(*) FROM users WHERE role = 'STUDENT' AND is_active = TRUE";
            try (PreparedStatement stmt = conn.prepareStatement(sql1);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("totalStudents", rs.getInt(1));
                }
            }
            
            // Placed students
            String sql2 = "SELECT COUNT(DISTINCT student_id) FROM applications WHERE status = 'SELECTED'";
            try (PreparedStatement stmt = conn.prepareStatement(sql2);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("placedStudents", rs.getInt(1));
                }
            }
            
            // Total applications
            String sql3 = "SELECT COUNT(*) FROM applications";
            try (PreparedStatement stmt = conn.prepareStatement(sql3);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("totalApplications", rs.getInt(1));
                }
            }
            
            // Total companies
            String sql4 = "SELECT COUNT(*) FROM companies WHERE is_active = TRUE";
            try (PreparedStatement stmt = conn.prepareStatement(sql4);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("totalCompanies", rs.getInt(1));
                }
            }
            
            // Calculate placement percentage
            int total = (Integer) stats.getOrDefault("totalStudents", 0);
            int placed = (Integer) stats.getOrDefault("placedStudents", 0);
            double percentage = total > 0 ? (placed * 100.0 / total) : 0.0;
            stats.put("placementPercentage", percentage);
            
        } catch (SQLException e) {
            System.err.println("Error fetching summary stats: " + e.getMessage());
            e.printStackTrace();
        }
        
        return stats;
    }
}
