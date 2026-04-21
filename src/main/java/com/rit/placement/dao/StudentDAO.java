package com.rit.placement.dao;

import com.rit.placement.model.Student;
import com.rit.placement.util.DBConnection;
import java.sql.*;

/**
 * DAO for the 'students' table.
 */
public class StudentDAO {

    /** Inserts a new student row (student_id is the FK to users.user_id). */
    public void insertStudent(Student s) throws SQLException {
        String sql = "INSERT INTO students (student_id, branch, current_sem) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, s.getStudentId());
            ps.setString(2, s.getBranch());
            ps.setInt(3, s.getCurrentSem());
            ps.executeUpdate();
        }
    }

    /** Fetch a student by primary key. Returns null if not found. */
    public Student getStudentById(int studentId) throws SQLException {
        String sql = "SELECT * FROM students WHERE student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Student s = new Student();
                s.setStudentId(rs.getInt("student_id"));
                s.setBranch(rs.getString("branch"));
                s.setCurrentSem(rs.getInt("current_sem"));
                s.setSkills(rs.getString("skills"));
                s.setProjects(rs.getString("projects"));
                s.setExperience(rs.getString("experience"));
                s.setUpdatedAt(rs.getTimestamp("updated_at"));
                return s;
            }
            return null;
        }
    }

    /** Update student profile (skills, projects, experience). */
    public void updateProfile(int studentId, String skills, String projects, String experience) 
            throws SQLException {
        String sql = "UPDATE students SET skills = ?, projects = ?, experience = ? WHERE student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, skills);
            ps.setString(2, projects);
            ps.setString(3, experience);
            ps.setInt(4, studentId);
            ps.executeUpdate();
        }
    }
    
    /** Get all students with their user information */
    public java.util.List<Student> getAllStudents() throws SQLException {
        String sql = "SELECT s.*, u.usn, u.name, u.email " +
                     "FROM students s " +
                     "JOIN users u ON s.student_id = u.user_id " +
                     "WHERE u.role = 'STUDENT'";
        java.util.List<Student> students = new java.util.ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Student s = new Student();
                s.setStudentId(rs.getInt("student_id"));
                s.setBranch(rs.getString("branch"));
                s.setCurrentSem(rs.getInt("current_sem"));
                s.setSkills(rs.getString("skills"));
                s.setProjects(rs.getString("projects"));
                s.setExperience(rs.getString("experience"));
                s.setUpdatedAt(rs.getTimestamp("updated_at"));
                s.setUsn(rs.getString("usn"));
                s.setName(rs.getString("name"));
                s.setEmail(rs.getString("email"));
                students.add(s);
            }
        }
        
        return students;
    }
}
