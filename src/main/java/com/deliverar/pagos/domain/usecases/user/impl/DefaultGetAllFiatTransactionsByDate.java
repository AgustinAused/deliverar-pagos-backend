package com.deliverar.pagos.domain.usecases.user.impl;

import com.deliverar.pagos.domain.entities.FiatTransaction;
import com.deliverar.pagos.domain.repositories.FiatTransactionRepository;
import com.deliverar.pagos.domain.usecases.user.GetAllFiatTransactionsByDate;
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
public class DefaultGetAllFiatTransactionsByDate implements GetAllFiatTransactionsByDate {
    private final FiatTransactionRepository fiatTransactionRepository;

    @Override
    public Page<FiatTransaction> get(Optional<Instant> sinceDate, int pageNumber, int pageSize, Sort.Direction direction) {
        Pageable pageReq = PageRequest.of(pageNumber, pageSize, by(direction, "transactionDate"));
        
        if (sinceDate.isPresent()) {
            return fiatTransactionRepository.findAllByTransactionDateGreaterThanEqualWithOwner(
                    sinceDate.get(), pageReq);
        } else {
            return fiatTransactionRepository.findAllWithOwner(pageReq);
        }
    }
} 