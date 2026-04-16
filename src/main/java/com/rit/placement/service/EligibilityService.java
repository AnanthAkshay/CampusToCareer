package com.rit.placement.service;

import com.rit.placement.dao.JobPostingDAO;
import com.rit.placement.dao.StudentDAO;
import com.rit.placement.model.JobPosting;
import com.rit.placement.model.Student;
import com.rit.placement.util.CGPACalculator;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Eligibility Service - Core logic for determining student eligibility for job postings.
 * 
 * Eligibility Criteria:
 * 1. CGPA >= job.min_cgpa
 * 2. student.branch in job.allowed_branches
 * 3. student.skills overlap with job.required_skills (basic match)
 */
public class EligibilityService {

    private final StudentDAO studentDAO = new StudentDAO();
    private final JobPostingDAO jobPostingDAO = new JobPostingDAO();

    /**
     * Check if a student is eligible for a specific job posting.
     * 
     * @param studentId the student's ID
     * @param jobId the job posting ID
     * @return true if eligible, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean isEligible(int studentId, int jobId) throws SQLException {
        // 1. Fetch student data
        Student student = studentDAO.getStudentById(studentId);
        if (student == null) {
            return false;
        }

        // 2. Fetch job posting data
        JobPosting job = jobPostingDAO.getJobPostingById(jobId);
        if (job == null) {
            return false;
        }

        // 3. Compute student CGPA
        double cgpa = CGPACalculator.calculateCGPA(studentId);
        if (cgpa < 0) {
            // No academic records found
            return false;
        }

        // 4. Check CGPA eligibility
        if (!checkCGPAEligibility(cgpa, job)) {
            return false;
        }

        // 5. Check branch eligibility
        if (!checkBranchEligibility(student.getBranch(), job)) {
            return false;
        }

        // 6. Check skills overlap (optional - if job has required skills)
        if (!checkSkillsEligibility(student.getSkills(), job)) {
            return false;
        }

        return true;
    }

    /**
     * Check if student's CGPA meets the minimum requirement.
     */
    private boolean checkCGPAEligibility(double studentCgpa, JobPosting job) {
        if (job.getMinCgpa() == null) {
            return true; // No CGPA requirement
        }
        return studentCgpa >= job.getMinCgpa().doubleValue();
    }

    /**
     * Check if student's branch is in the allowed branches list.
     */
    private boolean checkBranchEligibility(String studentBranch, JobPosting job) {
        String allowedBranches = job.getAllowedBranches();
        
        // If no branch restriction, all branches are eligible
        if (allowedBranches == null || allowedBranches.trim().isEmpty()) {
            return true;
        }

        // Normalize and check if student's branch is in the allowed list
        String normalizedStudentBranch = studentBranch.trim().toUpperCase();
        String normalizedAllowedBranches = allowedBranches.toUpperCase();
        
        // Split by comma and check each branch
        String[] branches = normalizedAllowedBranches.split(",");
        for (String branch : branches) {
            if (branch.trim().equals(normalizedStudentBranch)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Check if student's skills overlap with required skills.
     * Basic matching: at least one skill must match.
     */
    private boolean checkSkillsEligibility(String studentSkills, JobPosting job) {
        String requiredSkills = job.getRequiredSkills();
        
        // If no skills required, student is eligible
        if (requiredSkills == null || requiredSkills.trim().isEmpty()) {
            return true;
        }

        // If student has no skills but job requires skills, not eligible
        if (studentSkills == null || studentSkills.trim().isEmpty()) {
            return false;
        }

        // Normalize and create sets for comparison
        Set<String> studentSkillSet = normalizeSkills(studentSkills);
        Set<String> requiredSkillSet = normalizeSkills(requiredSkills);

        // Check if there's any overlap (at least one matching skill)
        for (String skill : studentSkillSet) {
            if (requiredSkillSet.contains(skill)) {
                return true; // At least one skill matches
            }
        }

        return false; // No matching skills
    }

    /**
     * Normalize skills string into a set of lowercase, trimmed skills.
     */
    private Set<String> normalizeSkills(String skills) {
        Set<String> skillSet = new HashSet<>();
        if (skills != null && !skills.trim().isEmpty()) {
            String[] skillArray = skills.split(",");
            for (String skill : skillArray) {
                String normalized = skill.trim().toLowerCase();
                if (!normalized.isEmpty()) {
                    skillSet.add(normalized);
                }
            }
        }
        return skillSet;
    }

    /**
     * Get eligibility details for debugging/display purposes.
     * Returns a detailed breakdown of eligibility checks.
     */
    public EligibilityResult getEligibilityDetails(int studentId, int jobId) throws SQLException {
        EligibilityResult result = new EligibilityResult();
        result.setStudentId(studentId);
        result.setJobId(jobId);

        // Fetch data
        Student student = studentDAO.getStudentById(studentId);
        JobPosting job = jobPostingDAO.getJobPostingById(jobId);

        if (student == null || job == null) {
            result.setEligible(false);
            result.setReason("Student or job not found");
            return result;
        }

        // Compute CGPA
        double cgpa = CGPACalculator.calculateCGPA(studentId);
        result.setCgpa(cgpa);

        if (cgpa < 0) {
            result.setEligible(false);
            result.setReason("No academic records found");
            return result;
        }

        // Check each criterion
        boolean cgpaEligible = checkCGPAEligibility(cgpa, job);
        boolean branchEligible = checkBranchEligibility(student.getBranch(), job);
        boolean skillsEligible = checkSkillsEligibility(student.getSkills(), job);

        result.setCgpaEligible(cgpaEligible);
        result.setBranchEligible(branchEligible);
        result.setSkillsEligible(skillsEligible);

        boolean overallEligible = cgpaEligible && branchEligible && skillsEligible;
        result.setEligible(overallEligible);

        if (!overallEligible) {
            if (!cgpaEligible) {
                result.setReason("CGPA below minimum requirement");
            } else if (!branchEligible) {
                result.setReason("Branch not allowed");
            } else if (!skillsEligible) {
                result.setReason("Required skills not met");
            }
        }

        return result;
    }

    /**
     * Inner class to hold detailed eligibility results.
     */
    public static class EligibilityResult {
        private int studentId;
        private int jobId;
        private double cgpa;
        private boolean eligible;
        private boolean cgpaEligible;
        private boolean branchEligible;
        private boolean skillsEligible;
        private String reason;

        // Getters and Setters
        public int getStudentId() { return studentId; }
        public void setStudentId(int studentId) { this.studentId = studentId; }

        public int getJobId() { return jobId; }
        public void setJobId(int jobId) { this.jobId = jobId; }

        public double getCgpa() { return cgpa; }
        public void setCgpa(double cgpa) { this.cgpa = cgpa; }

        public boolean isEligible() { return eligible; }
        public void setEligible(boolean eligible) { this.eligible = eligible; }

        public boolean isCgpaEligible() { return cgpaEligible; }
        public void setCgpaEligible(boolean cgpaEligible) { this.cgpaEligible = cgpaEligible; }

        public boolean isBranchEligible() { return branchEligible; }
        public void setBranchEligible(boolean branchEligible) { this.branchEligible = branchEligible; }

        public boolean isSkillsEligible() { return skillsEligible; }
        public void setSkillsEligible(boolean skillsEligible) { this.skillsEligible = skillsEligible; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}
