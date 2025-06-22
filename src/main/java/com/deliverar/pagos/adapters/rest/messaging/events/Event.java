package com.deliverar.pagos.adapters.rest.messaging.events;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public abstract class Event {
    private String topic;
    private Map<String, Object> data;
    private String eventId;
    private LocalDateTime timestamp;

    public Event() {
        this.eventId = java.util.UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
    }

    public Event(String topic, Map<String, Object> data) {
        this.topic = topic;
        this.data = data;
        this.eventId = java.util.UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
    }
} 