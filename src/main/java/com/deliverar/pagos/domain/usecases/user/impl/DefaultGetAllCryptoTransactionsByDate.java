package com.deliverar.pagos.domain.usecases.user.impl;

import com.deliverar.pagos.domain.entities.Transaction;
import com.deliverar.pagos.domain.repositories.TransactionRepository;
import com.deliverar.pagos.domain.usecases.user.GetAllCryptoTransactionsByDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

import static org.springframework.data.domain.Sort.by;

@Component
@RequiredArgsConstructor
public class DefaultGetAllCryptoTransactionsByDate implements GetAllCryptoTransactionsByDate {
    private final TransactionRepository transactionRepository;

    @Override
    public Page<Transaction> get(Optional<Instant> sinceDate, int pageNumber, int pageSize, Sort.Direction direction) {
        Pageable pageReq = PageRequest.of(pageNumber, pageSize, by(direction, "transactionDate"));
        
        if (sinceDate.isPresent()) {
            return transactionRepository.findAllByTransactionDateGreaterThanEqualWithOwners(
                    sinceDate.get(), pageReq);
        } else {
            return transactionRepository.findAllWithOwners(pageReq);
        }
    }
} 