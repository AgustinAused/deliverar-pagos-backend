package com.deliverar.pagos.domain.usecases.owner.impl;

import com.deliverar.pagos.domain.entities.Transaction;
import com.deliverar.pagos.domain.repositories.TransactionRepository;
import com.deliverar.pagos.domain.usecases.owner.GetTransactions;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import static org.springframework.data.domain.Sort.by;

@Component
@RequiredArgsConstructor
public class DefaultGetTransactions implements GetTransactions {
    private final TransactionRepository transactionRepository;

    @Override
    public Page<Transaction> get(int pageNumber, int pageSize, Sort.Direction direction) {
        Pageable pageReq = PageRequest.of(pageNumber, pageSize, by(direction, "transactionDate"));
        return transactionRepository.findAll(pageReq);
    }
}
