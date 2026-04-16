package com.rit.placement.util;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility to calculate CGPA for a student.
 * CGPA = average of all SGPA entries in academic_records.
 *
 * This class provides two overloaded variants of calculateCGPA():
 *  1. calculateCGPA(int studentId)
 *     — Opens its own connection. Suitable for one-off lookups.
 *  2. calculateCGPA(int studentId, Connection conn)
 *     — Reuses a caller-provided connection. Use this inside transactions
 *       or Servlet request handlers to avoid the overhead of a new connection
 *       per call.
 */
public class CGPACalculator {

    /**
     * Calculates CGPA by averaging all SGPA values for the given student.
     * Opens and closes its own JDBC connection.
     *
     * @param studentId the student's ID (PK in students table)
     * @return CGPA rounded to 2 decimal places, or -1.0 if no records found
     */
    public static double calculateCGPA(int studentId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return calculateCGPA(studentId, conn);
        }
    }

    /**
     * Calculates CGPA using a caller-supplied connection.
     * Use this overload inside Servlets or transactions where a connection
     * is already open, to avoid the cost of creating a new connection.
     *
     * @param studentId the student's ID (PK in students table)
     * @param conn      an open JDBC connection (caller manages lifecycle)
     * @return CGPA rounded to 2 decimal places, or -1.0 if no records found
     */
    public static double calculateCGPA(int studentId, Connection conn) throws SQLException {
        String sql = "SELECT sgpa FROM academic_records WHERE student_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();

            List<Double> sgpaList = new ArrayList<>();
            while (rs.next()) {
                sgpaList.add(rs.getDouble("sgpa"));
            }

            if (sgpaList.isEmpty()) return -1.0;

            double sum = 0;
            for (double s : sgpaList) sum += s;

            // Round to 2 decimal places
            double cgpa = sum / sgpaList.size();
            return Math.round(cgpa * 100.0) / 100.0;
        }
    }
}
