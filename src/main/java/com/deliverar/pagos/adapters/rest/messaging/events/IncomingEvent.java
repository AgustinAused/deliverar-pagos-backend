package com.deliverar.pagos.adapters.rest.messaging.events;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class IncomingEvent extends Event {
    private String correlationId;
    private String source;
    
    public IncomingEvent(String topic, Map<String, Object> payload, String correlationId, String source) {
        super(topic, payload);
        this.correlationId = correlationId;
        this.source = source;
    }
    
    public boolean validate() {
        return getTopic() != null && !getTopic().isEmpty() && 
               correlationId != null && !correlationId.isEmpty() &&
               source != null && !source.isEmpty();
    }
} 