package com.rit.placement.dao;

import com.rit.placement.model.CompanyRequest;
import com.rit.placement.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for company_requests table
 */
public class CompanyRequestDAO {

    /**
     * Insert a new company request
     */
    public int insertRequest(CompanyRequest request) throws SQLException {
        String sql = "INSERT INTO company_requests (company_name, description, email, company_type, status) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, request.getCompanyName());
            ps.setString(2, request.getDescription());
            ps.setString(3, request.getEmail());
            ps.setString(4, request.getCompanyType() != null ? request.getCompanyType() : "PRODUCT");
            ps.setString(5, request.getStatus() != null ? request.getStatus() : "PENDING");
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }
            throw new SQLException("Failed to retrieve generated request_id.");
        }
    }

    /**
     * Get all pending requests
     */
    public List<CompanyRequest> getPendingRequests() throws SQLException {
        String sql = "SELECT * FROM company_requests WHERE status = 'PENDING' ORDER BY requested_at DESC";
        List<CompanyRequest> requests = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                requests.add(mapRow(rs));
            }
        }
        return requests;
    }

    /**
     * Get all requests (for admin view)
     */
    public List<CompanyRequest> getAllRequests() throws SQLException {
        String sql = "SELECT * FROM company_requests ORDER BY requested_at DESC";
        List<CompanyRequest> requests = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                requests.add(mapRow(rs));
            }
        }
        return requests;
    }

    /**
     * Get request by ID
     */
    public CompanyRequest getRequestById(int requestId) throws SQLException {
        String sql = "SELECT * FROM company_requests WHERE request_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, requestId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;
        }
    }

    /**
     * Update request status (approve/reject)
     */
    public void updateStatus(int requestId, String status, int reviewedBy) throws SQLException {
        String sql = "UPDATE company_requests SET status = ?, reviewed_at = NOW(), reviewed_by = ? " +
                     "WHERE request_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, status);
            ps.setInt(2, reviewedBy);
            ps.setInt(3, requestId);
            ps.executeUpdate();
        }
    }

    /**
     * Delete a request
     */
    public void deleteRequest(int requestId) throws SQLException {
        String sql = "DELETE FROM company_requests WHERE request_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, requestId);
            ps.executeUpdate();
        }
    }

    /**
     * Map ResultSet row to CompanyRequest object
     */
    private CompanyRequest mapRow(ResultSet rs) throws SQLException {
        CompanyRequest request = new CompanyRequest();
        request.setRequestId(rs.getInt("request_id"));
        request.setCompanyName(rs.getString("company_name"));
        request.setDescription(rs.getString("description"));
        request.setEmail(rs.getString("email"));
        request.setCompanyType(rs.getString("company_type"));
        request.setStatus(rs.getString("status"));
        request.setRequestedAt(rs.getTimestamp("requested_at"));
        request.setReviewedAt(rs.getTimestamp("reviewed_at"));
        
        int reviewedBy = rs.getInt("reviewed_by");
        request.setReviewedBy(rs.wasNull() ? null : reviewedBy);
        
        return request;
    }
}
