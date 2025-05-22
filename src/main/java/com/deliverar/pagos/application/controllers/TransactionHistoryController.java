package com.deliverar.pagos.application.controllers;

import com.deliverar.pagos.application.mappers.OwnerMapper;
import com.deliverar.pagos.domain.dtos.GetFiatTransactionsResponse;
import com.deliverar.pagos.domain.dtos.GetTransactionsResponse;
import com.deliverar.pagos.domain.entities.FiatTransaction;
import com.deliverar.pagos.domain.entities.Transaction;
import com.deliverar.pagos.domain.usecases.user.GetFiatTransactions;
import com.deliverar.pagos.domain.usecases.user.GetTransactions;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Transaction history controller.
 */
@Tag(name = "Transacciones", description = "Operaciones relacionadas al historial de transacciones")
@RequestMapping("/api/transactions")
@RestController
@RequiredArgsConstructor
@Slf4j
public class TransactionHistoryController {
    private final OwnerMapper ownerMapper;
    private final GetTransactions getTransactions;
    private final GetFiatTransactions getFiatTransactions;

    @Operation(
            summary = "Obtener historial de transacciones",
            description = "Devuelve una lista paginada de transacciones según los filtros enviados por query params."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Transacciones obtenidas exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = GetTransactionsResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public GetTransactionsResponse getTransactions(
            @Parameter(description = "Número de página (inicia en 0)", example = "0")
            @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
            @Parameter(description = "Tamaño de la página", example = "10")
            @RequestParam(name = "size", required = false, defaultValue = "10") int size,
            @Parameter(description = "Dirección de ordenamiento (ASC o DESC)", example = "DESC")
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


    @GetMapping("/fiat")
    @ResponseStatus(HttpStatus.OK)
    public GetFiatTransactionsResponse getFiatTransactions(@RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
                                                           @RequestParam(name = "size", required = false, defaultValue = "10") int size,
                                                           @RequestParam(name = "direction", required = false, defaultValue = "DESC") Sort.Direction sortDirection
    ) {

        Page<FiatTransaction> page = getFiatTransactions.get(pageNumber, size, sortDirection);

        return GetFiatTransactionsResponse.builder()
                .transactions(ownerMapper.toFiatTransactionDtos(page.getContent()))
                .totalElements(page.getNumberOfElements())
                .page(pageNumber)
                .size(size)
                .hasNext(page.hasNext())
                .sortDirection(sortDirection.name())
                .build();
    }
}
