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
@Table(name = "fiat_transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FiatTransaction {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private Owner owner;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private CurrencyType currency;

    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionConcept concept;

    @Column(name = "transaction_date", nullable = false)
    private Instant transactionDate;

    @Builder.Default
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;
}
