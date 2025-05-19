package com.deliverar.pagos.application.controllers;

import com.deliverar.pagos.application.mappers.OwnerMapper;
import com.deliverar.pagos.domain.dtos.CreateOwnerRequest;
import com.deliverar.pagos.domain.dtos.CreateOwnerResponse;
import com.deliverar.pagos.domain.dtos.FiatExchangeRequest;
import com.deliverar.pagos.domain.dtos.GetTransactionsResponse;
import com.deliverar.pagos.domain.entities.Owner;
import com.deliverar.pagos.domain.entities.Transaction;
import com.deliverar.pagos.domain.usecases.owner.CreateOwner;
import com.deliverar.pagos.domain.usecases.owner.ExchangeFiat;
import com.deliverar.pagos.domain.usecases.owner.GetOwner;
import com.deliverar.pagos.domain.usecases.owner.GetOwnerTransactions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Owner controller.
 */
@RequestMapping("/api/owners")
@RestController
@RequiredArgsConstructor
@Slf4j
public class OwnerController {
    private final OwnerMapper ownerMapper;
    private final CreateOwner createOwner;
    private final GetOwner getOwner;
    private final GetOwnerTransactions getOwnerTransactions;
    private final ExchangeFiat exchangeFiat;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateOwnerResponse createOAuthState(@RequestBody CreateOwnerRequest request) {
        Owner owner = createOwner.create(request.getName(), request.getEmail(), request.getOwnerType());
        return CreateOwnerResponse.builder().id(owner.getId()).build();
    }

    @GetMapping("/{id}/transactions")
    @ResponseStatus(HttpStatus.OK)
    public GetTransactionsResponse getTransactions(@PathVariable UUID id,
                                                   @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
                                                   @RequestParam(name = "size", required = false, defaultValue = "10") int size,
                                                   @RequestParam(name = "direction", required = false, defaultValue = "DESC") Sort.Direction sortDirection
    ) {
        log.info("Get transactions of ownerId {} with page {}, size {} and sortDirection {}", id, pageNumber, size, sortDirection);
        Owner owner = getOwner.get(id);
        Page<Transaction> page = getOwnerTransactions.get(owner, pageNumber, size, sortDirection);

        return GetTransactionsResponse.builder()
                .transactions(ownerMapper.toTransactionDtos(page.getContent()))
                .totalElements(page.getNumberOfElements())
                .page(pageNumber)
                .size(size)
                .hasNext(page.hasNext())
                .sortDirection(sortDirection.name())
                .build();
    }

    @PostMapping("/{id}/fiat")
    @ResponseStatus(HttpStatus.OK)
    public BigDecimal exchangeOwnerFiat(@PathVariable UUID id,
                                        @RequestBody FiatExchangeRequest request) {
        log.info("Exchange ownerId {} with amount {} and operation {}", id, request.getAmount(), request.getOperation());
        Owner owner = getOwner.get(id);
        return exchangeFiat.exchange(owner, request.getAmount(), request.getOperation());
    }
}
