package com.rit.placement.dao;

import com.rit.placement.model.Application;
import com.rit.placement.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ApplicationDAO {
    
    /**
     * Inner class to hold application statistics
     */
    public static class ApplicationStats {
        private int total;
        private int applied;
        private int shortlisted;
        private int selected;
        private int rejected;
        
        public ApplicationStats(int total, int applied, int shortlisted, int selected, int rejected) {
            this.total = total;
            this.applied = applied;
            this.shortlisted = shortlisted;
            this.selected = selected;
            this.rejected = rejected;
        }
        
        public int getTotal() { return total; }
        public int getApplied() { return applied; }
        public int getShortlisted() { return shortlisted; }
        public int getSelected() { return selected; }
        public int getRejected() { return rejected; }
    }
    
    public List<Application> getApplicationsByStudent(int studentId) {
        List<Application> applications = new ArrayList<>();
        String sql = "SELECT a.application_id, a.student_id, a.job_id, a.status, a.applied_at, " +
                     "jp.role as job_title, c.company_name " +
                     "FROM applications a " +
                     "LEFT JOIN job_postings jp ON a.job_id = jp.job_id " +
                     "LEFT JOIN companies c ON jp.company_id = c.company_id " +
                     "WHERE a.student_id = ? ORDER BY a.applied_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Application app = new Application();
                app.setId(rs.getInt("application_id"));
                app.setStudentId(rs.getInt("student_id"));
                app.setJobId(rs.getInt("job_id"));
                app.setStatus(rs.getString("status"));
                app.setAppliedDate(rs.getTimestamp("applied_at"));
                app.setJobTitle(rs.getString("job_title"));
                app.setCompanyName(rs.getString("company_name"));
                applications.add(app);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching applications for student " + studentId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return applications;
    }
    
    /**
     * Get all applications for jobs posted by a specific company
     */
    public List<Application> getApplicationsByCompany(int companyId) {
        List<Application> applications = new ArrayList<>();
        String sql = "SELECT a.application_id, a.student_id, a.job_id, a.status, a.applied_at, " +
                     "jp.role as job_title, u.name as student_name, u.usn as student_usn " +
                     "FROM applications a " +
                     "JOIN job_postings jp ON a.job_id = jp.job_id " +
                     "JOIN users u ON a.student_id = u.user_id " +
                     "WHERE jp.company_id = ? " +
                     "ORDER BY a.applied_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, companyId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Application app = new Application();
                app.setId(rs.getInt("application_id"));
                app.setStudentId(rs.getInt("student_id"));
                app.setJobId(rs.getInt("job_id"));
                app.setStatus(rs.getString("status"));
                app.setAppliedDate(rs.getTimestamp("applied_at"));
                app.setJobTitle(rs.getString("job_title"));
                app.setStudentName(rs.getString("student_name"));
                app.setStudentUsn(rs.getString("student_usn"));
                applications.add(app);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching applications for company " + companyId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return applications;
    }
    
    /**
     * Update application status
     */
    public void updateApplicationStatus(int applicationId, String status) throws SQLException {
        String sql = "UPDATE applications SET status = ? WHERE application_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, applicationId);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Check if application belongs to a company's jobs (for security)
     */
    public boolean belongsToCompany(int applicationId, int companyId) {
        String sql = "SELECT COUNT(*) FROM applications a " +
                     "JOIN job_postings jp ON a.job_id = jp.job_id " +
                     "WHERE a.application_id = ? AND jp.company_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, applicationId);
            stmt.setInt(2, companyId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking application ownership: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Check if student has already applied for a job
     */
    public boolean hasApplied(int studentId, int jobId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM applications WHERE student_id = ? AND job_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, jobId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }
    
    /**
     * Insert new application and return generated ID
     */
    public int insertApplication(Application application) throws SQLException {
        String sql = "INSERT INTO applications (student_id, job_id, status) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, application.getStudentId());
            stmt.setInt(2, application.getJobId());
            stmt.setString(3, application.getStatus());
            stmt.executeUpdate();
            
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }
            throw new SQLException("Failed to retrieve generated application_id");
        }
    }
    
    /**
     * Get all applications (for admin/coordinator)
     */
    public List<Application> getAllApplications() throws SQLException {
        List<Application> applications = new ArrayList<>();
        String sql = "SELECT a.application_id, a.student_id, a.job_id, a.status, a.applied_at, " +
                     "jp.role as job_title, c.company_name, u.name as student_name, u.usn as student_usn " +
                     "FROM applications a " +
                     "LEFT JOIN job_postings jp ON a.job_id = jp.job_id " +
                     "LEFT JOIN companies c ON jp.company_id = c.company_id " +
                     "LEFT JOIN users u ON a.student_id = u.user_id " +
                     "ORDER BY a.applied_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Application app = new Application();
                app.setId(rs.getInt("application_id"));
                app.setStudentId(rs.getInt("student_id"));
                app.setJobId(rs.getInt("job_id"));
                app.setStatus(rs.getString("status"));
                app.setAppliedDate(rs.getTimestamp("applied_at"));
                app.setJobTitle(rs.getString("job_title"));
                app.setCompanyName(rs.getString("company_name"));
                app.setStudentName(rs.getString("student_name"));
                app.setStudentUsn(rs.getString("student_usn"));
                applications.add(app);
            }
        }
        return applications;
    }
    
    /**
     * Update application status (for admin/coordinator)
     */
    public void updateStatus(int applicationId, String status) throws SQLException {
        String sql = "UPDATE applications SET status = ? WHERE application_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, applicationId);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Get application statistics for a student
     */
    public ApplicationStats getStudentStats(Integer studentId) throws SQLException {
        String sql = "SELECT " +
                     "COUNT(*) as total, " +
                     "SUM(CASE WHEN status = 'APPLIED' THEN 1 ELSE 0 END) as applied, " +
                     "SUM(CASE WHEN status = 'SHORTLISTED' THEN 1 ELSE 0 END) as shortlisted, " +
                     "SUM(CASE WHEN status = 'SELECTED' THEN 1 ELSE 0 END) as selected, " +
                     "SUM(CASE WHEN status = 'REJECTED' THEN 1 ELSE 0 END) as rejected " +
                     "FROM applications WHERE student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new ApplicationStats(
                    rs.getInt("total"),
                    rs.getInt("applied"),
                    rs.getInt("shortlisted"),
                    rs.getInt("selected"),
                    rs.getInt("rejected")
                );
            }
        }
        return new ApplicationStats(0, 0, 0, 0, 0);
    }
}
