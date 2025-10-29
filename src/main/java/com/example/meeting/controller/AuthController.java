package com.example.meeting.controller;

import com.example.meeting.entity.User;
import com.example.meeting.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDateTime;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("user", new User());
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@ModelAttribute User user, Model model) {
        try {
            // Check if username or email already exists
            if (userRepository.findByUsername(user.getUsername()).isPresent()) {
                model.addAttribute("error", "Username already exists");
                return "signup";
            }
            if (userRepository.findByEmail(user.getEmail()).isPresent()) {
                model.addAttribute("error", "Email already exists");
                return "signup";
            }
            
            userRepository.save(user);
            return "redirect:/login?success=Registration successful";
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            return "signup";
        }
    }

    @GetMapping("/login")
    public String loginForm(@RequestParam(required = false) String error,
                           @RequestParam(required = false) String success,
                           Model model) {
        if (success != null) {
            model.addAttribute("success", success);
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, 
                       @RequestParam String password, 
                       Model model,
                       jakarta.servlet.http.HttpSession session) {
        try {
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null && user.getPassword().equals(password)) {
                user.setLastLogin(java.time.LocalDateTime.now());
                userRepository.save(user);
                
                session.setAttribute("userName", user.getFullName());
                session.setAttribute("userEmail", user.getEmail());
                session.setAttribute("userId", user.getId());
                return "redirect:/meetings/dashboard";
            } else {
                model.addAttribute("error", "Invalid username or password");
                return "login";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Login failed: " + e.getMessage());
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(jakarta.servlet.http.HttpSession session) {
        session.invalidate();
        return "redirect:/login?success=Logged out successfully";
    }
}