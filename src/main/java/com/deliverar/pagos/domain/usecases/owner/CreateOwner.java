package com.deliverar.pagos.domain.usecases.owner;

import com.deliverar.pagos.domain.entities.Owner;
import com.deliverar.pagos.domain.entities.OwnerType;

@FunctionalInterface
public interface CreateOwner {
    Owner create(String name, String email, OwnerType ownerType);
}
