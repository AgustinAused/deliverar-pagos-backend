package com.deliverar.pagos.domain.usecases.owner.impl;

import com.deliverar.pagos.domain.entities.Owner;
import com.deliverar.pagos.domain.repositories.OwnerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultGetOwnerTest {

    @Mock
    private OwnerRepository ownerRepository;

    @InjectMocks
    private DefaultGetOwner getOwner;

    @Test
    void get_ShouldReturnOwner_WhenFound() {
        UUID id = UUID.randomUUID();
        Owner stub = Owner.builder()
                .id(id)
                .name("Alice")
                .email("alice@example.com")
                .build();
        when(ownerRepository.findById(id)).thenReturn(Optional.of(stub));

        Owner result = getOwner.get(id);

        assertSame(stub, result, "Expected the stub Owner to be returned");
        verify(ownerRepository, times(1)).findById(id);
    }

    @Test
    void get_ShouldThrowNoSuchElementException_WhenNotFound() {
        UUID id = UUID.randomUUID();
        when(ownerRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> getOwner.get(id),
                "Expected NoSuchElementException when Owner is not present");
        verify(ownerRepository, times(1)).findById(id);
    }
}
