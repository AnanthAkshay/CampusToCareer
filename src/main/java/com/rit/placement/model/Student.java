package com.rit.placement.model;

import java.sql.Timestamp;

/**
 * JavaBean representing a row in the 'students' table.
 * Extended with user information for display purposes.
 */
public class Student {
    private int studentId;   // FK → users.user_id
    private String branch;
    private int currentSem;
    private String skills;
    private String projects;
    private String experience;
    private Timestamp updatedAt;
    
    // Extended fields from users table
    private String usn;
    private String name;
    private String email;
    private double cgpa;  // Calculated from academic_records

    // --- Getters & Setters ---
    public int getStudentId()                   { return studentId; }
    public void setStudentId(int studentId)     { this.studentId = studentId; }

    public String getBranch()                   { return branch; }
    public void setBranch(String branch)        { this.branch = branch; }

    public int getCurrentSem()                  { return currentSem; }
    public void setCurrentSem(int currentSem)   { this.currentSem = currentSem; }

    public String getSkills()                   { return skills; }
    public void setSkills(String skills)        { this.skills = skills; }

    public String getProjects()                 { return projects; }
    public void setProjects(String projects)    { this.projects = projects; }

    public String getExperience()               { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public Timestamp getUpdatedAt()             { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
    
    // Extended fields
    public String getUsn()                      { return usn; }
    public void setUsn(String usn)              { this.usn = usn; }
    
    public String getName()                     { return name; }
    public void setName(String name)            { this.name = name; }
    
    public String getEmail()                    { return email; }
    public void setEmail(String email)          { this.email = email; }
    
    public double getCgpa()                     { return cgpa; }
    public void setCgpa(double cgpa)            { this.cgpa = cgpa; }
}
