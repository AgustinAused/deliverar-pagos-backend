package com.deliverar.pagos.domain.usecases.user.impl;

import com.deliverar.pagos.domain.entities.Owner;
import com.deliverar.pagos.domain.entities.Transaction;
import com.deliverar.pagos.domain.repositories.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultGetTransactionsTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private DefaultGetTransactions getTransactions;

    private Transaction tx1;
    private Transaction tx2;

    @BeforeEach
    void setUp() {
        UUID id1 = UUID.randomUUID();
        Owner owner1 = Owner.builder()
                .id(id1)
                .name("Alice")
                .email("alice@example.com")
                .build();
        UUID id2 = UUID.randomUUID();
        Owner owner2 = Owner.builder()
                .id(id2)
                .name("David")
                .email("david@example.com")
                .build();


        tx1 = Transaction.builder()
                .id(UUID.randomUUID())
                .originOwner(owner1)
                .destinationOwner(owner2)
                .amount(BigInteger.TEN)
                .transactionDate(Instant.now())
                .build();
        tx2 = Transaction.builder()
                .id(UUID.randomUUID())
                .originOwner(owner1)
                .destinationOwner(owner2)
                .amount(BigInteger.TWO)
                .transactionDate(Instant.now())
                .build();
    }

    @Test
    void get_ShouldReturnPageOfTransactions_WithCorrectPageable() {
        int page = 0, size = 5;
        Sort.Direction dir = Sort.Direction.ASC;
        List<Transaction> content = List.of(tx1, tx2);
        Page<Transaction> stubPage = new PageImpl<>(content);
        when(transactionRepository.findAll(any(Pageable.class)))
                .thenReturn(stubPage);

        Page<Transaction> result = getTransactions.get(page, size, dir);

        assertSame(stubPage, result, "Expected the same Page returned from repository");
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(transactionRepository, times(1)).findAll(captor.capture());

        Pageable pg = captor.getValue();
        assertEquals(page, pg.getPageNumber(), "Page number should match");
        assertEquals(size, pg.getPageSize(), "Page size should match");
        assertEquals(dir, Objects.requireNonNull(pg.getSort().getOrderFor("transactionDate")).getDirection(),
                "Sort direction should match on 'transactionDate'");
    }

    @Test
    void get_ShouldReturnEmptyPage_WhenRepositoryReturnsEmpty() {
        when(transactionRepository.findAll(any(Pageable.class)))
                .thenReturn(Page.empty());

        Page<Transaction> result = getTransactions.get(0, 10, Sort.Direction.DESC);

        assertTrue(result.isEmpty(), "Expected an empty page when no transactions found");
        verify(transactionRepository).findAll(any(Pageable.class));
    }
}
