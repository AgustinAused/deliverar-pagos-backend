package com.deliverar.pagos.application.controllers;

import com.deliverar.pagos.domain.dtos.AuthRequest;
import com.deliverar.pagos.domain.dtos.AuthResponse;
import com.deliverar.pagos.domain.entities.User;
import com.deliverar.pagos.domain.repositories.UserRepository;
import com.deliverar.pagos.infrastructure.security.JwtUtil;
import com.deliverar.pagos.infrastructure.security.ActiveDirectoryService;
import com.unboundid.ldap.sdk.*;
import com.unboundid.ldap.sdk.extensions.StartTLSExtendedRequest;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustAllTrustManager;

import javax.net.ssl.SSLSocketFactory;
import java.util.List;
import java.util.ArrayList;
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
    private final ActiveDirectoryService activeDirectoryService;
    private final long expiresIn;

    public AuthController(
            AuthenticationManager authManager,
            JwtUtil jwtUtil,
            UserRepository userRepository,
            ActiveDirectoryService activeDirectoryService,
            @Value("${jwt.expiration}") long jwtExpirationMs
    ) {
        this.authManager = authManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.activeDirectoryService = activeDirectoryService;
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
            authResponse = new AuthResponse(accessToken, refreshToken, expiresIn, user.getRole(), user.getRole().getPermissions());
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

    @Operation(summary = "Iniciar sesión con Active Directory (Simple)",
            description = "Autentica un usuario directamente contra Active Directory sin validación local")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Autenticación exitosa",
                content = @Content),
        @ApiResponse(responseCode = "401", description = "Credenciales inválidas",
                content = @Content),
        @ApiResponse(responseCode = "400", description = "Parámetros requeridos faltantes",
                content = @Content)
    })
    @PostMapping("/ldap-login")
    public ResponseEntity<?> ldapLogin(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        
        if (email == null || password == null || email.trim().isEmpty() || password.trim().isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "Parámetros 'email' y 'password' son requeridos"));
        }

        String domain = "DELIVERAR";
        String fqdnUser;
        if (email.contains("@")) {
            fqdnUser = email;
        } else {
            fqdnUser = domain + "\\" + email;
        }
        
        String ldapHost = "ad.deliver.ar";
        int ldapPort = 389;

        LDAPConnection connection = null;
        try {
            SSLUtil sslUtil = new SSLUtil(new TrustAllTrustManager());
            SSLSocketFactory sslSocketFactory = sslUtil.createSSLSocketFactory();

            connection = new LDAPConnection(ldapHost, ldapPort);
            connection.processExtendedOperation(
                    new StartTLSExtendedRequest(sslSocketFactory)
            );

            BindResult bindResult = connection.bind(fqdnUser, password);

            if (bindResult.getResultCode() == ResultCode.SUCCESS) {
                log.info("Usuario '{}' autenticado exitosamente via LDAP", email);
                
                // Obtener grupos del usuario
                List<String> userGroups = getUserGroups(connection, email, domain);
                log.info("Grupos del usuario '{}': {}", email, userGroups);
                
                String userEmail = email.contains("@") ? email : email + "@deliver.ar";
                String accessToken = jwtUtil.generateAccessToken(userEmail, "ldap-user", "USER", userGroups);
                String refreshToken = jwtUtil.generateRefreshToken(userEmail);
                
                return ResponseEntity.ok(Map.of(
                    "accessToken", accessToken,
                    "refreshToken", refreshToken,
                    "expiresIn", expiresIn,
                    "message", "Usuario autenticado exitosamente"
                ));
            } else {
                log.warn("Autenticación fallida para usuario '{}': {}", email, bindResult.getDiagnosticMessage());
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Credenciales inválidas: " + bindResult.getDiagnosticMessage()));
            }

        } catch (LDAPException e) {
            log.error("Error LDAP autenticando usuario '{}': {}", email, e.getDiagnosticMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error LDAP: " + e.getDiagnosticMessage()));
        } catch (Exception e) {
            log.error("Error general autenticando usuario '{}': {}", email, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error general: " + e.getMessage()));
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    private List<String> getUserGroups(LDAPConnection connection, String email, String domain) {
        List<String> groups = new ArrayList<>();
        try {
            // Construir el filtro de búsqueda para encontrar al usuario
            String searchFilter;
            if (email.contains("@")) {
                searchFilter = String.format("(userPrincipalName=%s)", email);
            } else {
                searchFilter = String.format("(sAMAccountName=%s)", email);
            }
            
            // Buscar el usuario en AD
            String baseDN = String.format("DC=%s,DC=ar", domain.toLowerCase().replace("deliverar", "deliver"));
            SearchRequest searchRequest = new SearchRequest(
                baseDN,
                SearchScope.SUB,
                searchFilter,
                "memberOf", "cn"
            );
            
            SearchResult searchResult = connection.search(searchRequest);
            
            if (searchResult.getEntryCount() > 0) {
                SearchResultEntry userEntry = searchResult.getSearchEntries().get(0);
                
                // Obtener los grupos (memberOf)
                Attribute memberOfAttribute = userEntry.getAttribute("memberOf");
                if (memberOfAttribute != null) {
                    for (String groupDN : memberOfAttribute.getValues()) {
                        // Extraer el nombre del grupo del DN
                        String groupName = extractGroupName(groupDN);
                        if (groupName != null) {
                            groups.add(groupName);
                        }
                    }
                }
            }
            
        } catch (LDAPException e) {
            log.error("Error obteniendo grupos para usuario '{}': {}", email, e.getDiagnosticMessage());
        }
        
        return groups;
    }
    
    private String extractGroupName(String groupDN) {
        // Extraer el nombre del grupo de un DN como "CN=Grupo,OU=Groups,DC=deliver,DC=ar"
        if (groupDN != null && groupDN.toLowerCase().startsWith("cn=")) {
            int startIndex = 3; // Después de "CN="
            int endIndex = groupDN.indexOf(',');
            if (endIndex > startIndex) {
                return groupDN.substring(startIndex, endIndex);
            }
        }
        return null;
    }
}
