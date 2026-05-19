package com.rit.placement;

import com.rit.placement.util.DBConnection;
import java.sql.*;

public class MigrateJobs {
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection()) {
            
            // 1. Create tables
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS branches (" +
                             "branch_id INT AUTO_INCREMENT PRIMARY KEY, " +
                             "branch_name VARCHAR(50) UNIQUE NOT NULL" +
                             ") ENGINE=InnoDB");
                             
                stmt.execute("CREATE TABLE IF NOT EXISTS job_branches (" +
                             "job_id INT NOT NULL, " +
                             "branch_id INT NOT NULL, " +
                             "PRIMARY KEY (job_id, branch_id), " +
                             "FOREIGN KEY (job_id) REFERENCES job_postings(job_id) ON DELETE CASCADE, " +
                             "FOREIGN KEY (branch_id) REFERENCES branches(branch_id) ON DELETE CASCADE" +
                             ") ENGINE=InnoDB");
                             
                stmt.execute("CREATE TABLE IF NOT EXISTS job_skills (" +
                             "job_id INT NOT NULL, " +
                             "skill_id INT NOT NULL, " +
                             "PRIMARY KEY (job_id, skill_id), " +
                             "FOREIGN KEY (job_id) REFERENCES job_postings(job_id) ON DELETE CASCADE, " +
                             "FOREIGN KEY (skill_id) REFERENCES skills(skill_id) ON DELETE CASCADE" +
                             ") ENGINE=InnoDB");
            }

            // 2. Migrate job_postings
            String selectJobs = "SELECT job_id, allowed_branches, required_skills FROM job_postings";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(selectJobs)) {
                 
                while (rs.next()) {
                    int jobId = rs.getInt("job_id");
                    String branches = rs.getString("allowed_branches");
                    String skills = rs.getString("required_skills");
                    
                    if (branches != null && !branches.trim().isEmpty()) {
                        for (String b : branches.split(",")) {
                            b = b.trim().toUpperCase();
                            if (b.isEmpty()) continue;
                            int branchId = getOrCreateBranch(conn, b);
                            linkJobBranch(conn, jobId, branchId);
                        }
                    }
                    
                    if (skills != null && !skills.trim().isEmpty()) {
                        for (String s : skills.split(",")) {
                            s = s.trim().toLowerCase();
                            if (s.isEmpty()) continue;
                            int skillId = getOrCreateSkill(conn, s);
                            linkJobSkill(conn, jobId, skillId);
                        }
                    }
                }
            }
            
            System.out.println("Migration complete!");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBConnection.closePool();
        }
    }
    
    private static int getOrCreateBranch(Connection conn, String branchName) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT branch_id FROM branches WHERE branch_name = ?")) {
            ps.setString(1, branchName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        try (PreparedStatement ps = conn.prepareStatement("INSERT IGNORE INTO branches (branch_name) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, branchName);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        }
        try (PreparedStatement ps = conn.prepareStatement("SELECT branch_id FROM branches WHERE branch_name = ?")) {
            ps.setString(1, branchName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return -1;
    }
    
    private static int getOrCreateSkill(Connection conn, String skillName) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT skill_id FROM skills WHERE skill_name = ?")) {
            ps.setString(1, skillName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        try (PreparedStatement ps = conn.prepareStatement("INSERT IGNORE INTO skills (skill_name) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, skillName);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        }
        try (PreparedStatement ps = conn.prepareStatement("SELECT skill_id FROM skills WHERE skill_name = ?")) {
            ps.setString(1, skillName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return -1;
    }
    
    private static void linkJobBranch(Connection conn, int jobId, int branchId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("INSERT IGNORE INTO job_branches (job_id, branch_id) VALUES (?, ?)")) {
            ps.setInt(1, jobId);
            ps.setInt(2, branchId);
            ps.executeUpdate();
        }
    }
    
    private static void linkJobSkill(Connection conn, int jobId, int skillId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("INSERT IGNORE INTO job_skills (job_id, skill_id) VALUES (?, ?)")) {
            ps.setInt(1, jobId);
            ps.setInt(2, skillId);
            ps.executeUpdate();
        }
    }
}
