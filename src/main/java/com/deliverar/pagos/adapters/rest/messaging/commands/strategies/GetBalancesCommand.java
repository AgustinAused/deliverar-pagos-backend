package com.deliverar.pagos.adapters.rest.messaging.commands.strategies;

import com.deliverar.pagos.adapters.rest.messaging.commands.BaseCommand;
import com.deliverar.pagos.adapters.rest.messaging.commands.CommandResult;
import com.deliverar.pagos.adapters.rest.messaging.core.EventPublisher;
import com.deliverar.pagos.adapters.rest.messaging.events.EventType;
import com.deliverar.pagos.adapters.rest.messaging.events.IncomingEvent;
import com.deliverar.pagos.adapters.rest.messaging.events.OutgoingEvent;
import com.deliverar.pagos.adapters.rest.messaging.events.EventStatus;
import com.deliverar.pagos.domain.entities.Owner;
import com.deliverar.pagos.domain.usecases.owner.GetOwnerByEmail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetBalancesCommand extends BaseCommand {

    private final GetOwnerByEmail getOwnerByEmailUseCase;
    private final EventPublisher eventPublisher;

    @Override
    public boolean canHandle(EventType eventType) {
        return EventType.GET_BALANCES_REQUEST.equals(eventType);
    }

    @Override
    protected boolean validate(IncomingEvent event) {
        Map<String, Object> payload = event.getPayload();
        return payload != null && payload.containsKey("email");
    }

    @Override
    protected CommandResult process(IncomingEvent event) {
        try {
            Map<String, Object> payload = event.getPayload();
            String email = (String) payload.get("email");

            // Validate owner
            var ownerOptional = getOwnerByEmailUseCase.get(email);
            if (ownerOptional.isEmpty()) {
                return CommandResult.buildFailure("Owner not found with email: " + email);
            }

            log.info("Get balances request initiated for email: {}", email);

            // Start async processing to get balances and publish result
            CompletableFuture.runAsync(() -> {
                processBalancesRetrieval(email, event);
            });

            // Return immediate success - the actual result will be published asynchronously
            return CommandResult.buildSuccess(null, "Get balances request initiated successfully");

        } catch (Exception e) {
            log.error("Error processing get balances command", e);
            return CommandResult.buildFailure("Failed to process get balances request: " + e.getMessage());
        }
    }

    /**
     * Asynchronously processes the balances retrieval and publishes the result
     */
    private void processBalancesRetrieval(String email, IncomingEvent originalEvent) {
        try {
            log.info("Starting to retrieve balances for email: {}", email);

            // Get owner
            var ownerOptional = getOwnerByEmailUseCase.get(email);
            if (ownerOptional.isEmpty()) {
                publishErrorResponse("Owner not found with email: " + email, originalEvent);
                return;
            }

            Owner owner = ownerOptional.get();

            // Build response according to documentation
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("email", email);
            response.put("fiatBalance", owner.getWallet().getFiatBalance());
            response.put("cryptoBalance", owner.getWallet().getCryptoBalance());
            response.put("lastUpdated", owner.getWallet().getUpdatedAt().toString());

            // Add traceData if present in the request
            if (originalEvent.getPayload().containsKey("traceData")) {
                response.put("traceData", originalEvent.getPayload().get("traceData"));
            }

            // Publish success response
            OutgoingEvent successEvent = OutgoingEvent.buildResponse(
                    originalEvent,
                    EventType.GET_BALANCES_RESPONSE,
                    response,
                    EventStatus.SUCCESS
            );
            eventPublisher.publish(successEvent);
            log.info("Get balances response published successfully for email: {}", email);

        } catch (Exception e) {
            log.error("Error in async balances retrieval for email: {}", email, e);
            publishErrorResponse("Failed to retrieve balances: " + e.getMessage(), originalEvent);
        }
    }

    /**
     * Publishes an error response
     */
    private void publishErrorResponse(String errorMessage, IncomingEvent originalEvent) {
        eventPublisher.publishError(originalEvent, errorMessage);
        log.error("Error response published: {}", errorMessage);
    }
} 