package com.deliverar.pagos.adapters.rest.messaging.commands.strategies;

import com.deliverar.pagos.adapters.rest.messaging.commands.AsyncBaseCommand;
import com.deliverar.pagos.adapters.rest.messaging.commands.CommandResult;
import com.deliverar.pagos.adapters.rest.messaging.commands.utils.ResponseBuilder;
import com.deliverar.pagos.adapters.rest.messaging.commands.utils.ValidationUtils;
import com.deliverar.pagos.adapters.rest.messaging.core.EventPublisher;
import com.deliverar.pagos.adapters.rest.messaging.events.EventType;
import com.deliverar.pagos.adapters.rest.messaging.events.IncomingEvent;
import com.deliverar.pagos.domain.entities.Owner;
import com.deliverar.pagos.domain.repositories.OwnerRepository;
import com.deliverar.pagos.domain.usecases.owner.GetOwnerByEmail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Component
public class WalletDeletionCommand extends AsyncBaseCommand {

    private final GetOwnerByEmail getOwnerByEmailUseCase;
    private final OwnerRepository ownerRepository;

    public WalletDeletionCommand(EventPublisher eventPublisher, GetOwnerByEmail getOwnerByEmailUseCase, OwnerRepository ownerRepository) {
        super(eventPublisher);
        this.getOwnerByEmailUseCase = getOwnerByEmailUseCase;
        this.ownerRepository = ownerRepository;
    }

    @Override
    public boolean canHandle(EventType eventType) {
        return EventType.WALLET_DELETION_REQUEST.equals(eventType);
    }

    @Override
    protected boolean validate(IncomingEvent event) {
        try {
            Map<String, Object> payload = event.getPayload();
            ValidationUtils.validateRequiredFields(payload, "email");
            ValidationUtils.validateEmailFormat((String) payload.get("email"));
            return true;
        } catch (IllegalArgumentException e) {
            log.warn("Validation failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    protected CommandResult process(IncomingEvent event) {
        try {
            Map<String, Object> payload = event.getPayload();
            String email = (String) payload.get("email");

            log.info("Wallet deletion request initiated for email: {}", email);

            // Start async processing to delete wallet and publish result
            processAsyncWithErrorHandling(() -> {
                processWalletDeletion(email, payload, event);
            }, event, "wallet deletion");

            // Return immediate success - the actual result will be published asynchronously
            return CommandResult.buildSuccess(null, "Wallet deletion request initiated successfully");

        } catch (Exception e) {
            log.error("Error processing wallet deletion command", e);
            return CommandResult.buildFailure("Failed to process wallet deletion request: " + e.getMessage());
        }
    }

    /**
     * Asynchronously processes the wallet deletion and publishes the result
     */
    private void processWalletDeletion(String email, Map<String, Object> originalData, IncomingEvent originalEvent) {
        try {
            log.info("Starting to delete wallet for email: {}", email);

            // Validate owner exists using ValidationUtils
            Owner owner = ValidationUtils.validateOwnerExists(getOwnerByEmailUseCase, email);

            // Delete the owner (which will cascade to delete the wallet)
            ownerRepository.delete(owner);

            // Build response according to documentation
            Map<String, Object> response = ResponseBuilder.createResponse(originalData,
                    "email", email,
                    "deletedAt", Instant.now().toString(),
                    "message", "Usuario eliminado exitosamente"
            );

            // Publish success response
            publishSuccessResponse(originalEvent, EventType.WALLET_DELETION_RESPONSE, response);
            log.info("Wallet deletion response published successfully for email: {}", email);

        } catch (Exception e) {
            log.error("Error in async wallet deletion for email: {}", email, e);
            publishErrorResponse(originalEvent, "Failed to delete wallet: " + e.getMessage());
        }
    }
} 