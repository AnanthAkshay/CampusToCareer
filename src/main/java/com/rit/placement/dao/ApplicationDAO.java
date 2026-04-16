package com.rit.placement.dao;

import com.rit.placement.model.Application;
import com.rit.placement.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for the 'applications' table.
 */
public class ApplicationDAO {

    /**
     * Insert a new application and return the generated application_id.
     */
    public int insertApplication(Application application) throws SQLException {
        String sql = "INSERT INTO applications (student_id, job_id, status) VALUES (?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setInt(1, application.getStudentId());
            ps.setInt(2, application.getJobId());
            ps.setString(3, application.getStatus() != null ? application.getStatus() : "APPLIED");
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }
            throw new SQLException("Failed to retrieve generated application_id.");
        }
    }

    /**
     * Check if a student has already applied for a specific job.
     */
    public boolean hasApplied(int studentId, int jobId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM applications WHERE student_id = ? AND job_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, studentId);
            ps.setInt(2, jobId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        }
    }

    /**
     * Get all applications for a specific student with job and company details.
     */
    public List<Application> getApplicationsByStudent(int studentId) throws SQLException {
        String sql = "SELECT a.*, j.role as job_role, c.company_name " +
                     "FROM applications a " +
                     "INNER JOIN job_postings j ON a.job_id = j.job_id " +
                     "INNER JOIN companies c ON j.company_id = c.company_id " +
                     "WHERE a.student_id = ? " +
                     "ORDER BY a.application_id DESC";
        List<Application> applications = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                applications.add(mapRowWithDetails(rs));
            }
        }
        return applications;
    }

    /**
     * Get all applications for a specific job with student details.
     */
    public List<Application> getApplicationsByJob(int jobId) throws SQLException {
        String sql = "SELECT a.*, u.name as student_name, u.usn as student_usn, " +
                     "j.role as job_role, c.company_name " +
                     "FROM applications a " +
                     "INNER JOIN users u ON a.student_id = u.user_id " +
                     "INNER JOIN job_postings j ON a.job_id = j.job_id " +
                     "INNER JOIN companies c ON j.company_id = c.company_id " +
                     "WHERE a.job_id = ? " +
                     "ORDER BY a.application_id DESC";
        List<Application> applications = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, jobId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                Application app = mapRowWithDetails(rs);
                app.setStudentName(rs.getString("student_name"));
                app.setStudentUsn(rs.getString("student_usn"));
                applications.add(app);
            }
        }
        return applications;
    }

    /**
     * Get all applications (for admin/coordinator).
     */
    public List<Application> getAllApplications() throws SQLException {
        String sql = "SELECT a.*, u.name as student_name, u.usn as student_usn, " +
                     "j.role as job_role, c.company_name " +
                     "FROM applications a " +
                     "INNER JOIN users u ON a.student_id = u.user_id " +
                     "INNER JOIN job_postings j ON a.job_id = j.job_id " +
                     "INNER JOIN companies c ON j.company_id = c.company_id " +
                     "ORDER BY a.application_id DESC";
        List<Application> applications = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Application app = mapRowWithDetails(rs);
                app.setStudentName(rs.getString("student_name"));
                app.setStudentUsn(rs.getString("student_usn"));
                applications.add(app);
            }
        }
        return applications;
    }

    /**
     * Update application status.
     */
    public void updateStatus(int applicationId, String status) throws SQLException {
        String sql = "UPDATE applications SET status = ? WHERE application_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, status);
            ps.setInt(2, applicationId);
            ps.executeUpdate();
        }
    }

    /**
     * Get application by ID.
     */
    public Application getApplicationById(int applicationId) throws SQLException {
        String sql = "SELECT a.*, u.name as student_name, u.usn as student_usn, " +
                     "j.role as job_role, c.company_name " +
                     "FROM applications a " +
                     "INNER JOIN users u ON a.student_id = u.user_id " +
                     "INNER JOIN job_postings j ON a.job_id = j.job_id " +
                     "INNER JOIN companies c ON j.company_id = c.company_id " +
                     "WHERE a.application_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, applicationId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                Application app = mapRowWithDetails(rs);
                app.setStudentName(rs.getString("student_name"));
                app.setStudentUsn(rs.getString("student_usn"));
                return app;
            }
            return null;
        }
    }

    /**
     * Delete an application.
     */
    public void deleteApplication(int applicationId) throws SQLException {
        String sql = "DELETE FROM applications WHERE application_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, applicationId);
            ps.executeUpdate();
        }
    }

    /**
     * Get application statistics for a student.
     */
    public ApplicationStats getStudentStats(int studentId) throws SQLException {
        String sql = "SELECT " +
                     "COUNT(*) as total, " +
                     "SUM(CASE WHEN status = 'APPLIED' THEN 1 ELSE 0 END) as applied, " +
                     "SUM(CASE WHEN status = 'SHORTLISTED' THEN 1 ELSE 0 END) as shortlisted, " +
                     "SUM(CASE WHEN status = 'SELECTED' THEN 1 ELSE 0 END) as selected, " +
                     "SUM(CASE WHEN status = 'REJECTED' THEN 1 ELSE 0 END) as rejected " +
                     "FROM applications WHERE student_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return new ApplicationStats(
                    rs.getInt("total"),
                    rs.getInt("applied"),
                    rs.getInt("shortlisted"),
                    rs.getInt("selected"),
                    rs.getInt("rejected")
                );
            }
            return new ApplicationStats(0, 0, 0, 0, 0);
        }
    }

    /**
     * Map ResultSet row to Application object (with job and company details).
     */
    private Application mapRowWithDetails(ResultSet rs) throws SQLException {
        Application app = new Application();
        app.setApplicationId(rs.getInt("application_id"));
        app.setStudentId(rs.getInt("student_id"));
        app.setJobId(rs.getInt("job_id"));
        app.setStatus(rs.getString("status"));
        
        // Handle applied_at timestamp (might not exist in old schema)
        try {
            app.setAppliedAt(rs.getTimestamp("applied_at"));
        } catch (SQLException e) {
            // Column doesn't exist, ignore
        }
        
        app.setJobRole(rs.getString("job_role"));
        app.setCompanyName(rs.getString("company_name"));
        return app;
    }

    /**
     * Inner class for application statistics.
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
}
