package com.rit.placement.model;

import java.sql.Timestamp;

/**
 * JavaBean representing a row in the 'users' table.
 */
public class User {
    private int userId;
    private String usn;
    private String name;
    private String email;
    private String passwordHash;
    private String role;       // STUDENT, PROCTOR, COORDINATOR, FACULTY, ADMIN, COMPANY
    private boolean isActive;
    private Timestamp createdAt;
    private Integer companyId; // For COMPANY role users - links to companies table

    // --- Getters & Setters ---
    public int getUserId()              { return userId; }
    public void setUserId(int userId)   { this.userId = userId; }
    
    // Alias method for compatibility with servlet code
    public int getId()                  { return userId; }

    public String getUsn()              { return usn; }
    public void setUsn(String usn)      { this.usn = usn; }

    public String getName()             { return name; }
    public void setName(String name)    { this.name = name; }
    
    public String getEmail()            { return email; }
    public void setEmail(String email)  { this.email = email; }

    public String getPasswordHash()                     { return passwordHash; }
    public void setPasswordHash(String passwordHash)    { this.passwordHash = passwordHash; }

    public String getRole()             { return role; }
    public void setRole(String role)    { this.role = role; }

    public boolean isActive()                   { return isActive; }
    public void setActive(boolean isActive)     { this.isActive = isActive; }

    public Timestamp getCreatedAt()                 { return createdAt; }
    public void setCreatedAt(Timestamp createdAt)   { this.createdAt = createdAt; }
    
    public Integer getCompanyId()                   { return companyId; }
    public void setCompanyId(Integer companyId)     { this.companyId = companyId; }
}
