package com.deliverar.pagos.adapters.rest.messaging.commands.strategies;

import com.deliverar.pagos.adapters.rest.messaging.commands.AsyncBaseCommand;
import com.deliverar.pagos.adapters.rest.messaging.commands.CommandResult;
import com.deliverar.pagos.adapters.rest.messaging.commands.utils.ResponseBuilder;
import com.deliverar.pagos.adapters.rest.messaging.core.EventPublisher;
import com.deliverar.pagos.adapters.rest.messaging.events.EventType;
import com.deliverar.pagos.adapters.rest.messaging.events.IncomingEvent;
import com.deliverar.pagos.domain.entities.FiatTransaction;
import com.deliverar.pagos.domain.usecases.user.GetAllFiatTransactionsByDate;
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
public class GetAllFiatTransactionsCommand extends AsyncBaseCommand {

    private final GetAllFiatTransactionsByDate getAllFiatTransactionsByDateUseCase;

    public GetAllFiatTransactionsCommand(EventPublisher eventPublisher, GetAllFiatTransactionsByDate getAllFiatTransactionsByDateUseCase) {
        super(eventPublisher);
        this.getAllFiatTransactionsByDateUseCase = getAllFiatTransactionsByDateUseCase;
    }

    @Override
    public boolean canHandle(EventType eventType) {
        return EventType.GET_ALL_FIAT_TRANSACTIONS_REQUEST.equals(eventType);
    }

    @Override
    protected boolean validate(IncomingEvent event) {
        try {
            Map<String, Object> payload = event.getPayload();
            
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

            log.info("Get all fiat transactions request initiated");

            // Start async processing to get transactions and publish result
            processAsyncWithErrorHandling(() -> {
                processGetAllFiatTransactions(payload, event);
            }, event, "get all fiat transactions");

            // Return immediate success - the actual result will be published asynchronously
            return CommandResult.buildSuccess(null, "Get all fiat transactions request initiated successfully");

        } catch (Exception e) {
            log.error("Error processing get all fiat transactions command", e);
            return CommandResult.buildFailure("Failed to process get all fiat transactions request: " + e.getMessage());
        }
    }

    /**
     * Asynchronously processes the get all fiat transactions and publishes the result
     */
    private void processGetAllFiatTransactions(Map<String, Object> originalData, IncomingEvent originalEvent) {
        try {
            log.info("Starting to get all fiat transactions");

            // Parse transactionDateSince (optional)
            Optional<Instant> sinceDate = Optional.empty();
            if (originalData.containsKey("transactionDateSince") && originalData.get("transactionDateSince") != null) {
                String dateStr = originalData.get("transactionDateSince").toString();
                if (!dateStr.isEmpty()) {
                    sinceDate = Optional.of(Instant.parse(dateStr));
                    log.info("Filtering all fiat transactions since: {}", sinceDate.get());
                }
            }

            // Get all transactions using the new use case with date filtering
            Page<FiatTransaction> transactionsPage = getAllFiatTransactionsByDateUseCase.get(
                    sinceDate, 0, Integer.MAX_VALUE, Sort.Direction.DESC);

            // Convert transactions to response format
            List<Map<String, Object>> transactionsList = transactionsPage.getContent().stream()
                    .map(this::convertTransactionToMap)
                    .collect(Collectors.toList());

            // Build response according to documentation (exact fields)
            Map<String, Object> response = ResponseBuilder.createResponse(originalData,
                    "transactions", transactionsList,
                    "transactionDateSince", originalData.getOrDefault("transactionDateSince", "")
            );

            // Publish success response
            publishSuccessResponse(originalEvent, EventType.GET_ALL_FIAT_TRANSACTIONS_RESPONSE, response);
            log.info("Get all fiat transactions response published successfully");

        } catch (Exception e) {
            log.error("Error in async get all fiat transactions", e);
            publishErrorResponse(originalEvent, "Failed to get all fiat transactions: " + e.getMessage());
        }
    }

    /**
     * Converts a FiatTransaction entity to a Map for the response
     */
    private Map<String, Object> convertTransactionToMap(FiatTransaction transaction) {
        return Map.of(
                "email", transaction.getOwner().getEmail(),
                "amount", transaction.getAmount().toString(),
                "concept", transaction.getConcept().name(),
                "type", getTransactionType(transaction),
                "status", transaction.getStatus().name(),
                "transactionDate", transaction.getTransactionDate().toString()
        );
    }

    private String getTransactionType(FiatTransaction transaction) {
        if (transaction.getConcept() != null) {
            switch (transaction.getConcept()) {
                case DEPOSIT:
                    return "DEPOSIT";
                case WITHDRAWAL:
                    return "WITHDRAWAL";
                case PAYMENT:
                    return "PAYMENT";
                case RECEIPT:
                    return "RECEIPT";
                default:
                    return transaction.getConcept().name();
            }
        }
        return "PAYMENT"; // Default type
    }
} 