package com.deliverar.pagos.domain.usecases.owner;

import com.deliverar.pagos.domain.entities.ExchangeOperation;
import com.deliverar.pagos.domain.entities.Owner;

import java.math.BigDecimal;

@FunctionalInterface
public interface ExchangeFiat {
    BigDecimal exchange(Owner owner, BigDecimal amount, ExchangeOperation exchangeOperation);
}
