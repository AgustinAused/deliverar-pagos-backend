package com.deliverar.pagos.adapters.rest.messaging.commands.strategies;

import com.deliverar.pagos.adapters.rest.messaging.commands.BaseCommand;
import com.deliverar.pagos.adapters.rest.messaging.commands.CommandResult;
import com.deliverar.pagos.adapters.rest.messaging.events.EventType;
import com.deliverar.pagos.adapters.rest.messaging.events.IncomingEvent;
import com.deliverar.pagos.domain.entities.ExchangeOperation;
import com.deliverar.pagos.domain.entities.Owner;
import com.deliverar.pagos.domain.usecases.owner.ExchangeFiat;
import com.deliverar.pagos.domain.usecases.owner.GetOwnerByEmail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class FiatDepositCommand extends BaseCommand {

    private final GetOwnerByEmail getOwnerByEmailUseCase;
    private final ExchangeFiat exchangeFiatUseCase;

    @Override
    public boolean canHandle(EventType eventType) {
        return EventType.FIAT_DEPOSIT_REQUEST.equals(eventType);
    }

    @Override
    protected boolean validate(IncomingEvent event) {
        Map<String, Object> data = event.getData();
        return data != null &&
                data.containsKey("email") &&
                data.containsKey("amount");
    }

    @Override
    protected CommandResult process(IncomingEvent event) {
        try {
            Map<String, Object> data = event.getData();

            String email = (String) data.get("email");
            BigDecimal amount = new BigDecimal(data.get("amount").toString());
            String description = (String) data.getOrDefault("description", "Fiat deposit");

            // Validate owner using the use case
            var ownerOptional = getOwnerByEmailUseCase.get(email);
            if (ownerOptional.isEmpty()) {
                return CommandResult.buildFailure("Owner not found with email: " + email);
            }

            Owner owner = ownerOptional.get();

            // Process the deposit using the use case
            BigDecimal newBalance = exchangeFiatUseCase.exchange(owner, amount, ExchangeOperation.INFLOW);

            // Build response
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("transaction_id", UUID.randomUUID().toString()); // The use case creates the transaction internally
            response.put("email", email);
            response.put("amount", amount);
            response.put("new_balance", newBalance);
            response.put("currency", "FIAT");
            response.put("concept", "DEPOSIT");
            response.put("description", description);
            response.put("status", "COMPLETED");
            response.put("created_at", Instant.now());

            // Add traceData if present in the request
            if (data.containsKey("traceData")) {
                response.put("traceData", data.get("traceData"));
            }

            return CommandResult.buildSuccess(response, "Fiat deposit completed successfully");

        } catch (Exception e) {
            log.error("Error processing fiat deposit command", e);
            return CommandResult.buildFailure("Failed to process fiat deposit: " + e.getMessage());
        }
    }
} 