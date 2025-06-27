package com.deliverar.pagos.domain.dtos;

import com.deliverar.pagos.domain.entities.OwnerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OwnerDto {
    private UUID id;
    private String name;
    private String email;
    private OwnerType ownerType;
    private BigDecimal fiatBalance;
    private BigDecimal cryptoBalance;
}
