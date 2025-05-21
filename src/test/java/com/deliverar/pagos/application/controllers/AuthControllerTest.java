package com.deliverar.pagos.application.controllers;
import com.deliverar.pagos.domain.dtos.AuthRequest;
import com.deliverar.pagos.domain.dtos.AuthResponse;
import com.deliverar.pagos.domain.entities.Role;
import com.deliverar.pagos.domain.entities.User;
import com.deliverar.pagos.domain.repositories.UserRepository;
import com.deliverar.pagos.infrastructure.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authManager;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private UserRepository userRepository;

    private AuthController controller;
    private final long jwtMs = 3600000;

    @BeforeEach
    void setUp() {
        controller = new AuthController(authManager, jwtUtil, userRepository, jwtMs);
    }

    @Test
    void login_Successful_ReturnsTokens() {
        AuthRequest req = new AuthRequest("user@example.com", "pwd");
        Authentication auth = new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword());
        when(authManager.authenticate(any())).thenReturn(auth);

        User user = User.builder()
                .id(UUID.randomUUID())
                .email(req.getEmail())
                .passwordHash("hash")
                .role(Role.ADMIN)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        when(userRepository.findByEmailIgnoreCase(req.getEmail())).thenReturn(Optional.of(user));

        when(jwtUtil.generateAccessToken(eq(req.getEmail()), eq(user.getId().toString()), eq(user.getRole().name())))
                .thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(req.getEmail())).thenReturn("refresh-token");

        ResponseEntity<AuthResponse> resp = controller.login(req);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        AuthResponse body = resp.getBody();
        assertNotNull(body);
        assertEquals("access-token", body.getAccessToken());
        assertEquals("refresh-token", body.getRefreshToken());
        assertEquals(jwtMs / 1000, body.getExpiresIn());
    }

    @Test
    void login_InvalidCredentials_Throws() {
        AuthRequest req = new AuthRequest("user@example.com", "pwd");
        when(authManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad"));

        assertThrows(BadCredentialsException.class, () -> controller.login(req));
    }

    @Test
    void refresh_ValidToken_ReturnsNewAccessToken() {
        String oldRefresh = "old-refresh";
        Map<String,String> bodyIn = Map.of("refreshToken", oldRefresh);
        when(jwtUtil.extractUsername(oldRefresh)).thenReturn("user@example.com");
        when(jwtUtil.validateToken(oldRefresh, "user@example.com")).thenReturn(true);

        User user = User.builder()
                .id(UUID.randomUUID())
                .email("user@example.com")
                .passwordHash("hash")
                .role(Role.CORE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateAccessToken("user@example.com", user.getId().toString(), user.getRole().name()))
                .thenReturn("new-access");

        ResponseEntity<Map<String, Object>> resp = controller.refresh(bodyIn);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        Map<String, Object> body = resp.getBody();
        assertEquals("new-access", body.get("accessToken"));
        assertEquals(jwtMs / 1000, body.get("expiresIn"));
    }

    @Test
    void refresh_InvalidToken_ReturnsUnauthorized() {
        String bad = "bad-refresh";
        when(jwtUtil.extractUsername(bad)).thenReturn("user@example.com");
        when(jwtUtil.validateToken(bad, "user@example.com")).thenReturn(false);

        ResponseEntity<Map<String, Object>> resp = controller.refresh(Map.of("refreshToken", bad));
        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
        assertNull(resp.getBody());
    }
}
