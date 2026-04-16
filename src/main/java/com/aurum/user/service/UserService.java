package com.aurum.user.service;

import com.aurum.user.User;
import com.aurum.user.dto.SignupRequest;
import com.aurum.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.aurum.user.dto.LoginRequest;
import com.aurum.config.JwtUtil;
import com.aurum.portfolio.Portfolio;
import com.aurum.portfolio.repository.PortfolioRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtUtil jwtUtil;
    private final PortfolioRepository portfolioRepository;

    public void signup(SignupRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .createdAt(LocalDateTime.now())
                .build();

        // ✅ Save user first
        userRepository.save(user);

        // ✅ CREATE PORTFOLIO (FIX)
        Portfolio portfolio = Portfolio.builder()
                .user(user)
                .build();

        portfolioRepository.save(portfolio);
    }
    public String login(LoginRequest request) {

        System.out.println("LOGIN API HIT 🔥");

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        System.out.println("User found: " + user.getEmail());

        boolean match = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());
        System.out.println("Password match: " + match);

        if (!match) {
            throw new RuntimeException("Invalid password");
        }

        return jwtUtil.generateToken(user.getEmail());
    }
}