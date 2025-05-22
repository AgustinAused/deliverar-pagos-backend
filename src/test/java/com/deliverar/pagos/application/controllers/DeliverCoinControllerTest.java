package com.deliverar.pagos.application.controllers;

import com.deliverar.pagos.adapters.crypto.service.DeliverCoinService;
import com.deliverar.pagos.domain.dtos.MintBurnRequest;
import com.deliverar.pagos.domain.dtos.TransactionResponse;
import com.deliverar.pagos.domain.dtos.TransferRequest;
import com.deliverar.pagos.domain.dtos.BuyCryptoRequest;
import com.deliverar.pagos.domain.entities.Owner;
import com.deliverar.pagos.domain.entities.Transaction;
import com.deliverar.pagos.domain.entities.TransactionStatus;
import com.deliverar.pagos.domain.exceptions.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        TransferRequest req = mock(TransferRequest.class);
        when(deliverCoinService.asyncTransfer(req)).thenReturn(trackingId);
        
        ResponseEntity<Map<String, Object>> response = controller.transfer(req);
        
        assertEquals(202, response.getStatusCode().value(), "Should return HTTP 202 Accepted");
        Map<String, Object> body = response.getBody();
        assertNotNull(body, "Response body should not be null");
        assertEquals("pending", body.get("status"));
        assertEquals(trackingId, body.get("trackingId"));
        assertEquals("Transferencia iniciada y en proceso", body.get("message"));
        verify(deliverCoinService, times(1)).asyncTransfer(req);
    }

    @Test
    void transferStatus_ShouldReturnTransactionResponse() {
        UUID originId = UUID.randomUUID();
        UUID destId   = UUID.randomUUID();
        UUID txId     = UUID.randomUUID();
        Owner origin = Owner.builder().id(originId).build();
        Owner dest   = Owner.builder().id(destId).build();
        Transaction tx = Transaction.builder()
                .id(txId)
                .originOwner(origin)
                .destinationOwner(dest)
                .amount(BigDecimal.TEN)
                .status(TransactionStatus.SUCCESS)
                .blockchainTxHash("0xabc123")
                .build();

        when(deliverCoinService.getTransferStatus(trackingId)).thenReturn(tx);
        when(deliverCoinService.getEmailByOwnerId(originId)).thenReturn("from@example.com");
        when(deliverCoinService.getEmailByOwnerId(destId)).thenReturn("to@example.com");

        ResponseEntity<TransactionResponse> response = controller.transferStatus(trackingId);

        assertEquals(200, response.getStatusCode().value(), "Should return HTTP 200 OK");
        com.deliverar.pagos.domain.dtos.TransactionResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(txId, body.getId());
        assertEquals("from@example.com", body.getFromOwnerEmail());
        assertEquals("to@example.com", body.getToOwnerEmail());
        assertEquals(BigDecimal.TEN, body.getAmount());
        assertEquals("SUCCESS", body.getStatus());
        assertEquals("0xabc123", body.getTxHash());
        verify(deliverCoinService).getTransferStatus(trackingId);
    }

    @Test
    void mint_ShouldReturnSuccessWhenNoException() throws Exception {
        MintBurnRequest req = MintBurnRequest.builder()
                .email("user@example.com")
                .amount(BigDecimal.valueOf(100.00))
                .build();
        TransactionReceipt receipt = mock(TransactionReceipt.class);
        when(receipt.getTransactionHash()).thenReturn("0xdef456");
        when(deliverCoinService.mint(BigDecimal.valueOf(100.00), "user@example.com")).thenReturn(receipt);

        ResponseEntity<?> response = controller.mint(req);

        assertEquals(200, response.getStatusCode().value());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("success", body.get("status"));
        assertEquals("0xdef456", body.get("transactionHash"));
        verify(deliverCoinService).mint(BigDecimal.valueOf(100.00), "user@example.com");
    }

    @Test
    void mint_ShouldReturnErrorWhenExceptionThrown() throws Exception {
        MintBurnRequest req = mock(MintBurnRequest.class);
        when(req.getAmount()).thenReturn(BigDecimal.valueOf(50));
        when(req.getEmail()).thenReturn("user2@example.com");
        when(deliverCoinService.mint(any(), any())).thenThrow(new RuntimeException("mint failed"));

        ResponseEntity<?> response = controller.mint(req);

        assertEquals(500, response.getStatusCode().value());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("mint failed", body.get("error"));
    }

    @Test
    void burn_ShouldReturnSuccessWhenNoException() throws Exception {
        MintBurnRequest req = mock(MintBurnRequest.class);
        when(req.getAmount()).thenReturn(BigDecimal.valueOf(200));
        when(req.getEmail()).thenReturn("user3@example.com");
        TransactionReceipt receipt = mock(TransactionReceipt.class);
        when(receipt.getTransactionHash()).thenReturn("0xghi789");
        when(deliverCoinService.burn(BigDecimal.valueOf(200), "user3@example.com")).thenReturn(receipt);

        ResponseEntity<?> response = controller.burn(req);

        assertEquals(200, response.getStatusCode().value());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("success", body.get("status"));
        assertEquals("0xghi789", body.get("transactionHash"));
        verify(deliverCoinService).burn(BigDecimal.valueOf(200), "user3@example.com");
    }

    @Test
    void burn_ShouldReturnErrorWhenExceptionThrown() throws Exception {
        MintBurnRequest req = mock(MintBurnRequest.class);
        when(req.getAmount()).thenReturn(BigDecimal.valueOf(300));
        when(req.getEmail()).thenReturn("user4@example.com");
        when(deliverCoinService.burn(any(), any())).thenThrow(new RuntimeException("burn failed"));

        ResponseEntity<?> response = controller.burn(req);

        assertEquals(500, response.getStatusCode().value());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("burn failed", body.get("error"));
    }

    @Test
    void getBalance_ShouldReturnBalanceWhenNoException() throws Exception {
        String email = "bal@example.com";
        when(deliverCoinService.balanceOf(email)).thenReturn(BigDecimal.valueOf(12345));

        ResponseEntity<?> response = controller.getBalance(email);

        assertEquals(200, response.getStatusCode().value());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals(email, body.get("email"));
        assertEquals(BigDecimal.valueOf(12345), body.get("balance"));
    }

    @Test
    void getBalance_ShouldReturnErrorWhenExceptionThrown() throws Exception {
        String email = "bal2@example.com";
        when(deliverCoinService.balanceOf(email)).thenThrow(new RuntimeException("balance error"));

        ResponseEntity<?> response = controller.getBalance(email);

        assertEquals(500, response.getStatusCode().value());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("balance error", body.get("error"));
    }

    @Test
    void totalSupply_ShouldReturnSupplyWhenNoException() throws Exception {
        when(deliverCoinService.totalSupply()).thenReturn(BigDecimal.valueOf(99999));

        ResponseEntity<?> response = controller.totalSupply();

        assertEquals(200, response.getStatusCode().value());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals(BigDecimal.valueOf(99999), body.get("totalSupply"));
    }

    @Test
    void totalSupply_ShouldReturnErrorWhenExceptionThrown() throws Exception {
        when(deliverCoinService.totalSupply()).thenThrow(new RuntimeException("supply error"));

        ResponseEntity<?> response = controller.totalSupply();

        assertEquals(500, response.getStatusCode().value());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("supply error", body.get("error"));
    }

    @Test
    void buyCrypto_ShouldReturnAcceptedAndPendingStatus() throws Exception {
        // Arrange
        BuyCryptoRequest req = new BuyCryptoRequest();
        req.setEmail("buyer@example.com");
        req.setAmount(BigDecimal.valueOf(100));
        when(deliverCoinService.buyCryptoWithFiat(req.getEmail(), req.getAmount())).thenReturn(trackingId);

        // Act
        ResponseEntity<Map<String, Object>> response = controller.buyCrypto(req);

        // Assert
        assertEquals(202, response.getStatusCode().value(), "Should return HTTP 202 Accepted");
        Map<String, Object> body = response.getBody();
        assertNotNull(body, "Response body should not be null");
        assertEquals("pending", body.get("status"));
        assertEquals(trackingId, body.get("trackingId"));
        assertEquals("Compra iniciada y en proceso", body.get("message"));
        verify(deliverCoinService, times(1)).buyCryptoWithFiat(req.getEmail(), req.getAmount());
    }

    @Test
    void buyCrypto_ShouldReturnBadRequestWhenInsufficientBalance() throws Exception {
        // Arrange
        BuyCryptoRequest req = new BuyCryptoRequest();
        req.setEmail("poor@example.com");
        req.setAmount(BigDecimal.valueOf(1000));
        when(deliverCoinService.buyCryptoWithFiat(req.getEmail(), req.getAmount()))
            .thenThrow(new BadRequestException("Insufficient fiat balance"));

        // Act
        ResponseEntity<Map<String, Object>> response = controller.buyCrypto(req);

        // Assert
        assertEquals(400, response.getStatusCode().value(), "Should return HTTP 400 Bad Request");
        Map<String, Object> body = response.getBody();
        assertNotNull(body, "Response body should not be null");
        assertEquals("Insufficient fiat balance", body.get("error"));
    }

    @Test
    void buyCrypto_ShouldReturnErrorWhenServiceFails() throws Exception {
        // Arrange
        BuyCryptoRequest req = new BuyCryptoRequest();
        req.setEmail("error@example.com");
        req.setAmount(BigDecimal.valueOf(50));
        when(deliverCoinService.buyCryptoWithFiat(req.getEmail(), req.getAmount()))
            .thenThrow(new RuntimeException("Service error"));

        // Act
        ResponseEntity<Map<String, Object>> response = controller.buyCrypto(req);

        // Assert
        assertEquals(500, response.getStatusCode().value(), "Should return HTTP 500 Internal Server Error");
        Map<String, Object> body = response.getBody();
        assertNotNull(body, "Response body should not be null");
        assertEquals("Service error", body.get("error"));
    }
}
