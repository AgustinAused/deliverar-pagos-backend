package com.deliverar.pagos.adapters.rest.messaging.commands.strategies;

import com.deliverar.pagos.adapters.rest.messaging.commands.AsyncBaseCommand;
import com.deliverar.pagos.adapters.rest.messaging.commands.CommandResult;
import com.deliverar.pagos.adapters.rest.messaging.commands.utils.ResponseBuilder;
import com.deliverar.pagos.adapters.rest.messaging.commands.utils.ValidationUtils;
import com.deliverar.pagos.adapters.rest.messaging.core.EventPublisher;
import com.deliverar.pagos.adapters.rest.messaging.events.EventType;
import com.deliverar.pagos.adapters.rest.messaging.events.IncomingEvent;
import com.deliverar.pagos.domain.entities.Owner;
import com.deliverar.pagos.domain.entities.Transaction;
import com.deliverar.pagos.domain.usecases.owner.GetOwnerByEmail;
import com.deliverar.pagos.domain.usecases.owner.GetOwnerCryptoTransactionsByDate;
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
public class GetUserCryptoTransactionsCommand extends AsyncBaseCommand {

    private final GetOwnerByEmail getOwnerByEmailUseCase;
    private final GetOwnerCryptoTransactionsByDate getOwnerCryptoTransactionsByDateUseCase;

    public GetUserCryptoTransactionsCommand(EventPublisher eventPublisher, GetOwnerByEmail getOwnerByEmailUseCase, GetOwnerCryptoTransactionsByDate getOwnerCryptoTransactionsByDateUseCase) {
        super(eventPublisher);
        this.getOwnerByEmailUseCase = getOwnerByEmailUseCase;
        this.getOwnerCryptoTransactionsByDateUseCase = getOwnerCryptoTransactionsByDateUseCase;
    }

    @Override
    public boolean canHandle(EventType eventType) {
        return EventType.GET_USER_CRYPTO_TRANSACTIONS_REQUEST.equals(eventType);
    }

    @Override
    protected boolean validate(IncomingEvent event) {
        try {
            Map<String, Object> data = event.getData();
            ValidationUtils.validateRequiredFields(data, "email");
            ValidationUtils.validateEmailFormat((String) data.get("email"));
            
            // Validate transactionDateSince if present (optional)
            if (data.containsKey("transactionDateSince") && data.get("transactionDateSince") != null) {
                log.info("Transaction dates since: {}", data.get("transactionDateSince"));
                String dateStr = data.get("transactionDateSince").toString();
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
            Map<String, Object> data = event.getData();
            String email = (String) data.get("email");

            log.info("Get user crypto transactions request initiated for email: {}", email);

            // Start async processing to get transactions and publish result
            processAsyncWithErrorHandling(() -> {
                processGetUserCryptoTransactions(email, data, event);
            }, event, "get user crypto transactions");

            // Return immediate success - the actual result will be published asynchronously
            return CommandResult.buildSuccess(null, "Get user crypto transactions request initiated successfully");

        } catch (Exception e) {
            log.error("Error processing get user crypto transactions command", e);
            return CommandResult.buildFailure("Failed to process get user crypto transactions request: " + e.getMessage());
        }
    }

    /**
     * Asynchronously processes the get user crypto transactions and publishes the result
     */
    private void processGetUserCryptoTransactions(String email, Map<String, Object> originalData, IncomingEvent originalEvent) {
        try {
            log.info("Starting to get crypto transactions for email: {}", email);

            // Validate owner exists using ValidationUtils
            Owner owner = ValidationUtils.validateOwnerExists(getOwnerByEmailUseCase, email);

            // Parse transactionDateSince (optional)
            Optional<Instant> sinceDate = Optional.empty();
            if (originalData.containsKey("transactionDateSince") && originalData.get("transactionDateSince") != null) {
                String dateStr = originalData.get("transactionDateSince").toString();
                if (!dateStr.isEmpty()) {
                    sinceDate = Optional.of(Instant.parse(dateStr));
                    log.info("Filtering crypto transactions since: {}", sinceDate.get());
                }
            }

            // Get transactions using the new use case with date filtering
            Page<Transaction> transactionsPage = getOwnerCryptoTransactionsByDateUseCase.get(
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
            publishSuccessResponse(originalEvent, EventType.GET_USER_CRYPTO_TRANSACTIONS_RESPONSE, response);
            log.info("Get user crypto transactions response published successfully for email: {}", email);

        } catch (Exception e) {
            log.error("Error in async get user crypto transactions for email: {}", email, e);
            publishErrorResponse(originalEvent, "Failed to get user crypto transactions: " + e.getMessage());
        }
    }

    /**
     * Converts a Transaction entity to a Map for the response
     */
    private Map<String, Object> convertTransactionToMap(Transaction transaction) {
        return Map.of(
                "id", transaction.getId().toString(),
                "fromEmail", transaction.getOriginOwner().getEmail(),
                "toEmail", transaction.getDestinationOwner().getEmail(),
                "amount", transaction.getAmount().toString(),
                "concept", transaction.getConcept(),
                "type", getTransactionType(transaction),
                "status", transaction.getStatus().name(),
                "blockchainTxHash", transaction.getBlockchainTxHash(),
                "transactionDate", transaction.getTransactionDate().toString()
        );
    }

    private String getTransactionType(Transaction transaction) {
        if (transaction.getConcept() != null) {
            if (transaction.getConcept().contains("BUY")) {
                return "BUY";
            } else if (transaction.getConcept().contains("SELL")) {
                return "SELL";
            } else if (transaction.getConcept().contains("TRANSFER") || transaction.getConcept().contains("PAYMENT")) {
                return "PAYMENT";
            }
        }
        return "PAYMENT"; // Default type
    }
} 