package com.deliverar.pagos.domain.usecases.user.impl;

import com.deliverar.pagos.domain.entities.User;
import com.deliverar.pagos.domain.repositories.UserRepository;
import com.deliverar.pagos.domain.usecases.user.GetUserList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DefaultGetUserList implements GetUserList {
    private final UserRepository userRepository;

    @Override
    public List<User> get() {
        return userRepository.findAll();
    }
}
