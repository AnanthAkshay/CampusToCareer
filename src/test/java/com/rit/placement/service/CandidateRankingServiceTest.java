package com.rit.placement.service;

import com.rit.placement.model.JobPosting;
import com.rit.placement.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for CandidateRankingService scoring logic
 */
@DisplayName("CandidateRankingService Tests")
class CandidateRankingServiceTest {
    
    private CandidateRankingService service;
    
    @BeforeEach
    void setUp() {
        service = new CandidateRankingService();
    }
    
    @ParameterizedTest
    @CsvSource({
        "10.0, 40",  // Perfect CGPA
        "9.0, 36",   // 9.0 CGPA
        "8.0, 32",   // 8.0 CGPA
        "7.5, 30",   // 7.5 CGPA
        "7.0, 28",   // 7.0 CGPA
        "6.0, 24",   // 6.0 CGPA
        "5.0, 20",   // 5.0 CGPA
        "0.0, 0"     // 0.0 CGPA
    })
    @DisplayName("Should calculate normalized CGPA score")
    void testCGPAScore_Normalized(double cgpa, int expectedScore) throws Exception {
        // When: Calculating CGPA score
        int score = calculateCGPAScore(cgpa);
        
        // Then: Should match expected normalized score
        assertThat(score).isEqualTo(expectedScore);
    }
    
    @Test
    @DisplayName("Should calculate CGPA score - formula verification")
    void testCGPAScore_FormulaVerification() throws Exception {
        // Given: CGPA of 8.5
        double cgpa = 8.5;
        
        // When: Calculating score
        int score = calculateCGPAScore(cgpa);
        
        // Then: Should be (8.5 / 10) * 40 = 34
        assertThat(score).isEqualTo(34);
    }
    
    @Test
    @DisplayName("Should calculate skills score - perfect match")
    void testSkillsScore_PerfectMatch() throws Exception {
        // Given: Candidate has all required skills
        JobPosting job = createJobWithSkills("Java, Spring, SQL");
        Student student = createStudentWithSkills("Java, Spring, SQL");
        
        // When: Calculating skills score
        int score = calculateSkillsScore(job, student);
        
        // Then: Should get full 40 points
        assertThat(score).isEqualTo(40);
    }
    
    @Test
    @DisplayName("Should calculate skills score - 50% match")
    void testSkillsScore_HalfMatch() throws Exception {
        // Given: Candidate has 1 out of 2 required skills
        JobPosting job = createJobWithSkills("Java, Spring");
        Student student = createStudentWithSkills("Java, Python");
        
        // When: Calculating skills score
        int score = calculateSkillsScore(job, student);
        
        // Then: Should get 50% of 40 = 20 points
        assertThat(score).isEqualTo(20);
    }
    
    @Test
    @DisplayName("Should calculate skills score - 33% match")
    void testSkillsScore_OneThirdMatch() throws Exception {
        // Given: Candidate has 1 out of 3 required skills
        JobPosting job = createJobWithSkills("Java, Spring, SQL");
        Student student = createStudentWithSkills("Java, Python, Django");
        
        // When: Calculating skills score
        int score = calculateSkillsScore(job, student);
        
        // Then: Should get 33% of 40 = 13 points (rounded)
        assertThat(score).isEqualTo(13);
    }
    
    @ParameterizedTest
    @CsvSource({
        "10, 20",   // >= 10 applications
        "15, 20",   // >= 10 applications
        "9, 15",    // 5-9 applications
        "5, 15",    // 5-9 applications
        "4, 10",    // 1-4 applications
        "1, 10",    // 1-4 applications
        "0, 0"      // 0 applications
    })
    @DisplayName("Should calculate activity score based on application count")
    void testActivityScore(int applicationCount, int expectedScore) throws Exception {
        // When: Calculating activity score
        int score = calculateActivityScore(applicationCount);
        
        // Then: Should match expected score
        assertThat(score).isEqualTo(expectedScore);
    }
    
    @Test
    @DisplayName("Should calculate activity score - high activity")
    void testActivityScore_HighActivity() throws Exception {
        // Given: Candidate with 12 applications
        int applicationCount = 12;
        
        // When: Calculating activity score
        int score = calculateActivityScore(applicationCount);
        
        // Then: Should get 20 points
        assertThat(score).isEqualTo(20);
    }
    
    @Test
    @DisplayName("Should calculate activity score - moderate activity")
    void testActivityScore_ModerateActivity() throws Exception {
        // Given: Candidate with 7 applications
        int applicationCount = 7;
        
        // When: Calculating activity score
        int score = calculateActivityScore(applicationCount);
        
        // Then: Should get 15 points
        assertThat(score).isEqualTo(15);
    }
    
    @Test
    @DisplayName("Should calculate activity score - low activity")
    void testActivityScore_LowActivity() throws Exception {
        // Given: Candidate with 3 applications
        int applicationCount = 3;
        
        // When: Calculating activity score
        int score = calculateActivityScore(applicationCount);
        
        // Then: Should get 10 points
        assertThat(score).isEqualTo(10);
    }
    
    @Test
    @DisplayName("Should calculate activity score - no activity")
    void testActivityScore_NoActivity() throws Exception {
        // Given: Candidate with 0 applications
        int applicationCount = 0;
        
        // When: Calculating activity score
        int score = calculateActivityScore(applicationCount);
        
        // Then: Should get 0 points
        assertThat(score).isEqualTo(0);
    }
    
    @Test
    @DisplayName("Should produce consistent scores for same input")
    void testConsistency() throws Exception {
        // Given: Same candidate data
        double cgpa = 8.0;
        JobPosting job = createJobWithSkills("Java, Spring");
        Student student = createStudentWithSkills("Java, Spring");
        int applicationCount = 7;
        
        // When: Calculating score multiple times
        int score1 = calculateCGPAScore(cgpa) + 
                     calculateSkillsScore(job, student) + 
                     calculateActivityScore(applicationCount);
        
        int score2 = calculateCGPAScore(cgpa) + 
                     calculateSkillsScore(job, student) + 
                     calculateActivityScore(applicationCount);
        
        int score3 = calculateCGPAScore(cgpa) + 
                     calculateSkillsScore(job, student) + 
                     calculateActivityScore(applicationCount);
        
        // Then: All scores should be identical
        assertThat(score1).isEqualTo(score2).isEqualTo(score3);
        
        // And: Total should be 32 + 40 + 15 = 87
        assertThat(score1).isEqualTo(87);
    }
    
    @Test
    @DisplayName("Should calculate total score correctly")
    void testTotalScore() throws Exception {
        // Given: Candidate with CGPA 9.0, all skills match, 10 applications
        double cgpa = 9.0;
        JobPosting job = createJobWithSkills("Java, Spring, SQL");
        Student student = createStudentWithSkills("Java, Spring, SQL, React");
        int applicationCount = 10;
        
        // When: Calculating total score
        int cgpaScore = calculateCGPAScore(cgpa);
        int skillsScore = calculateSkillsScore(job, student);
        int activityScore = calculateActivityScore(applicationCount);
        int totalScore = cgpaScore + skillsScore + activityScore;
        
        // Then: Should be 36 + 40 + 20 = 96
        assertThat(cgpaScore).isEqualTo(36);
        assertThat(skillsScore).isEqualTo(40);
        assertThat(activityScore).isEqualTo(20);
        assertThat(totalScore).isEqualTo(96);
    }
    
    // Helper methods to access private methods via reflection
    private int calculateCGPAScore(double cgpa) throws Exception {
        Method method = CandidateRankingService.class.getDeclaredMethod(
            "calculateCGPAScore", double.class);
        method.setAccessible(true);
        return (int) method.invoke(service, cgpa);
    }
    
    private int calculateSkillsScore(JobPosting job, Student student) throws Exception {
        Method method = CandidateRankingService.class.getDeclaredMethod(
            "calculateSkillsScore", JobPosting.class, Student.class);
        method.setAccessible(true);
        return (int) method.invoke(service, job, student);
    }
    
    private int calculateActivityScore(int applicationCount) throws Exception {
        Method method = CandidateRankingService.class.getDeclaredMethod(
            "calculateActivityScore", int.class);
        method.setAccessible(true);
        return (int) method.invoke(service, applicationCount);
    }
    
    // Helper methods to create test objects
    private JobPosting createJobWithSkills(String skills) {
        JobPosting job = new JobPosting();
        job.setRequiredSkills(skills);
        return job;
    }
    
    private Student createStudentWithSkills(String skills) {
        Student student = new Student();
        student.setSkills(skills);
        return student;
    }
}
