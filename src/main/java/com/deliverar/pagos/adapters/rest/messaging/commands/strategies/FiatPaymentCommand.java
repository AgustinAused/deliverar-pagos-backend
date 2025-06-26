package com.deliverar.pagos.adapters.rest.messaging.commands.strategies;

import com.deliverar.pagos.adapters.rest.messaging.commands.AsyncBaseCommand;
import com.deliverar.pagos.adapters.rest.messaging.commands.CommandResult;
import com.deliverar.pagos.adapters.rest.messaging.commands.utils.ResponseBuilder;
import com.deliverar.pagos.adapters.rest.messaging.commands.utils.ValidationUtils;
import com.deliverar.pagos.adapters.rest.messaging.core.EventPublisher;
import com.deliverar.pagos.adapters.rest.messaging.events.EventType;
import com.deliverar.pagos.adapters.rest.messaging.events.IncomingEvent;
import com.deliverar.pagos.domain.entities.Owner;
import com.deliverar.pagos.domain.usecases.owner.GetOwnerByEmail;
import com.deliverar.pagos.domain.usecases.owner.PayWithFiat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Slf4j
@Component
public class FiatPaymentCommand extends AsyncBaseCommand {

    private final GetOwnerByEmail getOwnerByEmailUseCase;
    private final PayWithFiat payWithFiatUseCase;

    public FiatPaymentCommand(EventPublisher eventPublisher, GetOwnerByEmail getOwnerByEmailUseCase, PayWithFiat payWithFiatUseCase) {
        super(eventPublisher);
        this.getOwnerByEmailUseCase = getOwnerByEmailUseCase;
        this.payWithFiatUseCase = payWithFiatUseCase;
    }

    @Override
    public boolean canHandle(EventType eventType) {
        return EventType.FIAT_PAYMENT_REQUEST.equals(eventType);
    }

    @Override
    protected boolean validate(IncomingEvent event) {
        try {
            Map<String, Object> payload = event.getPayload();
            ValidationUtils.validateRequiredFields(payload, "fromEmail", "toEmail", "amount");
            ValidationUtils.validateEmailFormat((String) payload.get("fromEmail"));
            ValidationUtils.validateEmailFormat((String) payload.get("toEmail"));
            
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
            String fromEmail = (String) payload.get("fromEmail");
            String toEmail = (String) payload.get("toEmail");
            BigDecimal amount = ValidationUtils.parseBigDecimal(payload, "amount", BigDecimal.ZERO);

            log.info("Fiat payment request initiated from {} to {} with amount: {}", fromEmail, toEmail, amount);

            // Start async processing to process payment and publish result
            processAsyncWithErrorHandling(() -> {
                processFiatPayment(fromEmail, toEmail, amount, payload, event);
            }, event, "fiat payment");

            // Return immediate success - the actual result will be published asynchronously
            return CommandResult.buildSuccess(null, "Fiat payment request initiated successfully");

        } catch (Exception e) {
            log.error("Error processing fiat payment command", e);
            return CommandResult.buildFailure("Failed to process fiat payment request: " + e.getMessage());
        }
    }

    /**
     * Asynchronously processes the fiat payment and publishes the result
     */
    private void processFiatPayment(String fromEmail, String toEmail, BigDecimal amount, Map<String, Object> originalData, IncomingEvent originalEvent) {
        try {
            log.info("Starting to process fiat payment from {} to {} with amount: {}", fromEmail, toEmail, amount);

            // Validate both owners exist using ValidationUtils
            Owner fromOwner = ValidationUtils.validateOwnerExists(getOwnerByEmailUseCase, fromEmail);
            Owner toOwner = ValidationUtils.validateOwnerExists(getOwnerByEmailUseCase, toEmail);

            // Process the transfer using the new use case
            payWithFiatUseCase.pay(fromOwner, toOwner, amount);

            // Get updated owner payload to get current balances
            Owner updatedFromOwner = ValidationUtils.validateOwnerExists(getOwnerByEmailUseCase, fromEmail);

            // Build response according to documentation
            Map<String, Object> response = ResponseBuilder.createResponse(originalData,
                    "fromEmail", fromEmail,
                    "toEmail", toEmail,
                    "amount", amount,
                    "concept", originalData.getOrDefault("concept", "Fiat payment"),
                    "status", "SUCCESS",
                    "transactionDate", Instant.now().toString(),
                    "currentFiatBalance", updatedFromOwner.getWallet().getFiatBalance(),
                    "currentCryptoBalance", updatedFromOwner.getWallet().getCryptoBalance()
            );

            // Publish success response
            publishSuccessResponse(originalEvent, EventType.FIAT_PAYMENT_RESPONSE, response);
            log.info("Fiat payment response published successfully from {} to {}", fromEmail, toEmail);

        } catch (Exception e) {
            log.error("Error in async fiat payment from {} to {}", fromEmail, toEmail, e);
            publishErrorResponse(originalEvent, "Failed to process fiat payment: " + e.getMessage());
        }
    }
} 