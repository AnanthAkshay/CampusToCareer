package com.rit.placement.dao;

import com.rit.placement.model.StudentPerformance;
import com.rit.placement.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProctorDAO {
    
    public List<StudentPerformance> getStudentPerformance(int proctorId) {
        List<StudentPerformance> students = new ArrayList<>();
        // Security: Only fetch students assigned to this proctor via mapping table
        String sql = "SELECT u.user_id, u.usn, u.name, u.email, s.branch, s.skills, " +
                     "(SELECT AVG(ar.sgpa) FROM academic_records ar WHERE ar.student_id = s.student_id) as cgpa, " +
                     "(SELECT COUNT(*) FROM applications a WHERE a.student_id = s.student_id) as app_count, " +
                     "(SELECT COUNT(*) > 0 FROM applications a WHERE a.student_id = s.student_id AND a.status = 'SELECTED') as is_placed " +
                     "FROM users u " +
                     "JOIN students s ON u.user_id = s.student_id " +
                     "JOIN proctor_student_map psm ON s.student_id = psm.student_id " +
                     "WHERE psm.proctor_id = ? AND u.role = 'STUDENT' " +
                     "ORDER BY u.name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, proctorId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                StudentPerformance sp = new StudentPerformance();
                sp.setStudentId(rs.getInt("user_id"));
                sp.setUsn(rs.getString("usn"));
                sp.setName(rs.getString("name"));
                sp.setEmail(rs.getString("email"));
                sp.setBranch(rs.getString("branch"));
                sp.setSkills(rs.getString("skills"));
                sp.setCgpa(rs.getDouble("cgpa"));
                sp.setApplicationsCount(rs.getInt("app_count"));
                sp.setPlaced(rs.getBoolean("is_placed"));
                students.add(sp);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching student performance for proctor " + proctorId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return students;
    }
    
    public StudentPerformance getStudentDetail(int studentId, int proctorId) {
        // Security: Verify student belongs to this proctor via mapping table
        String sql = "SELECT u.user_id, u.usn, u.name, u.email, s.branch, s.skills, " +
                     "(SELECT AVG(ar.sgpa) FROM academic_records ar WHERE ar.student_id = s.student_id) as cgpa, " +
                     "(SELECT COUNT(*) FROM applications a WHERE a.student_id = s.student_id) as app_count, " +
                     "(SELECT COUNT(*) > 0 FROM applications a WHERE a.student_id = s.student_id AND a.status = 'SELECTED') as is_placed " +
                     "FROM users u " +
                     "JOIN students s ON u.user_id = s.student_id " +
                     "JOIN proctor_student_map psm ON s.student_id = psm.student_id " +
                     "WHERE u.user_id = ? AND psm.proctor_id = ? AND u.role = 'STUDENT'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, proctorId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                StudentPerformance sp = new StudentPerformance();
                sp.setStudentId(rs.getInt("user_id"));
                sp.setUsn(rs.getString("usn"));
                sp.setName(rs.getString("name"));
                sp.setEmail(rs.getString("email"));
                sp.setBranch(rs.getString("branch"));
                sp.setSkills(rs.getString("skills"));
                sp.setCgpa(rs.getDouble("cgpa"));
                sp.setApplicationsCount(rs.getInt("app_count"));
                sp.setPlaced(rs.getBoolean("is_placed"));
                return sp;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching student detail for student " + studentId + ", proctor " + proctorId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Get list of students assigned to a proctor
     */
    public List<com.rit.placement.model.Student> getStudentsByProctorId(Integer proctorId) throws SQLException {
        List<com.rit.placement.model.Student> students = new ArrayList<>();
        String sql = "SELECT u.user_id, u.usn, u.name, u.email, s.branch, s.current_sem, s.skills, " +
                     "(SELECT AVG(ar.sgpa) FROM academic_records ar WHERE ar.student_id = s.student_id) as cgpa " +
                     "FROM users u " +
                     "JOIN students s ON u.user_id = s.student_id " +
                     "JOIN proctor_student_map psm ON s.student_id = psm.student_id " +
                     "WHERE psm.proctor_id = ? AND u.role = 'STUDENT' " +
                     "ORDER BY u.name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, proctorId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                com.rit.placement.model.Student student = new com.rit.placement.model.Student();
                student.setStudentId(rs.getInt("user_id"));
                student.setUsn(rs.getString("usn"));
                student.setName(rs.getString("name"));
                student.setEmail(rs.getString("email"));
                student.setBranch(rs.getString("branch"));
                student.setCurrentSem(rs.getInt("current_sem"));
                student.setSkills(rs.getString("skills"));
                student.setCgpa(rs.getDouble("cgpa"));
                students.add(student);
            }
        }
        return students;
    }
}
