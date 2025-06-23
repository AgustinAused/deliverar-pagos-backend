package com.deliverar.pagos.domain.usecases.owner.impl;

import com.deliverar.pagos.domain.entities.Owner;
import com.deliverar.pagos.domain.entities.OwnerType;
import com.deliverar.pagos.domain.entities.Wallet;
import com.deliverar.pagos.domain.repositories.OwnerRepository;
import com.deliverar.pagos.domain.usecases.owner.CreateOwner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class DefaultCreateOwner implements CreateOwner {
    private final OwnerRepository ownerRepository;

    @Override
    @Transactional
    public Owner create(String name, String email, OwnerType ownerType, BigDecimal initialFiatBalance, BigDecimal initialCryptoBalance) {
        Objects.requireNonNull(name, "Name cannot be null");
        Objects.requireNonNull(email, "Email cannot be null");
        Objects.requireNonNull(ownerType, "OwnerType cannot be null");
        Objects.requireNonNull(initialFiatBalance, "InitialFiatBalance cannot be null");
        Objects.requireNonNull(initialCryptoBalance, "InitialCryptoBalance cannot be null");

        Wallet wallet = Wallet.builder()
                .fiatBalance(initialFiatBalance)
                .cryptoBalance(initialCryptoBalance)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Owner owner = Owner.builder()
                .name(name)
                .email(email)
                .ownerType(ownerType)
                .wallet(wallet)
                .build();

        return ownerRepository.save(owner);
    }
}
