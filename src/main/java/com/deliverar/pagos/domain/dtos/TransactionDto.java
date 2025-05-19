package com.deliverar.pagos.domain.dtos;

import com.deliverar.pagos.domain.entities.CurrencyType;
import com.deliverar.pagos.domain.entities.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDto {
    private UUID id;
    private OwnerDto originOwner;
    private OwnerDto destinationOwner;
    private BigDecimal amount;
    private CurrencyType currency;
    private BigDecimal conversionRate;
    private String concept;
    private String blockchainTxHash;
    private Instant transactionDate;
    private Instant createdAt;
    private TransactionStatus status;
}
