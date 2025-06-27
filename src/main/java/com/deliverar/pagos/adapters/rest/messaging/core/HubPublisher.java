package com.deliverar.pagos.adapters.rest.messaging.core;

import com.deliverar.pagos.adapters.rest.messaging.core.dtos.ImmutableEvent;
import com.deliverar.pagos.domain.exceptions.InternalServerException;
import com.deliverar.pagos.domain.exceptions.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class HubPublisher {
    private final WebClient webClient;
    private final HubAuthService authService;

    public HubPublisher(@Value("${hub.url}") String hubUrl, HubAuthService authService) {
        this.webClient = WebClient.builder()
                .baseUrl(hubUrl)
                .build();
        this.authService = authService;
    }

    public Mono<Void> publish(ImmutableEvent pub) {
        log.debug("Enviando petición al Hub: {}", pub);
        return publishWithAuth(pub, false);
    }

    private Mono<Void> publishWithAuth(ImmutableEvent pub, boolean isRetry) {
        return authService.getToken()
                .flatMap(token -> makeRequest(pub, token))
                .onErrorResume(UnauthorizedException.class, error -> {
                    if (!isRetry) {
                        log.warn("Token expirado, refrescando y reintentando...");
                        return authService.refreshToken()
                                .then(publishWithAuth(pub, true));
                    }
                    return Mono.error(new RuntimeException("Falló autenticación después del retry"));
                })
                .doOnSuccess(r -> log.info("Publicado en Hub: {}", pub))
                .doOnError(err -> {
                    if (!(err instanceof InternalServerException) || isRetry) {
                        log.error("Error publicando en Hub", err);
                    }
                });
    }

    private Mono<Void> makeRequest(ImmutableEvent pub, String token) {
        return webClient.post()
                .uri("/hub/publish")
                .header("Authorization", "Bearer " + token)
                .bodyValue(pub)
                .retrieve()
                .onStatus(status -> status.value() == 401,
                        response -> Mono.error(new UnauthorizedException("Unauthorized - token expired or invalid")))
                .onStatus(HttpStatusCode::is4xxClientError,
                        response -> Mono.error(new BadRequestException("Client error: " + response.statusCode().value())))
                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new InternalServerException("Server error: " + response.statusCode().value())))
                .toBodilessEntity()
                .doOnNext(response -> log.info("Core hub response status code {}", response.getStatusCode()))
                .then();
    }
}
