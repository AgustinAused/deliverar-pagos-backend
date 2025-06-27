package com.deliverar.pagos.domain.usecases.owner;

import com.deliverar.pagos.domain.entities.Owner;

import java.math.BigDecimal;

@FunctionalInterface
public interface CreateOwner {
    Owner create(Owner owner, BigDecimal initialFiatBalance, BigDecimal initialCryptoBalance);
}
