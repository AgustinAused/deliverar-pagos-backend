package com.deliverar.pagos.domain.usecases.user;

import com.deliverar.pagos.domain.entities.FiatTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

@FunctionalInterface
public interface GetFiatTransactions {
    Page<FiatTransaction> get(int pageNumber, int pageSize, Sort.Direction direction);
}
