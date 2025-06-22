package com.deliverar.pagos.adapters.rest.messaging.commands.strategies;

import com.deliverar.pagos.adapters.rest.messaging.commands.BaseCommand;
import com.deliverar.pagos.adapters.rest.messaging.commands.CommandResult;
import com.deliverar.pagos.adapters.rest.messaging.events.EventType;
import com.deliverar.pagos.adapters.rest.messaging.events.IncomingEvent;
import com.deliverar.pagos.domain.entities.Owner;
import com.deliverar.pagos.domain.entities.OwnerType;
import com.deliverar.pagos.domain.usecases.owner.CreateOwner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WalletCreationCommand extends BaseCommand {

    private final CreateOwner createOwnerUseCase;

    @Override
    public boolean canHandle(EventType eventType) {
        return EventType.WALLET_CREATION_REQUEST.equals(eventType);
    }

    @Override
    protected boolean validate(IncomingEvent event) {
        Map<String, Object> data = event.getData();
        return data != null &&
                data.containsKey("name") &&
                data.containsKey("email");
    }

    @Override
    protected CommandResult process(IncomingEvent event) {
        try {
            Map<String, Object> data = event.getData();

            String name = (String) data.get("name");
            String email = (String) data.get("email");

            BigDecimal initialFiatBalance = BigDecimal.ZERO;
            BigDecimal initialCryptoBalance = BigDecimal.ZERO;

            if (data.containsKey("initial_fiat_balance")) {
                initialFiatBalance = new BigDecimal(data.get("initial_fiat_balance").toString());
            }
            if (data.containsKey("initial_crypto_balance")) {
                initialCryptoBalance = new BigDecimal(data.get("initial_crypto_balance").toString());
            }

            // Create owner with wallet using the use case
            Owner owner = createOwnerUseCase.create(name, email, OwnerType.NATURAL);

            // Set initial balances
            owner.getWallet().setFiatBalance(initialFiatBalance);
            owner.getWallet().setCryptoBalance(initialCryptoBalance);
            owner.getWallet().setUpdatedAt(Instant.now());

            // Build response with traceData if present
            Map<String, Object> responseData = new java.util.HashMap<>();
            responseData.put("name", owner.getName());
            responseData.put("email", owner.getEmail());
            responseData.put("createdAt", owner.getWallet().getCreatedAt());

            // Add traceData if present in the request
            if (data.containsKey("traceData")) {
                responseData.put("traceData", data.get("traceData"));
            }

            return CommandResult.buildSuccess(responseData, "Wallet created successfully");

        } catch (Exception e) {
            log.error("Error processing wallet creation command", e);
            return CommandResult.buildFailure("Failed to create wallet: " + e.getMessage());
        }
    }
} 