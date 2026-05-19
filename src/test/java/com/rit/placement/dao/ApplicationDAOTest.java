package com.rit.placement.dao;

import com.rit.placement.BaseTest;
import com.rit.placement.model.Application;
import com.rit.placement.model.User;
import com.rit.placement.util.MockDBConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for ApplicationDAO
 */
@DisplayName("ApplicationDAO Tests")
class ApplicationDAOTest extends BaseTest {
    
    private MockApplicationDAO applicationDAO;
    private MockUserDAO userDAO;
    private int testStudentId;
    private int testJobId;
    
    @BeforeEach
    public void setUpDAO() throws SQLException {
        applicationDAO = new MockApplicationDAO();
        userDAO = new MockUserDAO();
        
        // Create test student
        User student = new User();
        student.setUsn("1RI21IS100");
        student.setName("Test Student");
        student.setPasswordHash("password");
        student.setRole("STUDENT");
        student.setActive(true);
        testStudentId = userDAO.insertUser(student);
        
        // Create test student record
        try (Connection conn = MockDBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO students (student_id, branch, current_sem) VALUES (?, ?, ?)")) {
            stmt.setInt(1, testStudentId);
            stmt.setString(2, "ISE");
            stmt.setInt(3, 6);
            stmt.executeUpdate();
        }
        
        // Create test company and job
        try (Connection conn = MockDBConnection.getConnection()) {
            PreparedStatement stmt1 = conn.prepareStatement(
                "INSERT INTO companies (company_name) VALUES (?)",
                PreparedStatement.RETURN_GENERATED_KEYS);
            stmt1.setString(1, "Test Company");
            stmt1.executeUpdate();
            
            var keys = stmt1.getGeneratedKeys();
            int companyId = 0;
            if (keys.next()) {
                companyId = keys.getInt(1);
            }
            
            PreparedStatement stmt2 = conn.prepareStatement(
                "INSERT INTO job_postings (company_id, role, min_cgpa) VALUES (?, ?, ?)",
                PreparedStatement.RETURN_GENERATED_KEYS);
            stmt2.setInt(1, companyId);
            stmt2.setString(2, "Software Engineer");
            stmt2.setDouble(3, 7.0);
            stmt2.executeUpdate();
            
            keys = stmt2.getGeneratedKeys();
            if (keys.next()) {
                testJobId = keys.getInt(1);
            }
        }
    }
    
    @Test
    @DisplayName("Should create application successfully")
    void testCreateApplication() throws SQLException {
        // Given: An application
        Application application = new Application();
        application.setStudentId(testStudentId);
        application.setJobId(testJobId);
        application.setStatus("PENDING");
        
        // When: Inserting application
        int applicationId = applicationDAO.insertApplication(application);
        
        // Then: Application should be created with valid ID
        assertThat(applicationId).isGreaterThan(0);
        
        // And: Application should be retrievable
        Application retrieved = applicationDAO.getApplicationById(applicationId);
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getStudentId()).isEqualTo(testStudentId);
        assertThat(retrieved.getJobId()).isEqualTo(testJobId);
        assertThat(retrieved.getStatus()).isEqualTo("PENDING");
    }
    
    @Test
    @DisplayName("Should check if student has applied")
    void testHasApplied() throws SQLException {
        // Given: Student has not applied
        assertThat(applicationDAO.hasApplied(testStudentId, testJobId)).isFalse();
        
        // When: Student applies
        Application application = new Application();
        application.setStudentId(testStudentId);
        application.setJobId(testJobId);
        application.setStatus("PENDING");
        applicationDAO.insertApplication(application);
        
        // Then: hasApplied should return true
        assertThat(applicationDAO.hasApplied(testStudentId, testJobId)).isTrue();
    }
    
    @Test
    @DisplayName("Should get applications by student")
    void testGetApplicationsByStudent() throws SQLException {
        // Given: Student has multiple applications
        Application app1 = createApplication(testStudentId, testJobId, "PENDING");
        
        // Create another job
        int jobId2 = createTestJob("Backend Developer");
        Application app2 = createApplication(testStudentId, jobId2, "SHORTLISTED");
        
        applicationDAO.insertApplication(app1);
        applicationDAO.insertApplication(app2);
        
        // When: Getting applications by student
        List<Application> applications = applicationDAO.getApplicationsByStudent(testStudentId);
        
        // Then: Should return all applications
        assertThat(applications).hasSize(2);
        assertThat(applications).extracting(Application::getStatus)
            .containsExactlyInAnyOrder("PENDING", "SHORTLISTED");
    }
    
    @Test
    @DisplayName("Should update application status")
    void testUpdateApplicationStatus() throws SQLException {
        // Given: An application exists
        Application application = new Application();
        application.setStudentId(testStudentId);
        application.setJobId(testJobId);
        application.setStatus("PENDING");
        int applicationId = applicationDAO.insertApplication(application);
        
        // When: Updating status
        applicationDAO.updateApplicationStatus(applicationId, "SHORTLISTED");
        
        // Then: Status should be updated
        Application updated = applicationDAO.getApplicationById(applicationId);
        assertThat(updated.getStatus()).isEqualTo("SHORTLISTED");
    }
    
    @Test
    @DisplayName("Should prevent duplicate applications")
    void testPreventDuplicateApplications() throws SQLException {
        // Given: Student has applied
        Application application = new Application();
        application.setStudentId(testStudentId);
        application.setJobId(testJobId);
        application.setStatus("PENDING");
        applicationDAO.insertApplication(application);
        
        // When: Trying to apply again
        // Then: Should throw SQLException due to unique constraint
        assertThat(applicationDAO.hasApplied(testStudentId, testJobId)).isTrue();
    }
    
    private Application createApplication(int studentId, int jobId, String status) {
        Application app = new Application();
        app.setStudentId(studentId);
        app.setJobId(jobId);
        app.setStatus(status);
        return app;
    }
    
    private int createTestJob(String role) throws SQLException {
        try (Connection conn = MockDBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO job_postings (company_id, role, min_cgpa) VALUES (1, ?, 7.0)",
                 PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, role);
            stmt.executeUpdate();
            
            var keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }
            throw new SQLException("Failed to create test job");
        }
    }
}
