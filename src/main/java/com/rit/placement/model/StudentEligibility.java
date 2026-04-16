package com.rit.placement.model;

/**
 * DTO for student eligibility information.
 * Used to display eligible students for a job posting.
 */
public class StudentEligibility {
    private int studentId;
    private String name;
    private String usn;
    private String branch;
    private double cgpa;
    private boolean eligible;
    private String eligibilityStatus;

    // Constructors
    public StudentEligibility() {}

    public StudentEligibility(int studentId, String name, String usn, String branch, 
                             double cgpa, boolean eligible) {
        this.studentId = studentId;
        this.name = name;
        this.usn = usn;
        this.branch = branch;
        this.cgpa = cgpa;
        this.eligible = eligible;
        this.eligibilityStatus = eligible ? "Eligible" : "Not Eligible";
    }

    // Getters and Setters
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUsn() { return usn; }
    public void setUsn(String usn) { this.usn = usn; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    public double getCgpa() { return cgpa; }
    public void setCgpa(double cgpa) { this.cgpa = cgpa; }

    public boolean isEligible() { return eligible; }
    public void setEligible(boolean eligible) { 
        this.eligible = eligible;
        this.eligibilityStatus = eligible ? "Eligible" : "Not Eligible";
    }

    public String getEligibilityStatus() { return eligibilityStatus; }
    public void setEligibilityStatus(String eligibilityStatus) { 
        this.eligibilityStatus = eligibilityStatus; 
    }
}
