package com.deliverar.pagos.adapters.rest.messaging.commands.strategies;

import com.deliverar.pagos.adapters.rest.messaging.commands.AsyncBaseCommand;
import com.deliverar.pagos.adapters.rest.messaging.commands.CommandResult;
import com.deliverar.pagos.adapters.rest.messaging.commands.utils.OwnerTypeUtils;
import com.deliverar.pagos.adapters.rest.messaging.commands.utils.ResponseBuilder;
import com.deliverar.pagos.adapters.rest.messaging.commands.utils.ValidationUtils;
import com.deliverar.pagos.adapters.rest.messaging.core.EventPublisher;
import com.deliverar.pagos.adapters.rest.messaging.events.EventType;
import com.deliverar.pagos.adapters.rest.messaging.events.IncomingEvent;
import com.deliverar.pagos.domain.entities.Owner;
import com.deliverar.pagos.domain.entities.OwnerType;
import com.deliverar.pagos.domain.usecases.owner.CreateOwner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Component
public class WalletCreationCommand extends AsyncBaseCommand {

    private final CreateOwner createOwnerUseCase;

    public WalletCreationCommand(EventPublisher eventPublisher, CreateOwner createOwnerUseCase) {
        super(eventPublisher);
        this.createOwnerUseCase = createOwnerUseCase;
    }

    @Override
    public boolean canHandle(EventType eventType) {
        return EventType.WALLET_CREATION_REQUEST.equals(eventType);
    }

    @Override
    protected boolean validate(IncomingEvent event) {
        try {
            Map<String, Object> data = event.getData();
            ValidationUtils.validateRequiredFields(data, "name", "email");
            ValidationUtils.validateEmailFormat((String) data.get("email"));
            return true;
        } catch (IllegalArgumentException e) {
            log.warn("Validation failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    protected CommandResult process(IncomingEvent event) {
        try {
            Map<String, Object> data = event.getData();
            String name = (String) data.get("name");
            String email = (String) data.get("email");

            log.info("Wallet creation request initiated for email: {}", email);

            // Start async processing to create wallet and publish result
            processAsyncWithErrorHandling(() -> {
                processWalletCreation(name, email, data, event);
            }, event, "wallet creation");

            // Return immediate success - the actual result will be published asynchronously
            return CommandResult.buildSuccess(null, "Wallet creation request initiated successfully");

        } catch (Exception e) {
            log.error("Error processing wallet creation command", e);
            return CommandResult.buildFailure("Failed to process wallet creation request: " + e.getMessage());
        }
    }

    /**
     * Asynchronously processes the wallet creation and publishes the result
     */
    private void processWalletCreation(String name, String email, Map<String, Object> originalData, IncomingEvent originalEvent) {
        try {
            log.info("Starting to create wallet for email: {}", email);

            // Determine owner type based on origin module
            OwnerType ownerType = OwnerTypeUtils.determineOwnerType(originalData);
            log.info("Creating owner with type: {} for email: {}", ownerType, email);

            // Get initial balances using ValidationUtils
            BigDecimal initialFiatBalance = ValidationUtils.parseBigDecimal(originalData, "initialFiatBalance", BigDecimal.ZERO);
            BigDecimal initialCryptoBalance = ValidationUtils.parseBigDecimal(originalData, "initialCryptoBalance", BigDecimal.ZERO);

            // Create owner with wallet using the use case
            Owner owner = createOwnerUseCase.create(name, email, ownerType, initialFiatBalance, initialCryptoBalance);

            // Build response using ResponseBuilder
            Map<String, Object> response = ResponseBuilder.createResponse(originalData,
                    "name", owner.getName(),
                    "email", owner.getEmail(),
                    "createdAt", owner.getWallet().getCreatedAt().toString()
            );

            // Publish success response
            publishSuccessResponse(originalEvent, EventType.WALLET_CREATION_RESPONSE, response);
            log.info("Wallet creation response published successfully for email: {}", email);

        } catch (Exception e) {
            log.error("Error in async wallet creation for email: {}", email, e);
            publishErrorResponse(originalEvent, "Failed to create wallet: " + e.getMessage());
        }
    }
} 