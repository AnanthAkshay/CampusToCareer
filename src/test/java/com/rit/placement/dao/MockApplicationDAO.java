package com.rit.placement.dao;

import com.rit.placement.model.Application;
import com.rit.placement.util.MockDBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Mock version of ApplicationDAO that uses MockDBConnection
 */
public class MockApplicationDAO {
    
    public int insertApplication(Application application) throws SQLException {
        String sql = "INSERT INTO applications (student_id, job_id, status) VALUES (?, ?, ?)";
        
        try (Connection conn = MockDBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, application.getStudentId());
            stmt.setInt(2, application.getJobId());
            stmt.setString(3, application.getStatus());
            stmt.executeUpdate();
            
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new SQLException("Failed to retrieve generated application_id");
        }
    }
    
    public boolean hasApplied(int studentId, int jobId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM applications WHERE student_id = ? AND job_id = ?";
        try (Connection conn = MockDBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, jobId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
    
    public List<Application> getApplicationsByStudent(int studentId) throws SQLException {
        List<Application> applications = new ArrayList<>();
        String sql = "SELECT application_id, student_id, job_id, status, applied_at " +
                     "FROM applications WHERE student_id = ? ORDER BY applied_at DESC";
        
        try (Connection conn = MockDBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Application app = new Application();
                    app.setId(rs.getInt("application_id"));
                    app.setStudentId(rs.getInt("student_id"));
                    app.setJobId(rs.getInt("job_id"));
                    app.setStatus(rs.getString("status"));
                    app.setAppliedDate(rs.getTimestamp("applied_at"));
                    applications.add(app);
                }
            }
        }
        return applications;
    }
    
    public void updateApplicationStatus(int applicationId, String status) throws SQLException {
        String sql = "UPDATE applications SET status = ? WHERE application_id = ?";
        try (Connection conn = MockDBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, applicationId);
            stmt.executeUpdate();
        }
    }
    
    public Application getApplicationById(int applicationId) throws SQLException {
        String sql = "SELECT application_id, student_id, job_id, status, applied_at " +
                     "FROM applications WHERE application_id = ?";
        
        try (Connection conn = MockDBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, applicationId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Application app = new Application();
                    app.setId(rs.getInt("application_id"));
                    app.setStudentId(rs.getInt("student_id"));
                    app.setJobId(rs.getInt("job_id"));
                    app.setStatus(rs.getString("status"));
                    app.setAppliedDate(rs.getTimestamp("applied_at"));
                    return app;
                }
            }
        }
        return null;
    }
}
