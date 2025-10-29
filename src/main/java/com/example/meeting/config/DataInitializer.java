package com.example.meeting.config;

import com.example.meeting.entity.Meeting;
import com.example.meeting.entity.User;
import com.example.meeting.repository.MeetingRepository;
import com.example.meeting.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;

    public DataInitializer(MeetingRepository meetingRepository, UserRepository userRepository) {
        this.meetingRepository = meetingRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Create sample user if none exists
        if (userRepository.count() == 0) {
            User sampleUser = new User();
            sampleUser.setFullName("John Doe");
            sampleUser.setEmail("john@example.com");
            sampleUser.setUsername("admin");
            sampleUser.setPassword("admin");
            sampleUser.setCompany("Tech Corp");
            sampleUser.setJobTitle("Manager");
            userRepository.save(sampleUser);
            System.out.println("✅ Sample user created: admin/admin");
        }
        
        if (meetingRepository.count() == 0) {
            Meeting meeting1 = new Meeting();
            meeting1.setTitle("Team Standup");
            meeting1.setDescription("Daily team standup meeting");
            meeting1.setStartTime(LocalDateTime.now().plusHours(1));
            meeting1.setEndTime(LocalDateTime.now().plusHours(2));
            meeting1.setStatus("Scheduled");
            meeting1.setLocation("Conference Room A");
            meeting1.setParticipants("team@company.com");
            meetingRepository.save(meeting1);

            Meeting meeting2 = new Meeting();
            meeting2.setTitle("Project Review");
            meeting2.setDescription("Weekly project review meeting");
            meeting2.setStartTime(LocalDateTime.now().plusDays(1));
            meeting2.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));
            meeting2.setStatus("Scheduled");
            meeting2.setLocation("Conference Room B");
            meeting2.setParticipants("project@company.com");
            meetingRepository.save(meeting2);

            System.out.println("✅ Sample meetings created successfully!");
        }
    }
}