package com.rit.placement.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rit.placement.model.AcademicRecord;
import com.rit.placement.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for the 'academic_records' table.
 */
public class AcademicDAO {
    private static final Logger logger = LoggerFactory.getLogger(AcademicDAO.class);

    /** Inserts one academic record (one semester SGPA for a student). */
    public void insertRecord(AcademicRecord r) throws SQLException {
        String sql = "INSERT INTO academic_records (student_id, semester, sgpa) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, r.getStudentId());
            ps.setInt(2, r.getSemester());
            ps.setDouble(3, r.getSgpa());
            ps.executeUpdate();
        }
    }

    /** Returns all academic records for a given student, ordered by semester. */
    public List<AcademicRecord> getRecordsByStudentId(int studentId) throws SQLException {
        String sql = "SELECT * FROM academic_records WHERE student_id = ? ORDER BY semester";
        List<AcademicRecord> records = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AcademicRecord r = new AcademicRecord();
                    r.setRecordId(rs.getInt("record_id"));
                    r.setStudentId(rs.getInt("student_id"));
                    r.setSemester(rs.getInt("semester"));
                    r.setSgpa(rs.getDouble("sgpa"));
                    records.add(r);
                }
            }
        }
        return records;
    }
}
