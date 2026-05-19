package com.rit.placement.dao;

import com.rit.placement.BaseTest;
import com.rit.placement.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for UserDAO
 */
@DisplayName("UserDAO Tests")
class UserDAOTest extends BaseTest {
    
    private MockUserDAO userDAO;
    
    @BeforeEach
    public void setUpDAO() {
        userDAO = new MockUserDAO();
    }
    
    @Test
    @DisplayName("Should find user by USN")
    void testFindByUsn() throws SQLException {
        // Given: A user exists in database
        User user = new User();
        user.setUsn("1RI21IS001");
        user.setName("John Doe");
        user.setPasswordHash("password123");
        user.setRole("STUDENT");
        user.setActive(true);
        
        int userId = userDAO.insertUser(user);
        
        // When: Finding user by USN
        User found = userDAO.getUserByUSN("1RI21IS001");
        
        // Then: User should be found with correct details
        assertThat(found).isNotNull();
        assertThat(found.getUserId()).isEqualTo(userId);
        assertThat(found.getUsn()).isEqualTo("1RI21IS001");
        assertThat(found.getName()).isEqualTo("John Doe");
        assertThat(found.getRole()).isEqualTo("STUDENT");
        assertThat(found.isActive()).isTrue();
    }
    
    @Test
    @DisplayName("Should return null for non-existent USN")
    void testFindByUsnNotFound() {
        // When: Finding user that doesn't exist
        User found = userDAO.getUserByUSN("NONEXISTENT");
        
        // Then: Should return null
        assertThat(found).isNull();
    }
    
    @Test
    @DisplayName("Should find user by ID")
    void testFindById() throws SQLException {
        // Given: A user exists in database
        User user = new User();
        user.setUsn("1RI21IS002");
        user.setName("Jane Smith");
        user.setPasswordHash("password456");
        user.setRole("PROCTOR");
        user.setActive(true);
        
        int userId = userDAO.insertUser(user);
        
        // When: Finding user by ID
        User found = userDAO.getUserById(userId);
        
        // Then: User should be found
        assertThat(found).isNotNull();
        assertThat(found.getUserId()).isEqualTo(userId);
        assertThat(found.getUsn()).isEqualTo("1RI21IS002");
        assertThat(found.getName()).isEqualTo("Jane Smith");
        assertThat(found.getRole()).isEqualTo("PROCTOR");
    }
    
    @Test
    @DisplayName("Should return null for non-existent ID")
    void testFindByIdNotFound() {
        // When: Finding user that doesn't exist
        User found = userDAO.getUserById(999);
        
        // Then: Should return null
        assertThat(found).isNull();
    }
    
    @Test
    @DisplayName("Should handle company user with company_id")
    void testCompanyUser() throws SQLException {
        // Given: A company user with company_id
        User user = new User();
        user.setUsn("COMP001");
        user.setName("Tech Corp");
        user.setPasswordHash("password789");
        user.setRole("COMPANY");
        user.setActive(true);
        user.setCompanyId(1);
        
        int userId = userDAO.insertUser(user);
        
        // When: Finding user
        User found = userDAO.getUserById(userId);
        
        // Then: Company ID should be set
        assertThat(found).isNotNull();
        assertThat(found.getCompanyId()).isEqualTo(1);
        assertThat(found.getRole()).isEqualTo("COMPANY");
    }
    
    @Test
    @DisplayName("Should insert multiple users")
    void testInsertMultipleUsers() throws SQLException {
        // Given: Multiple users
        User user1 = createTestUser("1RI21IS003", "Alice", "STUDENT");
        User user2 = createTestUser("1RI21IS004", "Bob", "STUDENT");
        User user3 = createTestUser("ADMIN001", "Admin", "ADMIN");
        
        // When: Inserting users
        int id1 = userDAO.insertUser(user1);
        int id2 = userDAO.insertUser(user2);
        int id3 = userDAO.insertUser(user3);
        
        // Then: All users should be inserted with unique IDs
        assertThat(id1).isGreaterThan(0);
        assertThat(id2).isGreaterThan(id1);
        assertThat(id3).isGreaterThan(id2);
        
        // And: All users should be retrievable
        assertThat(userDAO.getUserById(id1)).isNotNull();
        assertThat(userDAO.getUserById(id2)).isNotNull();
        assertThat(userDAO.getUserById(id3)).isNotNull();
    }
    
    private User createTestUser(String usn, String name, String role) {
        User user = new User();
        user.setUsn(usn);
        user.setName(name);
        user.setPasswordHash("password");
        user.setRole(role);
        user.setActive(true);
        return user;
    }
}
