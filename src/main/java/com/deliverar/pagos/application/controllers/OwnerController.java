package com.deliverar.pagos.application.controllers;

import com.deliverar.pagos.application.mappers.OwnerMapper;
import com.deliverar.pagos.domain.dtos.*;
import com.deliverar.pagos.domain.entities.FiatTransaction;
import com.deliverar.pagos.domain.entities.Owner;
import com.deliverar.pagos.domain.entities.Transaction;
import com.deliverar.pagos.domain.usecases.owner.*;
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

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Owner controller.
 */
@Tag(name = "Propietarios", description = "Operaciones relacionadas con propietarios, balances y transacciones")
@RequestMapping("/api/owners")
@RestController
@RequiredArgsConstructor
@Slf4j
public class OwnerController {
    private final OwnerMapper ownerMapper;
    private final CreateOwner createOwner;
    private final GetOwner getOwner;
    private final GetOwnerTransactions getOwnerTransactions;
    private final GetOwnerFiatTransactions getOwnerFiatTransactions;
    private final ExchangeFiat exchangeFiat;

    @Operation(summary = "Crear nuevo propietario",
            description = "Crea un nuevo propietario en el sistema con su nombre, email y tipo")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Propietario creado exitosamente",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = CreateOwnerResponse.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos en la solicitud",
                content = @Content)
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateOwnerResponse createOAuthState(@RequestBody CreateOwnerRequest request) {
        log.info("Create owner request: {}", request);
        Owner owner = createOwner.create(request.getName(), request.getEmail().toLowerCase(), request.getOwnerType());
        return CreateOwnerResponse.builder().id(owner.getId()).build();
    }

    @Operation(summary = "Obtener balances",
            description = "Obtiene los balances de fiat y crypto de un propietario específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Balances recuperados exitosamente",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = GetBalancesResponse.class))),
        @ApiResponse(responseCode = "404", description = "Propietario no encontrado",
                content = @Content)
    })
    @GetMapping("/{id}/balances")
    @ResponseStatus(HttpStatus.OK)
    public GetBalancesResponse getBalances(
            @Parameter(description = "ID del propietario", required = true)
            @PathVariable UUID id) {
        log.info("Get balances for owner {}", id);
        Owner owner = getOwner.get(id);
        return GetBalancesResponse.builder()
                .fiatBalance(owner.getWallet().getFiatBalance())
                .cryptoBalance(owner.getWallet().getCryptoBalance())
                .build();
    }

    @GetMapping("/{id}/transactions")
    @ResponseStatus(HttpStatus.OK)
    public GetTransactionsResponse getTransactions(@PathVariable UUID id,
                                                   @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
                                                   @RequestParam(name = "size", required = false, defaultValue = "10") int size,
                                                   @RequestParam(name = "direction", required = false, defaultValue = "DESC") Sort.Direction sortDirection
    ) {
        log.info("Get crypto transactions of ownerId {} with page {}, size {} and sortDirection {}", id, pageNumber, size, sortDirection);
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

    @GetMapping("/{id}/transactions/fiat")
    @ResponseStatus(HttpStatus.OK)
    public GetFiatTransactionsResponse getFiatTransactions(@PathVariable UUID id,
                                                           @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
                                                           @RequestParam(name = "size", required = false, defaultValue = "10") int size,
                                                           @RequestParam(name = "direction", required = false, defaultValue = "DESC") Sort.Direction sortDirection
    ) {
        log.info("Get fiat transactions of ownerId {} with page {}, size {} and sortDirection {}", id, pageNumber, size, sortDirection);
        Owner owner = getOwner.get(id);
        Page<FiatTransaction> page = getOwnerFiatTransactions.get(owner, pageNumber, size, sortDirection);

        return GetFiatTransactionsResponse.builder()
                .transactions(ownerMapper.toFiatTransactionDtos(page.getContent()))
                .totalElements(page.getNumberOfElements())
                .page(pageNumber)
                .size(size)
                .hasNext(page.hasNext())
                .sortDirection(sortDirection.name())
                .build();
    }

    @Operation(summary = "Realizar operación fiat",
            description = "Realiza una operación de depósito o retiro de moneda fiat para un propietario específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operación realizada exitosamente",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(type = "number", format = "decimal",
                                description = "Nuevo balance después de la operación"))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o fondos insuficientes",
                content = @Content),
        @ApiResponse(responseCode = "404", description = "Propietario no encontrado",
                content = @Content)
    })
    @PostMapping("/{id}/fiat")
    @ResponseStatus(HttpStatus.OK)
    public BigDecimal exchangeOwnerFiat(
            @Parameter(description = "ID del propietario", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Detalles de la operación fiat (monto y tipo de operación)", required = true)
            @RequestBody FiatExchangeRequest request) {
        log.info("Exchange ownerId {} with amount {} and operation {}", id, request.getAmount(), request.getOperation());
        Owner owner = getOwner.get(id);
        return exchangeFiat.exchange(owner, request.getAmount(), request.getOperation());
    }
}