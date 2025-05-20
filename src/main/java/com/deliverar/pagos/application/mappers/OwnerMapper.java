package com.deliverar.pagos.application.mappers;

import com.deliverar.pagos.domain.dtos.FiatTransactionDto;
import com.deliverar.pagos.domain.dtos.OwnerDto;
import com.deliverar.pagos.domain.dtos.TransactionDto;
import com.deliverar.pagos.domain.entities.FiatTransaction;
import com.deliverar.pagos.domain.entities.Owner;
import com.deliverar.pagos.domain.entities.Transaction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OwnerMapper {
    public OwnerDto toOwnerDto(Owner entity) {
        return OwnerDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .ownerType(entity.getOwnerType())
                .build();
    }

    public TransactionDto toTransactionDto(Transaction entity) {
        return TransactionDto.builder()
                .id(entity.getId())
                .originOwner(toOwnerDto(entity.getOriginOwner()))
                .destinationOwner(toOwnerDto(entity.getDestinationOwner()))
                .amount(entity.getAmount())
                .currency(entity.getCurrency())
                .conversionRate(entity.getConversionRate())
                .concept(entity.getConcept())
                .blockchainTxHash(entity.getBlockchainTxHash())
                .transactionDate(entity.getTransactionDate())
                .createdAt(entity.getCreatedAt())
                .status(entity.getStatus())
                .build();
    }

    public List<TransactionDto> toTransactionDtos(List<Transaction> transactions) {
        return transactions.stream().map(this::toTransactionDto).collect(Collectors.toList());
    }

    public FiatTransactionDto toFiatTransactionDto(FiatTransaction entity) {
        return FiatTransactionDto.builder()
                .id(entity.getId())
                .owner(toOwnerDto(entity.getOwner()))
                .amount(BigDecimal.valueOf(entity.getAmount().doubleValue()))
                .currency(entity.getCurrency())
                .concept(entity.getConcept().name())
                .transactionDate(entity.getTransactionDate())
                .createdAt(entity.getCreatedAt())
                .status(entity.getStatus())
                .build();
    }

    public List<FiatTransactionDto> toFiatTransactionDtos(List<FiatTransaction> transactions) {
        return transactions.stream().map(this::toFiatTransactionDto).collect(Collectors.toList());
    }
}
