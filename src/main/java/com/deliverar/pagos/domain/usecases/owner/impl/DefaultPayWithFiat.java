package com.deliverar.pagos.domain.usecases.owner.impl;

import com.deliverar.pagos.domain.entities.*;
import com.deliverar.pagos.domain.exceptions.BadRequestException;
import com.deliverar.pagos.domain.repositories.FiatTransactionRepository;
import com.deliverar.pagos.domain.repositories.OwnerRepository;
import com.deliverar.pagos.domain.usecases.owner.PayWithFiat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class DefaultPayWithFiat implements PayWithFiat {
    private final OwnerRepository ownerRepository;
    private final FiatTransactionRepository fiatTransactionRepository;

    @Override
    @Transactional
    public void pay(Owner fromOwner, Owner toOwner, BigDecimal amount) {
        Objects.requireNonNull(fromOwner, "FromOwner cannot be null");
        Objects.requireNonNull(toOwner, "ToOwner cannot be null");
        Objects.requireNonNull(amount, "Amount cannot be null");

        // Validate different owners
        if (fromOwner.getId().equals(toOwner.getId())) {
            throw new BadRequestException("Cannot transfer to the same owner");
        }

        // Validate positive amount
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Transfer amount must be positive");
        }

        // Validate sufficient balance
        if (fromOwner.getWallet().getFiatBalance().compareTo(amount) < 0) {
            throw new BadRequestException("Insufficient fiat balance");
        }

        // Update balances
        fromOwner.getWallet().setFiatBalance(fromOwner.getWallet().getFiatBalance().subtract(amount));
        toOwner.getWallet().setFiatBalance(toOwner.getWallet().getFiatBalance().add(amount));

        // Create transaction records
        FiatTransaction fromTransaction = FiatTransaction.builder()
                .owner(fromOwner)
                .amount(amount.negate()) // Negative for outflow
                .currency(CurrencyType.FIAT)
                .concept(TransactionConcept.PAYMENT)
                .transactionDate(Instant.now())
                .status(TransactionStatus.SUCCESS)
                .build();

        FiatTransaction toTransaction = FiatTransaction.builder()
                .owner(toOwner)
                .amount(amount) // Positive for inflow
                .currency(CurrencyType.FIAT)
                .concept(TransactionConcept.RECEIPT)
                .transactionDate(Instant.now())
                .status(TransactionStatus.SUCCESS)
                .build();

        ownerRepository.save(fromOwner);
        ownerRepository.save(toOwner);
        fiatTransactionRepository.save(fromTransaction);
        fiatTransactionRepository.save(toTransaction);
    }
}
