package com.deliverar.pagos.domain.dtos;

import com.deliverar.pagos.domain.entities.ExchangeOperation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FiatExchangeRequest {
    private BigDecimal amount;
    private ExchangeOperation operation;
}
