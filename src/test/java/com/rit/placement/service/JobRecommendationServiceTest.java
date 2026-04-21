package com.rit.placement.service;

import com.rit.placement.model.JobPosting;
import com.rit.placement.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.lang.reflect.Method;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for JobRecommendationService scoring logic
 */
@DisplayName("JobRecommendationService Tests")
class JobRecommendationServiceTest {
    
    private JobRecommendationService service;
    
    @BeforeEach
    void setUp() {
        service = new JobRecommendationService();
    }
    
    @Test
    @DisplayName("Should calculate CGPA score correctly - meets requirement")
    void testCGPAScore_MeetsRequirement() throws Exception {
        // Given: Student CGPA meets job requirement
        JobPosting job = createJob(7.0);
        double studentCGPA = 8.0;
        
        // When: Calculating CGPA score
        int score = calculateCGPAScore(job, studentCGPA);
        
        // Then: Should get full 40 points
        assertThat(score).isEqualTo(40);
    }
    
    @Test
    @DisplayName("Should calculate CGPA score correctly - below requirement")
    void testCGPAScore_BelowRequirement() throws Exception {
        // Given: Student CGPA below job requirement
        JobPosting job = createJob(8.0);
        double studentCGPA = 7.0;
        
        // When: Calculating CGPA score
        int score = calculateCGPAScore(job, studentCGPA);
        
        // Then: Should get 0 points
        assertThat(score).isEqualTo(0);
    }
    
    @Test
    @DisplayName("Should calculate CGPA score correctly - no requirement")
    void testCGPAScore_NoRequirement() throws Exception {
        // Given: Job has no CGPA requirement
        JobPosting job = createJob(null);
        double studentCGPA = 7.5;
        
        // When: Calculating CGPA score
        int score = calculateCGPAScore(job, studentCGPA);
        
        // Then: Should get full 40 points
        assertThat(score).isEqualTo(40);
    }
    
    @ParameterizedTest
    @CsvSource({
        "7.0, 7.0, 40",   // Exactly meets
        "7.0, 8.0, 40",   // Exceeds
        "7.0, 10.0, 40",  // Far exceeds
        "8.0, 7.5, 0",    // Below
        "8.0, 7.9, 0"     // Just below
    })
    @DisplayName("Should calculate CGPA score for various scenarios")
    void testCGPAScore_ParameterizedTest(double minCGPA, double studentCGPA, int expectedScore) throws Exception {
        // Given: Job with min CGPA and student with CGPA
        JobPosting job = createJob(minCGPA);
        
        // When: Calculating score
        int score = calculateCGPAScore(job, studentCGPA);
        
        // Then: Should match expected score
        assertThat(score).isEqualTo(expectedScore);
    }
    
    @Test
    @DisplayName("Should calculate skills score - perfect match")
    void testSkillsScore_PerfectMatch() throws Exception {
        // Given: Student has all required skills
        JobPosting job = createJobWithSkills("Java, Spring, SQL");
        Student student = createStudentWithSkills("Java, Spring, SQL, React");
        
        // When: Calculating skills score
        int score = calculateSkillsScore(job, student);
        
        // Then: Should get full 40 points
        assertThat(score).isEqualTo(40);
    }
    
    @Test
    @DisplayName("Should calculate skills score - partial match")
    void testSkillsScore_PartialMatch() throws Exception {
        // Given: Student has 2 out of 3 required skills
        JobPosting job = createJobWithSkills("Java, Spring, SQL");
        Student student = createStudentWithSkills("Java, Spring, Python");
        
        // When: Calculating skills score
        int score = calculateSkillsScore(job, student);
        
        // Then: Should get 2/3 * 40 = 27 points (rounded)
        assertThat(score).isEqualTo(27);
    }
    
    @Test
    @DisplayName("Should calculate skills score - no match")
    void testSkillsScore_NoMatch() throws Exception {
        // Given: Student has no required skills
        JobPosting job = createJobWithSkills("Java, Spring, SQL");
        Student student = createStudentWithSkills("Python, Django, Flask");
        
        // When: Calculating skills score
        int score = calculateSkillsScore(job, student);
        
        // Then: Should get 0 points
        assertThat(score).isEqualTo(0);
    }
    
    @Test
    @DisplayName("Should calculate skills score - no skills specified")
    void testSkillsScore_NoSkillsSpecified() throws Exception {
        // Given: Job has no skills requirement
        JobPosting job = createJobWithSkills(null);
        Student student = createStudentWithSkills("Java, Python");
        
        // When: Calculating skills score
        int score = calculateSkillsScore(job, student);
        
        // Then: Should get default 20 points
        assertThat(score).isEqualTo(20);
    }
    
    @Test
    @DisplayName("Should calculate skills score - student has no skills")
    void testSkillsScore_StudentNoSkills() throws Exception {
        // Given: Student has no skills listed
        JobPosting job = createJobWithSkills("Java, Spring");
        Student student = createStudentWithSkills(null);
        
        // When: Calculating skills score
        int score = calculateSkillsScore(job, student);
        
        // Then: Should get 0 points
        assertThat(score).isEqualTo(0);
    }
    
    @Test
    @DisplayName("Should calculate eligibility score - eligible")
    void testEligibilityScore_Eligible() throws Exception {
        // Given: Student is eligible (CGPA + branch)
        JobPosting job = createJobWithBranch("CSE, ISE", 7.0);
        Student student = createStudentWithBranch("ISE");
        double cgpa = 8.0;
        
        // When: Calculating eligibility score
        int score = calculateEligibilityScore(job, student, cgpa);
        
        // Then: Should get 20 points
        assertThat(score).isEqualTo(20);
    }
    
    @Test
    @DisplayName("Should calculate eligibility score - not eligible (CGPA)")
    void testEligibilityScore_NotEligibleCGPA() throws Exception {
        // Given: Student doesn't meet CGPA requirement
        JobPosting job = createJobWithBranch("ISE", 8.0);
        Student student = createStudentWithBranch("ISE");
        double cgpa = 7.0;
        
        // When: Calculating eligibility score
        int score = calculateEligibilityScore(job, student, cgpa);
        
        // Then: Should get 0 points
        assertThat(score).isEqualTo(0);
    }
    
    @Test
    @DisplayName("Should calculate eligibility score - not eligible (branch)")
    void testEligibilityScore_NotEligibleBranch() throws Exception {
        // Given: Student's branch not allowed
        JobPosting job = createJobWithBranch("CSE", 7.0);
        Student student = createStudentWithBranch("ECE");
        double cgpa = 8.0;
        
        // When: Calculating eligibility score
        int score = calculateEligibilityScore(job, student, cgpa);
        
        // Then: Should get 0 points
        assertThat(score).isEqualTo(0);
    }
    
    // Helper methods to access private methods via reflection
    private int calculateCGPAScore(JobPosting job, double cgpa) throws Exception {
        Method method = JobRecommendationService.class.getDeclaredMethod(
            "calculateCGPAScore", JobPosting.class, double.class);
        method.setAccessible(true);
        return (int) method.invoke(service, job, cgpa);
    }
    
    private int calculateSkillsScore(JobPosting job, Student student) throws Exception {
        Method method = JobRecommendationService.class.getDeclaredMethod(
            "calculateSkillsScore", JobPosting.class, Student.class);
        method.setAccessible(true);
        return (int) method.invoke(service, job, student);
    }
    
    private int calculateEligibilityScore(JobPosting job, Student student, double cgpa) throws Exception {
        Method method = JobRecommendationService.class.getDeclaredMethod(
            "calculateEligibilityScore", JobPosting.class, Student.class, double.class);
        method.setAccessible(true);
        return (int) method.invoke(service, job, student, cgpa);
    }
    
    // Helper methods to create test objects
    private JobPosting createJob(Double minCGPA) {
        JobPosting job = new JobPosting();
        if (minCGPA != null) {
            job.setMinCgpa(BigDecimal.valueOf(minCGPA));
        }
        return job;
    }
    
    private JobPosting createJobWithSkills(String skills) {
        JobPosting job = new JobPosting();
        job.setRequiredSkills(skills);
        return job;
    }
    
    private JobPosting createJobWithBranch(String branches, double minCGPA) {
        JobPosting job = new JobPosting();
        job.setAllowedBranches(branches);
        job.setMinCgpa(BigDecimal.valueOf(minCGPA));
        return job;
    }
    
    private Student createStudentWithSkills(String skills) {
        Student student = new Student();
        student.setSkills(skills);
        return student;
    }
    
    private Student createStudentWithBranch(String branch) {
        Student student = new Student();
        student.setBranch(branch);
        return student;
    }
}
