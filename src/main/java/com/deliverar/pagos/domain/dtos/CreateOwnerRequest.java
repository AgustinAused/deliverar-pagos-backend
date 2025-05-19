package com.deliverar.pagos.domain.dtos;

import com.deliverar.pagos.domain.entities.OwnerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateOwnerRequest {
    private String name;
    private String email;
    private OwnerType ownerType;
}
