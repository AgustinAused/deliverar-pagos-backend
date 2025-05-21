package com.deliverar.pagos.domain.usecases.user;

import com.deliverar.pagos.domain.entities.User;

import java.util.UUID;

@FunctionalInterface
public interface GetUser {
    User get(UUID id);
}
