package com.rit.placement.model;

/**
 * JavaBean representing a row in the 'academic_records' table.
 */
public class AcademicRecord {
    private int recordId;
    private int studentId;
    private int semester;
    private double sgpa;

    // --- Getters & Setters ---
    public int getRecordId()                    { return recordId; }
    public void setRecordId(int recordId)       { this.recordId = recordId; }

    public int getStudentId()                   { return studentId; }
    public void setStudentId(int studentId)     { this.studentId = studentId; }

    public int getSemester()                    { return semester; }
    public void setSemester(int semester)       { this.semester = semester; }

    public double getSgpa()                     { return sgpa; }
    public void setSgpa(double sgpa)            { this.sgpa = sgpa; }
}
