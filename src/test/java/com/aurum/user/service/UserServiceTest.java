
package com.aurum.user.service;

import com.aurum.config.JwtUtil;
import com.aurum.portfolio.Portfolio;
import com.aurum.portfolio.repository.PortfolioRepository;
import com.aurum.user.User;
import com.aurum.user.dto.LoginRequest;
import com.aurum.user.dto.SignupRequest;
import com.aurum.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PortfolioRepository portfolioRepository;

    @InjectMocks
    private UserService userService;

    private final BCryptPasswordEncoder encoder =
            new BCryptPasswordEncoder();

    @Test
    void signupCreatesUserAndPortfolio() {
        SignupRequest request = mock(SignupRequest.class);
        when(request.getEmail()).thenReturn("test@example.com");
        when(request.getFullName()).thenReturn("Test User");
        when(request.getPassword()).thenReturn("password123");
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.empty());

        userService.signup(request);

        verify(userRepository).save(any(User.class));
        verify(portfolioRepository).save(any(Portfolio.class));
    }

    @Test
    void signupThrowsExceptionWhenEmailAlreadyExists() {
        SignupRequest request = mock(SignupRequest.class);
        when(request.getEmail()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(mock(User.class)));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.signup(request)
        );

        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
        verify(portfolioRepository, never()).save(any(Portfolio.class));
    }

    @Test
    void loginReturnsTokenForValidCredentials() {
        LoginRequest request = mock(LoginRequest.class);
        when(request.getEmail()).thenReturn("test@example.com");
        when(request.getPassword()).thenReturn("password123");

        User user = mock(User.class);
        when(user.getEmail()).thenReturn("test@example.com");
        when(user.getPasswordHash()).thenReturn(
                encoder.encode("password123")
        );
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("test@example.com"))
                .thenReturn("test-jwt-token");

        String token = userService.login(request);

        assertEquals("test-jwt-token", token);
        verify(jwtUtil).generateToken("test@example.com");
    }

    @Test
    void loginThrowsExceptionWhenUserDoesNotExist() {
        LoginRequest request = mock(LoginRequest.class);
        when(request.getEmail()).thenReturn("missing@example.com");
        when(userRepository.findByEmail("missing@example.com"))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.login(request)
        );

        assertEquals("User not found", exception.getMessage());
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    void loginThrowsExceptionForIncorrectPassword() {
        LoginRequest request = mock(LoginRequest.class);
        when(request.getEmail()).thenReturn("test@example.com");
        when(request.getPassword()).thenReturn("wrong-password");

        User user = mock(User.class);
        when(user.getEmail()).thenReturn("test@example.com");
        when(user.getPasswordHash()).thenReturn(
                encoder.encode("correct-password")
        );
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.login(request)
        );

        assertEquals("Invalid password", exception.getMessage());
        verify(jwtUtil, never()).generateToken(anyString());
    }
}
