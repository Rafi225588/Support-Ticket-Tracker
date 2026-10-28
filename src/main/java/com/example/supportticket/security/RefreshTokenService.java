package com.example.supportticket.security;

import com.example.supportticket.entity.RefreshToken;
import com.example.supportticket.entity.User;
import com.example.supportticket.exception.UnauthorizedException;
import com.example.supportticket.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshExpirationDays;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               @Value("${app.jwt.refresh-expiration-days}") long refreshExpirationDays) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshExpirationDays = refreshExpirationDays;
    }
    
    @Transactional
    public String create(User user) {
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        RefreshToken token = new RefreshToken();
        token.setTokenHash(hash(rawToken));
        token.setUser(user);
        token.setExpiresAt(LocalDateTime.now().plusDays(refreshExpirationDays));
        refreshTokenRepository.save(token);
        return rawToken;
    }

    @Transactional(noRollbackFor = UnauthorizedException.class)
    public Rotated rotate(String rawToken) {
        RefreshToken old = refreshTokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (old.isRevoked()) {
            refreshTokenRepository.revokeAllByUserId(old.getUser().getId());
            throw new UnauthorizedException("Refresh token is no longer valid. Please log in again");
        }
        if (old.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("Session expired. Please log in again");
        }

        old.setRevoked(true);
        refreshTokenRepository.save(old);

        User user = old.getUser();
        return new Rotated(user, create(user));
    }
    @Transactional
    public void revoke(String rawToken) {
        refreshTokenRepository.findByTokenHash(hash(rawToken)).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    @Transactional
    public void revokeAllForUser(Long userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public record Rotated(User user, String refreshToken) {
    }
}