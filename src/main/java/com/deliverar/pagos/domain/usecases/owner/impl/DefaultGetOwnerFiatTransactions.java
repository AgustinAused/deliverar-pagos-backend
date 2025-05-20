package com.deliverar.pagos.domain.usecases.owner.impl;

import com.deliverar.pagos.domain.entities.FiatTransaction;
import com.deliverar.pagos.domain.entities.Owner;
import com.deliverar.pagos.domain.repositories.FiatTransactionRepository;
import com.deliverar.pagos.domain.usecases.owner.GetOwnerFiatTransactions;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import static org.springframework.data.domain.Sort.by;

@Component
@RequiredArgsConstructor
public class DefaultGetOwnerFiatTransactions implements GetOwnerFiatTransactions {
    private final FiatTransactionRepository fiatTransactionRepository;

    @Override
    public Page<FiatTransaction> get(Owner owner, int pageNumber, int pageSize, Sort.Direction direction) {
        Pageable pageReq = PageRequest.of(pageNumber, pageSize, by(direction, "transactionDate"));
        return fiatTransactionRepository.findByOwner_Id(owner.getId(), pageReq);
    }
}
