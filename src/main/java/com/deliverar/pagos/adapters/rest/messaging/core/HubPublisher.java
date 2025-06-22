package com.deliverar.pagos.adapters.rest.messaging.core;

import com.deliverar.pagos.adapters.rest.messaging.core.dtos.ImmutableEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
        return webClient.post()
                .uri("/hub/publish")
                .bodyValue(pub)
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(r -> log.info("Publicado en Hub: {}", pub))
                .doOnError(err -> log.error("Error publicando en Hub", err))
                .then();
    }
}
