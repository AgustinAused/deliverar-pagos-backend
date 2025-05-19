package com.deliverar.pagos.domain.usecases.owner.impl;

import com.deliverar.pagos.domain.entities.Owner;
import com.deliverar.pagos.domain.entities.Transaction;
import com.deliverar.pagos.domain.repositories.TransactionRepository;
import com.deliverar.pagos.domain.usecases.owner.GetOwnerTransactions;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import static org.springframework.data.domain.Sort.by;

@Component
@RequiredArgsConstructor
public class DefaultGetOwnerTransactions implements GetOwnerTransactions {
    private final TransactionRepository transactionRepository;

    @Override
    public Page<Transaction> get(Owner owner, int pageNumber, int pageSize, Sort.Direction direction) {
        Pageable pageReq = PageRequest.of(pageNumber, pageSize, by(direction, "transactionDate"));
        return transactionRepository
                .findByOriginOwner_IdOrDestinationOwner_Id(owner.getId(), owner.getId(), pageReq);
    }
}
