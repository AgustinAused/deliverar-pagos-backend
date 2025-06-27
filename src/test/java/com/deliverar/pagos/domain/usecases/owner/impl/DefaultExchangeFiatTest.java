package com.deliverar.pagos.domain.usecases.owner.impl;

import com.deliverar.pagos.domain.entities.ExchangeOperation;
import com.deliverar.pagos.domain.entities.Owner;
import com.deliverar.pagos.domain.entities.OwnerType;
import com.deliverar.pagos.domain.entities.Wallet;
import com.deliverar.pagos.domain.exceptions.BadRequestException;
import com.deliverar.pagos.domain.repositories.FiatTransactionRepository;
import com.deliverar.pagos.domain.repositories.OwnerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultExchangeFiatTest {

    @Mock
    private OwnerRepository ownerRepository;

    @Mock
    private FiatTransactionRepository fiatTransactionRepository;

    @InjectMocks
    private DefaultExchangeFiat exchangeFiat;

    private Owner owner;

    @BeforeEach
    void setUp() {
        Wallet wallet = Wallet.builder()
                .fiatBalance(new BigDecimal("100.00"))
                .cryptoBalance(BigDecimal.ZERO)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        owner = Owner.builder()
                .id(UUID.randomUUID())
                .name("Test Owner")
                .email("test@example.com")
                .ownerType(OwnerType.DELIVERY)
                .wallet(wallet)
                .build();
    }

    @Test
    void exchange_Inflow_ShouldIncreaseBalance() {
        BigDecimal result = exchangeFiat.exchange(owner, new BigDecimal("50.00"), ExchangeOperation.INFLOW);

        assertEquals(new BigDecimal("150.00"), result);
        ArgumentCaptor<Owner> captor = ArgumentCaptor.forClass(Owner.class);
        verify(ownerRepository, times(1)).save(captor.capture());
        verify(fiatTransactionRepository, times(1)).save(any());
        assertEquals(new BigDecimal("150.00"), captor.getValue().getWallet().getFiatBalance());
    }

    @Test
    void exchange_Outflow_ShouldDecreaseBalance() {
        BigDecimal result = exchangeFiat.exchange(owner, new BigDecimal("30.00"), ExchangeOperation.OUTFLOW);

        assertEquals(new BigDecimal("70.00"), result);
        verify(fiatTransactionRepository, times(1)).save(any());
        verify(ownerRepository, times(1)).save(owner);
    }

    @Test
    void exchange_Outflow_ShouldThrow_WhenInsufficientBalance() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> exchangeFiat.exchange(owner, new BigDecimal("150.00"), ExchangeOperation.OUTFLOW)
        );
        assertEquals("Insufficient fiat balance", ex.getMessage());
        verify(ownerRepository, never()).save(any());
    }

    @Test
    void exchange_ShouldThrow_WhenOwnerIsNull() {
        assertThrows(NullPointerException.class,
                () -> exchangeFiat.exchange(null, BigDecimal.ONE, ExchangeOperation.INFLOW)
        );
    }

    @Test
    void exchange_ShouldThrow_WhenAmountIsNull() {
        assertThrows(NullPointerException.class,
                () -> exchangeFiat.exchange(owner, null, ExchangeOperation.INFLOW)
        );
    }

    @Test
    void exchange_ShouldThrow_WhenOperationIsNull() {
        assertThrows(NullPointerException.class,
                () -> exchangeFiat.exchange(owner, BigDecimal.ONE, null)
        );
    }
}
