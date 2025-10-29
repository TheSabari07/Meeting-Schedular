package com.example.meeting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.meeting.entity.Meeting;
import java.util.List;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    List<Meeting> findByCreatedById(Long userId);
    List<Meeting> findByParticipantsContaining(String email);
}
