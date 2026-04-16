package com.aurum.auth;

import com.aurum.user.dto.SignupRequest;
import com.aurum.user.repository.UserRepository;
import com.aurum.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.aurum.user.dto.LoginRequest;
import com.aurum.config.JwtUtil;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {

        userService.signup(request);

        return ResponseEntity.ok().body(
                Map.of("message", "User registered successfully")
        );
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        String token = userService.login(request);

        return ResponseEntity.ok().body(
                Map.of("token", token)
        );
    }
}