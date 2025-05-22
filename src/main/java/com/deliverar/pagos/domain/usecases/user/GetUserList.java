package com.deliverar.pagos.domain.usecases.user;

import com.deliverar.pagos.domain.entities.User;

import java.util.List;

@FunctionalInterface
public interface GetUserList {
    List<User> get();
}