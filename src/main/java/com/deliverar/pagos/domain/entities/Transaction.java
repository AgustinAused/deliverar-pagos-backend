package com.deliverar.pagos.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_owner_id", nullable = false)
    private Owner originOwner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_owner_id", nullable = false)
    private Owner destinationOwner;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private CurrencyType currency;

    @Column(name = "conversion_rate", nullable = false)
    private BigDecimal conversionRate;

    @Column(name = "concept")
    private String concept;

    @Column(name = "blockchain_tx_hash")
    private String blockchainTxHash;

    @Column(name = "transaction_date", nullable = false)
    private Instant transactionDate;

    @Builder.Default
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;
}
