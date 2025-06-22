package com.deliverar.pagos.adapters.rest.messaging.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class HubAuthService {
    private final WebClient authClient;
    private final String user;
    private final String pass;
    private final AtomicReference<String> cachedToken = new AtomicReference<>();

    public HubAuthService(
            @Value("${hub.url}") String hubUrl,
            @Value("${hub.user}") String user,
            @Value("${hub.password}") String pass
    ) {
        this.authClient = WebClient.builder().baseUrl(hubUrl).build();
        this.user = user;
        this.pass = pass;
    }

    public Mono<String> getToken() {
        if (cachedToken.get() != null) {
            return Mono.just(cachedToken.get());
        }
        return login();
    }

    public Mono<Void> refreshToken() {
        return login().then();
    }

    private Mono<String> login() {
        log.debug("Iniciando login al Hub con usuario: {}", user);
        return authClient.post()
                .uri("/auth/login")
                .bodyValue(Map.of("username", user, "password", pass))
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp -> Mono.error(new RuntimeException("Login failed: " + resp.statusCode())))
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .map(body -> {
                    String token = (String) body.get("access_token");
                    cachedToken.set(token);
                    log.debug("Token obtenido exitosamente");
                    return token;
                })
                .doOnError(err -> log.error("Error durante el login al Hub", err));
    }
} 