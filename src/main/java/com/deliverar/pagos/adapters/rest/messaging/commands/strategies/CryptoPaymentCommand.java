package com.deliverar.pagos.adapters.rest.messaging.commands.strategies;

import com.deliverar.pagos.adapters.rest.messaging.commands.BaseCommand;
import com.deliverar.pagos.adapters.rest.messaging.commands.CommandResult;
import com.deliverar.pagos.adapters.rest.messaging.events.EventType;
import com.deliverar.pagos.adapters.rest.messaging.events.IncomingEvent;
import com.deliverar.pagos.domain.entities.Owner;
import com.deliverar.pagos.domain.entities.Transaction;
import com.deliverar.pagos.domain.entities.TransactionStatus;
import com.deliverar.pagos.domain.entities.CurrencyType;
import com.deliverar.pagos.domain.repositories.OwnerRepository;
import com.deliverar.pagos.domain.repositories.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CryptoPaymentCommand extends BaseCommand {

    private final OwnerRepository ownerRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public boolean canHandle(EventType eventType) {
        return EventType.CRYPTO_PAYMENT_REQUEST.equals(eventType);
    }

    @Override
    protected boolean validate(IncomingEvent event) {
        Map<String, Object> data = event.getData();
        return data != null && 
               data.containsKey("from_email") && 
               data.containsKey("to_email") &&
               data.containsKey("amount");
    }

    @Override
    protected CommandResult process(IncomingEvent event) {
        try {
            Map<String, Object> data = event.getData();
            
            String fromEmail = (String) data.get("from_email");
            String toEmail = (String) data.get("to_email");
            BigDecimal amount = new BigDecimal(data.get("amount").toString());
            String description = (String) data.getOrDefault("description", "Crypto payment");

            // Validate sender
            var fromOwnerOptional = ownerRepository.findByEmail(fromEmail);
            if (fromOwnerOptional.isEmpty()) {
                return CommandResult.buildFailure("Sender not found with email: " + fromEmail);
            }

            // Validate recipient
            var toOwnerOptional = ownerRepository.findByEmail(toEmail);
            if (toOwnerOptional.isEmpty()) {
                return CommandResult.buildFailure("Recipient not found with email: " + toEmail);
            }

            Owner fromOwner = fromOwnerOptional.get();
            Owner toOwner = toOwnerOptional.get();

            // Check if sender has sufficient balance
            if (fromOwner.getWallet().getCryptoBalance().compareTo(amount) < 0) {
                return CommandResult.buildFailure("Insufficient crypto balance");
            }

            // Process the transfer
            fromOwner.getWallet().setCryptoBalance(
                fromOwner.getWallet().getCryptoBalance().subtract(amount)
            );
            fromOwner.getWallet().setUpdatedAt(Instant.now());

            toOwner.getWallet().setCryptoBalance(
                toOwner.getWallet().getCryptoBalance().add(amount)
            );
            toOwner.getWallet().setUpdatedAt(Instant.now());

            // Save both owners
            ownerRepository.save(fromOwner);
            ownerRepository.save(toOwner);

            // Create transaction record
            Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID())
                .originOwner(fromOwner)
                .destinationOwner(toOwner)
                .amount(amount)
                .currency(CurrencyType.CRYPTO)
                .conversionRate(BigDecimal.ONE) // 1:1 for crypto
                .concept(description)
                .transactionDate(Instant.now())
                .status(TransactionStatus.SUCCESS)
                .build();

            transactionRepository.save(transaction);

            // Build response
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("transaction_id", transaction.getId().toString());
            response.put("from_email", fromEmail);
            response.put("to_email", toEmail);
            response.put("amount", amount);
            response.put("currency", "CRYPTO");
            response.put("status", "COMPLETED");
            response.put("description", description);
            response.put("created_at", transaction.getTransactionDate());

            // Add traceData if present in the request
            if (data.containsKey("traceData")) {
                response.put("traceData", data.get("traceData"));
            }

            return CommandResult.buildSuccess(response, "Crypto payment completed successfully");

        } catch (Exception e) {
            log.error("Error processing crypto payment command", e);
            return CommandResult.buildFailure("Failed to process crypto payment: " + e.getMessage());
        }
    }
} 