package com.deliverar.pagos.domain.usecases.user.impl;

import com.deliverar.pagos.domain.entities.Role;
import com.deliverar.pagos.domain.entities.User;
import com.deliverar.pagos.domain.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultGetUserTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DefaultGetUser getUser;

    @Test
    void get_WhenUserExists_ReturnsUser() {
        UUID id = UUID.randomUUID();
        User stub = User.builder()
                .id(id)
                .name("Test")
                .email("test@example.com")
                .passwordHash("hash")
                .role(Role.CORE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        when(userRepository.findById(id)).thenReturn(Optional.of(stub));

        User result = getUser.get(id);

        assertSame(stub, result, "Expected repository user to be returned");
        verify(userRepository, times(1)).findById(id);
    }

    @Test
    void get_WhenUserNotFound_ThrowsNoSuchElementException() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> getUser.get(id),
                "Expected NoSuchElementException when user not found");
        verify(userRepository, times(1)).findById(id);
    }
}
