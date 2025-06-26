package com.deliverar.pagos.adapters.rest.messaging.commands;

import com.deliverar.pagos.adapters.rest.messaging.events.EventType;
import com.deliverar.pagos.adapters.rest.messaging.events.IncomingEvent;

public interface Command {
    CommandResult execute(IncomingEvent event);

    boolean canHandle(EventType eventType);
} 