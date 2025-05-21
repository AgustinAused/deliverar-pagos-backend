package com.deliverar.pagos.domain.dtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequest {
    private String fromEmail;
    private String toEmail;
    private BigDecimal amount;
}