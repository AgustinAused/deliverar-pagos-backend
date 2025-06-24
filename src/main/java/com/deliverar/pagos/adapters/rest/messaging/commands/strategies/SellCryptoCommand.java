package com.deliverar.pagos.adapters.rest.messaging.commands.strategies;

import com.deliverar.pagos.adapters.crypto.service.DeliverCoinService;
import com.deliverar.pagos.adapters.rest.messaging.commands.BaseCommand;
import com.deliverar.pagos.adapters.rest.messaging.commands.CommandResult;
import com.deliverar.pagos.adapters.rest.messaging.core.EventPublisher;
import com.deliverar.pagos.adapters.rest.messaging.events.EventType;
import com.deliverar.pagos.adapters.rest.messaging.events.IncomingEvent;
import com.deliverar.pagos.adapters.rest.messaging.events.OutgoingEvent;
import com.deliverar.pagos.adapters.rest.messaging.events.EventStatus;
import com.deliverar.pagos.domain.entities.Owner;
import com.deliverar.pagos.domain.entities.Transaction;
import com.deliverar.pagos.domain.entities.TransactionStatus;
import com.deliverar.pagos.domain.usecases.owner.GetOwnerByEmail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class SellCryptoCommand extends BaseCommand {

    private final GetOwnerByEmail getOwnerByEmailUseCase;
    private final DeliverCoinService deliverCoinService;
    private final EventPublisher eventPublisher;

    private static final int MAX_WAIT_SECONDS = 60;
    private static final int POLL_INTERVAL_MS = 20000;

    @Override
    public boolean canHandle(EventType eventType) {
        return EventType.SELL_CRYPTO_REQUEST.equals(eventType);
    }

    @Override
    protected boolean validate(IncomingEvent event) {
        Map<String, Object> data = event.getData();
        return data != null &&
                data.containsKey("email") &&
                data.containsKey("amount");
    }

    @Override
    protected CommandResult process(IncomingEvent event) {
        try {
            Map<String, Object> data = event.getData();

            String email = (String) data.get("email");
            BigDecimal amount = new BigDecimal(data.get("amount").toString());

            // Validate owner using the use case
            var ownerOptional = getOwnerByEmailUseCase.get(email);
            if (ownerOptional.isEmpty()) {
                return CommandResult.buildFailure("Owner not found with email: " + email);
            }

            // Process the crypto sale using the service (returns transaction ID)
            UUID transactionId = deliverCoinService.sellCryptoForFiat(email, amount);
            log.info("Sell crypto transaction initiated with ID: {}", transactionId);

            // Start async processing to wait for final status and publish result
            CompletableFuture.runAsync(() -> {
                processTransactionCompletion(transactionId, email, amount, event);
            });

            // Return immediate success - the actual result will be published asynchronously when final status is reached
            return CommandResult.buildSuccess(null, "Crypto sale initiated successfully");

        } catch (Exception e) {
            log.error("Error processing sell crypto command", e);
            return CommandResult.buildFailure("Failed to process crypto sale: " + e.getMessage());
        }
    }

    /**
     * Asynchronously processes the transaction completion and publishes the result
     * Only publishes when the transaction reaches a final status (SUCCESS or FAILURE)
     */
    private void processTransactionCompletion(UUID transactionId, String email, BigDecimal amount, IncomingEvent originalEvent) {
        try {
            log.info("Starting to monitor transaction {} for final status", transactionId);
            
            // Wait for the transaction to reach final status (ignore PENDING)
            Transaction transaction = waitForFinalTransactionStatus(transactionId);

            if (transaction.getStatus() == TransactionStatus.FAILURE) {
                log.info("Transaction {} reached FAILURE status, publishing error response", transactionId);
                publishErrorResponse("Crypto sale failed: Transaction failed on blockchain", originalEvent);
                return;
            }

            log.info("Transaction {} reached SUCCESS status, publishing success response", transactionId);

            // Refresh owner data to get updated balances
            var ownerOptional = getOwnerByEmailUseCase.get(email);
            if (ownerOptional.isEmpty()) {
                publishErrorResponse("Owner not found after transaction completion", originalEvent);
                return;
            }

            Owner owner = ownerOptional.get();

            // Build response according to documentation
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("email", email);
            response.put("cryptoAmount", amount); // 1:1 conversion rate
            response.put("status", transaction.getStatus().name());
            response.put("blockchainTxHash", transaction.getBlockchainTxHash());
            response.put("transactionDate", transaction.getTransactionDate().toString());
            response.put("currentFiatBalance", owner.getWallet().getFiatBalance());
            response.put("currentCryptoBalance", owner.getWallet().getCryptoBalance());

            // Add traceData if present in the request
            if (originalEvent.getData().containsKey("traceData")) {
                response.put("traceData", originalEvent.getData().get("traceData"));
            }

            // Publish success response only when final status is reached
            OutgoingEvent successEvent = OutgoingEvent.buildResponse(
                    originalEvent,
                    EventType.SELL_CRYPTO_RESPONSE,
                    response,
                    EventStatus.SUCCESS
            );
            eventPublisher.publish(successEvent);
            log.info("Sell crypto response published successfully for transaction ID: {}", transactionId);

        } catch (Exception e) {
            log.error("Error in async transaction processing for ID: {}", transactionId, e);
            publishErrorResponse("Failed to process crypto sale: " + e.getMessage(), originalEvent);
        }
    }

    /**
     * Publishes an error response
     */
    private void publishErrorResponse(String errorMessage, IncomingEvent originalEvent) {
        eventPublisher.publishError(originalEvent, errorMessage);
        log.error("Error response published: {}", errorMessage);
    }

    /**
     * Waits for the transaction to reach a final status (SUCCESS or FAILURE)
     * Ignores PENDING status and continues polling until final status is reached
     *
     * @param transactionId The transaction ID to monitor
     * @return The transaction with final status
     * @throws RuntimeException if timeout is reached
     */
    private Transaction waitForFinalTransactionStatus(UUID transactionId) {
        Instant startTime = Instant.now();

        while (Duration.between(startTime, Instant.now()).getSeconds() < MAX_WAIT_SECONDS) {
            try {
                Transaction transaction = deliverCoinService.getTransferStatus(transactionId);

                if (transaction.getStatus() == TransactionStatus.SUCCESS ||
                        transaction.getStatus() == TransactionStatus.FAILURE) {
                    log.info("Transaction {} reached final status: {}", transactionId, transaction.getStatus());
                    return transaction;
                }

                log.debug("Transaction {} still PENDING, waiting for final status...", transactionId);
                Thread.sleep(POLL_INTERVAL_MS);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Transaction monitoring interrupted", e);
            } catch (Exception e) {
                log.error("Error checking transaction status for ID: {}", transactionId, e);
                throw new RuntimeException("Failed to check transaction status", e);
            }
        }

        throw new RuntimeException("Timeout waiting for transaction " + transactionId + " to reach final status");
    }
} 