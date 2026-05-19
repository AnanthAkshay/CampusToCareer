package com.rit.placement;

import com.rit.placement.util.DBConnection;
import java.sql.*;

public class MigrateSkills {
    public static void main(String[] args) {
        System.out.println("Starting skills migration...");
        try (Connection conn = DBConnection.getConnection()) {
            
            // 1. Create tables
            System.out.println("Creating tables...");
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS skills (" +
                             "skill_id INT AUTO_INCREMENT PRIMARY KEY, " +
                             "skill_name VARCHAR(100) UNIQUE NOT NULL" +
                             ") ENGINE=InnoDB;");
                stmt.execute("CREATE TABLE IF NOT EXISTS student_skills (" +
                             "student_id INT NOT NULL, " +
                             "skill_id INT NOT NULL, " +
                             "PRIMARY KEY (student_id, skill_id), " +
                             "FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE, " +
                             "FOREIGN KEY (skill_id) REFERENCES skills(skill_id) ON DELETE CASCADE" +
                             ") ENGINE=InnoDB;");
            }

            // 2. Migrate data
            System.out.println("Migrating data...");
            String selectSql = "SELECT student_id, skills FROM students WHERE skills IS NOT NULL AND skills != ''";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(selectSql)) {
                
                String insertSkillSql = "INSERT IGNORE INTO skills (skill_name) VALUES (?)";
                String selectSkillSql = "SELECT skill_id FROM skills WHERE skill_name = ?";
                String insertStudentSkillSql = "INSERT IGNORE INTO student_skills (student_id, skill_id) VALUES (?, ?)";
                
                try (PreparedStatement psInsertSkill = conn.prepareStatement(insertSkillSql);
                     PreparedStatement psSelectSkill = conn.prepareStatement(selectSkillSql);
                     PreparedStatement psInsertStudentSkill = conn.prepareStatement(insertStudentSkillSql)) {
                    
                    while (rs.next()) {
                        int studentId = rs.getInt("student_id");
                        String skills = rs.getString("skills");
                        
                        String[] skillArray = skills.split(",");
                        for (String skill : skillArray) {
                            skill = skill.trim().toLowerCase();
                            if (skill.isEmpty()) continue;
                            
                            // Insert skill
                            psInsertSkill.setString(1, skill);
                            psInsertSkill.executeUpdate();
                            
                            // Get skill id
                            psSelectSkill.setString(1, skill);
                            ResultSet rsSkill = psSelectSkill.executeQuery();
                            if (rsSkill.next()) {
                                int skillId = rsSkill.getInt(1);
                                
                                // Map to student
                                psInsertStudentSkill.setInt(1, studentId);
                                psInsertStudentSkill.setInt(2, skillId);
                                psInsertStudentSkill.executeUpdate();
                            }
                            rsSkill.close();
                        }
                    }
                }
            }
            
            // 3. Drop column from students? Or leave it for now just in case?
            // "The skills column in students is a comma-delimited string... Update StudentDAO.java to join and fetch."
            // Better drop it to ensure we aren't using it anymore!
            System.out.println("Dropping skills column from students...");
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE students DROP COLUMN skills");
            }
            
            System.out.println("Migration complete!");
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBConnection.closePool();
        }
    }
}
