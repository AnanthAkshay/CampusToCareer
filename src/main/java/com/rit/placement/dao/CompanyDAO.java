package com.rit.placement.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rit.placement.model.Company;
import com.rit.placement.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for the 'companies' table.
 */
public class CompanyDAO {
    private static final Logger logger = LoggerFactory.getLogger(CompanyDAO.class);

    /**
     * Insert a new company and return the generated company_id.
     */
    public int insertCompany(Company company) throws SQLException {
        String sql = "INSERT INTO companies (company_name, description, company_type) VALUES (?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, company.getCompanyName());
            ps.setString(2, company.getDescription());
            ps.setString(3, company.getCompanyType());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }
            throw new SQLException("Failed to retrieve generated company_id.");
        }
    }

    /**
     * Get all companies ordered by creation date (newest first).
     */
    public List<Company> getAllCompanies() throws SQLException {
        String sql = "SELECT * FROM companies ORDER BY created_at DESC";
        List<Company> companies = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                companies.add(mapRow(rs));
            }
        }
        return companies;
    }

    /**
     * Get a company by ID.
     */
    public Company getCompanyById(int companyId) throws SQLException {
        String sql = "SELECT * FROM companies WHERE company_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, companyId);
            try (ResultSet rs = ps.executeQuery()) {
            
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        }
    }

    /**
     * Update company details.
     */
    public void updateCompany(Company company) throws SQLException {
        String sql = "UPDATE companies SET company_name = ?, description = ?, company_type = ? WHERE company_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, company.getCompanyName());
            ps.setString(2, company.getDescription());
            ps.setString(3, company.getCompanyType());
            ps.setInt(4, company.getCompanyId());
            ps.executeUpdate();
        }
    }

    /**
     * Delete a company by ID.
     */
    public void deleteCompany(int companyId) throws SQLException {
        String sql = "DELETE FROM companies WHERE company_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, companyId);
            ps.executeUpdate();
        }
    }

    /**
     * Map ResultSet row to Company object.
     */
    private Company mapRow(ResultSet rs) throws SQLException {
        Company company = new Company();
        company.setCompanyId(rs.getInt("company_id"));
        company.setCompanyName(rs.getString("company_name"));
        company.setDescription(rs.getString("description"));
        company.setCompanyType(rs.getString("company_type"));
        company.setCreatedAt(rs.getTimestamp("created_at"));
        return company;
    }
}
