package com.deliverar.pagos.application.controllers;

import com.deliverar.pagos.domain.dtos.AuthRequest;
import com.deliverar.pagos.domain.dtos.AuthResponse;
import com.deliverar.pagos.domain.entities.User;
import com.deliverar.pagos.domain.repositories.UserRepository;
import com.deliverar.pagos.infrastructure.security.JwtUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Tag(name = "Autenticación", description = "Operaciones de autenticación y tokens JWT")
@Slf4j
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

    @Operation(summary = "Iniciar sesión",
            description = "Autentica un usuario usando email y contraseña, retorna tokens de acceso y refresco")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Autenticación exitosa",
                content = { @Content(mediaType = "application/json",
                        schema = @Schema(implementation = AuthResponse.class)) }),
        @ApiResponse(responseCode = "401", description = "Credenciales inválidas",
                content = @Content),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida",
                content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );
        String username = auth.getName();
        User user = userRepository.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        log.error("User: {}", user);
        String accessToken = jwtUtil.generateAccessToken(
                username,
                user.getId().toString(),
                user.getRole().name()
        );
        String refreshToken = jwtUtil.generateRefreshToken(username);

        log.error("Access Token: {}", accessToken);
        AuthResponse authResponse = null;
        try{
            authResponse = new AuthResponse(accessToken, refreshToken, expiresIn, user.getRole(), user.getRole().getPermissions())
        } catch (Exception e){
            log.error("Error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.ok(authResponse);
    }

    @Operation(summary = "Refrescar token",
            description = "Genera un nuevo token de acceso usando un token de refresco válido")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token refrescado exitosamente",
                content = { @Content(mediaType = "application/json",
                        schema = @Schema(implementation = Map.class)) }),
        @ApiResponse(responseCode = "401", description = "Token de refresco inválido o expirado",
                content = @Content)
    })
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
