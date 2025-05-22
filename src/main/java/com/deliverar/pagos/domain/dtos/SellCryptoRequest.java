package com.deliverar.pagos.domain.dtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SellCryptoRequest {
    private String email;
    private BigDecimal amount;
}
