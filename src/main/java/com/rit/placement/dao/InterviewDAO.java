package com.rit.placement.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rit.placement.model.Interview;
import com.rit.placement.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for the 'interviews' table
 */
public class InterviewDAO {
    private static final Logger logger = LoggerFactory.getLogger(InterviewDAO.class);
    
    /**
     * Schedule a new interview
     */
    public int scheduleInterview(Interview interview) throws SQLException {
        String sql = "INSERT INTO interviews (application_id, student_id, company_id, job_id, " +
                     "interview_date, interview_mode, interview_location, interview_link, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, interview.getApplicationId());
            stmt.setInt(2, interview.getStudentId());
            stmt.setInt(3, interview.getCompanyId());
            stmt.setInt(4, interview.getJobId());
            stmt.setTimestamp(5, interview.getInterviewDate());
            stmt.setString(6, interview.getInterviewMode());
            stmt.setString(7, interview.getInterviewLocation());
            stmt.setString(8, interview.getInterviewLink());
            stmt.setString(9, interview.getNotes());
            
            stmt.executeUpdate();
            
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }
            throw new SQLException("Failed to retrieve generated interview_id");
        }
    }
    
    /**
     * Get interviews for a company
     */
    public List<Interview> getInterviewsByCompany(int companyId) throws SQLException {
        String sql = "SELECT i.*, u.name as student_name, u.usn as student_usn, " +
                     "jp.role as job_title, c.company_name " +
                     "FROM interviews i " +
                     "JOIN users u ON i.student_id = u.user_id " +
                     "JOIN job_postings jp ON i.job_id = jp.job_id " +
                     "JOIN companies c ON i.company_id = c.company_id " +
                     "WHERE i.company_id = ? " +
                     "ORDER BY i.interview_date DESC";
        
        List<Interview> interviews = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, companyId);
            try (ResultSet rs = stmt.executeQuery()) {
            
                while (rs.next()) {
                    interviews.add(mapRow(rs));
                }
            }
        }
        
        return interviews;
    }
    
    /**
     * Get interviews for a student
     */
    public List<Interview> getInterviewsByStudent(int studentId) throws SQLException {
        String sql = "SELECT i.*, u.name as student_name, u.usn as student_usn, " +
                     "jp.role as job_title, c.company_name " +
                     "FROM interviews i " +
                     "JOIN users u ON i.student_id = u.user_id " +
                     "JOIN job_postings jp ON i.job_id = jp.job_id " +
                     "JOIN companies c ON i.company_id = c.company_id " +
                     "WHERE i.student_id = ? " +
                     "ORDER BY i.interview_date DESC";
        
        List<Interview> interviews = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
            
                while (rs.next()) {
                    interviews.add(mapRow(rs));
                }
            }
        }
        
        return interviews;
    }
    
    /**
     * Get interview by ID
     */
    public Interview getInterviewById(int interviewId) throws SQLException {
        String sql = "SELECT i.*, u.name as student_name, u.usn as student_usn, " +
                     "jp.role as job_title, c.company_name " +
                     "FROM interviews i " +
                     "JOIN users u ON i.student_id = u.user_id " +
                     "JOIN job_postings jp ON i.job_id = jp.job_id " +
                     "JOIN companies c ON i.company_id = c.company_id " +
                     "WHERE i.interview_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, interviewId);
            try (ResultSet rs = stmt.executeQuery()) {
            
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Update interview status
     */
    public void updateInterviewStatus(int interviewId, String status) throws SQLException {
        String sql = "UPDATE interviews SET status = ? WHERE interview_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            stmt.setInt(2, interviewId);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Check if interview belongs to company (for security)
     */
    public boolean belongsToCompany(int interviewId, int companyId) {
        String sql = "SELECT COUNT(*) FROM interviews WHERE interview_id = ? AND company_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, interviewId);
            stmt.setInt(2, companyId);
            try (ResultSet rs = stmt.executeQuery()) {
            
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.error("Error checking interview ownership: " + e.getMessage());
            logger.error("Database error", e);
        }
        
        return false;
    }
    
    /**
     * Check if student has conflicting interview at the same time
     * Prevents double-booking of students
     */
    public boolean hasConflictingInterview(int studentId, Timestamp interviewDate) {
        // Check for interviews within 1 hour window
        String sql = "SELECT COUNT(*) FROM interviews " +
                     "WHERE student_id = ? " +
                     "AND ABS(TIMESTAMPDIFF(MINUTE, interview_date, ?)) < 60 " +
                     "AND status != 'CANCELLED'";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, studentId);
            stmt.setTimestamp(2, interviewDate);
            try (ResultSet rs = stmt.executeQuery()) {
            
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.error("Error checking interview conflicts: " + e.getMessage());
            logger.error("Database error", e);
        }
        
        return false;
    }
    
    /**
     * Map ResultSet to Interview object
     */
    private Interview mapRow(ResultSet rs) throws SQLException {
        Interview interview = new Interview();
        interview.setInterviewId(rs.getInt("interview_id"));
        interview.setApplicationId(rs.getInt("application_id"));
        interview.setStudentId(rs.getInt("student_id"));
        interview.setCompanyId(rs.getInt("company_id"));
        interview.setJobId(rs.getInt("job_id"));
        interview.setInterviewDate(rs.getTimestamp("interview_date"));
        interview.setInterviewMode(rs.getString("interview_mode"));
        interview.setInterviewLocation(rs.getString("interview_location"));
        interview.setInterviewLink(rs.getString("interview_link"));
        interview.setStatus(rs.getString("status"));
        interview.setNotes(rs.getString("notes"));
        interview.setCreatedAt(rs.getTimestamp("created_at"));
        interview.setUpdatedAt(rs.getTimestamp("updated_at"));
        interview.setStudentName(rs.getString("student_name"));
        interview.setStudentUsn(rs.getString("student_usn"));
        interview.setJobTitle(rs.getString("job_title"));
        interview.setCompanyName(rs.getString("company_name"));
        return interview;
    }
}
