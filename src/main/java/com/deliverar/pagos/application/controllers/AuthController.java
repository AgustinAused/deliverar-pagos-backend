package com.deliverar.pagos.application.controllers;

import com.deliverar.pagos.domain.dtos.AuthRequest;
import com.deliverar.pagos.domain.dtos.AuthResponse;
import com.deliverar.pagos.infrastructure.security.JwtUtil;
import com.deliverar.pagos.domain.entities.User;
import com.deliverar.pagos.domain.repositories.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final long expiresIn;

    public AuthController(
            AuthenticationManager authManager,
            JwtUtil jwtUtil,
            UserRepository userRepository,
            @Value("${jwt.expiration}") long jwtExpirationMs
    ) {
        this.authManager = authManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.expiresIn = jwtExpirationMs / 1000;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );
        String username = auth.getName();
        User user = userRepository.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        String accessToken = jwtUtil.generateAccessToken(
                username,
                user.getId().toString(),
                user.getRole().name()
        );
        String refreshToken = jwtUtil.generateRefreshToken(username);

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken, expiresIn));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refresh(
            @RequestBody Map<String, String> body
    ) {
        String refreshToken = body.get("refreshToken");
        String username = jwtUtil.extractUsername(refreshToken);
        if (!jwtUtil.validateToken(refreshToken, username)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userRepository.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        String newAccessToken = jwtUtil.generateAccessToken(
                username,
                user.getId().toString(),
                user.getRole().name()
        );
        return ResponseEntity.ok(Map.of(
                "accessToken", newAccessToken,
                "expiresIn", expiresIn
        ));
    }
}