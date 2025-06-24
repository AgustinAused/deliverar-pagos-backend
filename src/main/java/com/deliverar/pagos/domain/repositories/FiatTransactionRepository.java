package com.deliverar.pagos.domain.repositories;

import com.deliverar.pagos.domain.entities.FiatTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface FiatTransactionRepository extends JpaRepository<FiatTransaction, UUID> {
    Page<FiatTransaction> findByOwner_Id(
            UUID ownerId,
            Pageable pageable
    );
    
    Page<FiatTransaction> findByOwner_IdAndTransactionDateGreaterThanEqual(
            UUID ownerId,
            Instant sinceDate,
            Pageable pageable
    );
}
