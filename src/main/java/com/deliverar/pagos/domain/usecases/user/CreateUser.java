package com.deliverar.pagos.domain.usecases.user;

import com.deliverar.pagos.domain.entities.Role;
import com.deliverar.pagos.domain.entities.User;

@FunctionalInterface
public interface CreateUser {
    User create(String name, String email, String passwordHash, Role role);
}