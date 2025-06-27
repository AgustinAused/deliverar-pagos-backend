package com.deliverar.pagos.domain.usecases.owner;

import com.deliverar.pagos.domain.entities.Owner;

import java.util.UUID;

@FunctionalInterface
public interface GetOwner {
    Owner get(UUID id);
}
