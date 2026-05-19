package com.rit.placement.service;

import com.rit.placement.factory.DAOFactory;

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

    private final StudentDAO studentDAO = DAOFactory.getInstance().getStudentDAO();
    private final JobPostingDAO jobPostingDAO = DAOFactory.getInstance().getJobPostingDAO();

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

        return isEligible(student, cgpa, job);
    }

    /**
     * Check if a student is eligible for a specific job posting (optimized memory version).
     * 
     * @param student the student object
     * @param cgpa the calculated CGPA
     * @param job the job posting
     * @return true if eligible, false otherwise
     */
    public boolean isEligible(Student student, double cgpa, JobPosting job) {
        if (student == null || job == null || cgpa < 0) {
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
        if (!checkSkillsEligibility(student.getStudentId(), job)) {
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
        if (allowedBranches == null || allowedBranches.trim().isEmpty()) {
            return true;
        }
        try {
            return jobPostingDAO.hasAllowedBranch(job.getJobId(), studentBranch);
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Check if student's skills overlap with required skills.
     */
    private boolean checkSkillsEligibility(int studentId, JobPosting job) {
        String requiredSkills = job.getRequiredSkills();
        if (requiredSkills == null || requiredSkills.trim().isEmpty()) {
            return true;
        }
        try {
            return jobPostingDAO.hasOverlappingSkills(job.getJobId(), studentId);
        } catch (SQLException e) {
            return false;
        }
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
        boolean skillsEligible = checkSkillsEligibility(student.getStudentId(), job);

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
