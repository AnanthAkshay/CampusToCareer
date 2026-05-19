package com.rit.placement.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rit.placement.model.Student;
import com.rit.placement.util.DBConnection;
import java.sql.*;

/**
 * DAO for the 'students' table.
 */
public class StudentDAO {
    private static final Logger logger = LoggerFactory.getLogger(StudentDAO.class);

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

    private String getStudentSkills(Connection conn, int studentId) throws SQLException {
        String sql = "SELECT GROUP_CONCAT(sk.skill_name SEPARATOR ', ') FROM student_skills ss " +
                     "JOIN skills sk ON ss.skill_id = sk.skill_id WHERE ss.student_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String s = rs.getString(1);
                    return s != null ? s : "";
                }
            }
        }
        return "";
    }

    private int getOrCreateSkill(Connection conn, String skillName) throws SQLException {
        String querySelect = "SELECT skill_id FROM skills WHERE skill_name = ?";
        try (PreparedStatement ps = conn.prepareStatement(querySelect)) {
            ps.setString(1, skillName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        String queryInsert = "INSERT IGNORE INTO skills (skill_name) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(queryInsert, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, skillName);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        try (PreparedStatement ps = conn.prepareStatement(querySelect)) {
            ps.setString(1, skillName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    /** Fetch a student by primary key. Returns null if not found. */
    public Student getStudentById(int studentId) throws SQLException {
        String sql = "SELECT * FROM students WHERE student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Student s = new Student();
                    s.setStudentId(rs.getInt("student_id"));
                    s.setBranch(rs.getString("branch"));
                    s.setCurrentSem(rs.getInt("current_sem"));
                    s.setSkills(getStudentSkills(conn, studentId));
                    s.setProjects(rs.getString("projects"));
                    s.setExperience(rs.getString("experience"));
                    s.setUpdatedAt(rs.getTimestamp("updated_at"));
                    return s;
                }
                return null;
            }
        }
    }

    /** Update student profile (skills, projects, experience). */
    public void updateProfile(int studentId, String skills, String projects, String experience) 
            throws SQLException {
        String sql = "UPDATE students SET projects = ?, experience = ? WHERE student_id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Update projects and experience
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, projects);
                    ps.setString(2, experience);
                    ps.setInt(3, studentId);
                    ps.executeUpdate();
                }
                
                // Clear old student skills
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM student_skills WHERE student_id = ?")) {
                    ps.setInt(1, studentId);
                    ps.executeUpdate();
                }
                
                // Insert new student skills
                if (skills != null && !skills.trim().isEmpty()) {
                    String[] skillArr = skills.split(",");
                    for (String skill : skillArr) {
                        String cleanSkill = skill.trim().toLowerCase();
                        if (!cleanSkill.isEmpty()) {
                            int skillId = getOrCreateSkill(conn, cleanSkill);
                            if (skillId != -1) {
                                try (PreparedStatement ps = conn.prepareStatement(
                                        "INSERT IGNORE INTO student_skills (student_id, skill_id) VALUES (?, ?)")) {
                                    ps.setInt(1, studentId);
                                    ps.setInt(2, skillId);
                                    ps.executeUpdate();
                                }
                            }
                        }
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
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
                int studentId = rs.getInt("student_id");
                s.setStudentId(studentId);
                s.setBranch(rs.getString("branch"));
                s.setCurrentSem(rs.getInt("current_sem"));
                s.setSkills(getStudentSkills(conn, studentId));
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
