package com.rit.placement.model;

public class StudentPerformance {
    private int studentId;
    private String usn;
    private String name;
    private String email;
    private String branch;
    private String skills;
    private double cgpa;
    private int applicationsCount;
    private boolean isPlaced;
    private String riskStatus;
    
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    
    public String getUsn() { return usn; }
    public void setUsn(String usn) { this.usn = usn; }
    
    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
    
    public double getCgpa() { return cgpa; }
    public void setCgpa(double cgpa) { this.cgpa = cgpa; }
    
    public int getApplicationsCount() { return applicationsCount; }
    public void setApplicationsCount(int applicationsCount) { this.applicationsCount = applicationsCount; }
    
    public boolean isPlaced() { return isPlaced; }
    public void setPlaced(boolean placed) { isPlaced = placed; }
    
    public String getRiskStatus() { 
        if (cgpa < 7.0 || applicationsCount == 0) {
            return "AT RISK";
        }
        return "SAFE";
    }
    public void setRiskStatus(String riskStatus) { this.riskStatus = riskStatus; }
}
