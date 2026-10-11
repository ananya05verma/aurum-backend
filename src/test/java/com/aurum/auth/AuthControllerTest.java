package com.aurum.auth;

import com.aurum.config.JwtUtil;
import com.aurum.user.dto.LoginRequest;
import com.aurum.user.dto.SignupRequest;
import com.aurum.user.service.UserService;
import com.aurum.user.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthController authController;

    @Test
    void loginReturnsToken() {
        LoginRequest request = new LoginRequest();

        when(userService.login(request)).thenReturn("test-token");

        ResponseEntity<?> response = authController.login(request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(
                "test-token",
                ((Map<?, ?>) response.getBody()).get("token")
        );

        verify(userService).login(request);
    }

    @Test
    void signupReturnsSuccessMessage() {
        SignupRequest request = new SignupRequest();

        ResponseEntity<?> response = authController.signup(request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(
                "User registered successfully",
                ((Map<?, ?>) response.getBody()).get("message")
        );

        verify(userService).signup(request);
    }
}