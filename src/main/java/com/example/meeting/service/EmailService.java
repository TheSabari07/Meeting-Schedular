package com.example.meeting.service;

import com.example.meeting.entity.Meeting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public void sendMeetingInvitation(Meeting meeting) {
        if (mailSender == null || meeting.getParticipants() == null || meeting.getParticipants().trim().isEmpty()) {
            System.out.println("Email service not configured or no participants specified");
            return;
        }

        try {
            String[] emails = meeting.getParticipants().split(",");
            for (String email : emails) {
                email = email.trim();
                if (email.contains("@")) {
                    sendEmail(email, meeting);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to send meeting invitation emails: " + e.getMessage());
        }
    }

    private void sendEmail(String toEmail, Meeting meeting) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Meeting Invitation: " + meeting.getTitle());
            message.setText(buildEmailContent(meeting));
            
            mailSender.send(message);
            System.out.println("Meeting invitation sent to: " + toEmail);
        } catch (Exception e) {
            System.err.println("Failed to send email to " + toEmail + ": " + e.getMessage());
        }
    }

    private String buildEmailContent(Meeting meeting) {
        StringBuilder content = new StringBuilder();
        content.append("=== MEETING INVITATION ===\n\n");
        content.append("You have been invited to a meeting!\n\n");
        
        content.append("MEETING DETAILS:\n");
        content.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        content.append("📋 Title: ").append(meeting.getTitle()).append("\n");
        
        if (meeting.getDescription() != null && !meeting.getDescription().trim().isEmpty()) {
            content.append("📝 Description: ").append(meeting.getDescription()).append("\n");
        }
        
        content.append("📅 Start Date & Time: ").append(meeting.getStartTime()).append("\n");
        
        if (meeting.getEndTime() != null) {
            content.append("⏰ End Time: ").append(meeting.getEndTime()).append("\n");
        }
        
        if (meeting.getLocation() != null && !meeting.getLocation().trim().isEmpty()) {
            content.append("📍 Location: ").append(meeting.getLocation()).append("\n");
        }
        
        if (meeting.getCreatedBy() != null) {
            content.append("👤 Organizer: ").append(meeting.getCreatedBy().getFullName());
            if (meeting.getCreatedBy().getEmail() != null) {
                content.append(" (").append(meeting.getCreatedBy().getEmail()).append(")");
            }
            content.append("\n");
            
            if (meeting.getCreatedBy().getCompany() != null) {
                content.append("🏢 Company: ").append(meeting.getCreatedBy().getCompany()).append("\n");
            }
            
            if (meeting.getCreatedBy().getJobTitle() != null) {
                content.append("💼 Job Title: ").append(meeting.getCreatedBy().getJobTitle()).append("\n");
            }
            
            if (meeting.getCreatedBy().getPhoneNumber() != null) {
                content.append("📞 Contact: ").append(meeting.getCreatedBy().getPhoneNumber()).append("\n");
            }
        }
        
        if (meeting.getParticipants() != null && !meeting.getParticipants().trim().isEmpty()) {
            content.append("👥 All Participants: ").append(meeting.getParticipants()).append("\n");
        }
        
        content.append("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        content.append("\n📌 IMPORTANT NOTES:\n");
        content.append("• Please confirm your attendance\n");
        content.append("• Add this meeting to your calendar\n");
        content.append("• Contact the organizer if you have any questions\n\n");
        
        content.append("This invitation was sent automatically by the Meeting Scheduler System.\n");
        content.append("\nBest regards,\nMeeting Scheduler Team");
        
        return content.toString();
    }
}