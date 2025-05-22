
package com.deliverar.pagos.domain.usecases.owner.impl;

import com.deliverar.pagos.domain.entities.Owner;
import com.deliverar.pagos.domain.repositories.OwnerRepository;
import com.deliverar.pagos.domain.usecases.owner.GetOwnerList;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultGetOwnerList implements GetOwnerList {
    private final OwnerRepository ownerRepository;

    @Override
    public Page<Owner> get(int pageNumber, int pageSize, Sort sort) {
        Pageable pageReq = PageRequest.of(pageNumber, pageSize, sort);
        return ownerRepository.findAll(pageReq);
    }
}
