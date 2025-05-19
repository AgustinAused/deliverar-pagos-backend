package com.deliverar.pagos.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "wallets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Builder.Default
    @Column(name = "fiat_balance", nullable = false, precision = 18, scale = 2)
    private BigDecimal fiatBalance = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "crypto_balance", nullable = false, precision = 18, scale = 8)
    private BigDecimal cryptoBalance = BigDecimal.ZERO;

    @CreatedDate
    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @LastModifiedDate
    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @OneToOne(mappedBy = "wallet")
    private Owner owner;
}
