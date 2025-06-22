package com.deliverar.pagos.adapters.rest.messaging.core;

import com.deliverar.pagos.adapters.rest.messaging.core.dtos.ImmutableEvent;
import com.deliverar.pagos.adapters.rest.messaging.events.EventStatus;
import com.deliverar.pagos.adapters.rest.messaging.events.EventType;
import com.deliverar.pagos.adapters.rest.messaging.events.IncomingEvent;
import com.deliverar.pagos.adapters.rest.messaging.events.OutgoingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final HubPublisher hubPublisher;

    public void publish(OutgoingEvent event) {
        try {
            log.info("Publishing event: {}", event.getTopic());

            ImmutableEvent hubEvent = new ImmutableEvent(
                    event.getTopic(),
                    event.getData()
            );

            hubPublisher.publish(hubEvent)
                    .doOnSuccess(v -> log.info("Event published successfully: {}", event.getTopic()))
                    .doOnError(e -> log.error("Failed to publish event: {}", event.getTopic(), e))
                    .subscribe();

        } catch (Exception e) {
            log.error("Error publishing event: {}", event.getTopic(), e);
        }
    }

    public void publishError(IncomingEvent incomingEvent, String errorMessage) {
        OutgoingEvent errorEvent = OutgoingEvent.buildResponse(
                incomingEvent,
                EventType.ERROR_RESPONSE,
                Map.of("error", errorMessage),
                EventStatus.FAILURE
        );
        publish(errorEvent);
    }
} 