package com.rit.placement.model;

/**
 * DTO for job application status information.
 * Combines job posting details with student-specific application status.
 * 
 * This DTO is used to display jobs with their eligibility and application status
 * for a specific student, avoiding the need to reuse JobPosting fields for temporary data.
 */
public class JobApplicationStatus {
    private JobPosting job;
    private boolean hasApplied;
    private boolean isEligible;
    private String eligibilityReason;

    // Constructors
    public JobApplicationStatus() {}

    public JobApplicationStatus(JobPosting job, boolean hasApplied, boolean isEligible) {
        this.job = job;
        this.hasApplied = hasApplied;
        this.isEligible = isEligible;
    }

    public JobApplicationStatus(JobPosting job, boolean hasApplied, boolean isEligible, String eligibilityReason) {
        this.job = job;
        this.hasApplied = hasApplied;
        this.isEligible = isEligible;
        this.eligibilityReason = eligibilityReason;
    }

    // Getters and Setters
    public JobPosting getJob() { 
        return job; 
    }
    
    public void setJob(JobPosting job) { 
        this.job = job; 
    }

    public boolean isHasApplied() { 
        return hasApplied; 
    }
    
    public void setHasApplied(boolean hasApplied) { 
        this.hasApplied = hasApplied; 
    }

    public boolean isEligible() { 
        return isEligible; 
    }
    
    public void setEligible(boolean eligible) { 
        this.isEligible = eligible; 
    }

    public String getEligibilityReason() { 
        return eligibilityReason; 
    }
    
    public void setEligibilityReason(String eligibilityReason) { 
        this.eligibilityReason = eligibilityReason; 
    }

    // Convenience methods for JSP
    public String getApplicationStatus() {
        if (hasApplied) {
            return "APPLIED";
        } else if (!isEligible) {
            return "NOT_ELIGIBLE";
        } else {
            return "ELIGIBLE";
        }
    }

    public boolean canApply() {
        return isEligible && !hasApplied;
    }
}
