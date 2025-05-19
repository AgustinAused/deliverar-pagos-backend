package com.deliverar.pagos.domain.usecases.owner;

import com.deliverar.pagos.domain.entities.Owner;
import com.deliverar.pagos.domain.entities.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

@FunctionalInterface
public interface GetOwnerTransactions {
    Page<Transaction> get(Owner owner, int pageNumber, int pageSize, Sort.Direction direction);
}
