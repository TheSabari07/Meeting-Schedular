package com.example.meeting.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.meeting.entity.Meeting;
import com.example.meeting.repository.MeetingRepository;

@Service
@Transactional
public class MeetingService {
    private final MeetingRepository meetingRepository;

    public MeetingService(MeetingRepository meetingRepository) {
        this.meetingRepository = meetingRepository;
    }

    @Transactional(readOnly = true)
    public List<Meeting> getAllMeetings() {
        return meetingRepository.findAll();
    }

    @Transactional
    public Meeting saveMeeting(Meeting meeting) {
        return meetingRepository.save(meeting);
    }

    @Transactional(readOnly = true)
    public Meeting getMeetingById(Long id) {
        return meetingRepository.findById(id).orElse(null);
    }

    @Transactional
    public void deleteMeeting(Long id) {
        meetingRepository.deleteById(id);
    }
    
    @Transactional(readOnly = true)
    public List<Meeting> getMeetingsByUserId(Long userId) {
        return meetingRepository.findByCreatedById(userId);
    }
    
    @Transactional(readOnly = true)
    public List<Meeting> getAllUserMeetings(Long userId, String userEmail) {
        List<Meeting> createdMeetings = meetingRepository.findByCreatedById(userId);
        if (createdMeetings == null) createdMeetings = new java.util.ArrayList<>();
        
        List<Meeting> invitedMeetings = new java.util.ArrayList<>();
        if (userEmail != null && !userEmail.trim().isEmpty()) {
            invitedMeetings = meetingRepository.findByParticipantsContaining(userEmail);
            if (invitedMeetings == null) invitedMeetings = new java.util.ArrayList<>();
        }
        
        // Combine and remove duplicates
        java.util.Set<Meeting> allMeetings = new java.util.HashSet<>(createdMeetings);
        allMeetings.addAll(invitedMeetings);
        
        return new java.util.ArrayList<>(allMeetings);
    }
}
