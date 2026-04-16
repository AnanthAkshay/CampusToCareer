package com.rit.placement.model;

import java.sql.Timestamp;

/**
 * JavaBean representing a row in the 'companies' table.
 */
public class Company {
    private int companyId;
    private String companyName;
    private String description;
    private String companyType;
    private Timestamp createdAt;

    // --- Getters & Setters ---
    public int getCompanyId()                       { return companyId; }
    public void setCompanyId(int companyId)         { this.companyId = companyId; }

    public String getCompanyName()                  { return companyName; }
    public void setCompanyName(String companyName)  { this.companyName = companyName; }

    public String getDescription()                  { return description; }
    public void setDescription(String description)  { this.description = description; }

    public String getCompanyType()                  { return companyType; }
    public void setCompanyType(String companyType)  { this.companyType = companyType; }

    public Timestamp getCreatedAt()                 { return createdAt; }
    public void setCreatedAt(Timestamp createdAt)   { this.createdAt = createdAt; }
}
