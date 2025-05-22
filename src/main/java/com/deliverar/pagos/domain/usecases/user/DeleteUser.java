package com.deliverar.pagos.domain.usecases.user;

import java.util.UUID;

@FunctionalInterface
public interface DeleteUser {
    void delete(UUID id);
}