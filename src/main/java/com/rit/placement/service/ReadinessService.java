package com.rit.placement.service;

import com.rit.placement.model.Student;
import java.util.ArrayList;
import java.util.List;

/**
 * Service to calculate Placement Readiness Score and provide recommendations.
 * 
 * Scoring Logic (Total: 100 points):
 * - CGPA: 0-50 points
 * - Applications: 0-30 points
 * - Skills: 0-20 points
 */
public class ReadinessService {
    
    /**
     * Calculate placement readiness score for a student.
     * 
     * @param student Student object with CGPA and skills
     * @param applicationsCount Number of applications submitted
     * @return Total readiness score (0-100)
     */
    public int calculateScore(Student student, int applicationsCount) {
        int cgpaScore = calculateCgpaScore(student.getCgpa());
        int applicationScore = calculateApplicationScore(applicationsCount);
        int skillsScore = calculateSkillsScore(student.getSkills());
        
        return cgpaScore + applicationScore + skillsScore;
    }
    
    /**
     * Calculate CGPA component score (0-50 points).
     * 
     * Rules:
     * - CGPA >= 9.0: 50 points
     * - CGPA 8.0-8.99: 40 points
     * - CGPA 7.0-7.99: 30 points
     * - CGPA < 7.0: 15 points
     */
    private int calculateCgpaScore(double cgpa) {
        if (cgpa >= 9.0) {
            return 50;
        } else if (cgpa >= 8.0) {
            return 40;
        } else if (cgpa >= 7.0) {
            return 30;
        } else {
            return 15;
        }
    }
    
    /**
     * Calculate applications component score (0-30 points).
     * 
     * Rules:
     * - Applications >= 10: 30 points
     * - Applications 5-9: 20 points
     * - Applications 1-4: 10 points
     * - Applications = 0: 0 points
     */
    private int calculateApplicationScore(int applicationsCount) {
        if (applicationsCount >= 10) {
            return 30;
        } else if (applicationsCount >= 5) {
            return 20;
        } else if (applicationsCount >= 1) {
            return 10;
        } else {
            return 0;
        }
    }
    
    /**
     * Calculate skills component score (0-20 points).
     * 
     * Rules:
     * - Skills present (non-empty): 20 points
     * - Skills empty: 0 points
     */
    private int calculateSkillsScore(String skills) {
        if (skills != null && !skills.trim().isEmpty()) {
            return 20;
        } else {
            return 0;
        }
    }
    
    /**
     * Generate personalized recommendations based on student profile.
     * 
     * @param student Student object
     * @param applicationsCount Number of applications
     * @return List of actionable recommendations
     */
    public List<String> getRecommendations(Student student, int applicationsCount) {
        List<String> recommendations = new ArrayList<>();
        
        // Check CGPA
        if (student.getCgpa() < 7.0) {
            recommendations.add("📚 Focus on improving your CGPA - aim for 7.0+ to increase placement opportunities");
        } else if (student.getCgpa() < 8.0) {
            recommendations.add("📈 Good progress! Try to push your CGPA above 8.0 for premium companies");
        }
        
        // Check applications
        if (applicationsCount == 0) {
            recommendations.add("💼 Start applying to jobs - browse available positions and submit applications");
        } else if (applicationsCount < 5) {
            recommendations.add("🎯 Apply to more companies - aim for at least 10 applications to maximize chances");
        } else if (applicationsCount < 10) {
            recommendations.add("✨ You're on track! Consider applying to a few more companies");
        }
        
        // Check skills
        String skills = student.getSkills();
        if (skills == null || skills.trim().isEmpty()) {
            recommendations.add("🛠️ Add your technical skills and projects to your profile - this is crucial for recruiters");
        }
        
        // Check projects
        String projects = student.getProjects();
        if (projects == null || projects.trim().isEmpty()) {
            recommendations.add("💡 Add projects to showcase your practical experience and problem-solving abilities");
        }
        
        // Check experience
        String experience = student.getExperience();
        if (experience == null || experience.trim().isEmpty()) {
            recommendations.add("🏢 Add any internships or work experience to strengthen your profile");
        }
        
        // Positive reinforcement if doing well
        if (student.getCgpa() >= 8.0 && applicationsCount >= 10 && 
            skills != null && !skills.trim().isEmpty()) {
            recommendations.add("🎉 Excellent! You're well-prepared for placements. Keep up the great work!");
        }
        
        return recommendations;
    }
    
    /**
     * Get readiness level description based on score.
     * 
     * @param score Readiness score (0-100)
     * @return Level description
     */
    public String getReadinessLevel(int score) {
        if (score >= 80) {
            return "Excellent";
        } else if (score >= 60) {
            return "Good";
        } else if (score >= 40) {
            return "Fair";
        } else {
            return "Needs Improvement";
        }
    }
    
    /**
     * Get color code for readiness level (for UI).
     * 
     * @param score Readiness score (0-100)
     * @return Color code (green, blue, orange, red)
     */
    public String getReadinessColor(int score) {
        if (score >= 80) {
            return "green";
        } else if (score >= 60) {
            return "blue";
        } else if (score >= 40) {
            return "orange";
        } else {
            return "red";
        }
    }
}
