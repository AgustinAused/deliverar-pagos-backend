package com.deliverar.pagos.application.controllers;

import com.deliverar.pagos.domain.dtos.MintBurnRequest;
import com.deliverar.pagos.domain.dtos.TransferRequest;
import com.deliverar.pagos.adapters.crypto.service.DeliverCoinService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliverCoinControllerTest {

    @Mock
    private DeliverCoinService deliverCoinService;

    @InjectMocks
    private DeliverCoinController controller;

    private UUID trackingId;

    @BeforeEach
    void setUp() {
        trackingId = UUID.randomUUID();
    }

    @Test
    void transfer_ShouldReturnAcceptedAndPendingStatus() {
        // Arrange
        TransferRequest req = mock(TransferRequest.class);
        when(deliverCoinService.asyncTransfer(req)).thenReturn(trackingId);

        // Act
        ResponseEntity<Map<String, Object>> response = controller.transfer(req);

        // Assert
        assertEquals(202, response.getStatusCodeValue(), "Should return HTTP 202 Accepted");
        Map<String, Object> body = response.getBody();
        assertNotNull(body, "Response body should not be null");
        assertEquals("pending", body.get("status"));
        assertEquals(trackingId, body.get("trackingId"));
        assertEquals("Transferencia iniciada y en proceso", body.get("message"));
        verify(deliverCoinService, times(1)).asyncTransfer(req);
    }

    @Test
    void transferStatus_ShouldReturnTransactionResponse() {
        // Arrange
        UUID originId = UUID.randomUUID();
        UUID destId   = UUID.randomUUID();
        UUID txId     = UUID.randomUUID();
        // Build a stub transaction entity
        com.deliverar.pagos.domain.entities.Owner origin = com.deliverar.pagos.domain.entities.Owner.builder().id(originId).build();
        com.deliverar.pagos.domain.entities.Owner dest   = com.deliverar.pagos.domain.entities.Owner.builder().id(destId).build();
        com.deliverar.pagos.domain.entities.Transaction tx = com.deliverar.pagos.domain.entities.Transaction.builder()
                .id(txId)
                .originOwner(origin)
                .destinationOwner(dest)
                .amount(BigInteger.TEN)
                .status(com.deliverar.pagos.domain.entities.TransactionStatus.SUCCESS)
                .blockchainTxHash("0xabc123")
                .build();

        when(deliverCoinService.getTransferStatus(trackingId)).thenReturn(tx);
        when(deliverCoinService.getEmailByOwnerId(originId)).thenReturn("from@example.com");
        when(deliverCoinService.getEmailByOwnerId(destId)).thenReturn("to@example.com");

        // Act
        ResponseEntity<com.deliverar.pagos.domain.dtos.TransactionResponse> response = controller.transferStatus(trackingId);

        // Assert
        assertEquals(200, response.getStatusCodeValue(), "Should return HTTP 200 OK");
        com.deliverar.pagos.domain.dtos.TransactionResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(txId, body.getId());
        assertEquals("from@example.com", body.getFromOwnerEmail());
        assertEquals("to@example.com", body.getToOwnerEmail());
        assertEquals(BigInteger.TEN, body.getAmount());
        assertEquals("SUCCESS", body.getStatus());
        assertEquals("0xabc123", body.getTxHash());
        verify(deliverCoinService).getTransferStatus(trackingId);
    }

    @Test
    void mint_ShouldReturnSuccessWhenNoException() throws Exception {
        // Arrange
        MintBurnRequest req = mock(MintBurnRequest.class);
        when(req.getAmount()).thenReturn(BigInteger.valueOf(100));
        when(req.getEmail()).thenReturn("user@example.com");
        TransactionReceipt receipt = mock(TransactionReceipt.class);
        when(receipt.getTransactionHash()).thenReturn("0xdef456");
        when(deliverCoinService.mint(BigInteger.valueOf(100), "user@example.com")).thenReturn(receipt);

        // Act
        ResponseEntity<?> response = controller.mint(req);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("success", body.get("status"));
        assertEquals("0xdef456", body.get("transactionHash"));
        verify(deliverCoinService).mint(BigInteger.valueOf(100), "user@example.com");
    }

    @Test
    void mint_ShouldReturnErrorWhenExceptionThrown() throws Exception {
        // Arrange
        MintBurnRequest req = mock(MintBurnRequest.class);
        when(req.getAmount()).thenReturn(BigInteger.valueOf(50));
        when(req.getEmail()).thenReturn("user2@example.com");
        when(deliverCoinService.mint(any(), any())).thenThrow(new RuntimeException("mint failed"));

        // Act
        ResponseEntity<?> response = controller.mint(req);

        // Assert
        assertEquals(500, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("mint failed", body.get("error"));
    }

    @Test
    void burn_ShouldReturnSuccessWhenNoException() throws Exception {
        // Arrange
        MintBurnRequest req = mock(MintBurnRequest.class);
        when(req.getAmount()).thenReturn(BigInteger.valueOf(200));
        when(req.getEmail()).thenReturn("user3@example.com");
        TransactionReceipt receipt = mock(TransactionReceipt.class);
        when(receipt.getTransactionHash()).thenReturn("0xghi789");
        when(deliverCoinService.burn(BigInteger.valueOf(200), "user3@example.com")).thenReturn(receipt);

        // Act
        ResponseEntity<?> response = controller.burn(req);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("success", body.get("status"));
        assertEquals("0xghi789", body.get("transactionHash"));
        verify(deliverCoinService).burn(BigInteger.valueOf(200), "user3@example.com");
    }

    @Test
    void burn_ShouldReturnErrorWhenExceptionThrown() throws Exception {
        // Arrange
        MintBurnRequest req = mock(MintBurnRequest.class);
        when(req.getAmount()).thenReturn(BigInteger.valueOf(300));
        when(req.getEmail()).thenReturn("user4@example.com");
        when(deliverCoinService.burn(any(), any())).thenThrow(new RuntimeException("burn failed"));

        // Act
        ResponseEntity<?> response = controller.burn(req);

        // Assert
        assertEquals(500, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("burn failed", body.get("error"));
    }

    @Test
    void getBalance_ShouldReturnBalanceWhenNoException() throws Exception {
        // Arrange
        String email = "bal@example.com";
        when(deliverCoinService.balanceOf(email)).thenReturn(BigInteger.valueOf(12345));

        // Act
        ResponseEntity<?> response = controller.getBalance(email);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals(email, body.get("email"));
        assertEquals(BigInteger.valueOf(12345), body.get("balance"));
    }

    @Test
    void getBalance_ShouldReturnErrorWhenExceptionThrown() throws Exception {
        // Arrange
        String email = "bal2@example.com";
        when(deliverCoinService.balanceOf(email)).thenThrow(new RuntimeException("balance error"));

        // Act
        ResponseEntity<?> response = controller.getBalance(email);

        // Assert
        assertEquals(500, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("balance error", body.get("error"));
    }

    @Test
    void totalSupply_ShouldReturnSupplyWhenNoException() throws Exception {
        // Arrange
        when(deliverCoinService.totalSupply()).thenReturn(BigInteger.valueOf(99999));

        // Act
        ResponseEntity<?> response = controller.totalSupply();

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals(BigInteger.valueOf(99999), body.get("totalSupply"));
    }

    @Test
    void totalSupply_ShouldReturnErrorWhenExceptionThrown() throws Exception {
        // Arrange
        when(deliverCoinService.totalSupply()).thenThrow(new RuntimeException("supply error"));

        // Act
        ResponseEntity<?> response = controller.totalSupply();

        // Assert
        assertEquals(500, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("supply error", body.get("error"));
    }
}
