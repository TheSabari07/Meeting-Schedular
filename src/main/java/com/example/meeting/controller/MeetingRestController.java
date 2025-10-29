package com.example.meeting.controller;

import com.example.meeting.entity.Meeting;
import com.example.meeting.repository.MeetingRepository;
import com.example.meeting.service.MeetingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/meetings")
public class MeetingRestController {

    private final MeetingService meetingService;
    
    @Autowired
    private MeetingRepository meetingRepository;

    public MeetingRestController(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    @GetMapping
    public ResponseEntity<List<Meeting>> all() {
        try {
            List<Meeting> meetings = meetingService.getAllMeetings();
            System.out.println("API: Found " + meetings.size() + " meetings");
            return ResponseEntity.ok(meetings);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<Meeting> create(@RequestBody Meeting meeting) {
        try {
            System.out.println("API: Creating meeting: " + meeting.getTitle());
            Meeting savedMeeting = meetingService.saveMeeting(meeting);
            System.out.println("API: Meeting created with ID: " + savedMeeting.getId());
            return ResponseEntity.ok(savedMeeting);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Meeting> update(@PathVariable Long id, @RequestBody Meeting meeting) {
        try {
            Meeting existing = meetingService.getMeetingById(id);
            if (existing == null) return ResponseEntity.notFound().build();
            // copy fields you want to allow update
            existing.setTitle(meeting.getTitle());
            existing.setStartTime(meeting.getStartTime());
            existing.setEndTime(meeting.getEndTime());
            existing.setLocation(meeting.getLocation());
            existing.setParticipants(meeting.getParticipants());
            Meeting updated = meetingService.saveMeeting(existing);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            meetingService.deleteMeeting(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/count")
    public ResponseEntity<Long> getMeetingCount() {
        try {
            long count = meetingRepository.count();
            System.out.println("API: Meeting count: " + count);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
