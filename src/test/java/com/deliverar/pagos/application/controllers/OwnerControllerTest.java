package com.deliverar.pagos.application.controllers;

import com.deliverar.pagos.application.mappers.OwnerMapper;
import com.deliverar.pagos.domain.dtos.CreateOwnerRequest;
import com.deliverar.pagos.domain.dtos.FiatExchangeRequest;
import com.deliverar.pagos.domain.dtos.FiatTransactionDto;
import com.deliverar.pagos.domain.dtos.TransactionDto;
import com.deliverar.pagos.domain.entities.*;
import com.deliverar.pagos.domain.usecases.owner.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OwnerControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OwnerMapper ownerMapper;
    @Mock
    private CreateOwner createOwner;
    @Mock
    private GetOwner getOwner;
    @Mock
    private GetOwnerList getOwnerList;
    @Mock
    private GetOwnerTransactions getOwnerTransactions;
    @Mock
    private GetOwnerFiatTransactions getOwnerFiatTransactions;
    @Mock
    private ExchangeFiat exchangeFiat;

    private OwnerController controller;
    private UUID ownerId;

    @BeforeEach
    void setUp() {
        controller = new OwnerController(ownerMapper, createOwner, getOwner, getOwnerList, getOwnerTransactions, getOwnerFiatTransactions, exchangeFiat);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        ownerId = UUID.randomUUID();
    }

    @Test
    void createOwner_ReturnsCreated() throws Exception {
        CreateOwnerRequest req = new CreateOwnerRequest("John", "john@example.com", OwnerType.NATURAL);
        when(createOwner.create(anyString(), anyString(), any(), any(), any())).thenReturn(Owner.builder().id(ownerId).build());

        mockMvc.perform(post("/api/owners").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"John\",\"email\":\"john@example.com\",\"ownerType\":\"NATURAL\"}")).andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(ownerId.toString()));
    }

    @Test
    void getBalances_ReturnsBalances() throws Exception {
        when(getOwner.get(ownerId)).thenReturn(Owner.builder().id(ownerId).wallet(Wallet.builder().fiatBalance(BigDecimal.valueOf(50)).cryptoBalance(BigDecimal.valueOf(1)).build()).build());

        mockMvc.perform(get("/api/owners/" + ownerId + "/balances")).andExpect(status().isOk()).andExpect(jsonPath("$.fiatBalance").value(50)).andExpect(jsonPath("$.cryptoBalance").value(1));
    }

    @Test
    void getTransactions_ReturnsPage() throws Exception {
        Transaction tx = Transaction.builder().id(UUID.randomUUID()).build();
        Page<Transaction> page = new PageImpl<>(List.of(tx));
        when(getOwner.get(ownerId)).thenReturn(Owner.builder().id(ownerId).build());
        when(getOwnerTransactions.get(any(), anyInt(), anyInt(), any())).thenReturn(page);
        when(ownerMapper.toTransactionDtos(anyList())).thenReturn(List.of(new TransactionDto()));

        mockMvc.perform(get("/api/owners/" + ownerId + "/transactions").param("page", "0").param("size", "1").param("direction", "ASC")).andExpect(status().isOk()).andExpect(jsonPath("$.transactions").isArray()).andExpect(jsonPath("$.totalElements").value(1)).andExpect(jsonPath("$.page").value(0)).andExpect(jsonPath("$.size").value(1));
    }

    @Test
    void getFiatTransactions_ReturnsPage() throws Exception {
        FiatTransaction ftx = FiatTransaction.builder().id(UUID.randomUUID()).build();
        Page<FiatTransaction> page = new PageImpl<>(List.of(ftx));
        when(getOwner.get(ownerId)).thenReturn(Owner.builder().id(ownerId).build());
        when(getOwnerFiatTransactions.get(any(), anyInt(), anyInt(), any())).thenReturn(page);
        when(ownerMapper.toFiatTransactionDtos(anyList())).thenReturn(List.of(new FiatTransactionDto()));

        mockMvc.perform(get("/api/owners/" + ownerId + "/transactions/fiat").param("page", "0").param("size", "1").param("direction", "DESC")).andExpect(status().isOk()).andExpect(jsonPath("$.transactions").isArray()).andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void exchangeOwnerFiat_ReturnsAmount() throws Exception {
        FiatExchangeRequest req = new FiatExchangeRequest(BigDecimal.TEN, ExchangeOperation.INFLOW);
        when(getOwner.get(ownerId)).thenReturn(Owner.builder().id(ownerId).build());
        when(exchangeFiat.exchange(any(), any(), any())).thenReturn(BigDecimal.TEN);

        mockMvc.perform(post("/api/owners/" + ownerId + "/fiat").contentType(MediaType.APPLICATION_JSON).content("{\"amount\":10,\"operation\":\"INFLOW\"}")).andExpect(status().isOk()).andExpect(content().string("10"));
    }
}
