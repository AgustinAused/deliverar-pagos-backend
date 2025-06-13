package com.deliverar.pagos.domain.dtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SendFiatRequest {
    private String senderEmail;
    private String recipientEmail;
    private BigDecimal amount;
}
