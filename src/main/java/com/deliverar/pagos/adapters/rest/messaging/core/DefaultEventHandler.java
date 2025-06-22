package com.deliverar.pagos.adapters.rest.messaging.core;

import com.deliverar.pagos.adapters.rest.messaging.commands.CommandManager;
import com.deliverar.pagos.adapters.rest.messaging.commands.CommandResult;
import com.deliverar.pagos.adapters.rest.messaging.events.EventStatus;
import com.deliverar.pagos.adapters.rest.messaging.events.EventType;
import com.deliverar.pagos.adapters.rest.messaging.events.IncomingEvent;
import com.deliverar.pagos.adapters.rest.messaging.events.OutgoingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class DefaultEventHandler implements EventHandler {

    private final CommandManager commandManager;
    private final EventPublisher eventPublisher;

    @Override
    public void handle(IncomingEvent event) {
        try {
            log.info("Handling event: {}", event.getTopic());

            CommandResult result = commandManager.executeCommand(event);

            if (result.isSuccess()) {
                publishSuccessResponse(event, result);
            } else {
                publishErrorResponse(event, result.getMessage());
            }

        } catch (Exception e) {
            log.error("Error handling event: {}", event.getTopic(), e);
            publishErrorResponse(event, "Internal server error");
        }
    }

    private void publishSuccessResponse(IncomingEvent event, CommandResult result) {
        EventType responseType = getResponseType(event.getTopic());
        OutgoingEvent response = OutgoingEvent.buildResponse(
                event,
                responseType,
                result.getData(),
                EventStatus.SUCCESS
        );
        eventPublisher.publish(response);
    }

    private void publishErrorResponse(IncomingEvent event, String errorMessage) {
        OutgoingEvent response = OutgoingEvent.buildResponse(
                event,
                EventType.ERROR_RESPONSE,
                Map.of("error", errorMessage),
                EventStatus.FAILURE
        );
        eventPublisher.publish(response);
    }

    private EventType getResponseType(String requestTopic) {
        String responseTopic = requestTopic.replace(".request", ".response");
        return EventType.fromTopic(responseTopic);
    }

    @Override
    public boolean canHandle(EventType eventType) {
        return true; // Default handler can handle all events
    }
} 