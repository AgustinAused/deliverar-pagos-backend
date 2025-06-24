package com.deliverar.pagos.domain.usecases.user;

import com.deliverar.pagos.domain.entities.FiatTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.Optional;

@FunctionalInterface
public interface GetAllFiatTransactionsByDate {
    Page<FiatTransaction> get(Optional<Instant> sinceDate, int pageNumber, int pageSize, Sort.Direction direction);
} 