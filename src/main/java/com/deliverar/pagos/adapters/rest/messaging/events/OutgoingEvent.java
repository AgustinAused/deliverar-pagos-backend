package com.deliverar.pagos.adapters.rest.messaging.events;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class OutgoingEvent extends Event {
    private String correlationId;
    private String target;
    private EventStatus status;

    public OutgoingEvent(String topic, Map<String, Object> payload, String correlationId, String target, EventStatus status) {
        super(topic, payload);
        this.correlationId = correlationId;
        this.target = target;
        this.status = status;
    }

    public static OutgoingEvent buildResponse(IncomingEvent incomingEvent, EventType responseType, Object payload, EventStatus status) {
        Map<String, Object> eventPayload;
        if (payload instanceof Map) {
            eventPayload = (Map<String, Object>) payload;
        } else {
            eventPayload = Map.of("value", payload);
        }
        
        return new OutgoingEvent(
                responseType.getTopic(),
                eventPayload,
                incomingEvent.getCorrelationId(),
                incomingEvent.getSource(),
                status
        );
    }
} 