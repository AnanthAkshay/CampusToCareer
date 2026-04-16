package com.rit.placement.model;

import java.sql.Timestamp;

/**
 * JavaBean representing a row in the 'students' table.
 */
public class Student {
    private int studentId;   // FK → users.user_id
    private String branch;
    private int currentSem;
    private String skills;
    private String projects;
    private String experience;
    private Timestamp updatedAt;

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
}
