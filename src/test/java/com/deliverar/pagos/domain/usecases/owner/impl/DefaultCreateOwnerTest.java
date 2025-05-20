package com.deliverar.pagos.domain.usecases.owner.impl;

import com.deliverar.pagos.domain.entities.Owner;
import com.deliverar.pagos.domain.entities.OwnerType;
import com.deliverar.pagos.domain.entities.Wallet;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultCreateOwnerTest {

    @Mock
    private OwnerRepository ownerRepository;

    @InjectMocks
    private DefaultCreateOwner createOwner;

    private final String name = "Jane Doe";
    private final String email = "jane.doe@example.com";
    private final OwnerType ownerType = OwnerType.NATURAL;

    @BeforeEach
    void setUp() {
        // No-op; Mockito handles initialization
    }

    @Test
    void create_ShouldReturn_SavedOwner() {
        // Arrange
        Owner stubOwner = Owner.builder()
                .id(UUID.randomUUID())
                .name(name)
                .email(email)
                .ownerType(ownerType)
                .wallet(
                        Wallet.builder()
                                .fiatBalance(BigDecimal.ZERO)
                                .cryptoBalance(BigDecimal.ZERO)
                                .createdAt(Instant.now())
                                .updatedAt(Instant.now())
                                .build()
                )
                .build();
        when(ownerRepository.save(any(Owner.class))).thenReturn(stubOwner);

        // Act
        Owner result = createOwner.create(name, email, ownerType);

        // Assert
        assertEquals(stubOwner, result, "Expected the saved owner to be returned");
        ArgumentCaptor<Owner> captor = ArgumentCaptor.forClass(Owner.class);
        verify(ownerRepository, times(1)).save(captor.capture());
        Owner toSave = captor.getValue();

        assertEquals(name, toSave.getName(), "Owner name should match input");
        assertEquals(email, toSave.getEmail(), "Owner email should match input");
        assertEquals(ownerType, toSave.getOwnerType(), "OwnerType should match input");
        assertNotNull(toSave.getWallet(), "Wallet should be initialized");
        assertEquals(BigDecimal.ZERO, toSave.getWallet().getFiatBalance(), "Initial fiat balance should be zero");
        assertEquals(BigDecimal.ZERO, toSave.getWallet().getCryptoBalance(), "Initial crypto balance should be zero");
        assertNotNull(toSave.getWallet().getCreatedAt(), "Wallet creation time should be set");
        assertNotNull(toSave.getWallet().getUpdatedAt(), "Wallet update time should be set");
    }

    @Test
    void create_ShouldThrow_WhenNameIsNull() {
        assertThrows(NullPointerException.class,
                () -> createOwner.create(null, email, ownerType),
                "Creating an owner with null name should throw NullPointerException");
    }

    @Test
    void create_ShouldThrow_WhenEmailIsNull() {
        assertThrows(NullPointerException.class,
                () -> createOwner.create(name, null, ownerType),
                "Creating an owner with null email should throw NullPointerException");
    }

    @Test
    void create_ShouldThrow_WhenOwnerTypeIsNull() {
        assertThrows(NullPointerException.class,
                () -> createOwner.create(name, email, null),
                "Creating an owner with null OwnerType should throw NullPointerException");
    }
}
