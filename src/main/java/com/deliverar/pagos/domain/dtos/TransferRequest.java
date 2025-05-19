package com.deliverar.pagos.domain.dtos;

import lombok.Data;

import java.math.BigInteger;

@Data
public class TransferRequest {
    private String fromEmail;
    private String toEmail;
    private BigInteger amount;
}