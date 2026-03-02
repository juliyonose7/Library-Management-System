package com.sgilib.backend.service.impl;

import com.sgilib.backend.api.dto.AuthResponse;
import com.sgilib.backend.api.dto.LoginRequest;
import com.sgilib.backend.api.dto.RefreshRequest;
import com.sgilib.backend.domain.AppUser;
import com.sgilib.backend.domain.RefreshToken;
import com.sgilib.backend.exception.ConflictException;
import com.sgilib.backend.exception.ResourceNotFoundException;
import com.sgilib.backend.repository.AppUserRepository;
import com.sgilib.backend.repository.RefreshTokenRepository;
import com.sgilib.backend.security.JwtService;
import com.sgilib.backend.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final AppUserRepository appUserRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final long refreshTokenExpirationMs;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           AppUserRepository appUserRepository,
                           RefreshTokenRepository refreshTokenRepository,
                           JwtService jwtService,
                           @Value("${security.jwt.refresh-token-expiration-ms}") long refreshTokenExpirationMs) {
        this.authenticationManager = authenticationManager;
        this.appUserRepository = appUserRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        AppUser user = appUserRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String accessToken = jwtService.generateToken(user.getUsername(), user.getRole().name());
        RefreshToken refreshToken = createRefreshToken(user);
        return new AuthResponse(accessToken, refreshToken.getToken());
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        RefreshToken savedToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new ConflictException("Invalid refresh token"));

        if (savedToken.isRevoked() || savedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ConflictException("Refresh token expired or revoked");
        }

        savedToken.setRevoked(true);
        refreshTokenRepository.save(savedToken);

        AppUser user = savedToken.getUser();
        String accessToken = jwtService.generateToken(user.getUsername(), user.getRole().name());
        RefreshToken newRefreshToken = createRefreshToken(user);
        return new AuthResponse(accessToken, newRefreshToken.getToken());
    }

    private RefreshToken createRefreshToken(AppUser user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setUser(user);
        refreshToken.setRevoked(false);
        refreshToken.setExpiresAt(LocalDateTime.now().plusNanos(refreshTokenExpirationMs * 1_000_000));
        return refreshTokenRepository.save(refreshToken);
    }
}
