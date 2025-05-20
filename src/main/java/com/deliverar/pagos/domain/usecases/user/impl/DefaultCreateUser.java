package com.deliverar.pagos.domain.usecases.user.impl;

import com.deliverar.pagos.domain.entities.Role;
import com.deliverar.pagos.domain.entities.User;
import com.deliverar.pagos.domain.repositories.UserRepository;
import com.deliverar.pagos.domain.usecases.user.CreateUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultCreateUser implements CreateUser {
    private final UserRepository userRepository;

    @Override
    public User create(String name, String email, String passwordHash, Role role) {
        User user = User.builder()
                .name(name)
                .email(email)
                .passwordHash(passwordHash)
                .role(role)
                .build();
        return userRepository.save(user);
    }
}
