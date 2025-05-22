package com.deliverar.pagos.domain.usecases.user.impl;

import com.deliverar.pagos.domain.repositories.UserRepository;
import com.deliverar.pagos.domain.usecases.user.DeleteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DefaultDeleteUser implements DeleteUser {
    private final UserRepository userRepository;

    @Override
    public void delete(UUID id) {
        userRepository.deleteById(id);
    }
}
