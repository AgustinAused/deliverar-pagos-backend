package com.deliverar.pagos.adapters.rest.messaging.commands.strategies;

import com.deliverar.pagos.adapters.rest.messaging.commands.BaseCommand;
import com.deliverar.pagos.adapters.rest.messaging.commands.CommandResult;
import com.deliverar.pagos.adapters.rest.messaging.commands.utils.OwnerTypeUtils;
import com.deliverar.pagos.adapters.rest.messaging.events.EventType;
import com.deliverar.pagos.adapters.rest.messaging.events.IncomingEvent;
import com.deliverar.pagos.domain.entities.Owner;
import com.deliverar.pagos.domain.entities.OwnerType;
import com.deliverar.pagos.domain.usecases.owner.CreateOwner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserCreationCommand extends BaseCommand {

    private final CreateOwner createOwnerUseCase;

    @Override
    public boolean canHandle(EventType eventType) {
        return EventType.USER_CREATION_REQUEST.equals(eventType);
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

            // Determine owner type based on origin module
            OwnerType ownerType = OwnerTypeUtils.determineOwnerType(data);
            log.info("Creating owner with type: {} for email: {}", ownerType, email);

            // Get initial balances (default to zero if not provided)
            BigDecimal initialFiatBalance = BigDecimal.ZERO;
            BigDecimal initialCryptoBalance = BigDecimal.ZERO;

            if (data.containsKey("initialFiatBalance")) {
                initialFiatBalance = new BigDecimal(data.get("initialFiatBalance").toString());
            }
            if (data.containsKey("initialCryptoBalance")) {
                initialCryptoBalance = new BigDecimal(data.get("initialCryptoBalance").toString());
            }

            // Create owner using the use case (user creation = owner creation in this module)
            Owner owner = createOwnerUseCase.create(name, email, ownerType, initialFiatBalance, initialCryptoBalance);

            // Build response as Map - both user.creation and wallet.creation publish wallet.creation.response
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("name", owner.getName());
            response.put("email", owner.getEmail());
            response.put("createdAt", owner.getWallet().getCreatedAt().toString());

            // Add traceData if present in the request
            if (data.containsKey("traceData")) {
                response.put("traceData", data.get("traceData"));
            }

            return CommandResult.buildSuccess(response, "User created successfully");

        } catch (Exception e) {
            log.error("Error processing user creation command", e);
            return CommandResult.buildFailure("Failed to create user: " + e.getMessage());
        }
    }
} 