package com.deliverar.pagos.adapters.rest.messaging.internal;

import com.deliverar.pagos.adapters.rest.messaging.core.EventRouter;
import com.deliverar.pagos.adapters.rest.messaging.core.dtos.Event;
import com.deliverar.pagos.adapters.rest.messaging.events.IncomingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubEventListener {

    private final EventRouter eventRouter;

    @EventListener
    public void onHubEvent(Event event) {
        try {
            log.info("Processing hub event asynchronously: {}", event);

            // Convertir Event interno a IncomingEvent
            IncomingEvent incomingEvent = new IncomingEvent(
                    event.getTopic(),
                    event.getData(),
                    generateCorrelationId(),
                    "external-hub"
            );

            // Enrutar evento para procesamiento asíncrono
            eventRouter.routeEvent(incomingEvent);

        } catch (Exception e) {
            log.error("Error processing hub event: {}", event, e);
        }
    }

    private String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }
}
