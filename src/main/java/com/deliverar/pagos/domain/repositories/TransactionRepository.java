package com.deliverar.pagos.domain.repositories;

import com.deliverar.pagos.domain.entities.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Page<Transaction> findByOriginOwner_IdOrDestinationOwner_Id(
            UUID originOwnerId,
            UUID destinationOwnerId,
            Pageable pageable
    );
}
