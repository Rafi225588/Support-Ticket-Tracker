package com.example.supportticket.service;

import com.example.supportticket.dto.AuthResponse;
import com.example.supportticket.dto.LoginRequest;
import com.example.supportticket.dto.RegisterRequest;
import com.example.supportticket.dto.UserResponse;
import com.example.supportticket.entity.User;
import com.example.supportticket.enums.Role;
import com.example.supportticket.exception.ConflictException;
import com.example.supportticket.exception.ResourceNotFoundException;
import com.example.supportticket.exception.UnauthorizedException;
import com.example.supportticket.repository.UserRepository;
import com.example.supportticket.security.JwtService;
import com.example.supportticket.security.RefreshTokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    public UserResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email already exists: " + email);
        }

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setRole(Role.CUSTOMER);
        user.setPassword(passwordEncoder.encode(request.password()));
        return UserResponse.from(userRepository.save(user));
    }

    public AuthResult login(LoginRequest request) {
        User user = userRepository.findByEmail(normalizeEmail(request.email()))
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (user.getPassword() == null
                || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        String refreshToken = refreshTokenService.create(user);
        return buildResult(user, refreshToken);
    }

    public AuthResult refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new UnauthorizedException("Refresh token is missing");
        }
        RefreshTokenService.Rotated rotated = refreshTokenService.rotate(refreshToken);
        return buildResult(rotated.user(), rotated.refreshToken());
    }

    public void logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenService.revoke(refreshToken);
        }
    }

    public UserResponse me(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));
        return UserResponse.from(user);
    }

    private AuthResult buildResult(User user, String refreshToken) {
        AuthResponse response = new AuthResponse(
                jwtService.generateAccessToken(user),
                "Bearer",
                jwtService.getAccessExpirationSeconds(),
                UserResponse.from(user));
        return new AuthResult(response, refreshToken);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    public record AuthResult(AuthResponse response, String refreshToken) {
    }
}