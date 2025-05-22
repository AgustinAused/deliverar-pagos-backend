package com.deliverar.pagos.domain.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetOwnersResponse {
    List<OwnerDto> ownersList;
    int page;
    int size;
    int totalElements;
    boolean hasNext;
    String sortDirection;
}