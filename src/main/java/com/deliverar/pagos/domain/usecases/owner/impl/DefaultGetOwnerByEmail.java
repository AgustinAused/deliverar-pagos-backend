package com.deliverar.pagos.domain.usecases.owner.impl;

import com.deliverar.pagos.domain.entities.Owner;
import com.deliverar.pagos.domain.repositories.OwnerRepository;
import com.deliverar.pagos.domain.usecases.owner.GetOwnerByEmail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DefaultGetOwnerByEmail implements GetOwnerByEmail {
    private final OwnerRepository ownerRepository;

    @Override
    public Optional<Owner> get(String email) {
        return ownerRepository.findByEmail(email);
    }
} 