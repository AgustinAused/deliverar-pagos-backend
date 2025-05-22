package com.deliverar.pagos.domain.usecases.user.impl;

import com.deliverar.pagos.domain.entities.FiatTransaction;
import com.deliverar.pagos.domain.repositories.FiatTransactionRepository;
import com.deliverar.pagos.domain.usecases.user.GetFiatTransactions;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import static org.springframework.data.domain.Sort.by;

@Component
@RequiredArgsConstructor
public class DefaultGetFiatTransactions implements GetFiatTransactions {
    private final FiatTransactionRepository fiatTransactionRepository;

    @Override
    public Page<FiatTransaction> get(int pageNumber, int pageSize, Sort.Direction direction) {
        Pageable pageReq = PageRequest.of(pageNumber, pageSize, by(direction, "transactionDate"));
        return fiatTransactionRepository.findAll(pageReq);
    }
}
