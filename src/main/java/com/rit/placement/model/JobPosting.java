package com.rit.placement.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

/**
 * JavaBean representing a row in the 'job_postings' table.
 */
public class JobPosting {
    private int jobId;
    private int companyId;
    private String role;
    private BigDecimal packageAmount;
    private BigDecimal minCgpa;
    private String allowedBranches;
    private String requiredSkills;
    private Date deadline;
    private Timestamp createdAt;
    
    // Additional field for display (not in DB)
    private String companyName;

    // --- Getters & Setters ---
    public int getJobId()                               { return jobId; }
    public void setJobId(int jobId)                     { this.jobId = jobId; }

    public int getCompanyId()                           { return companyId; }
    public void setCompanyId(int companyId)             { this.companyId = companyId; }

    public String getRole()                             { return role; }
    public void setRole(String role)                    { this.role = role; }

    public BigDecimal getPackageAmount()                { return packageAmount; }
    public void setPackageAmount(BigDecimal packageAmount) { this.packageAmount = packageAmount; }

    public BigDecimal getMinCgpa()                      { return minCgpa; }
    public void setMinCgpa(BigDecimal minCgpa)          { this.minCgpa = minCgpa; }

    public String getAllowedBranches()                  { return allowedBranches; }
    public void setAllowedBranches(String allowedBranches) { this.allowedBranches = allowedBranches; }

    public String getRequiredSkills()                   { return requiredSkills; }
    public void setRequiredSkills(String requiredSkills) { this.requiredSkills = requiredSkills; }

    public Date getDeadline()                           { return deadline; }
    public void setDeadline(Date deadline)              { this.deadline = deadline; }

    public Timestamp getCreatedAt()                     { return createdAt; }
    public void setCreatedAt(Timestamp createdAt)       { this.createdAt = createdAt; }

    public String getCompanyName()                      { return companyName; }
    public void setCompanyName(String companyName)      { this.companyName = companyName; }
}
