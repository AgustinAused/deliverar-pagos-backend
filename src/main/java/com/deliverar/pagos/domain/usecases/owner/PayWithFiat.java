package com.deliverar.pagos.domain.usecases.owner;

import com.deliverar.pagos.domain.entities.Owner;

import java.math.BigDecimal;

@FunctionalInterface
public interface PayWithFiat {
    void pay(Owner fromOwner, Owner toOwner, BigDecimal amount);
} 