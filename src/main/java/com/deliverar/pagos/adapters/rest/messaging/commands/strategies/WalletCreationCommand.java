package com.deliverar.pagos.adapters.rest.messaging.commands.strategies;

import com.deliverar.pagos.adapters.rest.messaging.commands.AsyncBaseCommand;
import com.deliverar.pagos.adapters.rest.messaging.commands.CommandResult;
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

import static com.deliverar.pagos.adapters.rest.messaging.events.EventType.*;

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
        return TENANT_CREATION_REQUEST.equals(eventType)
                || DELIVERY_USER_CREATED_REQUEST.equals(eventType)
                || WALLET_CREATION_REQUEST.equals(eventType);
    }

    @Override
    protected boolean validate(IncomingEvent event) {
        EventType eventType = EventType.fromTopic(event.getTopic());
        try {
            Map<String, Object> payload = event.getPayload();
            switch (eventType) {
                case TENANT_CREATION_REQUEST:
                    ValidationUtils.validateRequiredFields(payload, "razon_social", "email");
                    break;
                case DELIVERY_USER_CREATED_REQUEST:
                    ValidationUtils.validateRequiredFields(payload, "nombre", "apellido", "email");
                    break;
                case WALLET_CREATION_REQUEST:
                default:
                    ValidationUtils.validateRequiredFields(payload, "name", "email");
                    break;
            }
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
            EventType eventType = EventType.fromTopic(event.getTopic());
            Owner owner = buildOwner(payload, eventType);

            log.info("User creation request initiated for email: {}", owner.getEmail());

            // Start async processing to create user and publish result
            processAsyncWithErrorHandling(() -> {
                processUserCreation(owner, payload, event);
            }, event, "user creation");

            // Return immediate success - the actual result will be published asynchronously
            return CommandResult.buildSuccess(null, "User creation request initiated successfully");

        } catch (Exception e) {
            log.error("Error processing user creation command", e);
            return CommandResult.buildFailure("Failed to process user creation request: " + e.getMessage());
        }
    }

    /**
     * Asynchronously processes the user creation and publishes the result
     */
    private void processUserCreation(Owner owner, Map<String, Object> originalData, IncomingEvent originalEvent) {
        try {
            log.info("Starting to create user for email: {}", owner.getEmail());

            log.info("Creating owner with type: {} for email: {}", owner.getOwnerType(), owner.getEmail());

            // Get initial balances using ValidationUtils
            BigDecimal initialFiatBalance = ValidationUtils.parseBigDecimal(originalData, "initialFiatBalance", BigDecimal.ZERO);
            BigDecimal initialCryptoBalance = BigDecimal.ZERO;

            // Create owner using the use case
            Owner createdOwner = createOwnerUseCase.create(owner, initialFiatBalance, initialCryptoBalance);

            // Build response using ResponseBuilder - both user.creation and wallet.creation publish wallet.creation.response
            Map<String, Object> response = ResponseBuilder.createResponse(originalData,
                    "name", createdOwner.getName(),
                    "email", createdOwner.getEmail(),
                    "createdAt", createdOwner.getWallet().getCreatedAt().toString()
            );

            // Publish success response
            publishSuccessResponse(originalEvent, EventType.WALLET_CREATION_RESPONSE, response);
            log.info("User creation response published successfully for email: {}", createdOwner.getEmail());

        } catch (Exception e) {
            log.error("Error in async user creation for email: {}", owner.getEmail(), e);
            publishErrorResponse(originalEvent, "Failed to create user: " + e.getMessage());
        }
    }

    private Owner buildOwner(Map<String, Object> payload, EventType eventType) {
        Owner owner = Owner.builder()
                .email((String) payload.get("email"))
                .build();

        if (TENANT_CREATION_REQUEST == eventType) {
            owner.setName((String) payload.get("razon_social"));
            owner.setOwnerType(OwnerType.TENANT);
        }

        if (DELIVERY_USER_CREATED_REQUEST == eventType) {
            owner.setName(String.format("%s %s", payload.get("nombre"), payload.get("apellido")));
            owner.setOwnerType(OwnerType.DELIVERY);
        }

        if (WALLET_CREATION_REQUEST == eventType) {
            owner.setName((String) payload.get("name"));
            owner.setOwnerType(OwnerType.CLIENT);
        }
        return owner;
    }
}
