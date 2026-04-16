package com.rit.placement.model;

import java.sql.Timestamp;

/**
 * JavaBean representing a row in the 'applications' table.
 */
public class Application {
    private int applicationId;
    private int studentId;
    private int jobId;
    private String status; // APPLIED, SHORTLISTED, SELECTED, REJECTED
    private Timestamp appliedAt;
    
    // Additional fields for display (not in DB)
    private String studentName;
    private String studentUsn;
    private String jobRole;
    private String companyName;

    // --- Getters & Setters ---
    public int getApplicationId()                       { return applicationId; }
    public void setApplicationId(int applicationId)     { this.applicationId = applicationId; }

    public int getStudentId()                           { return studentId; }
    public void setStudentId(int studentId)             { this.studentId = studentId; }

    public int getJobId()                               { return jobId; }
    public void setJobId(int jobId)                     { this.jobId = jobId; }

    public String getStatus()                           { return status; }
    public void setStatus(String status)                { this.status = status; }

    public Timestamp getAppliedAt()                     { return appliedAt; }
    public void setAppliedAt(Timestamp appliedAt)       { this.appliedAt = appliedAt; }

    public String getStudentName()                      { return studentName; }
    public void setStudentName(String studentName)      { this.studentName = studentName; }

    public String getStudentUsn()                       { return studentUsn; }
    public void setStudentUsn(String studentUsn)        { this.studentUsn = studentUsn; }

    public String getJobRole()                          { return jobRole; }
    public void setJobRole(String jobRole)              { this.jobRole = jobRole; }

    public String getCompanyName()                      { return companyName; }
    public void setCompanyName(String companyName)      { this.companyName = companyName; }
}
