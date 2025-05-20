package com.deliverar.pagos.domain.usecases.owner.impl;

import com.deliverar.pagos.domain.entities.CurrencyType;
import com.deliverar.pagos.domain.entities.Owner;
import com.deliverar.pagos.domain.entities.Transaction;
import com.deliverar.pagos.domain.entities.TransactionStatus;
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
import org.springframework.data.domain.Sort;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultGetOwnerTransactionsTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private DefaultGetOwnerTransactions getTransactions;

    private Owner owner;

    @BeforeEach
    void setUp() {
        owner = Owner.builder()
                .id(UUID.randomUUID())
                .name("Bob")
                .email("bob@example.com")
                .build();
    }

    @Test
    void get_ShouldReturnPageOfTransactions_WithCorrectPageable() {
        int page = 1;
        int size = 2;
        Sort.Direction direction = Sort.Direction.DESC;
        Transaction t1 = Transaction.builder()
                .id(UUID.randomUUID())
                .originOwner(owner)
                .destinationOwner(owner)
                .amount(new BigInteger("10"))
                .currency(CurrencyType.FIAT)
                .status(TransactionStatus.SUCCESS)
                .transactionDate(Instant.now())
                .build();
        List<Transaction> list = List.of(t1);
        Page<Transaction> stubPage = new PageImpl<>(list);
        when(transactionRepository.findByOriginOwner_IdOrDestinationOwner_Id(
                eq(owner.getId()), eq(owner.getId()), any())).thenReturn(stubPage);

        Page<Transaction> result = getTransactions.get(owner, page, size, direction);

        assertEquals(stubPage, result, "Expected stub page to be returned");
        ArgumentCaptor<org.springframework.data.domain.Pageable> captor = ArgumentCaptor.forClass(org.springframework.data.domain.Pageable.class);
        verify(transactionRepository, times(1))
                .findByOriginOwner_IdOrDestinationOwner_Id(eq(owner.getId()), eq(owner.getId()), captor.capture());
        var pageable = captor.getValue();
        assertEquals(page, pageable.getPageNumber());
        assertEquals(size, pageable.getPageSize());
        assertEquals(direction, pageable.getSort().getOrderFor("transactionDate").getDirection());
    }

    @Test
    void get_ShouldReturnEmptyPage_WhenNoTransactions() {
        when(transactionRepository.findByOriginOwner_IdOrDestinationOwner_Id(
                any(), any(), any())).thenReturn(Page.empty());

        Page<Transaction> result = getTransactions.get(owner, 0, 10, Sort.Direction.ASC);

        assertTrue(result.isEmpty(), "Expected empty page when no transactions found");
        verify(transactionRepository).findByOriginOwner_IdOrDestinationOwner_Id(eq(owner.getId()), eq(owner.getId()), any());
    }
}
