package com.aurum.auth;

import com.aurum.user.dto.SignupRequest;
import com.aurum.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.aurum.user.dto.LoginRequest;
import com.aurum.config.JwtUtil;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/signup")
    public String signup(@RequestBody SignupRequest request) {
        userService.signup(request);
        return "User registered successfully";
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {
        System.out.println("CONTROLLER HIT 🔥");
        return userService.login(request);
    }
}