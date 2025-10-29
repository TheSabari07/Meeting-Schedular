package com.example.meeting.controller;
import com.example.meeting.entity.Meeting;
import com.example.meeting.entity.User;
import com.example.meeting.repository.UserRepository;
import com.example.meeting.service.MeetingService;
import com.example.meeting.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Controller
@RequestMapping("/meetings")
public class MeetingController {

    @Autowired
    private MeetingService meetingService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmailService emailService;

    @GetMapping
    @Transactional(readOnly = true)
    public String listMeetings(Model model, HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            String userEmail = (String) session.getAttribute("userEmail");
            if (userId == null) {
                return "redirect:/login";
            }
            
            List<Meeting> allMeetings = meetingService.getAllUserMeetings(userId, userEmail);
            LocalDateTime now = LocalDateTime.now();
            for (Meeting meeting : allMeetings) {
                meeting.setStatus(calculateMeetingStatus(meeting, now));
            }
            
            model.addAttribute("meetings", allMeetings);
            model.addAttribute("currentUserId", userId);
            return "meetings";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error loading meetings: " + e.getMessage());
            model.addAttribute("meetings", java.util.Collections.emptyList());
            return "meetings";
        }
    }

    @GetMapping("/new")
    public String newMeetingForm(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login?error=Please login first";
        }
        
        model.addAttribute("meeting", new Meeting());
        model.addAttribute("users", userRepository.findAll());
        return "new_meeting";
    }

    @PostMapping
    public String saveMeeting(@RequestParam(required = false) String title,
                             @RequestParam(required = false) String description,
                             @RequestParam(required = false) String startTime,
                             @RequestParam(required = false) String endTime,
                             @RequestParam(required = false) String location,
                             @RequestParam(required = false) String participants,
                             HttpSession session, Model model) {
        try {
            System.out.println("=== MEETING SAVE ATTEMPT ===");
            System.out.println("Title: " + title);
            System.out.println("StartTime: " + startTime);
            System.out.println("EndTime: " + endTime);
            
            Long userId = (Long) session.getAttribute("userId");
            System.out.println("User ID from session: " + userId);
            
            if (userId == null) {
                System.out.println("No user ID in session, redirecting to login");
                return "redirect:/login";
            }
            
            User user = userRepository.findById(userId).orElse(null);
            System.out.println("Found user: " + (user != null ? user.getUsername() : "null"));
            
            if (user == null) {
                System.out.println("User not found in database");
                return "redirect:/login";
            }
            
            if (title == null || title.trim().isEmpty()) {
                System.out.println("Title validation failed");
                model.addAttribute("error", "Meeting title is required");
                return "new_meeting";
            }
            
            if (startTime == null || startTime.trim().isEmpty()) {
                System.out.println("Start time validation failed");
                model.addAttribute("error", "Start time is required");
                return "new_meeting";
            }
            
            if (endTime == null || endTime.trim().isEmpty()) {
                System.out.println("End time validation failed");
                model.addAttribute("error", "End time is required");
                return "new_meeting";
            }
            
            Meeting meeting = new Meeting();
            meeting.setTitle(title.trim());
            meeting.setDescription(description != null ? description.trim() : null);
            meeting.setLocation(location != null ? location.trim() : null);
            meeting.setParticipants(participants != null ? participants.trim() : null);
            meeting.setCreatedBy(user);
            meeting.setStatus("Scheduled");
            
            try {
                meeting.setStartTime(LocalDateTime.parse(startTime));
                meeting.setEndTime(LocalDateTime.parse(endTime));
                System.out.println("Date parsing successful");
            } catch (Exception dateError) {
                System.out.println("Date parsing failed: " + dateError.getMessage());
                model.addAttribute("error", "Invalid date format");
                return "new_meeting";
            }
            
            System.out.println("About to save meeting...");
            Meeting savedMeeting = meetingService.saveMeeting(meeting);
            System.out.println("Meeting saved successfully with ID: " + savedMeeting.getId());
            
            return "redirect:/meetings/dashboard";
            
        } catch (Exception e) {
            System.err.println("Error saving meeting: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Failed to save meeting: " + e.getMessage());
            return "new_meeting";
        }
    }
    
    @GetMapping("/calendar")
    public String calendar(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        String userEmail = (String) session.getAttribute("userEmail");
        if (userId == null) {
            return "redirect:/login";
        }
        
        List<Meeting> userMeetings = meetingService.getAllUserMeetings(userId, userEmail);
        if (userMeetings == null) {
            userMeetings = new java.util.ArrayList<>();
        }
        
        LocalDateTime now = LocalDateTime.now();
        for (Meeting meeting : userMeetings) {
            if (meeting != null) {
                meeting.setStatus(calculateMeetingStatus(meeting, now));
            }
        }
        
        model.addAttribute("meetings", userMeetings);
        model.addAttribute("currentUserId", userId);
        return "calender";
    }
    
    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return "redirect:/login";
            }
            
            List<Meeting> userMeetings = meetingService.getMeetingsByUserId(userId);
            
            // Update meeting statuses dynamically
            LocalDateTime now = LocalDateTime.now();
            for (Meeting meeting : userMeetings) {
                meeting.setStatus(calculateMeetingStatus(meeting, now));
            }
            
            LocalDate today = now.toLocalDate();
            
            // Today's meetings
            long todaysMeetings = userMeetings.stream()
                .filter(m -> m.getStartTime() != null && 
                           m.getStartTime().toLocalDate().equals(today))
                .count();
                
            // Upcoming meetings
            long upcomingMeetings = userMeetings.stream()
                .filter(m -> m.getStartTime() != null && m.getStartTime().isAfter(now))
                .count();
                
            // Total participants
            long totalParticipants = userMeetings.stream()
                .filter(m -> m.getParticipants() != null && !m.getParticipants().trim().isEmpty())
                .mapToLong(m -> m.getParticipants().split(",").length)
                .sum();
                
            // Live meetings
            long liveMeetings = userMeetings.stream()
                .filter(m -> "Ongoing".equals(calculateMeetingStatus(m, now)))
                .count();
            
            String userName = (String) session.getAttribute("userName");
            if (userName == null) userName = "User";
            
            model.addAttribute("meetings", userMeetings);
            model.addAttribute("todaysMeetings", todaysMeetings);
            model.addAttribute("upcomingMeetings", upcomingMeetings);
            model.addAttribute("totalParticipants", totalParticipants);
            model.addAttribute("liveMeetings", liveMeetings);
            model.addAttribute("userName", userName);
            model.addAttribute("currentUserId", userId);
            
            return "dashboard";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error loading dashboard: " + e.getMessage());
            return "dashboard";
        }
    }
    
    @GetMapping("/edit/{id}")
    public String editMeeting(@PathVariable Long id, Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        Meeting meeting = meetingService.getMeetingById(id);
        if (meeting == null || !meeting.getCreatedBy().getId().equals(userId)) {
            return "redirect:/meetings/dashboard?error=Meeting not found or access denied";
        }
        
        // Format datetime for HTML input
        if (meeting.getStartTime() != null) {
            meeting.setStartTime(meeting.getStartTime());
        }
        if (meeting.getEndTime() != null) {
            meeting.setEndTime(meeting.getEndTime());
        }
        
        model.addAttribute("meeting", meeting);
        return "edit_meeting";
    }
    
    @PostMapping("/edit/{id}")
    public String updateMeeting(@PathVariable Long id, @ModelAttribute Meeting meeting, HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return "redirect:/login";
            }
            
            Meeting existingMeeting = meetingService.getMeetingById(id);
            if (existingMeeting == null || !existingMeeting.getCreatedBy().getId().equals(userId)) {
                return "redirect:/meetings/dashboard?error=Meeting not found or access denied";
            }
            
            LocalDateTime now = LocalDateTime.now();
            if (meeting.getStartTime().isBefore(now)) {
                return "redirect:/meetings/dashboard?error=Cannot schedule meeting in the past";
            }
            
            // Preserve the original meeting's ID and creator
            meeting.setId(id);
            meeting.setCreatedBy(existingMeeting.getCreatedBy());
            meeting.setStatus("Scheduled");
            
            meetingService.saveMeeting(meeting);
            return "redirect:/meetings/dashboard?success=Meeting updated successfully";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/meetings/dashboard?error=Failed to update meeting";
        }
    }
    
    @PostMapping("/delete/{id}")
    public String deleteMeeting(@PathVariable Long id, HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return "redirect:/login";
            }
            
            Meeting meeting = meetingService.getMeetingById(id);
            if (meeting == null || !meeting.getCreatedBy().getId().equals(userId)) {
                return "redirect:/meetings/dashboard?error=Meeting not found or access denied";
            }
            
            meetingService.deleteMeeting(id);
            return "redirect:/meetings/dashboard?success=Meeting deleted successfully";
        } catch (Exception e) {
            return "redirect:/meetings/dashboard?error=Failed to delete meeting";
        }
    }
    
    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            model.addAttribute("user", user);
        }
        return "profile";
    }
    
    private String calculateMeetingStatus(Meeting meeting, LocalDateTime now) {
        if (meeting.getStartTime() == null) return "Scheduled";
        
        if (now.isBefore(meeting.getStartTime())) {
            return "Scheduled";
        } else if (meeting.getEndTime() != null && now.isAfter(meeting.getEndTime())) {
            return "Ended";
        } else {
            return "Ongoing";
        }
    }
    

}
