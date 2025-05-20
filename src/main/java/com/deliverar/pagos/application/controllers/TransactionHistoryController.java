package com.deliverar.pagos.application.controllers;

import com.deliverar.pagos.application.mappers.OwnerMapper;
import com.deliverar.pagos.domain.dtos.GetTransactionsResponse;
import com.deliverar.pagos.domain.entities.Transaction;
import com.deliverar.pagos.domain.usecases.user.GetTransactions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Transaction history controller.
 */
@RequestMapping("/api/transactions")
@RestController
@RequiredArgsConstructor
@Slf4j
public class TransactionHistoryController {
    private final OwnerMapper ownerMapper;
    private final GetTransactions getTransactions;

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public GetTransactionsResponse getTransactions(
            @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
            @RequestParam(name = "size", required = false, defaultValue = "10") int size,
            @RequestParam(name = "direction", required = false, defaultValue = "DESC") Sort.Direction sortDirection
    ) {
        log.info("Get transactions with page {}, size {} and sortDirection {}", pageNumber, size, sortDirection);
        Page<Transaction> page = getTransactions.get(pageNumber, size, sortDirection);

        return GetTransactionsResponse.builder()
                .transactions(ownerMapper.toTransactionDtos(page.getContent()))
                .totalElements(page.getNumberOfElements())
                .page(pageNumber)
                .size(size)
                .hasNext(page.hasNext())
                .sortDirection(sortDirection.name())
                .build();
    }
}
