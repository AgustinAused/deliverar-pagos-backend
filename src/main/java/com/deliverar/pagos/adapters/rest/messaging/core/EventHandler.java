package com.deliverar.pagos.adapters.rest.messaging.core;

import com.deliverar.pagos.adapters.rest.messaging.events.EventType;
import com.deliverar.pagos.adapters.rest.messaging.events.IncomingEvent;

public interface EventHandler {
    void handle(IncomingEvent event);

    boolean canHandle(EventType eventType);
} 