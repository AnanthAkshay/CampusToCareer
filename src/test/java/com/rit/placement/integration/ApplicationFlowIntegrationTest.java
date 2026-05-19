package com.rit.placement.integration;

import com.rit.placement.BaseTest;
import com.rit.placement.dao.MockApplicationDAO;
import com.rit.placement.dao.MockNotificationDAO;
import com.rit.placement.dao.MockUserDAO;
import com.rit.placement.model.Application;
import com.rit.placement.model.Notification;
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
 * Integration tests simulating complete application flow
 */
@DisplayName("Application Flow Integration Tests")
class ApplicationFlowIntegrationTest extends BaseTest {
    
    private MockUserDAO userDAO;
    private MockApplicationDAO applicationDAO;
    private MockNotificationDAO notificationDAO;
    
    private int studentId;
    private int companyId;
    private int jobId;
    
    @BeforeEach
    public void setUpIntegrationTest() throws SQLException {
        userDAO = new MockUserDAO();
        applicationDAO = new MockApplicationDAO();
        notificationDAO = new MockNotificationDAO();
        
        // Set up test data
        setupTestData();
    }
    
    @Test
    @DisplayName("Complete flow: Student applies for job")
    void testCompleteApplicationFlow() throws SQLException {
        // Step 1: Student logs in (simulated by getting user)
        User student = userDAO.getUserById(studentId);
        assertThat(student).isNotNull();
        assertThat(student.getRole()).isEqualTo("STUDENT");
        
        // Step 2: Student checks if already applied
        boolean hasApplied = applicationDAO.hasApplied(studentId, jobId);
        assertThat(hasApplied).isFalse();
        
        // Step 3: Student submits application
        Application application = new Application();
        application.setStudentId(studentId);
        application.setJobId(jobId);
        application.setStatus("PENDING");
        
        int applicationId = applicationDAO.insertApplication(application);
        assertThat(applicationId).isGreaterThan(0);
        
        // Step 4: System creates notification for student
        Notification studentNotification = new Notification();
        studentNotification.setUserId(studentId);
        studentNotification.setTitle("Application Submitted");
        studentNotification.setMessage("Your application has been submitted successfully");
        studentNotification.setType("APPLICATION");
        studentNotification.setRelatedId(applicationId);
        
        int notificationId = notificationDAO.createNotification(studentNotification);
        assertThat(notificationId).isGreaterThan(0);
        
        // Step 5: System creates notification for company
        Notification companyNotification = new Notification();
        companyNotification.setUserId(companyId);
        companyNotification.setTitle("New Application Received");
        companyNotification.setMessage("A student has applied for your job posting");
        companyNotification.setType("APPLICATION");
        companyNotification.setRelatedId(applicationId);
        
        int companyNotificationId = notificationDAO.createNotification(companyNotification);
        assertThat(companyNotificationId).isGreaterThan(0);
        
        // Step 6: Verify application was created
        Application savedApplication = applicationDAO.getApplicationById(applicationId);
        assertThat(savedApplication).isNotNull();
        assertThat(savedApplication.getStatus()).isEqualTo("PENDING");
        
        // Step 7: Verify student can't apply again
        boolean hasAppliedNow = applicationDAO.hasApplied(studentId, jobId);
        assertThat(hasAppliedNow).isTrue();
        
        // Step 8: Verify notifications were created
        List<Notification> studentNotifications = notificationDAO.getNotificationsByUser(studentId);
        assertThat(studentNotifications).hasSize(1);
        assertThat(studentNotifications.get(0).getTitle()).isEqualTo("Application Submitted");
        
        List<Notification> companyNotifications = notificationDAO.getNotificationsByUser(companyId);
        assertThat(companyNotifications).hasSize(1);
        assertThat(companyNotifications.get(0).getTitle()).isEqualTo("New Application Received");
    }
    
    @Test
    @DisplayName("Complete flow: Company updates application status")
    void testStatusUpdateFlow() throws SQLException {
        // Step 1: Create initial application
        Application application = new Application();
        application.setStudentId(studentId);
        application.setJobId(jobId);
        application.setStatus("PENDING");
        int applicationId = applicationDAO.insertApplication(application);
        
        // Step 2: Company reviews and shortlists candidate
        applicationDAO.updateApplicationStatus(applicationId, "SHORTLISTED");
        
        // Step 3: System creates notification for student
        Notification notification = new Notification();
        notification.setUserId(studentId);
        notification.setTitle("Application Status Updated");
        notification.setMessage("Your application has been shortlisted");
        notification.setType("STATUS_UPDATE");
        notification.setRelatedId(applicationId);
        notificationDAO.createNotification(notification);
        
        // Step 4: Verify status was updated
        Application updated = applicationDAO.getApplicationById(applicationId);
        assertThat(updated.getStatus()).isEqualTo("SHORTLISTED");
        
        // Step 5: Verify notification was sent
        List<Notification> notifications = notificationDAO.getNotificationsByUser(studentId);
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).getType()).isEqualTo("STATUS_UPDATE");
        
        // Step 6: Student marks notification as read
        int notificationId = notifications.get(0).getNotificationId();
        notificationDAO.markAsRead(notificationId);
        
        // Step 7: Verify unread count decreased
        int unreadCount = notificationDAO.getUnreadCount(studentId);
        assertThat(unreadCount).isEqualTo(0);
    }
    
    @Test
    @DisplayName("Complete flow: Multiple status updates")
    void testMultipleStatusUpdates() throws SQLException {
        // Step 1: Create application
        Application application = new Application();
        application.setStudentId(studentId);
        application.setJobId(jobId);
        application.setStatus("PENDING");
        int applicationId = applicationDAO.insertApplication(application);
        
        // Step 2: Update to SHORTLISTED
        applicationDAO.updateApplicationStatus(applicationId, "SHORTLISTED");
        createStatusNotification(studentId, applicationId, "Shortlisted");
        
        // Step 3: Update to INTERVIEW
        applicationDAO.updateApplicationStatus(applicationId, "INTERVIEW");
        createStatusNotification(studentId, applicationId, "Interview Scheduled");
        
        // Step 4: Update to SELECTED
        applicationDAO.updateApplicationStatus(applicationId, "SELECTED");
        createStatusNotification(studentId, applicationId, "Selected");
        
        // Step 5: Verify final status
        Application finalApp = applicationDAO.getApplicationById(applicationId);
        assertThat(finalApp.getStatus()).isEqualTo("SELECTED");
        
        // Step 6: Verify all notifications were created
        List<Notification> notifications = notificationDAO.getNotificationsByUser(studentId);
        assertThat(notifications).hasSize(3);
        assertThat(notifications).extracting(Notification::getTitle)
            .contains("Shortlisted", "Interview Scheduled", "Selected");
    }
    
    @Test
    @DisplayName("Complete flow: Student applies to multiple jobs")
    void testMultipleApplications() throws SQLException {
        // Step 1: Create additional jobs
        int jobId2 = createTestJob("Backend Developer");
        int jobId3 = createTestJob("Frontend Developer");
        
        // Step 2: Student applies to all jobs
        int appId1 = createAndSubmitApplication(studentId, jobId, "PENDING");
        int appId2 = createAndSubmitApplication(studentId, jobId2, "PENDING");
        int appId3 = createAndSubmitApplication(studentId, jobId3, "PENDING");
        
        // Step 3: Verify all applications were created
        List<Application> applications = applicationDAO.getApplicationsByStudent(studentId);
        assertThat(applications).hasSize(3);
        
        // Step 4: Update different statuses
        applicationDAO.updateApplicationStatus(appId1, "SHORTLISTED");
        applicationDAO.updateApplicationStatus(appId2, "REJECTED");
        applicationDAO.updateApplicationStatus(appId3, "SELECTED");
        
        // Step 5: Verify statuses
        assertThat(applicationDAO.getApplicationById(appId1).getStatus()).isEqualTo("SHORTLISTED");
        assertThat(applicationDAO.getApplicationById(appId2).getStatus()).isEqualTo("REJECTED");
        assertThat(applicationDAO.getApplicationById(appId3).getStatus()).isEqualTo("SELECTED");
    }
    
    @Test
    @DisplayName("Complete flow: Notification management")
    void testNotificationManagement() throws SQLException {
        // Step 1: Create multiple notifications
        createStatusNotification(studentId, 1, "Notification 1");
        createStatusNotification(studentId, 2, "Notification 2");
        createStatusNotification(studentId, 3, "Notification 3");
        
        // Step 2: Verify unread count
        int unreadCount = notificationDAO.getUnreadCount(studentId);
        assertThat(unreadCount).isEqualTo(3);
        
        // Step 3: Mark one as read
        List<Notification> notifications = notificationDAO.getNotificationsByUser(studentId);
        notificationDAO.markAsRead(notifications.get(0).getNotificationId());
        
        // Step 4: Verify unread count decreased
        unreadCount = notificationDAO.getUnreadCount(studentId);
        assertThat(unreadCount).isEqualTo(2);
        
        // Step 5: Mark all as read
        notificationDAO.markAllAsRead(studentId);
        
        // Step 6: Verify all marked as read
        unreadCount = notificationDAO.getUnreadCount(studentId);
        assertThat(unreadCount).isEqualTo(0);
    }
    
    // Helper methods
    private void setupTestData() throws SQLException {
        // Create student user
        User student = new User();
        student.setUsn("1RI21IS999");
        student.setName("Integration Test Student");
        student.setPasswordHash("password");
        student.setRole("STUDENT");
        student.setActive(true);
        studentId = userDAO.insertUser(student);
        
        // Create student record
        try (Connection conn = MockDBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO students (student_id, branch, current_sem) VALUES (?, ?, ?)")) {
            stmt.setInt(1, studentId);
            stmt.setString(2, "ISE");
            stmt.setInt(3, 6);
            stmt.executeUpdate();
        }
        
        // Create company user
        User company = new User();
        company.setUsn("COMP999");
        company.setName("Integration Test Company");
        company.setPasswordHash("password");
        company.setRole("COMPANY");
        company.setActive(true);
        company.setCompanyId(1);
        companyId = userDAO.insertUser(company);
        
        // Create company record
        try (Connection conn = MockDBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO companies (company_id, company_name) VALUES (?, ?)",
                 PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, 1);
            stmt.setString(2, "Test Company");
            stmt.executeUpdate();
        }
        
        // Create job posting
        jobId = createTestJob("Software Engineer");
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
    
    private int createAndSubmitApplication(int studentId, int jobId, String status) throws SQLException {
        Application application = new Application();
        application.setStudentId(studentId);
        application.setJobId(jobId);
        application.setStatus(status);
        return applicationDAO.insertApplication(application);
    }
    
    private void createStatusNotification(int userId, int relatedId, String title) throws SQLException {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage("Status update message");
        notification.setType("STATUS_UPDATE");
        notification.setRelatedId(relatedId);
        notificationDAO.createNotification(notification);
    }
}
