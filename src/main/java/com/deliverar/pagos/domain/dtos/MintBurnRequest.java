package com.deliverar.pagos.domain.dtos;

import lombok.Data;

import java.math.BigInteger;


@Data
public class MintBurnRequest {
    private String email;
    private BigInteger amount;
}