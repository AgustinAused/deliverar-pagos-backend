package com.deliverar.pagos.domain.usecases.owner;

import com.deliverar.pagos.domain.entities.Owner;
import com.deliverar.pagos.domain.entities.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.Optional;

@FunctionalInterface
public interface GetOwnerCryptoTransactionsByDate {
    Page<Transaction> get(Owner owner, Optional<Instant> sinceDate, int pageNumber, int pageSize, Sort.Direction direction);
} 