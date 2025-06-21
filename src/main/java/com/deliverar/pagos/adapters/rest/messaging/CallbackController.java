package com.deliverar.pagos.adapters.rest.messaging;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/callback")
public class CallbackController {
    private static final Logger log = LoggerFactory.getLogger(CallbackController.class);
    private final ApplicationEventPublisher applicationEventPublisher;
    private final HubPublisher hubPublisher;

    public CallbackController(ApplicationEventPublisher applicationEventPublisher, HubPublisher hubPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.hubPublisher = hubPublisher;
    }

    @Operation(summary = "Verificar suscripción", description = "Endpoint para verificar la suscripción a eventos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Suscripción verificada exitosamente")
    })
    @GetMapping
    public ResponseEntity<String> verifySubscription(
            @Parameter(description = "Tópico de la suscripción") @RequestParam("topic") String topic,
            @Parameter(description = "Desafío de verificación") @RequestParam("challenge") String challenge) {
        log.info("Suscripción verificada: topic='{}', challenge='{}'", topic, challenge);
        return ResponseEntity.ok(challenge);
    }

    @Operation(summary = "Recibir evento", description = "Endpoint para recibir eventos del hub")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Evento procesado exitosamente"),
            @ApiResponse(responseCode = "200", description = "Evento procesado con errores")
    })
    @PostMapping
    public ResponseEntity<Void> receiveEvent(
            @Parameter(description = "Payload del evento") @RequestBody HubCallback callback) {
        try {
            log.info("Evento recibido: {}", callback);
            if (callback.event().equals("fail-testing")) {
                throw new RuntimeException("Test error");
            }
//            applicationEventPublisher.publishEvent(
//                new HubEvent(callback.event(), callback.data())
//            );
            return ResponseEntity.noContent().build();
        } catch (Exception ex) {
            log.error("Error procesando evento del hub, devolviendo 200 OK para evitar retry", ex);
            return ResponseEntity.ok().build();
        }
    }

    @PostMapping("/test")
    public ResponseEntity<String> testPublish(@RequestBody HubPublish pub) {
        try {
            hubPublisher.publish(pub).block();
            return ResponseEntity.ok("Publicado: " + pub);
        } catch (Exception ex) {
            log.error("Error publicando evento de prueba", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fallo al publicar evento de prueba");
        }
    }
}