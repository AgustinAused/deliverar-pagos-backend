package com.deliverar.pagos.domain.usecases.user.impl;

import com.deliverar.pagos.domain.entities.Role;
import com.deliverar.pagos.domain.entities.User;
import com.deliverar.pagos.domain.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultCreateUserTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DefaultCreateUser createUser;

    @Test
    void create_ShouldReturn_SavedUserAndPopulateFields() {
        User stub = User.builder()
                .id(UUID.randomUUID())
                .name("Alice")
                .email("alice@example.com")
                .passwordHash("hashedPwd")
                .role(Role.ADMIN)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        when(userRepository.save(any(User.class))).thenReturn(stub);

        User result = createUser.create("Alice", "alice@example.com", "hashedPwd", Role.ADMIN);

        assertEquals(stub, result, "Expected the same User returned from repository");
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(captor.capture());

        User toSave = captor.getValue();
        assertEquals("Alice", toSave.getName(), "Name should match input");
        assertEquals("alice@example.com", toSave.getEmail(), "Email should match input");
        assertEquals("hashedPwd", toSave.getPasswordHash(), "Password hash should match input");
        assertEquals(Role.ADMIN, toSave.getRole(), "Role should match input");
        assertNotNull(toSave.getCreatedAt(), "createdAt should be populated by builder");
        assertNotNull(toSave.getUpdatedAt(), "updatedAt should be populated by builder");
    }
}
