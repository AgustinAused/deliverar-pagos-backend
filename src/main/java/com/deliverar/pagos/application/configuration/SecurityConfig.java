package com.deliverar.pagos.application.configuration;

import com.deliverar.pagos.adapters.rest.security.JwtAuthenticationFilter;
import com.deliverar.pagos.infrastructure.security.JpaUserDetailsService;
import com.deliverar.pagos.infrastructure.security.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JpaUserDetailsService uds;
    private final JwtUtil jwtUtil;

    public SecurityConfig(JpaUserDetailsService uds, JwtUtil jwtUtil) {
        this.uds = uds;
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration cfg = new CorsConfiguration();
//        cfg.setAllowedOrigins(List.of("https://front.blockchain.deliver.ar/", "http://localhost:3000"));
//        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//        cfg.setAllowedHeaders(List.of("*"));
//        cfg.setAllowCredentials(true);
//        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
//        src.registerCorsConfiguration("/**", cfg);
//        return src;
//    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOriginPatterns("front.blockchain.deliver.ar"); // Permite cualquier origen
        cfg.setAllowedMethods(List.of(""));        // Permite cualquier método
        cfg.setAllowedHeaders(List.of("*"));        // Permite cualquier header
        cfg.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", cfg);
        return src;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1) CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 2) CSRF off
                .csrf(AbstractHttpConfigurer::disable)
                // 3) Sesión stateless
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 4) JWT filter
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil, uds),
                        UsernamePasswordAuthenticationFilter.class)
                // 5) Reglas de acceso
                .authorizeHttpRequests(auth -> auth
                        // permitir preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // AuthController
                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/refresh")
                        .permitAll()

                        // Swagger/OpenAPI
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**")
                        .permitAll()

                        // DeliverCoinController
                        .requestMatchers(HttpMethod.GET, "/api/delivercoin")
                        .hasAnyRole("ADMIN", "AUDITOR")
                        .requestMatchers(HttpMethod.POST, "/api/delivercoin/transfer")
                        .hasAnyRole("ADMIN", "CORE")
                        .requestMatchers(HttpMethod.GET, "/api/delivercoin/transfer/status")
                        .hasAnyRole("ADMIN", "CORE", "AUDITOR")
                        .requestMatchers(HttpMethod.POST, "/api/delivercoin/mint", "/api/delivercoin/burn")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/delivercoin/balance")
                        .hasAnyRole("ADMIN", "CORE", "AUDITOR")
                        .requestMatchers(HttpMethod.GET, "/api/delivercoin/supply")
                        .hasAnyRole("ADMIN", "AUDITOR")

                        // OwnerController
                        .requestMatchers(HttpMethod.GET, "/api/owners/*/balances")
                        .hasAnyRole("ADMIN", "CORE", "AUDITOR")
                        .requestMatchers(HttpMethod.GET, "/api/owners/*/transactions/fiat", "/api/owners/*/transactions")
                        .hasAnyRole("ADMIN", "CORE", "AUDITOR")
                        .requestMatchers(HttpMethod.POST, "/api/owners/*/fiat")
                        .hasAnyRole("ADMIN", "CORE")

                        // TransactionHistoryController
                        .requestMatchers(HttpMethod.GET, "/api/transactions/fiat", "/api/transactions")
                        .hasAnyRole("ADMIN", "AUDITOR")

                        // UserController
                        .requestMatchers(HttpMethod.GET, "/api/users")
                        .hasAnyRole("ADMIN", "AUDITOR")
                        .requestMatchers(HttpMethod.POST, "/api/users")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/users/*")
                        .hasAnyRole("ADMIN", "AUDITOR")
                        .requestMatchers(HttpMethod.PUT, "/api/users/*", "/api/users/*")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/*")
                        .hasRole("ADMIN")

                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }
}
