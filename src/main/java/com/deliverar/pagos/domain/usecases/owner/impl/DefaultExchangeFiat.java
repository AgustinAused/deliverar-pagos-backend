package com.deliverar.pagos.domain.usecases.owner.impl;

import com.deliverar.pagos.domain.entities.*;
import com.deliverar.pagos.domain.exceptions.BadRequestException;
import com.deliverar.pagos.domain.repositories.FiatTransactionRepository;
import com.deliverar.pagos.domain.repositories.OwnerRepository;
import com.deliverar.pagos.domain.usecases.owner.ExchangeFiat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

import static com.deliverar.pagos.domain.entities.ExchangeOperation.INFLOW;

@Component
@RequiredArgsConstructor
public class DefaultExchangeFiat implements ExchangeFiat {
    private final OwnerRepository ownerRepository;
    private final FiatTransactionRepository fiatTransactionRepository;

    @Override
    public BigDecimal exchange(Owner owner, BigDecimal amount, ExchangeOperation exchangeOperation) {
        Objects.requireNonNull(owner, "Owner cannot be null");
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(exchangeOperation, "ExchangeOperation cannot be null");

        BigDecimal previousFiatBalance = owner.getWallet().getFiatBalance();

        FiatTransaction transaction = FiatTransaction.builder()
                .owner(owner)
                .amount(amount)
                .currency(CurrencyType.FIAT)
                .transactionDate(Instant.now())
                .status(TransactionStatus.SUCCESS)
                .build();

        if (exchangeOperation == INFLOW) {
            owner.getWallet().setFiatBalance(previousFiatBalance.add(amount));
            transaction.setConcept(TransactionConcept.DEPOSIT);
        } else {
            if (previousFiatBalance.compareTo(amount) < 0) {
                throw new BadRequestException("Insufficient fiat balance");
            }
            owner.getWallet().setFiatBalance(previousFiatBalance.subtract(amount));
            transaction.setConcept(TransactionConcept.WITHDRAWAL);
        }

        ownerRepository.save(owner);
        fiatTransactionRepository.save(transaction);
        return owner.getWallet().getFiatBalance();
    }
}
