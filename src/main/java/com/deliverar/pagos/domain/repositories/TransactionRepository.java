package com.deliverar.pagos.domain.repositories;

import com.deliverar.pagos.domain.entities.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Page<Transaction> findByOriginOwner_IdOrDestinationOwner_Id(
            UUID originOwnerId,
            UUID destinationOwnerId,
            Pageable pageable
    );
    
    @Query("SELECT t FROM Transaction t " +
           "JOIN FETCH t.originOwner " +
           "JOIN FETCH t.destinationOwner " +
           "WHERE (t.originOwner.id = :ownerId OR t.destinationOwner.id = :ownerId)")
    Page<Transaction> findByOriginOwner_IdOrDestinationOwner_IdWithOwners(
            @Param("ownerId") UUID ownerId,
            Pageable pageable
    );
    
    @Query("SELECT t FROM Transaction t " +
           "JOIN FETCH t.originOwner " +
           "JOIN FETCH t.destinationOwner " +
           "WHERE (t.originOwner.id = :ownerId OR t.destinationOwner.id = :ownerId) " +
           "AND t.transactionDate >= :sinceDate")
    Page<Transaction> findByOriginOwner_IdOrDestinationOwner_IdAndTransactionDateGreaterThanEqualWithOwners(
            @Param("ownerId") UUID ownerId,
            @Param("sinceDate") Instant sinceDate,
            Pageable pageable
    );
}
