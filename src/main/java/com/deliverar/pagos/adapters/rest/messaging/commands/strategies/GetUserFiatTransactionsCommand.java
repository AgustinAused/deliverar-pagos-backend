package com.deliverar.pagos.adapters.rest.messaging.commands.strategies;

import com.deliverar.pagos.adapters.rest.messaging.commands.AsyncBaseCommand;
import com.deliverar.pagos.adapters.rest.messaging.commands.CommandResult;
import com.deliverar.pagos.adapters.rest.messaging.commands.utils.ResponseBuilder;
import com.deliverar.pagos.adapters.rest.messaging.commands.utils.ValidationUtils;
import com.deliverar.pagos.adapters.rest.messaging.core.EventPublisher;
import com.deliverar.pagos.adapters.rest.messaging.events.EventType;
import com.deliverar.pagos.adapters.rest.messaging.events.IncomingEvent;
import com.deliverar.pagos.domain.entities.FiatTransaction;
import com.deliverar.pagos.domain.entities.Owner;
import com.deliverar.pagos.domain.usecases.owner.GetOwnerByEmail;
import com.deliverar.pagos.domain.usecases.owner.GetOwnerFiatTransactionsByDate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class GetUserFiatTransactionsCommand extends AsyncBaseCommand {

    private final GetOwnerByEmail getOwnerByEmailUseCase;
    private final GetOwnerFiatTransactionsByDate getOwnerFiatTransactionsByDateUseCase;

    public GetUserFiatTransactionsCommand(EventPublisher eventPublisher, GetOwnerByEmail getOwnerByEmailUseCase, GetOwnerFiatTransactionsByDate getOwnerFiatTransactionsByDateUseCase) {
        super(eventPublisher);
        this.getOwnerByEmailUseCase = getOwnerByEmailUseCase;
        this.getOwnerFiatTransactionsByDateUseCase = getOwnerFiatTransactionsByDateUseCase;
    }

    @Override
    public boolean canHandle(EventType eventType) {
        return EventType.GET_USER_FIAT_TRANSACTIONS_REQUEST.equals(eventType);
    }

    @Override
    protected boolean validate(IncomingEvent event) {
        try {
            Map<String, Object> payload = event.getPayload();
            ValidationUtils.validateRequiredFields(payload, "email");
            ValidationUtils.validateEmailFormat((String) payload.get("email"));
            
            // Validate transactionDateSince if present (optional)
            if (payload.containsKey("transactionDateSince") && payload.get("transactionDateSince") != null) {
                log.info("Transaction dates since: {}", payload.get("transactionDateSince"));
                String dateStr = payload.get("transactionDateSince").toString();
                if (!dateStr.isEmpty()) {
                    Instant.parse(dateStr); // Validate date format
                }
            }
            
            return true;
        } catch (IllegalArgumentException | DateTimeParseException e) {
            log.warn("Validation failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    protected CommandResult process(IncomingEvent event) {
        try {
            Map<String, Object> payload = event.getPayload();
            String email = (String) payload.get("email");

            log.info("Get user fiat transactions request initiated for email: {}", email);

            // Start async processing to get transactions and publish result
            processAsyncWithErrorHandling(() -> {
                processGetUserFiatTransactions(email, payload, event);
            }, event, "get user fiat transactions");

            // Return immediate success - the actual result will be published asynchronously
            return CommandResult.buildSuccess(null, "Get user fiat transactions request initiated successfully");

        } catch (Exception e) {
            log.error("Error processing get user fiat transactions command", e);
            return CommandResult.buildFailure("Failed to process get user fiat transactions request: " + e.getMessage());
        }
    }

    /**
     * Asynchronously processes the get user fiat transactions and publishes the result
     */
    private void processGetUserFiatTransactions(String email, Map<String, Object> originalData, IncomingEvent originalEvent) {
        try {
            log.info("Starting to get fiat transactions for email: {}", email);

            // Validate owner exists using ValidationUtils
            Owner owner = ValidationUtils.validateOwnerExists(getOwnerByEmailUseCase, email);

            // Parse transactionDateSince (optional)
            Optional<Instant> sinceDate = Optional.empty();
            if (originalData.containsKey("transactionDateSince") && originalData.get("transactionDateSince") != null) {
                String dateStr = originalData.get("transactionDateSince").toString();
                if (!dateStr.isEmpty()) {
                    sinceDate = Optional.of(Instant.parse(dateStr));
                    log.info("Filtering transactions since: {}", sinceDate.get());
                }
            }

            // Get transactions using the new use case with date filtering
            Page<FiatTransaction> transactionsPage = getOwnerFiatTransactionsByDateUseCase.get(
                    owner, sinceDate, 0, Integer.MAX_VALUE, Sort.Direction.DESC);

            // Convert transactions to response format
            List<Map<String, Object>> transactionsList = transactionsPage.getContent().stream()
                    .map(this::convertTransactionToMap)
                    .collect(Collectors.toList());

            // Build response according to documentation (exact fields)
            Map<String, Object> response = ResponseBuilder.createResponse(originalData,
                    "email", email,
                    "transactions", transactionsList,
                    "transactionDateSince", originalData.getOrDefault("transactionDateSince", "")
            );

            // Publish success response
            publishSuccessResponse(originalEvent, EventType.GET_USER_FIAT_TRANSACTIONS_RESPONSE, response);
            log.info("Get user fiat transactions response published successfully for email: {}", email);

        } catch (Exception e) {
            log.error("Error in async get user fiat transactions for email: {}", email, e);
            publishErrorResponse(originalEvent, "Failed to get user fiat transactions: " + e.getMessage());
        }
    }

    /**
     * Converts a FiatTransaction entity to a Map for the response
     */
    private Map<String, Object> convertTransactionToMap(FiatTransaction transaction) {
        return Map.of(
                "amount", transaction.getAmount(),
                "concept", transaction.getConcept().name(),
                "type", transaction.getConcept().name(), // Using concept as type
                "status", transaction.getStatus().name(),
                "transactionDate", transaction.getTransactionDate().toString()
        );
    }
} 