package com.springboot.Job.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendOwnerApprovalEmail(String toEmail, String companyName, String companyPhone,
    		String description, String city, String township) {
            
    	try {
			String encodedCompanyName = URLEncoder.encode(companyName != null ? companyName : "", StandardCharsets.UTF_8);
			String encodedPhone = URLEncoder.encode(companyPhone != null ? companyPhone : "", StandardCharsets.UTF_8);
			String encodedDescription = URLEncoder.encode(description != null ? description : "", StandardCharsets.UTF_8);
			String encodedCity = URLEncoder.encode(city != null ? city : "", StandardCharsets.UTF_8);
			String encodedTownship = URLEncoder.encode(township != null ? township : "", StandardCharsets.UTF_8);
			
			String registrationUrl = String.format(
			"http://localhost:8080/owner/register?email=%s&companyName=%s&phone=%s&description=%s&address=%s&township=%s",
			toEmail, encodedCompanyName, encodedPhone, encodedDescription, encodedCity, encodedTownship
			);
				
	
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(toEmail);
		message.setSubject("Company Registration Approved - JobJump");
		message.setText(
		"Dear " + companyName + ",\n\n" +
		"Congratulations! Your company registration request has been approved by our admin team.\n\n" +
		"Please complete your registration by clicking the link below:\n\n" +
		registrationUrl + "\n\n" +
		"Your company information has been pre-filled for your convenience:\n" +
		"• Company Name: " + (companyName != null ? companyName : "To be completed") + "\n" +
		"• Email: " + toEmail + " (verified)\n" +
		"• Phone: " + (companyPhone != null ? companyPhone : "To be completed") + "\n" +
		"• Address: " + (city != null ? city : "To be completed") + "\n" +
		"• Township: " + (township != null ? township : "To be completed") + "\n" +
		"• Description: " + (description != null ? description : "To be completed") + "\n\n" +
		"Important Notes:\n" +
		"• You must use this exact link to register\n" +
		"• The email address is locked and cannot be changed\n" +
		"• You can modify other details during registration if needed\n" +
		"• You'll need to create a password for your account\n\n" +
		"Best regards,\n" +
		"JobJump Team"
		);
	
		mailSender.send(message);
		System.out.println("✅ Owner approval email sent to: " + toEmail);
		System.out.println("🔗 Registration URL: " + registrationUrl);
		
		} catch (Exception e) {
		System.err.println("❌ Failed to send owner approval email: " + e.getMessage());
		e.printStackTrace();
		}
	}
    
    //in 17.10.2025
    
    public void sendApplicationNotification(String toEmail, String subject, String messageText) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(messageText);
        
        mailSender.send(message);
    }
    
    public void sendNewApplicationAlert(String toEmail, String companyName, String jobTitle, 
                                      int applicationId, String applyDate) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("🚀 New Job Application - " + jobTitle);
        
        String emailContent = String.format(
            "Dear %s,\n\n" +
            "🎉 Great news! You have received a new job application.\n\n" +
            "📋 Application Details:\n" +
            "   • Job Title: %s\n" +
            "   • Application ID: #%d\n" +
            "   • Applied Date: %s\n\n" +
            "📱 Quick Actions:\n" +
            "   • Review Application: http://localhost:8080/owner/messages\n" +
            "   • View All Applications: http://localhost:8080/owner/messages\n" +
            "   • Manage Jobs: http://localhost:8080/owner/jobs\n\n" +
            "💼 Next Steps:\n" +
            "   1. Review the applicant's CV\n" +
            "   2. Update application status\n" +
            "   3. Contact qualified candidates\n\n" +
            "Best regards,\n" +
            "JobJump Team 🌟\n\n" +
            "P.S. Prompt responses to applicants improve your company's reputation!",
            companyName, jobTitle, applicationId, applyDate
        );
        
        message.setText(emailContent);
        
        try {
            mailSender.send(message);
            System.out.println("📧 New application alert sent to: " + toEmail);
        } catch (Exception e) {
            System.err.println("❌ Failed to send email: " + e.getMessage());
        }
    }
}