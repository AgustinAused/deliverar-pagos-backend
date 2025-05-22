package com.deliverar.pagos.domain.usecases.owner;

import com.deliverar.pagos.domain.entities.Owner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

@FunctionalInterface
public interface GetOwnerList {
    Page<Owner> get(int pageNumber, int pageSize, Sort sort);
}
