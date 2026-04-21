package com.rit.placement.dao;

import com.rit.placement.model.ProctorRemark;
import com.rit.placement.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProctorRemarkDAO {
    
    public boolean addRemark(int studentId, int proctorId, String remark) {
        // Security: Verify student belongs to this proctor via mapping table before adding remark
        String checkSql = "SELECT COUNT(*) FROM proctor_student_map WHERE student_id = ? AND proctor_id = ?";
        String insertSql = "INSERT INTO proctor_remarks (student_id, proctor_id, remark) VALUES (?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, studentId);
            checkStmt.setInt(2, proctorId);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next() && rs.getInt(1) > 0) {
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setInt(1, studentId);
                    insertStmt.setInt(2, proctorId);
                    insertStmt.setString(3, remark);
                    int rowsAffected = insertStmt.executeUpdate();
                    return rowsAffected > 0;
                }
            } else {
                System.err.println("Security violation: Proctor " + proctorId + " attempted to add remark for unauthorized student " + studentId);
            }
        } catch (SQLException e) {
            System.err.println("Error adding remark for student " + studentId + " by proctor " + proctorId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    public List<ProctorRemark> getRemarksByStudent(int studentId, int proctorId) {
        List<ProctorRemark> remarks = new ArrayList<>();
        // Security: Only fetch remarks for students assigned to this proctor
        String sql = "SELECT pr.*, u.name as proctor_name FROM proctor_remarks pr " +
                     "JOIN users u ON pr.proctor_id = u.user_id " +
                     "JOIN proctor_student_map psm ON pr.student_id = psm.student_id " +
                     "WHERE pr.student_id = ? AND psm.proctor_id = ? " +
                     "ORDER BY pr.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, proctorId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ProctorRemark remark = new ProctorRemark();
                remark.setId(rs.getInt("id"));
                remark.setStudentId(rs.getInt("student_id"));
                remark.setProctorId(rs.getInt("proctor_id"));
                remark.setRemark(rs.getString("remark"));
                remark.setCreatedAt(rs.getTimestamp("created_at"));
                remark.setProctorName(rs.getString("proctor_name"));
                remarks.add(remark);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching remarks for student " + studentId + " by proctor " + proctorId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return remarks;
    }
}
