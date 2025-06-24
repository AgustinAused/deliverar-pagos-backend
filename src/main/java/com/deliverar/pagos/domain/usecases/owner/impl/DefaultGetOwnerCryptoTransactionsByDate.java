package com.deliverar.pagos.domain.usecases.owner.impl;

import com.deliverar.pagos.domain.entities.Owner;
import com.deliverar.pagos.domain.entities.Transaction;
import com.deliverar.pagos.domain.repositories.TransactionRepository;
import com.deliverar.pagos.domain.usecases.owner.GetOwnerCryptoTransactionsByDate;
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
public class DefaultGetOwnerCryptoTransactionsByDate implements GetOwnerCryptoTransactionsByDate {
    private final TransactionRepository transactionRepository;

    @Override
    public Page<Transaction> get(Owner owner, Optional<Instant> sinceDate, int pageNumber, int pageSize, Sort.Direction direction) {
        Pageable pageReq = PageRequest.of(pageNumber, pageSize, by(direction, "transactionDate"));
        
        if (sinceDate.isPresent()) {
            return transactionRepository.findByOriginOwner_IdOrDestinationOwner_IdAndTransactionDateGreaterThanEqualWithOwners(
                    owner.getId(), sinceDate.get(), pageReq);
        } else {
            return transactionRepository.findByOriginOwner_IdOrDestinationOwner_IdWithOwners(
                    owner.getId(), pageReq);
        }
    }
} 