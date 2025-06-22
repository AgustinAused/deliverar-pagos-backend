package com.deliverar.pagos.adapters.rest.messaging.commands.strategies;

import com.deliverar.pagos.adapters.rest.messaging.commands.BaseCommand;
import com.deliverar.pagos.adapters.rest.messaging.commands.CommandResult;
import com.deliverar.pagos.adapters.rest.messaging.events.EventType;
import com.deliverar.pagos.adapters.rest.messaging.events.IncomingEvent;
import com.deliverar.pagos.domain.repositories.OwnerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WalletDeletionCommand extends BaseCommand {

    private final OwnerRepository ownerRepository;

    @Override
    public boolean canHandle(EventType eventType) {
        return EventType.WALLET_DELETION_REQUEST.equals(eventType);
    }

    @Override
    protected boolean validate(IncomingEvent event) {
        Map<String, Object> data = event.getData();
        return data != null && data.containsKey("email");
    }

    @Override
    protected CommandResult process(IncomingEvent event) {
        try {
            Map<String, Object> data = event.getData();
            String email = (String) data.get("email");

            // Find owner by email
            var ownerOptional = ownerRepository.findByEmail(email);
            if (ownerOptional.isEmpty()) {
                return CommandResult.buildFailure("Owner not found with email: " + email);
            }

            var owner = ownerOptional.get();

            // Delete the owner (which will cascade to delete the wallet)
            ownerRepository.delete(owner);

            // Build response with traceData if present
            Map<String, Object> responseData = new java.util.HashMap<>();
            responseData.put("email", email);
            responseData.put("deletedAt", java.time.Instant.now());

            // Add traceData if present in the request
            if (data.containsKey("traceData")) {
                responseData.put("traceData", data.get("traceData"));
            }

            return CommandResult.buildSuccess(responseData, "Wallet deleted successfully");

        } catch (Exception e) {
            log.error("Error processing wallet deletion command", e);
            return CommandResult.buildFailure("Failed to delete wallet: " + e.getMessage());
        }
    }
} 