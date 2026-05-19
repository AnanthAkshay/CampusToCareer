package com.rit.placement.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rit.placement.factory.DAOFactory;

import com.rit.placement.dao.ApplicationDAO;
import com.rit.placement.dao.StudentDAO;
import com.rit.placement.model.Application;
import com.rit.placement.model.JobPosting;
import com.rit.placement.model.Student;
import com.rit.placement.util.CGPACalculator;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Candidate Ranking Service
 * 
 * Ranks job applicants based on:
 * 1. CGPA match (40 points)
 * 2. Skills match (40 points)
 * 3. Application activity (20 points)
 * 
 * Total Score: 0-100
 */
public class CandidateRankingService {
    private static final Logger logger = LoggerFactory.getLogger(CandidateRankingService.class);
    
    private final StudentDAO studentDAO = DAOFactory.getInstance().getStudentDAO();
    private final ApplicationDAO applicationDAO = DAOFactory.getInstance().getApplicationDAO();
    
    /**
     * Rank candidates for a specific job
     * Returns applications sorted by rank (best candidates first)
     */
    public List<RankedCandidate> rankCandidates(List<Application> applications, JobPosting job) {
        List<RankedCandidate> rankedCandidates = new ArrayList<>();
        
        for (Application app : applications) {
            try {
                // Get student details
                Student student = studentDAO.getStudentById(app.getStudentId());
                if (student == null) continue;
                
                // Calculate CGPA
                double cgpa = CGPACalculator.calculateCGPA(app.getStudentId());
                
                // Get total applications count
                int totalApplications = applicationDAO.getApplicationCountByStudent(app.getStudentId());
                
                // Calculate score
                int score = calculateCandidateScore(job, student, cgpa, totalApplications);
                
                // Create ranked candidate
                RankedCandidate ranked = new RankedCandidate();
                ranked.setApplication(app);
                ranked.setStudent(student);
                ranked.setCgpa(cgpa);
                ranked.setScore(score);
                ranked.setTotalApplications(totalApplications);
                
                rankedCandidates.add(ranked);
                
            } catch (Exception e) {
                logger.error("Error ranking candidate " + app.getStudentId() + ": " + e.getMessage());
            }
        }
        
        // Sort by score (descending) and assign ranks
        rankedCandidates.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));
        
        for (int i = 0; i < rankedCandidates.size(); i++) {
            rankedCandidates.get(i).setRank(i + 1);
        }
        
        return rankedCandidates;
    }
    
    /**
     * Calculate candidate score (0-100)
     * FIXED FORMULA: CGPA (40) + Skills (40) + Activity (20)
     */
    private int calculateCandidateScore(JobPosting job, Student student, 
                                       double cgpa, int totalApplications) {
        int score = 0;
        
        // 1. CGPA Score (40 points) - Normalized
        score += calculateCGPAScore(cgpa);
        
        // 2. Skills Match Score (40 points)
        score += calculateSkillsScore(job, student);
        
        // 3. Application Activity Score (20 points)
        score += calculateActivityScore(totalApplications);
        
        return Math.min(100, score);
    }
    
    /**
     * CGPA Score (0-40 points)
     * FIXED: Normalized scoring - (cgpa / 10) * 40
     */
    private int calculateCGPAScore(double cgpa) {
        // Normalize CGPA to 0-40 scale
        int score = (int) Math.round((cgpa / 10.0) * 40);
        return Math.min(40, Math.max(0, score));
    }
    
    /**
     * Skills Match Score (0-40 points)
     * FIXED: Percentage of required skills that candidate possesses
     */
    private int calculateSkillsScore(JobPosting job, Student student) {
        String jobSkills = job.getRequiredSkills();
        String studentSkills = student.getSkills();
        
        // If no skills specified, return default score
        if (jobSkills == null || jobSkills.trim().isEmpty()) {
            return 20; // Default score
        }
        
        if (studentSkills == null || studentSkills.trim().isEmpty()) {
            return 0; // Student has no skills listed
        }
        
        // Tokenize and normalize
        Set<String> jobSkillSet = tokenizeSkills(jobSkills);
        Set<String> studentSkillSet = tokenizeSkills(studentSkills);
        
        if (jobSkillSet.isEmpty()) {
            return 20;
        }
        
        // Calculate match percentage
        int matchCount = 0;
        for (String jobSkill : jobSkillSet) {
            if (studentSkillSet.contains(jobSkill)) {
                matchCount++;
            }
        }
        
        // FIXED: Score = (matching skills / total job skills) * 40
        double matchPercentage = (double) matchCount / jobSkillSet.size();
        int score = (int) Math.round(matchPercentage * 40);
        
        return score;
    }
    
    /**
     * Application Activity Score (0-20 points)
     * FIXED: Consistent scoring based on application count
     * >= 10 applications: 20 points
     * 5-9 applications: 15 points
     * 1-4 applications: 10 points
     * 0 applications: 0 points
     */
    private int calculateActivityScore(int totalApplications) {
        if (totalApplications >= 10) {
            return 20;
        } else if (totalApplications >= 5) {
            return 15;
        } else if (totalApplications >= 1) {
            return 10;
        } else {
            return 0;
        }
    }
    
    /**
     * Tokenize skills string into normalized set
     */
    private Set<String> tokenizeSkills(String skills) {
        if (skills == null || skills.trim().isEmpty()) {
            return new HashSet<>();
        }
        
        // Split by common delimiters and normalize
        String[] tokens = skills.toLowerCase()
            .replaceAll("[^a-z0-9+#\\s,]", " ")
            .split("[,\\s]+");
        
        Set<String> skillSet = new HashSet<>();
        for (String token : tokens) {
            String normalized = token.trim();
            if (!normalized.isEmpty() && normalized.length() > 1) {
                skillSet.add(normalized);
            }
        }
        
        return skillSet;
    }
    
    /**
     * Inner class to hold ranked candidate data
     */
    public static class RankedCandidate {
        private Application application;
        private Student student;
        private double cgpa;
        private int score;
        private int rank;
        private int totalApplications;
        
        public Application getApplication() { return application; }
        public void setApplication(Application application) { this.application = application; }
        
        public Student getStudent() { return student; }
        public void setStudent(Student student) { this.student = student; }
        
        public double getCgpa() { return cgpa; }
        public void setCgpa(double cgpa) { this.cgpa = cgpa; }
        
        public int getScore() { return score; }
        public void setScore(int score) { this.score = score; }
        
        public int getRank() { return rank; }
        public void setRank(int rank) { this.rank = rank; }
        
        public int getTotalApplications() { return totalApplications; }
        public void setTotalApplications(int totalApplications) { this.totalApplications = totalApplications; }
    }
}
