package com.devproject.dpinUptime.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import com.devproject.dpinUptime.model.User;
import com.devproject.dpinUptime.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;

@Controller
public class AuthController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, Model model) {
        // Check if email already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            model.addAttribute("error", "Email already registered");
            return "register";
        }

        try {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            // user.setRole("USER");
            userRepository.save(user);
            return "redirect:/login?success";
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("error", "Email already registered");
            return "register";
        } catch (Exception e) {
            model.addAttribute("error", "An error occurred during registration");
            return "register";
        }
    }

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @GetMapping("/login/validator")
    public String validatorLogin() {
        return "validator-login";
    }
}