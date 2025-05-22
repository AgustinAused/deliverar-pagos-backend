package com.deliverar.pagos.application.controllers;

import com.deliverar.pagos.application.mappers.OwnerMapper;
import com.deliverar.pagos.domain.dtos.TransactionDto;
import com.deliverar.pagos.domain.entities.Transaction;
import com.deliverar.pagos.domain.usecases.user.GetFiatTransactions;
import com.deliverar.pagos.domain.usecases.user.GetTransactions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TransactionHistoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OwnerMapper ownerMapper;
    @Mock
    private GetTransactions getTransactions;
    @Mock
    private GetFiatTransactions getFiatTransactions;

    private TransactionHistoryController controller;

    @BeforeEach
    void setUp() {
        controller = new TransactionHistoryController(ownerMapper, getTransactions, getFiatTransactions);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getTransactions_DefaultParams_ReturnsMappedResponse() throws Exception {
        // Arrange default paging: page=0, size=10, direction=DESC
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        Transaction tx1 = Transaction.builder().id(id1).build();
        Transaction tx2 = Transaction.builder().id(id2).build();
        Page<Transaction> page = new PageImpl<>(List.of(tx1, tx2),
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "transactionDate")),
                2);
        when(getTransactions.get(0, 10, Sort.Direction.DESC)).thenReturn(page);

        List<TransactionDto> dtos = List.of(
                TransactionDto.builder().id(id1).build(),
                TransactionDto.builder().id(id2).build()
        );
        when(ownerMapper.toTransactionDtos(page.getContent())).thenReturn(dtos);

        // Act & Assert
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions").isArray())
                .andExpect(jsonPath("$.transactions.length()").value(2))
                .andExpect(jsonPath("$.transactions[0].id").value(id1.toString()))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.sortDirection").value("DESC"));

        verify(getTransactions).get(0, 10, Sort.Direction.DESC);
        verify(ownerMapper).toTransactionDtos(page.getContent());
    }

    @Test
    void getTransactions_WithParams_ReturnsMappedResponse() throws Exception {
        // Arrange custom paging: page=1, size=5, direction=ASC
        UUID id = UUID.randomUUID();
        Transaction tx = Transaction.builder().id(id).build();
        Page<Transaction> page = new PageImpl<>(List.of(tx),
                PageRequest.of(1, 5, Sort.by(Sort.Direction.ASC, "transactionDate")),
                1);
        when(getTransactions.get(1, 5, Sort.Direction.ASC)).thenReturn(page);

        List<TransactionDto> dtos = List.of(
                TransactionDto.builder().id(id).build()
        );
        when(ownerMapper.toTransactionDtos(page.getContent())).thenReturn(dtos);

        // Act & Assert
        mockMvc.perform(get("/api/transactions")
                        .param("page", "1")
                        .param("size", "5")
                        .param("direction", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions[0].id").value(id.toString()))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.sortDirection").value("ASC"));

        verify(getTransactions).get(1, 5, Sort.Direction.ASC);
        verify(ownerMapper).toTransactionDtos(page.getContent());
    }
}