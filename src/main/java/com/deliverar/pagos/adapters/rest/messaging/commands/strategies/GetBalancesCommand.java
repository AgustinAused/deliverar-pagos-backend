package com.deliverar.pagos.adapters.rest.messaging.commands.strategies;

import com.deliverar.pagos.adapters.rest.messaging.commands.BaseCommand;
import com.deliverar.pagos.adapters.rest.messaging.commands.CommandResult;
import com.deliverar.pagos.adapters.rest.messaging.events.EventType;
import com.deliverar.pagos.adapters.rest.messaging.events.IncomingEvent;
import com.deliverar.pagos.domain.entities.Owner;
import com.deliverar.pagos.domain.repositories.OwnerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetBalancesCommand extends BaseCommand {

    private final OwnerRepository ownerRepository;

    @Override
    public boolean canHandle(EventType eventType) {
        return EventType.GET_BALANCES_REQUEST.equals(eventType);
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

            Owner owner = ownerOptional.get();

            // Build response with balances
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("email", email);
            response.put("fiat_balance", owner.getWallet().getFiatBalance());
            response.put("crypto_balance", owner.getWallet().getCryptoBalance());
            response.put("currency", "USD"); // Assuming USD as default currency
            response.put("last_updated", owner.getWallet().getUpdatedAt());

            // Add traceData if present in the request
            if (data.containsKey("traceData")) {
                response.put("traceData", data.get("traceData"));
            }

            return CommandResult.buildSuccess(response, "Balances retrieved successfully");

        } catch (Exception e) {
            log.error("Error processing get balances command", e);
            return CommandResult.buildFailure("Failed to get balances: " + e.getMessage());
        }
    }
} 