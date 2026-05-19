package com.rit.placement.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rit.placement.model.Document;
import com.rit.placement.util.DBConnection;
import java.sql.*;

/**
 * DAO for the 'documents' table.
 */
public class DocumentDAO {
    private static final Logger logger = LoggerFactory.getLogger(DocumentDAO.class);

    /**
     * Insert a new document record
     */
    public int insertDocument(Document document) throws SQLException {
        String sql = "INSERT INTO documents (student_id, resume_path, certificates_path) VALUES (?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setInt(1, document.getStudentId());
            ps.setString(2, document.getResumePath());
            ps.setString(3, document.getCertificatesPath());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }
            throw new SQLException("Failed to retrieve generated document_id.");
        }
    }

    /**
     * Get document by student ID
     */
    public Document getDocumentByStudentId(int studentId) throws SQLException {
        String sql = "SELECT * FROM documents WHERE student_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
            
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        }
    }

    /**
     * Update resume path for a student
     */
    public void updateResumePath(int studentId, String resumePath) throws SQLException {
        String sql = "UPDATE documents SET resume_path = ? WHERE student_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, resumePath);
            ps.setInt(2, studentId);
            ps.executeUpdate();
        }
    }

    /**
     * Update certificates path for a student
     */
    public void updateCertificatesPath(int studentId, String certificatesPath) throws SQLException {
        String sql = "UPDATE documents SET certificates_path = ? WHERE student_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, certificatesPath);
            ps.setInt(2, studentId);
            ps.executeUpdate();
        }
    }

    /**
     * Delete document record
     */
    public void deleteDocument(int studentId) throws SQLException {
        String sql = "DELETE FROM documents WHERE student_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, studentId);
            ps.executeUpdate();
        }
    }

    /**
     * Map ResultSet row to Document object
     */
    private Document mapRow(ResultSet rs) throws SQLException {
        Document document = new Document();
        document.setDocumentId(rs.getInt("document_id"));
        document.setStudentId(rs.getInt("student_id"));
        document.setResumePath(rs.getString("resume_path"));
        document.setCertificatesPath(rs.getString("certificates_path"));
        document.setUploadedAt(rs.getTimestamp("uploaded_at"));
        document.setUpdatedAt(rs.getTimestamp("updated_at"));
        return document;
    }
}
