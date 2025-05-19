package com.deliverar.pagos.domain.usecases.owner.impl;

import com.deliverar.pagos.domain.entities.Owner;
import com.deliverar.pagos.domain.repositories.OwnerRepository;
import com.deliverar.pagos.domain.usecases.owner.GetOwner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DefaultGetOwner implements GetOwner {
    private final OwnerRepository ownerRepository;

    @Override
    public Owner get(UUID id) {
        return ownerRepository.findById(id).orElseThrow();
    }
}
