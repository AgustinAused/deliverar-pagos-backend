package com.deliverar.pagos.domain.dtos;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class TransactionResponse {
    private UUID id;
    private String fromOwnerEmail;
    private String toOwnerEmail;
    private BigDecimal amount;
    private String status;
    private String txHash;
    private String error;
}