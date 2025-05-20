package com.deliverar.pagos.domain.usecases.owner;

import com.deliverar.pagos.domain.entities.FiatTransaction;
import com.deliverar.pagos.domain.entities.Owner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

@FunctionalInterface
public interface GetOwnerFiatTransactions {
    Page<FiatTransaction> get(Owner owner, int pageNumber, int pageSize, Sort.Direction direction);
}
