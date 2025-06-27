package com.deliverar.pagos.adapters.rest.security;

import com.deliverar.pagos.infrastructure.security.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class LdapJwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    
    public LdapJwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(token);
                String userId = jwtUtil.extractUserId(token);
                
                // Solo procesar si es un token LDAP (identificado por userId = "ldap-user")
                if ("ldap-user".equals(userId) && jwtUtil.validateToken(token, username)) {
                    List<String> groups = jwtUtil.extractGroups(token);
                    List<GrantedAuthority> authorities = mapGroupsToAuthorities(groups);
                    
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            username, null, authorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {
                logger.error("LDAP JWT token processing failed", e);
            }
        }

        filterChain.doFilter(request, response);
    }
    
    private List<GrantedAuthority> mapGroupsToAuthorities(List<String> groups) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        if (groups != null) {
            for (String group : groups) {
                switch (group.toLowerCase()) {
                    case "blockchain-admin":
                        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                        break;
                    case "blockchain-core":
                        authorities.add(new SimpleGrantedAuthority("ROLE_CORE"));
                        break;
                    case "blockchain-auditor":
                        authorities.add(new SimpleGrantedAuthority("ROLE_AUDITOR"));
                        break;
                    default:
                        // Para otros grupos, agregar como rol con prefijo ROLE_
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + group.toUpperCase().replace("-", "_")));
                        break;
                }
            }
        }
        
        // Si no tiene grupos específicos, darle rol USER por defecto
        if (authorities.isEmpty()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }
        
        return authorities;
    }
}