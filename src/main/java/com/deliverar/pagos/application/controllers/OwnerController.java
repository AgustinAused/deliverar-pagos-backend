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
import java.util.List;
import java.util.UUID;

import static org.springframework.data.domain.Sort.by;

@Tag(name = "Owners", description = "Operaciones sobre owners (propietarios de wallets)")
@RequestMapping("/api/owners")
@RestController
@RequiredArgsConstructor
@Slf4j
public class OwnerController {
    private final OwnerMapper ownerMapper;
    private final CreateOwner createOwner;
    private final GetOwner getOwner;
    private final GetOwnerList getOwnerList;
    private final GetOwnerTransactions getOwnerTransactions;
    private final GetOwnerFiatTransactions getOwnerFiatTransactions;
    private final ExchangeFiat exchangeFiat;

    @Operation(
            summary = "Listar owners",
            description = "Obtiene una lista paginada de owners (propietarios de wallets)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista obtenida", content = @Content(schema = @Schema(implementation = GetOwnersResponse.class))),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error interno", content = @Content)
    })
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public GetOwnersResponse getOwners(
            @Parameter(description = "Número de página (inicia en 0)", example = "0") @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
            @Parameter(description = "Tamaño de la página", example = "10") @RequestParam(name = "size", required = false, defaultValue = "10") int size,
            @Parameter(description = "Campo por el que ordenar (por ejemplo: email)", example = "email") @RequestParam(name = "fieldBy", required = false, defaultValue = "email") String fieldBy,
            @Parameter(description = "Dirección de ordenamiento (ASC o DESC)", example = "ASC") @RequestParam(name = "direction", required = false, defaultValue = "ASC") Sort.Direction sortDirection) {
        log.info("Get owners");

        Page<Owner> page = getOwnerList.get(pageNumber, size, by(sortDirection, fieldBy));
        return GetOwnersResponse.builder()
                .ownersList(ownerMapper.toOwnerDtos(page.getContent()))
                .totalElements(page.getNumberOfElements())
                .page(pageNumber)
                .size(size)
                .hasNext(page.hasNext())
                .sortDirection(sortDirection.name())
                .build();
    }

    @Operation(
            summary = "Crear owner",
            description = "Crea un nuevo owner (propietario de wallet) en el sistema"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Owner creado exitosamente", content = @Content(schema = @Schema(implementation = CreateOwnerResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateOwnerResponse create(@RequestBody CreateOwnerRequest request) {
        log.info("Create owner request: {}", request);
        Owner owner = createOwner.create(
            request.getName(), 
            request.getEmail().toLowerCase(), 
            request.getOwnerType(),
            BigDecimal.ZERO, // Default initial fiat balance
            BigDecimal.ZERO  // Default initial crypto balance
        );
        return CreateOwnerResponse.builder().id(owner.getId()).build();
    }

    @Operation(
            summary = "Obtener owner por ID",
            description = "Devuelve los datos de un owner específico por su UUID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Owner obtenido", content = @Content(schema = @Schema(implementation = OwnerDto.class))),
            @ApiResponse(responseCode = "404", description = "Owner no encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public OwnerDto get(
            @Parameter(description = "ID del owner", required = true) @PathVariable UUID id) {
        log.info("Get owner with {}", id);
        Owner owner = getOwner.get(id);
        return ownerMapper.toOwnerDto(owner);
    }

    @Operation(
            summary = "Obtener balances de owner",
            description = "Devuelve los balances (fiat y crypto) del owner"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Balances obtenidos", content = @Content(schema = @Schema(implementation = GetBalancesResponse.class))),
            @ApiResponse(responseCode = "404", description = "Owner no encontrado", content = @Content)
    })
    @GetMapping("/{id}/balances")
    @ResponseStatus(HttpStatus.OK)
    public GetBalancesResponse getBalances(
            @Parameter(description = "ID del owner", required = true) @PathVariable UUID id) {
        log.info("Get balances for owner {}", id);
        Owner owner = getOwner.get(id);
        return GetBalancesResponse.builder()
                .fiatBalance(owner.getWallet().getFiatBalance())
                .cryptoBalance(owner.getWallet().getCryptoBalance())
                .build();
    }

    @Operation(
            summary = "Listar transacciones crypto del owner",
            description = "Devuelve la lista paginada de transacciones crypto asociadas a un owner"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transacciones obtenidas", content = @Content(schema = @Schema(implementation = GetTransactionsResponse.class))),
            @ApiResponse(responseCode = "404", description = "Owner no encontrado", content = @Content)
    })
    @GetMapping("/{id}/transactions")
    @ResponseStatus(HttpStatus.OK)
    public GetTransactionsResponse getTransactions(
            @Parameter(description = "ID del owner", required = true) @PathVariable UUID id,
            @Parameter(description = "Número de página (inicia en 0)", example = "0") @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
            @Parameter(description = "Tamaño de la página", example = "10") @RequestParam(name = "size", required = false, defaultValue = "10") int size,
            @Parameter(description = "Dirección de ordenamiento (ASC o DESC)", example = "DESC") @RequestParam(name = "direction", required = false, defaultValue = "DESC") Sort.Direction sortDirection
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

    @Operation(
            summary = "Listar transacciones fiat del owner",
            description = "Devuelve la lista paginada de transacciones fiat asociadas a un owner"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transacciones fiat obtenidas", content = @Content(schema = @Schema(implementation = GetFiatTransactionsResponse.class))),
            @ApiResponse(responseCode = "404", description = "Owner no encontrado", content = @Content)
    })
    @GetMapping("/{id}/transactions/fiat")
    @ResponseStatus(HttpStatus.OK)
    public GetFiatTransactionsResponse getFiatTransactions(
            @Parameter(description = "ID del owner", required = true) @PathVariable UUID id,
            @Parameter(description = "Número de página (inicia en 0)", example = "0") @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
            @Parameter(description = "Tamaño de la página", example = "10") @RequestParam(name = "size", required = false, defaultValue = "10") int size,
            @Parameter(description = "Dirección de ordenamiento (ASC o DESC)", example = "DESC") @RequestParam(name = "direction", required = false, defaultValue = "DESC") Sort.Direction sortDirection
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

    @Operation(
            summary = "Realizar exchange de saldo fiat (cargar o retirar)",
            description = "Permite al owner cargar o retirar saldo fiat según la operación solicitada"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operación realizada exitosamente", content = @Content(schema = @Schema(implementation = BigDecimal.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Owner no encontrado", content = @Content)
    })
    @PostMapping("/{id}/fiat")
    @ResponseStatus(HttpStatus.OK)
    public BigDecimal exchangeOwnerFiat(
            @Parameter(description = "ID del owner", required = true) @PathVariable UUID id,
            @RequestBody FiatExchangeRequest request) {
        log.info("Exchange ownerId {} with amount {} and operation {}", id, request.getAmount(), request.getOperation());
        Owner owner = getOwner.get(id);
        return exchangeFiat.exchange(owner, request.getAmount(), request.getOperation());
    }
}
