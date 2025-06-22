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
    
    public OutgoingEvent(String topic, Map<String, Object> data, String correlationId, String target, EventStatus status) {
        super(topic, data);
        this.correlationId = correlationId;
        this.target = target;
        this.status = status;
    }
    
    public static OutgoingEvent buildResponse(IncomingEvent incomingEvent, EventType responseType, Object data, EventStatus status) {
        return new OutgoingEvent(
            responseType.name(),
            Map.of("data", data),
            incomingEvent.getCorrelationId(),
            incomingEvent.getSource(),
            status
        );
    }
} 