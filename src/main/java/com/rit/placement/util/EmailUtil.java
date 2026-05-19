package com.rit.placement.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

/**
 * Email Utility for sending OTP emails via Gmail SMTP
 * 
 * CONFIGURATION:
 * - Uses Gmail SMTP server
 * - Requires App Password (not regular Gmail password)
 * - TLS encryption enabled
 */
public class EmailUtil {
    private static final Logger logger = LoggerFactory.getLogger(EmailUtil.class);

    // Gmail SMTP Configuration - Using Environment Variables for Security
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String SMTP_USERNAME = System.getenv("SMTP_USERNAME");
    private static final String SMTP_PASSWORD = System.getenv("SMTP_PASSWORD");
    private static final String FROM_EMAIL = System.getenv("SMTP_FROM_EMAIL");
    private static final String FROM_NAME = "RIT Placement Portal";

    /**
     * Send OTP email to user
     * 
     * @param toEmail Recipient email address
     * @param otp 6-digit OTP code
     * @param userName User's name for personalization
     * @return true if email sent successfully, false otherwise
     */
    public static boolean sendOTPEmail(String toEmail, String otp, String userName) {
        // DEV MODE: If SMTP not configured, log OTP to console instead
        if (SMTP_USERNAME == null || SMTP_PASSWORD == null || FROM_EMAIL == null || 
            SMTP_USERNAME.isEmpty() || SMTP_PASSWORD.isEmpty() || FROM_EMAIL.isEmpty()) {
            System.out.println("========================================");
            System.out.println("📧 DEV MODE: Email sending disabled");
            System.out.println("========================================");
            System.out.println("To: " + toEmail);
            System.out.println("User: " + userName);
            System.out.println("OTP: " + otp);
            System.out.println("========================================");
            System.out.println("⚠️  SMTP credentials not configured.");
            System.out.println("   Set environment variables to enable email:");
            System.out.println("   SMTP_USERNAME, SMTP_PASSWORD, SMTP_FROM_EMAIL");
            System.out.println("========================================");
            // Return true in dev mode so login can proceed
            return true;
        }

        try {
            // Create email session with authentication
            Session session = createEmailSession();

            // Create email message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL, FROM_NAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Your OTP for RIT Placement Portal Login");

            // Create HTML email body
            String htmlContent = createOTPEmailTemplate(otp, userName);
            message.setContent(htmlContent, "text/html; charset=utf-8");

            // Send email
            Transport.send(message);
            
            System.out.println("OTP email sent successfully to: " + toEmail);
            return true;

        } catch (Exception e) {
            logger.error("Failed to send OTP email: " + e.getMessage());
            logger.error("Exception occurred: ", e);
            return false;
        }
    }

    /**
     * Create email session with Gmail SMTP authentication
     */
    private static Session createEmailSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.ssl.trust", SMTP_HOST);

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USERNAME, SMTP_PASSWORD);
            }
        });
    }

    /**
     * Create professional HTML email template for OTP
     */
    private static String createOTPEmailTemplate(String otp, String userName) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta charset='UTF-8'>" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "</head>" +
                "<body style='margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f4f4f4;'>" +
                "    <table width='100%' cellpadding='0' cellspacing='0' style='background-color: #f4f4f4; padding: 20px;'>" +
                "        <tr>" +
                "            <td align='center'>" +
                "                <table width='600' cellpadding='0' cellspacing='0' style='background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>" +
                "                    <!-- Header -->" +
                "                    <tr>" +
                "                        <td style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 30px; text-align: center;'>" +
                "                            <h1 style='color: #ffffff; margin: 0; font-size: 24px;'>🎓 RIT Placement Portal</h1>" +
                "                        </td>" +
                "                    </tr>" +
                "                    <!-- Content -->" +
                "                    <tr>" +
                "                        <td style='padding: 40px 30px;'>" +
                "                            <h2 style='color: #333333; margin: 0 0 20px; font-size: 20px;'>Hello " + userName + ",</h2>" +
                "                            <p style='color: #666666; line-height: 1.6; margin: 0 0 20px;'>" +
                "                                You requested to login to your RIT Placement Portal account. Use the OTP below to complete your login:" +
                "                            </p>" +
                "                            <!-- OTP Box -->" +
                "                            <div style='background-color: #f8f9fa; border: 2px dashed #667eea; border-radius: 8px; padding: 20px; text-align: center; margin: 30px 0;'>" +
                "                                <p style='color: #666666; margin: 0 0 10px; font-size: 14px; text-transform: uppercase; letter-spacing: 1px;'>Your OTP Code</p>" +
                "                                <h1 style='color: #667eea; margin: 0; font-size: 36px; letter-spacing: 8px; font-weight: bold;'>" + otp + "</h1>" +
                "                            </div>" +
                "                            <p style='color: #666666; line-height: 1.6; margin: 0 0 10px;'>" +
                "                                <strong>⏰ This OTP is valid for 5 minutes only.</strong>" +
                "                            </p>" +
                "                            <p style='color: #666666; line-height: 1.6; margin: 0 0 20px;'>" +
                "                                If you didn't request this OTP, please ignore this email or contact support if you have concerns." +
                "                            </p>" +
                "                            <!-- Security Notice -->" +
                "                            <div style='background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0;'>" +
                "                                <p style='color: #856404; margin: 0; font-size: 14px;'>" +
                "                                    <strong>🔒 Security Tip:</strong> Never share your OTP with anyone. RIT staff will never ask for your OTP." +
                "                                </p>" +
                "                            </div>" +
                "                        </td>" +
                "                    </tr>" +
                "                    <!-- Footer -->" +
                "                    <tr>" +
                "                        <td style='background-color: #f8f9fa; padding: 20px 30px; text-align: center; border-top: 1px solid #e9ecef;'>" +
                "                            <p style='color: #999999; margin: 0; font-size: 12px;'>" +
                "                                © 2026 RIT ISE Placement Portal. All rights reserved." +
                "                            </p>" +
                "                            <p style='color: #999999; margin: 10px 0 0; font-size: 12px;'>" +
                "                                This is an automated email. Please do not reply." +
                "                            </p>" +
                "                        </td>" +
                "                    </tr>" +
                "                </table>" +
                "            </td>" +
                "        </tr>" +
                "    </table>" +
                "</body>" +
                "</html>";
    }

    /**
     * Generate a random 6-digit OTP
     */
    public static String generateOTP() {
        java.security.SecureRandom sr = new java.security.SecureRandom();
        int otp = 100000 + sr.nextInt(900000);
        return String.valueOf(otp);
    }

    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }
    
    /**
     * Send generic email (for notifications)
     * 
     * @param toEmail Recipient email address
     * @param subject Email subject
     * @param body Email body (plain text or HTML)
     * @return true if email sent successfully, false otherwise
     */
    public boolean sendEmail(String toEmail, String subject, String body) {
        // Validate environment variables are set
        if (SMTP_USERNAME == null || SMTP_PASSWORD == null || FROM_EMAIL == null) {
            logger.error("ERROR: SMTP credentials not configured. Please set environment variables:");
            logger.error("  SMTP_USERNAME, SMTP_PASSWORD, SMTP_FROM_EMAIL");
            return false;
        }
        
        // Validate email
        if (!isValidEmail(toEmail)) {
            logger.error("ERROR: Invalid email address: " + toEmail);
            return false;
        }

        try {
            // Create email session with authentication
            Session session = createEmailSession();

            // Create email message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL, FROM_NAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);

            // Set body (detect if HTML or plain text)
            if (body.trim().startsWith("<") || body.contains("<html>")) {
                message.setContent(body, "text/html; charset=utf-8");
            } else {
                message.setText(body);
            }

            // Send email
            Transport.send(message);
            
            System.out.println("Email sent successfully to: " + toEmail);
            return true;

        } catch (Exception e) {
            logger.error("Failed to send email to " + toEmail + ": " + e.getMessage());
            logger.error("Exception occurred: ", e);
            
            // Log failure for retry/monitoring
            logEmailFailure(toEmail, subject, e.getMessage());
            return false;
        }
    }
    
    /**
     * Log email failures for monitoring and retry
     */
    private void logEmailFailure(String toEmail, String subject, String error) {
        // In production, this would write to a database or log file for retry queue
        logger.error("EMAIL FAILURE LOG:");
        logger.error("  To: " + toEmail);
        logger.error("  Subject: " + subject);
        logger.error("  Error: " + error);
        logger.error("  Timestamp: " + new java.util.Date());
    }
}
