package com.deliverar.pagos.domain.usecases.owner;

import com.deliverar.pagos.domain.entities.Owner;

import java.util.Optional;

@FunctionalInterface
public interface GetOwnerByEmail {
    Optional<Owner> get(String email);
} 