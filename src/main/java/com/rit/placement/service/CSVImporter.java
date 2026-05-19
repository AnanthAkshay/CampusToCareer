package com.rit.placement.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rit.placement.util.DBConnection;
import com.rit.placement.util.PasswordUtil;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import java.io.*;
import java.sql.*;
import java.util.Map;

/**
 * Reads a student CSV file and bulk-imports into users, students,
 * and academic_records tables with full transaction handling.
 *
 * Actual CSV format (with header row, 0-indexed columns):
 *   Index 0: usn
 *   Index 1: name
 *   Index 2: sem3_sgpa        ← parsed as Semester 3 SGPA
 *   Index 3: total_cgpa       ← intentionally skipped (computed, not trusted)
 *   Index 4: sem2_sgpa        ← parsed as Semester 2 SGPA
 *   Index 5+: additional columns safely ignored
 *
 * SECURITY NOTE:
 * Default password for each student is their USN. It is hashed with BCrypt
 * before storage so that plain-text passwords never exist in the database.
 * Students should change their password on first login (not yet implemented).
 */
public class CSVImporter {
    private static final Logger logger = LoggerFactory.getLogger(CSVImporter.class);

    private static final String H_USN = "usn";
    private static final String H_NAME = "name";
    private static final String H_SEM3_SGPA = "sem3_sgpa";
    private static final String H_TOTAL_CGPA = "total_cgpa";
    private static final String H_SEM2_SGPA = "sem2_sgpa";

    /**
     * Imports students from the given CSV file path.
     * Each student produces: 1 users row, 1 students row, 2 academic_records rows.
     * Duplicate USNs are skipped. The entire file is one transaction.
     */
    public static void importCSV(String filePath) {
        String insertUser   = "INSERT INTO users (usn, name, password_hash, role, is_active) VALUES (?, ?, ?, 'STUDENT', TRUE)";
        String insertStudent = "INSERT INTO students (student_id, branch, current_sem) VALUES (?, 'ISE', 3)";
        String insertAcademic = "INSERT INTO academic_records (student_id, semester, sgpa) VALUES (?, ?, ?)";
        String checkUSN = "SELECT user_id FROM users WHERE usn = ?";

        try (Connection conn = DBConnection.getConnection();
             Reader reader = new BufferedReader(new FileReader(filePath));
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader(H_USN, H_NAME, H_SEM3_SGPA, H_TOTAL_CGPA, H_SEM2_SGPA)
                     .setSkipHeaderRecord(true)
                     .setIgnoreHeaderCase(true)
                     .setIgnoreEmptyLines(true)
                     .setTrim(true)
                     .build()
                     .parse(reader)) {

            conn.setAutoCommit(false); // start transaction
            int imported = 0, skipped = 0;
            int rowIndex = 0;

            for (CSVRecord record : parser) {
                rowIndex++;
                Savepoint sp = conn.setSavepoint("row_" + rowIndex);

                try {
                    String usn = getRequired(record, "usn");
                    String name = getRequired(record, "name");

                    double sem3Sgpa = parseDouble(record, "sem3_sgpa");
                    double sem2Sgpa = parseDouble(record, "sem2_sgpa");

                    if (sem2Sgpa < 0 || sem3Sgpa < 0) {
                        conn.rollback(sp);
                        skipped++;
                        continue;
                    }

                    try (PreparedStatement check = conn.prepareStatement(checkUSN)) {
                        check.setString(1, usn);
                        if (check.executeQuery().next()) {
                            conn.rollback(sp);
                            skipped++;
                            continue;
                        }
                    }

                    int userId;
                    String storedPasswordHash = PasswordUtil.hashPassword(usn);
                    try (PreparedStatement ps = conn.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS)) {
                        ps.setString(1, usn);
                        ps.setString(2, name);
                        ps.setString(3, storedPasswordHash);
                        ps.executeUpdate();
                        try (ResultSet keys = ps.getGeneratedKeys()) {
                            if (!keys.next()) {
                                throw new SQLException("Failed to retrieve generated user_id.");
                            }
                            userId = keys.getInt(1);
                        }
                    }

                    try (PreparedStatement ps = conn.prepareStatement(insertStudent)) {
                        ps.setInt(1, userId);
                        ps.executeUpdate();
                    }

                    try (PreparedStatement ps = conn.prepareStatement(insertAcademic)) {
                        ps.setInt(1, userId);
                        ps.setInt(2, 2);
                        ps.setDouble(3, sem2Sgpa);
                        ps.executeUpdate();

                        ps.setInt(1, userId);
                        ps.setInt(2, 3);
                        ps.setDouble(3, sem3Sgpa);
                        ps.executeUpdate();
                    }

                    imported++;

                } catch (Exception e) {
                    conn.rollback(sp);
                    skipped++;
                    logger.error("SKIP row " + rowIndex + ": " + e.getMessage());
                }
            }

            conn.commit(); // commit entire batch
            System.out.println("Import complete. Imported: " + imported + ", Skipped: " + skipped);

        } catch (Exception e) {
            logger.error("Import failed: " + e.getMessage());
            logger.error("Exception occurred: ", e);
            // Connection is auto-closed; uncommitted work is rolled back
        }
    }

    private static String getRequired(CSVRecord record, String header) {
        String value = getOptional(record, header);
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Missing required field: " + header);
        }
        return value;
    }

    private static String getOptional(CSVRecord record, String header) {
        if (record.isMapped(header)) {
            String v = record.get(header);
            return v == null ? null : v.trim();
        }

        Map<String, String> map = record.toMap();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getKey() != null && entry.getKey().trim().equalsIgnoreCase(header)) {
                return entry.getValue() == null ? null : entry.getValue().trim();
            }
        }
        return null;
    }

    private static double parseDouble(CSVRecord record, String header) {
        String raw = getOptional(record, header);
        if (raw == null || raw.isEmpty()) {
            throw new NumberFormatException("Missing numeric field: " + header);
        }
        return Double.parseDouble(raw);
    }

    /** Standalone entry point: pass CSV path as first argument. */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java CSVImporter <csv-file-path>");
            return;
        }
        importCSV(args[0]);
    }
}
