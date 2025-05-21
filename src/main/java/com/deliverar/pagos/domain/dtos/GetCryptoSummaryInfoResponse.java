package com.deliverar.pagos.domain.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetCryptoSummaryInfoResponse {
    Long totalOfTransactions;
    Long totalOfOwners;
    BigDecimal totalOfCryptos;
}
