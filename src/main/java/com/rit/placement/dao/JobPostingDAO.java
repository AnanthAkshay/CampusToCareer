package com.rit.placement.dao;

import com.rit.placement.model.JobPosting;
import com.rit.placement.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for the 'job_postings' table.
 */
public class JobPostingDAO {

    /**
     * Insert a new job posting and return the generated job_id.
     */
    public int insertJobPosting(JobPosting job) throws SQLException {
        String sql = "INSERT INTO job_postings (company_id, role, package, min_cgpa, allowed_branches, required_skills, deadline) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setInt(1, job.getCompanyId());
            ps.setString(2, job.getRole());
            ps.setBigDecimal(3, job.getPackageAmount());
            ps.setBigDecimal(4, job.getMinCgpa());
            ps.setString(5, job.getAllowedBranches());
            ps.setString(6, job.getRequiredSkills());
            ps.setDate(7, job.getDeadline());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }
            throw new SQLException("Failed to retrieve generated job_id.");
        }
    }

    /**
     * Get all job postings with company names (JOIN query).
     */
    public List<JobPosting> getAllJobPostings() throws SQLException {
        String sql = "SELECT j.*, c.company_name " +
                     "FROM job_postings j " +
                     "INNER JOIN companies c ON j.company_id = c.company_id " +
                     "ORDER BY j.created_at DESC";
        List<JobPosting> jobs = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                jobs.add(mapRowWithCompany(rs));
            }
        }
        return jobs;
    }

    /**
     * Get job postings by company ID.
     */
    public List<JobPosting> getJobPostingsByCompany(int companyId) throws SQLException {
        String sql = "SELECT j.*, c.company_name " +
                     "FROM job_postings j " +
                     "INNER JOIN companies c ON j.company_id = c.company_id " +
                     "WHERE j.company_id = ? " +
                     "ORDER BY j.created_at DESC";
        List<JobPosting> jobs = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, companyId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                jobs.add(mapRowWithCompany(rs));
            }
        }
        return jobs;
    }

    /**
     * Get a job posting by ID.
     */
    public JobPosting getJobPostingById(int jobId) throws SQLException {
        String sql = "SELECT j.*, c.company_name " +
                     "FROM job_postings j " +
                     "INNER JOIN companies c ON j.company_id = c.company_id " +
                     "WHERE j.job_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, jobId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return mapRowWithCompany(rs);
            }
            return null;
        }
    }

    /**
     * Get job postings eligible for a student (based on CGPA and branch).
     */
    public List<JobPosting> getEligibleJobPostings(double cgpa, String branch) throws SQLException {
        String sql = "SELECT j.*, c.company_name " +
                     "FROM job_postings j " +
                     "INNER JOIN companies c ON j.company_id = c.company_id " +
                     "WHERE j.min_cgpa <= ? " +
                     "AND (j.allowed_branches IS NULL OR j.allowed_branches LIKE ?) " +
                     "AND j.deadline >= CURDATE() " +
                     "ORDER BY j.created_at DESC";
        List<JobPosting> jobs = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setDouble(1, cgpa);
            ps.setString(2, "%" + branch + "%");
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                jobs.add(mapRowWithCompany(rs));
            }
        }
        return jobs;
    }

    /**
     * Update job posting details.
     */
    public void updateJobPosting(JobPosting job) throws SQLException {
        String sql = "UPDATE job_postings SET company_id = ?, role = ?, package = ?, min_cgpa = ?, " +
                     "allowed_branches = ?, required_skills = ?, deadline = ? WHERE job_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, job.getCompanyId());
            ps.setString(2, job.getRole());
            ps.setBigDecimal(3, job.getPackageAmount());
            ps.setBigDecimal(4, job.getMinCgpa());
            ps.setString(5, job.getAllowedBranches());
            ps.setString(6, job.getRequiredSkills());
            ps.setDate(7, job.getDeadline());
            ps.setInt(8, job.getJobId());
            ps.executeUpdate();
        }
    }

    /**
     * Delete a job posting by ID.
     */
    public void deleteJobPosting(int jobId) throws SQLException {
        String sql = "DELETE FROM job_postings WHERE job_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, jobId);
            ps.executeUpdate();
        }
    }

    /**
     * Map ResultSet row to JobPosting object (with company name).
     */
    private JobPosting mapRowWithCompany(ResultSet rs) throws SQLException {
        JobPosting job = new JobPosting();
        job.setJobId(rs.getInt("job_id"));
        job.setCompanyId(rs.getInt("company_id"));
        job.setRole(rs.getString("role"));
        job.setPackageAmount(rs.getBigDecimal("package"));
        job.setMinCgpa(rs.getBigDecimal("min_cgpa"));
        job.setAllowedBranches(rs.getString("allowed_branches"));
        job.setRequiredSkills(rs.getString("required_skills"));
        job.setDeadline(rs.getDate("deadline"));
        job.setCreatedAt(rs.getTimestamp("created_at"));
        job.setCompanyName(rs.getString("company_name"));
        return job;
    }
}
