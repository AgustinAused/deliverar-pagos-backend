package com.deliverar.pagos.domain.usecases.user.impl;

import com.deliverar.pagos.domain.entities.User;
import com.deliverar.pagos.domain.repositories.UserRepository;
import com.deliverar.pagos.domain.usecases.user.GetUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DefaultGetUser implements GetUser {
    private final UserRepository userRepository;

    @Override
    public User get(UUID id) {
        return userRepository.findById(id).orElseThrow();
    }
}
