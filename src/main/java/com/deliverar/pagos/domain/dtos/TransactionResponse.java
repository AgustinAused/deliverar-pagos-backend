package com.deliverar.pagos.domain.dtos;

import lombok.Builder;
import lombok.Data;

import java.math.BigInteger;
import java.util.UUID;

@Data
@Builder
public class TransactionResponse {
    private UUID id;
    private String fromOwnerEmail;
    private String toOwnerEmail;
    private BigInteger amount;
    private String status;
    private String txHash;
    private String error;
}