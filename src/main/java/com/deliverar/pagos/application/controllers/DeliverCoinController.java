package com.deliverar.pagos.application.controllers;

import com.deliverar.pagos.adapters.crypto.service.DeliverCoinService;
import com.deliverar.pagos.domain.dtos.MintBurnRequest;
import com.deliverar.pagos.domain.dtos.TransactionResponse;
import com.deliverar.pagos.domain.dtos.TransferRequest;
import com.deliverar.pagos.domain.dtos.BuyCryptoRequest;
import com.deliverar.pagos.domain.dtos.SellCryptoRequest;
import com.deliverar.pagos.domain.entities.Transaction;
import com.deliverar.pagos.domain.exceptions.BadRequestException;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigDecimal;
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

    @Operation(
            summary = "Get DeliverCoin summary info",
            description = "Returns a summary of DeliverCoin token statistics and blockchain status"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Summary info retrieved successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    @GetMapping()
    public ResponseEntity<Object> getCryptoSummaryInfo() {
        try {
            return ResponseEntity.ok(deliverCoinService.getCryptoSummaryInfo());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }


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

       try {
           UUID trackingId = deliverCoinService.asyncTransfer(request);

           return ResponseEntity.accepted().body(Map.of(
                   "status", "pending",
                   "trackingId", trackingId,
                   "message", "Transferencia iniciada y en proceso"
           ));
       }catch (BadRequestException e) {
              return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
         } catch (Exception e) {
              return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
       }
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
    public ResponseEntity<Map<String,String>> mint(@RequestBody MintBurnRequest request) {
        try {
            TransactionReceipt receipt = deliverCoinService.mint(request.getAmount());
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
    public ResponseEntity<Map<String,String>> burn(@RequestBody MintBurnRequest request) {
        try {
            TransactionReceipt receipt = deliverCoinService.burn(request.getAmount());
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
            BigDecimal balance = deliverCoinService.balanceOf(email);
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
            BigDecimal supply = deliverCoinService.totalSupply();
            return ResponseEntity.ok(Map.of("totalSupply", supply));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Comprar crypto con fiat",
            description = "Permite a un usuario comprar tokens DeliverCoin usando su balance en fiat")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Compra iniciada correctamente",
                content = @Content(mediaType = "application/json",
                schema = @Schema(example = "{\"status\": \"pending\", \"trackingId\": \"uuid\", \"message\": \"Compra iniciada y en proceso\"}"))),
        @ApiResponse(responseCode = "400", description = "Balance insuficiente o datos inválidos",
                content = @Content)
    })
    @PostMapping("/buy")
    public ResponseEntity<Map<String, Object>> buyCrypto(@RequestBody BuyCryptoRequest request) {
        try {
            UUID trackingId = deliverCoinService.buyCryptoWithFiat(request.getEmail(), request.getAmount());
            return ResponseEntity.accepted().body(Map.of(
                    "status", "pending",
                    "trackingId", trackingId,
                    "message", "Compra iniciada y en proceso"
            ));
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Vender crypto por fiat",
            description = "Permite a un usuario vender tokens DeliverCoin y recibir su balance en fiat")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Venta iniciada correctamente",
                content = @Content(mediaType = "application/json",
                schema = @Schema(example = "{\"status\": \"pending\", \"trackingId\": \"uuid\", \"message\": \"Venta iniciada y en proceso\"}"))),
        @ApiResponse(responseCode = "400", description = "Balance insuficiente o datos inválidos",
                content = @Content)
    })
    @PostMapping("/sell")
    public ResponseEntity<Map<String, Object>> sellCrypto(@RequestBody SellCryptoRequest request) {
        try {
            UUID trackingId = deliverCoinService.sellCryptoForFiat(request.getEmail(), request.getAmount());
            return ResponseEntity.accepted().body(Map.of(
                    "status", "pending",
                    "trackingId", trackingId,
                    "message", "Venta iniciada y en proceso"
            ));
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Sincronizar balance del owner",
            description = "Sincroniza el balance del owner en la base de datos con el de la blockchain")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sincronización exitosa",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"message\": \"Balance sincronizado\"}"))),
            @ApiResponse(responseCode = "500", description = "Error durante la sincronización",
                    content = @Content)
    })
    @PostMapping("/sync-balance")
    public ResponseEntity<Map<String, String>> syncOwnerBalance(@RequestParam String email) {
        try {
            deliverCoinService.syncBalance(email);
            return ResponseEntity.ok(Map.of("message", "Balance sincronizado"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

}
