package com.deliverar.pagos.application.controllers;

import com.deliverar.pagos.domain.entities.Transaction;
import com.deliverar.pagos.domain.dtos.MintBurnRequest;
import com.deliverar.pagos.domain.dtos.TransactionResponse;
import com.deliverar.pagos.domain.dtos.TransferRequest;
import com.deliverar.pagos.adapters.crypto.service.DeliverCoinService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;
import java.util.Map;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Tag(name = "DeliverCoin", description = "Operaciones con el token DeliverCoin: transferencias, mint, burn, balance y supply")
@RestController
@RequestMapping("/api/delivercoin")
@RequiredArgsConstructor
public class DeliverCoinController {

    private final DeliverCoinService deliverCoinService;

    @Operation(summary = "Iniciar transferencia de tokens",
            description = "Inicia una transferencia asíncrona de tokens entre dos usuarios")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Transferencia iniciada correctamente",
                content = @Content(mediaType = "application/json",
                schema = @Schema(example = "{\"status\": \"pending\", \"trackingId\": \"uuid\", \"message\": \"Transferencia iniciada y en proceso\"}"))),
        @ApiResponse(responseCode = "400", description = "Datos de la solicitud inválidos",
                content = @Content)
    })
    @PostMapping("/transfer")
    public ResponseEntity<Map<String, Object>> transfer(@RequestBody TransferRequest request) {
        UUID trackingId = deliverCoinService.asyncTransfer(request);

        return ResponseEntity.accepted().body(Map.of(
                "status", "pending",
                "trackingId", trackingId,
                "message", "Transferencia iniciada y en proceso"
        ));
    }

    @Operation(summary = "Consultar estado de transferencia",
            description = "Obtiene el estado actual de una transferencia usando su ID de seguimiento")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estado de transferencia recuperado exitosamente",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = TransactionResponse.class))),
        @ApiResponse(responseCode = "404", description = "Transferencia no encontrada",
                content = @Content)
    })
    @GetMapping("/transfer/status")
    public ResponseEntity<TransactionResponse> transferStatus(
            @Parameter(description = "ID de seguimiento de la transferencia", required = true)
            @RequestParam UUID trackingId) {
        Transaction entity = deliverCoinService.getTransferStatus(trackingId);
        TransactionResponse response = TransactionResponse.builder()
                .id(entity.getId())
                .fromOwnerEmail(deliverCoinService.getEmailByOwnerId(entity.getOriginOwner().getId()))
                .toOwnerEmail(deliverCoinService.getEmailByOwnerId(entity.getDestinationOwner().getId()))
                .amount(entity.getAmount())
                .status(entity.getStatus().name())
                .txHash(entity.getBlockchainTxHash())
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Crear nuevos tokens (mint)",
            description = "Crea nuevos tokens DeliverCoin y los asigna a un usuario específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tokens creados exitosamente",
                content = @Content(mediaType = "application/json",
                schema = @Schema(example = "{\"status\": \"success\", \"transactionHash\": \"0x...\"}"))),
        @ApiResponse(responseCode = "500", description = "Error en la creación de tokens",
                content = @Content)
    })
    @PostMapping("/mint")
    public ResponseEntity<?> mint(@RequestBody MintBurnRequest request) {
        try {
            TransactionReceipt receipt = deliverCoinService.mint(request.getAmount(), request.getEmail());
            return ResponseEntity.ok(Map.of("status", "success", "transactionHash", receipt.getTransactionHash()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Destruir tokens (burn)",
            description = "Destruye una cantidad específica de tokens DeliverCoin de un usuario")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tokens destruidos exitosamente",
                content = @Content(mediaType = "application/json",
                schema = @Schema(example = "{\"status\": \"success\", \"transactionHash\": \"0x...\"}"))),
        @ApiResponse(responseCode = "500", description = "Error en la destrucción de tokens",
                content = @Content)
    })
    @PostMapping("/burn")
    public ResponseEntity<?> burn(@RequestBody MintBurnRequest request) {
        try {
            TransactionReceipt receipt = deliverCoinService.burn(request.getAmount(), request.getEmail());
            return ResponseEntity.ok(Map.of("status", "success", "transactionHash", receipt.getTransactionHash()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Consultar balance de usuario",
            description = "Obtiene el balance de tokens DeliverCoin de un usuario específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Balance recuperado exitosamente",
                content = @Content(mediaType = "application/json",
                schema = @Schema(example = "{\"email\": \"user@example.com\", \"balance\": \"1000\"}"))),
        @ApiResponse(responseCode = "500", description = "Error al consultar el balance",
                content = @Content)
    })
    @GetMapping("/balance")
    public ResponseEntity<Map<String,Object>> getBalance(
            @Parameter(description = "Email del usuario", required = true)
            @RequestParam String email) {
        try {
            BigInteger balance = deliverCoinService.balanceOf(email);
            return ResponseEntity.ok(Map.of("email", email, "balance", balance));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Consultar suministro total",
            description = "Obtiene el suministro total de tokens DeliverCoin en circulación")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Suministro total recuperado exitosamente",
                content = @Content(mediaType = "application/json",
                schema = @Schema(example = "{\"totalSupply\": \"1000000\"}"))),
        @ApiResponse(responseCode = "500", description = "Error al consultar el suministro total",
                content = @Content)
    })
    @GetMapping("/supply")
    public ResponseEntity<Map<String,Object>> totalSupply() {
        try {
            BigInteger supply = deliverCoinService.totalSupply();
            return ResponseEntity.ok(Map.of("totalSupply", supply));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

}
