package com.rit.placement.model;

import java.sql.Timestamp;

public class ProctorRemark {
    private int id;
    private int studentId;
    private int proctorId;
    private String remark;
    private Timestamp createdAt;
    private String proctorName;
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    
    public int getProctorId() { return proctorId; }
    public void setProctorId(int proctorId) { this.proctorId = proctorId; }
    
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    
    public String getProctorName() { return proctorName; }
    public void setProctorName(String proctorName) { this.proctorName = proctorName; }
}
