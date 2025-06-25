package com.deliverar.pagos.adapters.rest.messaging.commands;

import com.deliverar.pagos.adapters.rest.messaging.core.EventPublisher;
import com.deliverar.pagos.adapters.rest.messaging.events.EventStatus;
import com.deliverar.pagos.adapters.rest.messaging.events.EventType;
import com.deliverar.pagos.adapters.rest.messaging.events.IncomingEvent;
import com.deliverar.pagos.adapters.rest.messaging.events.OutgoingEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Base class for commands that need to publish events asynchronously to the Hub.
 * Provides common methods for async processing and event publishing.
 */
@Slf4j
public abstract class AsyncBaseCommand extends BaseCommand {

    protected final EventPublisher eventPublisher;

    protected AsyncBaseCommand(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Publishes a success response to the Hub.
     * 
     * @param originalEvent The original incoming event
     * @param responseType The type of response event to publish
     * @param payload The response payload
     */
    protected void publishSuccessResponse(IncomingEvent originalEvent, EventType responseType, Map<String, Object> payload) {
        try {
            OutgoingEvent successEvent = OutgoingEvent.buildResponse(
                    originalEvent,
                    responseType,
                    payload,
                    EventStatus.SUCCESS
            );
            eventPublisher.publish(successEvent);
            log.info("Success response published for event type: {}", responseType);
        } catch (Exception e) {
            log.error("Error publishing success response for event type: {}", responseType, e);
            publishErrorResponse(originalEvent, "Failed to publish success response: " + e.getMessage());
        }
    }

    /**
     * Publishes an error response to the Hub.
     * 
     * @param originalEvent The original incoming event
     * @param errorMessage The error message
     */
    protected void publishErrorResponse(IncomingEvent originalEvent, String errorMessage) {
        try {
            eventPublisher.publishError(originalEvent, errorMessage);
            log.error("Error response published: {}", errorMessage);
        } catch (Exception e) {
            log.error("Error publishing error response: {}", errorMessage, e);
        }
    }

    /**
     * Processes a task asynchronously using CompletableFuture.
     * 
     * @param asyncTask The task to execute asynchronously
     */
    protected void processAsync(Runnable asyncTask) {
        CompletableFuture.runAsync(() -> {
            try {
                asyncTask.run();
            } catch (Exception e) {
                log.error("Error in async task execution", e);
            }
        });
    }

    /**
     * Processes a task asynchronously with error handling for a specific event.
     * 
     * @param asyncTask The task to execute asynchronously
     * @param originalEvent The original event for error handling
     * @param operationName The name of the operation for logging
     */
    protected void processAsyncWithErrorHandling(Runnable asyncTask, IncomingEvent originalEvent, String operationName) {
        CompletableFuture.runAsync(() -> {
            try {
                log.info("Starting async {} operation", operationName);
                asyncTask.run();
                log.info("Completed async {} operation successfully", operationName);
            } catch (Exception e) {
                log.error("Error in async {} operation", operationName, e);
                publishErrorResponse(originalEvent, "Failed to complete " + operationName + ": " + e.getMessage());
            }
        });
    }
} 