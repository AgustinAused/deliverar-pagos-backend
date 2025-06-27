package com.deliverar.pagos.adapters.rest.messaging.core;

import com.deliverar.pagos.adapters.rest.messaging.commands.CommandManager;
import com.deliverar.pagos.adapters.rest.messaging.events.EventType;
import com.deliverar.pagos.adapters.rest.messaging.events.IncomingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventRouter {

    private final Map<EventType, EventHandler> handlers = new ConcurrentHashMap<>();
    private final CommandManager commandManager;
    private final EventPublisher eventPublisher;

    public void routeEvent(IncomingEvent event) {
        try {
            EventType eventType = EventType.fromTopic(event.getTopic());

            if (!event.validate()) {
                log.error("Invalid event received: {}", event);
                return;
            }

            EventHandler handler = handlers.get(eventType);
            if (handler != null) {
                handler.handle(event);
            } else {
                // Use default handler
                DefaultEventHandler defaultHandler = new DefaultEventHandler(commandManager, eventPublisher);
                defaultHandler.handle(event);
            }

        } catch (IllegalArgumentException e) {
            log.error("Unknown event type: {}", event.getTopic());
        } catch (Exception e) {
            log.error("Error routing event: {}", event.getTopic(), e);
        }
    }

    public void registerHandler(EventType eventType, EventHandler handler) {
        handlers.put(eventType, handler);
        log.info("Registered handler for event type: {}", eventType);
    }
} 