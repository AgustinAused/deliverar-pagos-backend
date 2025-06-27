package com.deliverar.pagos.domain.usecases.owner.impl;

import com.deliverar.pagos.domain.entities.*;
import com.deliverar.pagos.domain.exceptions.BadRequestException;
import com.deliverar.pagos.domain.repositories.FiatTransactionRepository;
import com.deliverar.pagos.domain.repositories.OwnerRepository;
import com.deliverar.pagos.domain.usecases.owner.ExchangeFiat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;

import static com.deliverar.pagos.domain.entities.ExchangeOperation.INFLOW;

@Component
@RequiredArgsConstructor
public class DefaultExchangeFiat implements ExchangeFiat {
    private final OwnerRepository ownerRepository;
    private final FiatTransactionRepository fiatTransactionRepository;

    @Override
    public BigDecimal exchange(Owner owner, BigDecimal originalAmount, ExchangeOperation exchangeOperation) {
        Objects.requireNonNull(owner, "Owner cannot be null");
        Objects.requireNonNull(originalAmount, "Amount cannot be null");
        Objects.requireNonNull(exchangeOperation, "ExchangeOperation cannot be null");

        BigDecimal previousFiatBalance = owner.getWallet().getFiatBalance();

        BigDecimal formattedAmound = originalAmount.setScale(2, RoundingMode.HALF_UP);
        FiatTransaction transaction = FiatTransaction.builder()
                .owner(owner)
                .amount(formattedAmound)
                .currency(CurrencyType.FIAT)
                .transactionDate(Instant.now())
                .status(TransactionStatus.SUCCESS)
                .build();

        if (exchangeOperation == INFLOW) {
            owner.getWallet().setFiatBalance(previousFiatBalance.add(formattedAmound));
            transaction.setConcept(TransactionConcept.DEPOSIT);
        } else {
            if (previousFiatBalance.compareTo(formattedAmound) < 0) {
                throw new BadRequestException("Insufficient fiat balance");
            }
            owner.getWallet().setFiatBalance(previousFiatBalance.subtract(formattedAmound));
            transaction.setConcept(TransactionConcept.WITHDRAWAL);
        }

        ownerRepository.save(owner);
        fiatTransactionRepository.save(transaction);
        return owner.getWallet().getFiatBalance();
    }
}
