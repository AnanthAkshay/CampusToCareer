package com.rit.placement.factory;

import com.rit.placement.dao.*;

/**
 * Factory for providing singleton instances of DAOs.
 * Enables easier mocking for unit testing and decouples DAO construction
 * from servlet lifecycles.
 */
public class DAOFactory {
    private static DAOFactory instance;
    
    private UserDAO userDAO;
    private StudentDAO studentDAO;
    private JobPostingDAO jobPostingDAO;
    private ApplicationDAO applicationDAO;
    private InterviewDAO interviewDAO;
    private CompanyDAO companyDAO;
    private ProctorDAO proctorDAO;
    private AnalyticsDAO analyticsDAO;
    private DocumentDAO documentDAO;
    private NotificationDAO notificationDAO;
    private CompanyRequestDAO companyRequestDAO;
    private AcademicDAO academicDAO;

    private DAOFactory() {
        this.userDAO = new UserDAO();
        this.studentDAO = new StudentDAO();
        this.jobPostingDAO = new JobPostingDAO();
        this.applicationDAO = new ApplicationDAO();
        this.interviewDAO = new InterviewDAO();
        this.companyDAO = new CompanyDAO();
        this.proctorDAO = new ProctorDAO();
        this.analyticsDAO = new AnalyticsDAO();
        this.documentDAO = new DocumentDAO();
        this.notificationDAO = new NotificationDAO();
        this.companyRequestDAO = new CompanyRequestDAO();
        this.academicDAO = new AcademicDAO();
    }

    public static synchronized DAOFactory getInstance() {
        if (instance == null) {
            instance = new DAOFactory();
        }
        return instance;
    }

    // For testing/mocking
    public static synchronized void setInstance(DAOFactory mockFactory) {
        instance = mockFactory;
    }

    public UserDAO getUserDAO() { return userDAO; }
    public StudentDAO getStudentDAO() { return studentDAO; }
    public JobPostingDAO getJobPostingDAO() { return jobPostingDAO; }
    public ApplicationDAO getApplicationDAO() { return applicationDAO; }
    public InterviewDAO getInterviewDAO() { return interviewDAO; }
    public CompanyDAO getCompanyDAO() { return companyDAO; }
    public ProctorDAO getProctorDAO() { return proctorDAO; }
    public AnalyticsDAO getAnalyticsDAO() { return analyticsDAO; }
    public DocumentDAO getDocumentDAO() { return documentDAO; }
    public NotificationDAO getNotificationDAO() { return notificationDAO; }
    public CompanyRequestDAO getCompanyRequestDAO() { return companyRequestDAO; }
    public AcademicDAO getAcademicDAO() { return academicDAO; }
}
