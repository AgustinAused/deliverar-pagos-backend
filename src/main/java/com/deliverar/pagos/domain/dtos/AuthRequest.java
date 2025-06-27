package com.deliverar.pagos.domain.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthRequest {
    private String email;
    private String password;
}

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
class AdAuthRequest {
    private String username;
    private String password;
}
