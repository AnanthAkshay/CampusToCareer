package com.rit.placement;

import com.rit.placement.util.MockDBConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.sql.SQLException;

/**
 * Base test class with common setup and teardown
 */
public abstract class BaseTest {
    
    @BeforeEach
    public void setUp() throws SQLException {
        // Clear data before each test
        MockDBConnection.clearAllData();
        MockDBConnection.resetSequences();
    }
    
    @AfterEach
    public void tearDown() throws SQLException {
        // Clean up after each test
        MockDBConnection.clearAllData();
    }
}
