package com.deliverar.pagos.application.mappers;

import com.deliverar.pagos.domain.dtos.FiatTransactionDto;
import com.deliverar.pagos.domain.dtos.OwnerDto;
import com.deliverar.pagos.domain.dtos.TransactionDto;
import com.deliverar.pagos.domain.entities.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OwnerMapperTest {
    private OwnerMapper mapper;
    private Owner owner;
    private Transaction tx;
    private FiatTransaction fiatTx;

    @BeforeEach
    void setUp() {
        mapper = new OwnerMapper();
        UUID id = UUID.randomUUID();
        owner = Owner.builder()
                .id(id)
                .name("Test User")
                .email("test@example.com")
                .ownerType(OwnerType.NATURAL)
                .build();
        tx = Transaction.builder()
                .id(UUID.randomUUID())
                .originOwner(owner)
                .destinationOwner(owner)
                .amount(new BigDecimal("123"))
                .currency(CurrencyType.FIAT)
                .conversionRate(new BigDecimal("1.0"))
                .concept("payment")
                .blockchainTxHash("hash123")
                .transactionDate(Instant.now())
                .createdAt(Instant.now())
                .status(TransactionStatus.SUCCESS)
                .build();
        fiatTx = FiatTransaction.builder()
                .id(UUID.randomUUID())
                .owner(owner)
                .amount(new BigDecimal("67.89"))
                .currency(CurrencyType.FIAT)
                .concept(TransactionConcept.DEPOSIT)
                .transactionDate(Instant.now())
                .createdAt(Instant.now())
                .status(TransactionStatus.SUCCESS)
                .build();
    }

    @Test
    void toOwnerDto_MapsFieldsCorrectly() {
        OwnerDto dto = mapper.toOwnerDto(owner);
        assertEquals(owner.getId(), dto.getId());
        assertEquals(owner.getName(), dto.getName());
        assertEquals(owner.getEmail(), dto.getEmail());
        assertEquals(owner.getOwnerType(), dto.getOwnerType());
    }

    @Test
    void toTransactionDto_MapsAllFields() {
        TransactionDto dto = mapper.toTransactionDto(tx);
        assertEquals(tx.getId(), dto.getId());
        assertEquals(owner.getId(), dto.getOriginOwner().getId());
        assertEquals(owner.getId(), dto.getDestinationOwner().getId());
        assertEquals(tx.getAmount(), dto.getAmount());
        assertEquals(tx.getCurrency(), dto.getCurrency());
        assertEquals(tx.getConversionRate(), dto.getConversionRate());
        assertEquals(tx.getConcept(), dto.getConcept());
        assertEquals(tx.getBlockchainTxHash(), dto.getBlockchainTxHash());
        assertEquals(tx.getTransactionDate(), dto.getTransactionDate());
        assertEquals(tx.getCreatedAt(), dto.getCreatedAt());
        assertEquals(tx.getStatus(), dto.getStatus());
    }

    @Test
    void toTransactionDtos_ConvertsList() {
        List<TransactionDto> list = mapper.toTransactionDtos(List.of(tx, tx));
        assertEquals(2, list.size());
        assertEquals(tx.getId(), list.get(0).getId());
    }

    @Test
    void toFiatTransactionDto_MapsAllFields() {
        FiatTransactionDto dto = mapper.toFiatTransactionDto(fiatTx);
        assertEquals(fiatTx.getId(), dto.getId());
        assertEquals(owner.getId(), dto.getOwner().getId());
        assertEquals(fiatTx.getAmount().stripTrailingZeros(), dto.getAmount().stripTrailingZeros());
        assertEquals(fiatTx.getCurrency(), dto.getCurrency());
        assertEquals(fiatTx.getConcept().name(), dto.getConcept());
        assertEquals(fiatTx.getTransactionDate(), dto.getTransactionDate());
        assertEquals(fiatTx.getCreatedAt(), dto.getCreatedAt());
        assertEquals(fiatTx.getStatus(), dto.getStatus());
    }

    @Test
    void toFiatTransactionDtos_ConvertsList() {
        List<FiatTransactionDto> list = mapper.toFiatTransactionDtos(List.of(fiatTx));
        assertEquals(1, list.size());
        assertEquals(fiatTx.getId(), list.get(0).getId());
    }
}