package com.rit.placement.model;

/**
 * Detailed eligibility results for a student applying to a job posting.
 */
public class EligibilityResult {
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
