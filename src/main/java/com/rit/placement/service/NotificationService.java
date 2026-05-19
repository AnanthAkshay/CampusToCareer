package com.rit.placement.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rit.placement.factory.DAOFactory;

import com.rit.placement.dao.NotificationDAO;
import com.rit.placement.dao.UserDAO;
import com.rit.placement.model.Notification;
import com.rit.placement.model.User;
import com.rit.placement.util.EmailUtil;

/**
 * Enhanced Notification Service
 * Handles both in-app and email notifications
 */
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    private final NotificationDAO notificationDAO = DAOFactory.getInstance().getNotificationDAO();
    private final UserDAO userDAO = DAOFactory.getInstance().getUserDAO();
    private final EmailUtil emailUtil = new EmailUtil();
    
    /**
     * Notify student when they apply for a job
     */
    public void notifyJobApplication(int studentId, String companyName, String jobTitle, int applicationId) {
        try {
            // In-app notification
            Notification notification = new Notification();
            notification.setUserId(studentId);
            notification.setType("APPLICATION");
            notification.setRelatedId(applicationId);
            notification.setTitle("✅ Application Submitted");
            notification.setMessage(String.format("Your application for %s at %s has been successfully submitted. " +
                "You will be notified when the company reviews your application.", jobTitle, companyName));
            
            notificationDAO.createNotification(notification);
            
            // Email notification
            User user = userDAO.getUserById(studentId);
            if (user != null && user.getEmail() != null) {
                String subject = "Application Submitted - " + jobTitle;
                String body = String.format(
                    "Dear %s,\n\n" +
                    "Your application for %s at %s has been successfully submitted.\n\n" +
                    "Application Details:\n" +
                    "- Position: %s\n" +
                    "- Company: %s\n" +
                    "- Status: Under Review\n\n" +
                    "You will receive updates as your application progresses.\n\n" +
                    "Best regards,\n" +
                    "RIT Placement Team",
                    user.getName(), jobTitle, companyName, jobTitle, companyName
                );
                emailUtil.sendEmail(user.getEmail(), subject, body);
            }
            
        } catch (Exception e) {
            logger.error("Error creating application notification: " + e.getMessage());
            logger.error("Exception occurred: ", e);
        }
    }
    
    /**
     * Notify student when application status changes
     */
    public void notifyStatusChange(int studentId, String companyName, String jobTitle, 
                                   String oldStatus, String newStatus, int applicationId) {
        try {
            Notification notification = new Notification();
            notification.setUserId(studentId);
            notification.setType("STATUS_UPDATE");
            notification.setRelatedId(applicationId);
            
            String title = "Application Status Updated";
            String message = String.format("Your application for %s at %s has been updated from %s to %s",
                jobTitle, companyName, oldStatus, newStatus);
            
            notification.setTitle(title);
            notification.setMessage(message);
            
            notificationDAO.createNotification(notification);
            
            // Email notification
            User user = userDAO.getUserById(studentId);
            if (user != null && user.getEmail() != null) {
                String subject = "Application Status Update - " + jobTitle;
                String body = String.format(
                    "Dear %s,\n\n" +
                    "Your application status has been updated.\n\n" +
                    "Position: %s\n" +
                    "Company: %s\n" +
                    "Previous Status: %s\n" +
                    "New Status: %s\n\n" +
                    "Please check your dashboard for more details.\n\n" +
                    "Best regards,\n" +
                    "RIT Placement Team",
                    user.getName(), jobTitle, companyName, oldStatus, newStatus
                );
                emailUtil.sendEmail(user.getEmail(), subject, body);
            }
            
        } catch (Exception e) {
            logger.error("Error creating status change notification: " + e.getMessage());
            logger.error("Exception occurred: ", e);
        }
    }
    
    /**
     * Notify student when shortlisted
     */
    public void notifyShortlisted(int studentId, String companyName, String jobTitle, int applicationId) {
        try {
            Notification notification = new Notification();
            notification.setUserId(studentId);
            notification.setType("STATUS_UPDATE");
            notification.setRelatedId(applicationId);
            notification.setTitle("🎉 You've been Shortlisted!");
            notification.setMessage(String.format("Congratulations! You have been shortlisted for %s at %s. " +
                "The company will contact you soon for the next steps.", jobTitle, companyName));
            
            notificationDAO.createNotification(notification);
            
            // Email notification
            User user = userDAO.getUserById(studentId);
            if (user != null && user.getEmail() != null) {
                String subject = "🎉 Congratulations! You've been Shortlisted - " + jobTitle;
                String body = String.format(
                    "Dear %s,\n\n" +
                    "Congratulations! We have great news for you.\n\n" +
                    "You have been SHORTLISTED for:\n" +
                    "Position: %s\n" +
                    "Company: %s\n\n" +
                    "This means your profile has impressed the company and you're moving forward in the selection process.\n\n" +
                    "Next Steps:\n" +
                    "- The company will contact you soon for the next round\n" +
                    "- Keep checking your dashboard for updates\n" +
                    "- Prepare for potential interviews\n\n" +
                    "Best of luck!\n\n" +
                    "Best regards,\n" +
                    "RIT Placement Team",
                    user.getName(), jobTitle, companyName
                );
                emailUtil.sendEmail(user.getEmail(), subject, body);
            }
            
        } catch (Exception e) {
            logger.error("Error creating shortlist notification: " + e.getMessage());
            logger.error("Exception occurred: ", e);
        }
    }
    
    /**
     * Notify student when rejected
     */
    public void notifyRejected(int studentId, String companyName, String jobTitle, int applicationId) {
        try {
            Notification notification = new Notification();
            notification.setUserId(studentId);
            notification.setType("STATUS_UPDATE");
            notification.setRelatedId(applicationId);
            notification.setTitle("Application Update");
            notification.setMessage(String.format("Thank you for your interest in %s at %s. " +
                "Unfortunately, we are unable to proceed with your application at this time. " +
                "We encourage you to apply for other opportunities.", jobTitle, companyName));
            
            notificationDAO.createNotification(notification);
            
            // Email notification
            User user = userDAO.getUserById(studentId);
            if (user != null && user.getEmail() != null) {
                String subject = "Application Update - " + jobTitle;
                String body = String.format(
                    "Dear %s,\n\n" +
                    "Thank you for your interest in the %s position at %s.\n\n" +
                    "After careful consideration, we regret to inform you that we are unable to proceed with your application at this time.\n\n" +
                    "We encourage you to:\n" +
                    "- Continue applying for other opportunities\n" +
                    "- Update your skills and profile\n" +
                    "- Stay positive and keep trying\n\n" +
                    "Remember, this is just one opportunity among many. Your perfect role is out there!\n\n" +
                    "Best regards,\n" +
                    "RIT Placement Team",
                    user.getName(), jobTitle, companyName
                );
                emailUtil.sendEmail(user.getEmail(), subject, body);
            }
            
        } catch (Exception e) {
            logger.error("Error creating rejection notification: " + e.getMessage());
            logger.error("Exception occurred: ", e);
        }
    }
    
    /**
     * Notify student when selected
     */
    public void notifySelected(int studentId, String companyName, String jobTitle, int applicationId) {
        try {
            Notification notification = new Notification();
            notification.setUserId(studentId);
            notification.setType("STATUS_UPDATE");
            notification.setRelatedId(applicationId);
            notification.setTitle("🎊 Congratulations! You're Selected!");
            notification.setMessage(String.format("Excellent news! You have been selected for %s at %s. " +
                "The company will reach out to you with the offer details soon.", jobTitle, companyName));
            
            notificationDAO.createNotification(notification);
            
            // Email notification
            User user = userDAO.getUserById(studentId);
            if (user != null && user.getEmail() != null) {
                String subject = "🎊 Congratulations! You're SELECTED - " + jobTitle;
                String body = String.format(
                    "Dear %s,\n\n" +
                    "🎊 CONGRATULATIONS! 🎊\n\n" +
                    "We are thrilled to inform you that you have been SELECTED for:\n\n" +
                    "Position: %s\n" +
                    "Company: %s\n\n" +
                    "This is a significant achievement and we're incredibly proud of you!\n\n" +
                    "Next Steps:\n" +
                    "- The company will contact you with offer details\n" +
                    "- Review the offer carefully\n" +
                    "- Complete any required documentation\n" +
                    "- Celebrate your success!\n\n" +
                    "Once again, congratulations on this wonderful achievement!\n\n" +
                    "Best regards,\n" +
                    "RIT Placement Team",
                    user.getName(), jobTitle, companyName
                );
                emailUtil.sendEmail(user.getEmail(), subject, body);
            }
            
        } catch (Exception e) {
            logger.error("Error creating selection notification: " + e.getMessage());
            logger.error("Exception occurred: ", e);
        }
    }
    
    /**
     * Notify student when interview is scheduled
     */
    public void notifyInterviewScheduled(int studentId, String companyName, String jobTitle, 
                                        String interviewDate, String mode, int interviewId) {
        try {
            Notification notification = new Notification();
            notification.setUserId(studentId);
            notification.setType("INTERVIEW");
            notification.setRelatedId(interviewId);
            notification.setTitle("📅 Interview Scheduled");
            notification.setMessage(String.format("Your interview for %s at %s has been scheduled for %s (%s mode). " +
                "Please check your dashboard for details.", jobTitle, companyName, interviewDate, mode));
            
            notificationDAO.createNotification(notification);
            
            // Email notification
            User user = userDAO.getUserById(studentId);
            if (user != null && user.getEmail() != null) {
                String subject = "📅 Interview Scheduled - " + jobTitle;
                String body = String.format(
                    "Dear %s,\n\n" +
                    "Your interview has been scheduled!\n\n" +
                    "Interview Details:\n" +
                    "Position: %s\n" +
                    "Company: %s\n" +
                    "Date & Time: %s\n" +
                    "Mode: %s\n\n" +
                    "Preparation Tips:\n" +
                    "- Review the job description\n" +
                    "- Research the company\n" +
                    "- Prepare your introduction\n" +
                    "- Test your internet connection (if online)\n" +
                    "- Dress professionally\n" +
                    "- Be on time\n\n" +
                    "Check your dashboard for complete interview details.\n\n" +
                    "Best of luck!\n\n" +
                    "Best regards,\n" +
                    "RIT Placement Team",
                    user.getName(), jobTitle, companyName, interviewDate, mode
                );
                emailUtil.sendEmail(user.getEmail(), subject, body);
            }
            
        } catch (Exception e) {
            logger.error("Error creating interview notification: " + e.getMessage());
            logger.error("Exception occurred: ", e);
        }
    }
    
    /**
     * Notify students when a new job is posted (individual notification)
     */
    public void notifyNewJobPosted(int studentId, String jobTitle, String companyName, int jobId) {
        try {
            // In-app notification
            Notification notification = new Notification();
            notification.setUserId(studentId);
            notification.setType("JOB_POSTED");
            notification.setRelatedId(jobId);
            notification.setTitle("🆕 New Job Opportunity");
            notification.setMessage(String.format("A new position for %s has been posted by %s. You are eligible to apply!", 
                jobTitle, companyName));
            
            notificationDAO.createNotification(notification);
            
            // Email notification
            User user = userDAO.getUserById(studentId);
            if (user != null && user.getEmail() != null) {
                String subject = "New Job Opportunity - " + jobTitle;
                String body = String.format(
                    "Dear %s,\n\n" +
                    "Great news! A new job opportunity matching your profile has been posted.\n\n" +
                    "Position: %s\n" +
                    "Company: %s\n\n" +
                    "You are eligible to apply for this position. Login to your dashboard to view details and submit your application.\n\n" +
                    "Don't miss this opportunity!\n\n" +
                    "Best regards,\n" +
                    "RIT Placement Team",
                    user.getName(), jobTitle, companyName
                );
                emailUtil.sendEmail(user.getEmail(), subject, body);
            }
            
        } catch (Exception e) {
            logger.error("Error creating job posted notification for student " + studentId + ": " + e.getMessage());
            logger.error("Exception occurred: ", e);
        }
    }
    
    /**
     * Notify students when a new job is posted (system-wide notification - deprecated)
     */
    public void notifyNewJobPosted(String jobTitle, String companyName, int jobId) {
        try {
            // This would typically notify all eligible students
            // For now, we'll create a system-wide notification
            Notification notification = new Notification();
            notification.setUserId(0); // System notification
            notification.setType("JOB_POSTED");
            notification.setRelatedId(jobId);
            notification.setTitle("🆕 New Job Posted");
            notification.setMessage(String.format("A new position for %s has been posted by %s. Check it out now!", 
                jobTitle, companyName));
            
            notificationDAO.createNotification(notification);
            
            System.out.println("New job notification created: " + jobTitle + " at " + companyName);
            
        } catch (Exception e) {
            logger.error("Error creating job posted notification: " + e.getMessage());
            logger.error("Exception occurred: ", e);
        }
    }
    
    /**
     * Notify company when student applies for their job
     */
    public void notifyCompanyOfApplication(int companyId, String studentName, String jobTitle, int applicationId) {
        try {
            // In-app notification for company
            Notification notification = new Notification();
            notification.setUserId(companyId);
            notification.setType("APPLICATION");
            notification.setRelatedId(applicationId);
            notification.setTitle("📩 New Application Received");
            notification.setMessage(String.format("%s has applied for the %s position. Review their profile now.", 
                studentName, jobTitle));
            
            notificationDAO.createNotification(notification);
            
            // Email notification
            User user = userDAO.getUserById(companyId);
            if (user != null && user.getEmail() != null) {
                String subject = "New Application - " + jobTitle;
                String body = String.format(
                    "Dear Recruiter,\n\n" +
                    "You have received a new application for the %s position.\n\n" +
                    "Applicant: %s\n" +
                    "Position: %s\n\n" +
                    "Login to your dashboard to review the candidate's profile and take action.\n\n" +
                    "Best regards,\n" +
                    "RIT Placement Team",
                    jobTitle, studentName, jobTitle
                );
                emailUtil.sendEmail(user.getEmail(), subject, body);
            }
            
        } catch (Exception e) {
            logger.error("Error creating company application notification: " + e.getMessage());
            logger.error("Exception occurred: ", e);
        }
    }
}