package com.deliverar.pagos.application.controllers;

import com.deliverar.pagos.adapters.crypto.service.DeliverCoinService;
import com.deliverar.pagos.domain.dtos.MintBurnRequest;
import com.deliverar.pagos.domain.dtos.TransactionResponse;
import com.deliverar.pagos.domain.dtos.TransferRequest;
import com.deliverar.pagos.domain.entities.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/delivercoin")
@RequiredArgsConstructor
public class DeliverCoinController {

    private final DeliverCoinService deliverCoinService;

    @PostMapping("/transfer")
    public ResponseEntity<Map<String, Object>> transfer(@RequestBody TransferRequest request) {
        UUID trackingId = deliverCoinService.asyncTransfer(request);

        return ResponseEntity.accepted().body(Map.of(
                "status", "pending",
                "trackingId", trackingId,
                "message", "Transferencia iniciada y en proceso"
        ));
    }

    @GetMapping("/transfer/status")
    public ResponseEntity<TransactionResponse> transferStatus(@RequestParam UUID trackingId) {
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


    @PostMapping("/mint")
    public ResponseEntity<?> mint(@RequestBody MintBurnRequest request) {
        try {
            TransactionReceipt receipt = deliverCoinService.mint(request.getAmount(), request.getEmail());
            return ResponseEntity.ok(Map.of("status", "success", "transactionHash", receipt.getTransactionHash()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/burn")
    public ResponseEntity<?> burn(@RequestBody MintBurnRequest request) {
        try {
            TransactionReceipt receipt = deliverCoinService.burn(request.getAmount(), request.getEmail());
            return ResponseEntity.ok(Map.of("status", "success", "transactionHash", receipt.getTransactionHash()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/balance")
    public ResponseEntity<?> getBalance(@RequestParam String email) {
        try {
            BigDecimal balance = deliverCoinService.balanceOf(email);
            return ResponseEntity.ok(Map.of("email", email, "balance", balance));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/supply")
    public ResponseEntity<?> totalSupply() {
        try {
            BigDecimal supply = deliverCoinService.totalSupply();
            return ResponseEntity.ok(Map.of("totalSupply", supply));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

}
