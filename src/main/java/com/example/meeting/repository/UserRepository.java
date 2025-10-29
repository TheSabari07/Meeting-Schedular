package com.example.meeting.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.meeting.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
}
