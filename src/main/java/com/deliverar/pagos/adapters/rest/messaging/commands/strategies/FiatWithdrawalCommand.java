package com.deliverar.pagos.adapters.rest.messaging.commands.strategies;

import com.deliverar.pagos.adapters.rest.messaging.commands.AsyncBaseCommand;
import com.deliverar.pagos.adapters.rest.messaging.commands.CommandResult;
import com.deliverar.pagos.adapters.rest.messaging.commands.utils.ResponseBuilder;
import com.deliverar.pagos.adapters.rest.messaging.commands.utils.ValidationUtils;
import com.deliverar.pagos.adapters.rest.messaging.core.EventPublisher;
import com.deliverar.pagos.adapters.rest.messaging.events.EventType;
import com.deliverar.pagos.adapters.rest.messaging.events.IncomingEvent;
import com.deliverar.pagos.domain.entities.ExchangeOperation;
import com.deliverar.pagos.domain.entities.Owner;
import com.deliverar.pagos.domain.usecases.owner.ExchangeFiat;
import com.deliverar.pagos.domain.usecases.owner.GetOwnerByEmail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Slf4j
@Component
public class FiatWithdrawalCommand extends AsyncBaseCommand {

    private final GetOwnerByEmail getOwnerByEmailUseCase;
    private final ExchangeFiat exchangeFiatUseCase;

    public FiatWithdrawalCommand(EventPublisher eventPublisher, GetOwnerByEmail getOwnerByEmailUseCase, ExchangeFiat exchangeFiatUseCase) {
        super(eventPublisher);
        this.getOwnerByEmailUseCase = getOwnerByEmailUseCase;
        this.exchangeFiatUseCase = exchangeFiatUseCase;
    }

    @Override
    public boolean canHandle(EventType eventType) {
        return EventType.FIAT_WITHDRAWAL_REQUEST.equals(eventType);
    }

    @Override
    protected boolean validate(IncomingEvent event) {
        try {
            Map<String, Object> payload = event.getPayload();
            ValidationUtils.validateRequiredFields(payload, "email", "amount");
            ValidationUtils.validateEmailFormat((String) payload.get("email"));
            
            BigDecimal amount = ValidationUtils.parseBigDecimal(payload, "amount", BigDecimal.ZERO);
            ValidationUtils.validatePositiveAmount(amount, "amount");
            
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
            BigDecimal amount = ValidationUtils.parseBigDecimal(payload, "amount", BigDecimal.ZERO);

            log.info("Fiat withdrawal request initiated for email: {} with amount: {}", email, amount);

            // Start async processing to process withdrawal and publish result
            processAsyncWithErrorHandling(() -> {
                processFiatWithdrawal(email, amount, payload, event);
            }, event, "fiat withdrawal");

            // Return immediate success - the actual result will be published asynchronously
            return CommandResult.buildSuccess(null, "Fiat withdrawal request initiated successfully");

        } catch (Exception e) {
            log.error("Error processing fiat withdrawal command", e);
            return CommandResult.buildFailure("Failed to process fiat withdrawal request: " + e.getMessage());
        }
    }

    /**
     * Asynchronously processes the fiat withdrawal and publishes the result
     */
    private void processFiatWithdrawal(String email, BigDecimal amount, Map<String, Object> originalData, IncomingEvent originalEvent) {
        try {
            log.info("Starting to process fiat withdrawal for email: {} with amount: {}", email, amount);

            // Validate owner exists using ValidationUtils
            Owner owner = ValidationUtils.validateOwnerExists(getOwnerByEmailUseCase, email);

            // Validate sufficient balance
            ValidationUtils.validateSufficientBalance(owner, amount, "fiat");

            // Process the withdrawal using the use case
            exchangeFiatUseCase.exchange(owner, amount, ExchangeOperation.OUTFLOW);

            // Get updated owner payload to get current balances
            Owner updatedOwner = ValidationUtils.validateOwnerExists(getOwnerByEmailUseCase, email);

            // Build response according to documentation
            Map<String, Object> response = ResponseBuilder.createResponse(originalData,
                    "email", email,
                    "amount", amount,
                    "concept", originalData.getOrDefault("concept", "Fiat withdrawal"),
                    "status", "SUCCESS",
                    "transactionDate", Instant.now().toString(),
                    "currentFiatBalance", updatedOwner.getWallet().getFiatBalance(),
                    "currentCryptoBalance", updatedOwner.getWallet().getCryptoBalance()
            );

            // Publish success response
            publishSuccessResponse(originalEvent, EventType.FIAT_WITHDRAWAL_RESPONSE, response);
            log.info("Fiat withdrawal response published successfully for email: {}", email);

        } catch (Exception e) {
            log.error("Error in async fiat withdrawal for email: {}", email, e);
            publishErrorResponse(originalEvent, "Failed to process fiat withdrawal: " + e.getMessage());
        }
    }
} 