package com.rit.placement.service;

import com.rit.placement.dao.ApplicationDAO;
import com.rit.placement.dao.JobPostingDAO;
import com.rit.placement.dao.StudentDAO;
import com.rit.placement.model.JobPosting;
import com.rit.placement.model.Student;
import com.rit.placement.util.CGPACalculator;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Job Recommendation Service
 * 
 * Recommends jobs to students based on:
 * 1. CGPA eligibility (40 points)
 * 2. Skills match (40 points)
 * 3. Branch match (10 points)
 * 4. Application history (10 points)
 * 
 * Score: 0-100
 */
public class JobRecommendationService {
    
    private final JobPostingDAO jobPostingDAO = new JobPostingDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final ApplicationDAO applicationDAO = new ApplicationDAO();
    
    /**
     * Get recommended jobs for a student
     * Returns top N jobs sorted by recommendation score
     */
    public List<RecommendedJob> getRecommendedJobs(int studentId, int limit) throws SQLException {
        // 1. Get student profile
        Student student = studentDAO.getStudentById(studentId);
        if (student == null) {
            return new ArrayList<>();
        }
        
        // 2. Calculate student's CGPA
        double cgpa = CGPACalculator.calculateCGPA(studentId);
        
        // 3. Get all eligible job postings
        List<JobPosting> allJobs = jobPostingDAO.getEligibleJobPostings(cgpa, student.getBranch());
        
        // 4. Get student's application history
        Set<Integer> appliedJobIds = getAppliedJobIds(studentId);
        
        // 5. Score each job
        List<RecommendedJob> recommendations = new ArrayList<>();
        for (JobPosting job : allJobs) {
            // Skip jobs already applied to
            if (appliedJobIds.contains(job.getJobId())) {
                continue;
            }
            
            int score = calculateRecommendationScore(job, student, cgpa, appliedJobIds);
            
            RecommendedJob recommendation = new RecommendedJob();
            recommendation.setJob(job);
            recommendation.setScore(score);
            recommendation.setReasons(generateReasons(job, student, cgpa));
            
            recommendations.add(recommendation);
        }
        
        // 6. Sort by score (descending) and limit
        return recommendations.stream()
            .sorted((a, b) -> Integer.compare(b.getScore(), a.getScore()))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    /**
     * Calculate recommendation score (0-100)
     * FIXED FORMULA: CGPA (40) + Skills (40) + Eligibility (20)
     */
    private int calculateRecommendationScore(JobPosting job, Student student, 
                                            double cgpa, Set<Integer> appliedJobIds) {
        int score = 0;
        
        // 1. CGPA Match (40 points) - Binary: meets requirement or not
        score += calculateCGPAScore(job, cgpa);
        
        // 2. Skills Match (40 points) - Percentage based
        score += calculateSkillsScore(job, student);
        
        // 3. Eligibility Score (20 points) - Binary: eligible or not
        score += calculateEligibilityScore(job, student, cgpa);
        
        return Math.min(100, score);
    }
    
    /**
     * CGPA Score (0-40 points)
     * FIXED: Binary scoring - meets requirement = 40, doesn't meet = 0
     */
    private int calculateCGPAScore(JobPosting job, double cgpa) {
        if (job.getMinCgpa() == null) {
            return 40; // No CGPA requirement = full points
        }
        
        double minCgpa = job.getMinCgpa().doubleValue();
        
        // Binary scoring: meets requirement or not
        if (cgpa >= minCgpa) {
            return 40; // Meets requirement
        } else {
            return 0; // Below requirement
        }
    }
    
    /**
     * Eligibility Score (0-20 points)
     * Checks branch eligibility
     */
    private int calculateEligibilityScore(JobPosting job, Student student, double cgpa) {
        // Check CGPA eligibility
        if (job.getMinCgpa() != null && cgpa < job.getMinCgpa().doubleValue()) {
            return 0;
        }
        
        // Check branch eligibility
        String allowedBranches = job.getAllowedBranches();
        String studentBranch = student.getBranch();
        
        if (allowedBranches == null || allowedBranches.trim().isEmpty()) {
            return 20; // All branches allowed
        }
        
        if (studentBranch == null) {
            return 0; // Unknown branch
        }
        
        // Check if student's branch is in allowed list
        String normalizedBranches = allowedBranches.toLowerCase();
        String normalizedStudentBranch = studentBranch.toLowerCase();
        
        if (normalizedBranches.contains(normalizedStudentBranch)) {
            return 20; // Eligible
        }
        
        return 0; // Not eligible
    }
    
    /**
     * Skills Match Score (0-40 points)
     * FIXED: Proper percentage calculation based on matching skills
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
        
        // Normalize and tokenize
        Set<String> jobSkillSet = tokenizeSkills(jobSkills);
        Set<String> studentSkillSet = tokenizeSkills(studentSkills);
        
        // Calculate match percentage
        int matchCount = 0;
        for (String jobSkill : jobSkillSet) {
            if (studentSkillSet.contains(jobSkill)) {
                matchCount++;
            }
        }
        
        if (jobSkillSet.isEmpty()) {
            return 20;
        }
        
        // FIXED: Score = (matching skills / total job skills) * 40
        double matchPercentage = (double) matchCount / jobSkillSet.size();
        int score = (int) Math.round(matchPercentage * 40);
        
        return score;
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
            .replaceAll("[^a-z0-9+#\\s,]", " ") // Keep alphanumeric, +, #
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
     * Generate human-readable reasons for recommendation
     */
    private List<String> generateReasons(JobPosting job, Student student, double cgpa) {
        List<String> reasons = new ArrayList<>();
        
        // CGPA reason
        if (job.getMinCgpa() != null) {
            double minCgpa = job.getMinCgpa().doubleValue();
            double difference = cgpa - minCgpa;
            
            if (difference >= 1.0) {
                reasons.add("Your CGPA (" + String.format("%.2f", cgpa) + 
                           ") exceeds the requirement (" + String.format("%.2f", minCgpa) + ")");
            } else if (difference >= 0) {
                reasons.add("You meet the CGPA requirement (" + String.format("%.2f", minCgpa) + ")");
            }
        }
        
        // Skills reason
        String jobSkills = job.getRequiredSkills();
        String studentSkills = student.getSkills();
        
        if (jobSkills != null && !jobSkills.trim().isEmpty() && 
            studentSkills != null && !studentSkills.trim().isEmpty()) {
            
            Set<String> jobSkillSet = tokenizeSkills(jobSkills);
            Set<String> studentSkillSet = tokenizeSkills(studentSkills);
            
            Set<String> matchingSkills = new HashSet<>(jobSkillSet);
            matchingSkills.retainAll(studentSkillSet);
            
            if (!matchingSkills.isEmpty()) {
                if (matchingSkills.size() == 1) {
                    reasons.add("Matching skill: " + matchingSkills.iterator().next());
                } else if (matchingSkills.size() <= 3) {
                    reasons.add("Matching skills: " + String.join(", ", matchingSkills));
                } else {
                    reasons.add(matchingSkills.size() + " matching skills found");
                }
            }
        }
        
        // Branch reason
        String allowedBranches = job.getAllowedBranches();
        if (allowedBranches != null && !allowedBranches.trim().isEmpty()) {
            if (allowedBranches.toLowerCase().contains(student.getBranch().toLowerCase())) {
                reasons.add("Open to " + student.getBranch() + " students");
            }
        } else {
            reasons.add("Open to all branches");
        }
        
        // Package reason
        if (job.getPackageAmount() != null) {
            reasons.add("Package: ₹" + job.getPackageAmount() + " LPA");
        }
        
        return reasons;
    }
    
    /**
     * Get set of job IDs student has already applied to
     */
    private Set<Integer> getAppliedJobIds(int studentId) {
        try {
            return applicationDAO.getApplicationsByStudent(studentId).stream()
                .map(app -> app.getJobId())
                .collect(Collectors.toSet());
        } catch (Exception e) {
            System.err.println("Error fetching applied jobs: " + e.getMessage());
            return new HashSet<>();
        }
    }
    
    /**
     * Inner class to hold recommendation with score
     */
    public static class RecommendedJob {
        private JobPosting job;
        private int score;
        private List<String> reasons;
        
        public JobPosting getJob() { return job; }
        public void setJob(JobPosting job) { this.job = job; }
        
        public int getScore() { return score; }
        public void setScore(int score) { this.score = score; }
        
        public List<String> getReasons() { return reasons; }
        public void setReasons(List<String> reasons) { this.reasons = reasons; }
        
        public String getScoreLabel() {
            if (score >= 80) return "Excellent Match";
            if (score >= 60) return "Good Match";
            if (score >= 40) return "Fair Match";
            return "Consider";
        }
        
        public String getScoreColor() {
            if (score >= 80) return "#10b981"; // Green
            if (score >= 60) return "#3b82f6"; // Blue
            if (score >= 40) return "#f59e0b"; // Orange
            return "#6b7280"; // Gray
        }
    }
}
