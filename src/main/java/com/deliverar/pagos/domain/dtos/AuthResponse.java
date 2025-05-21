package com.deliverar.pagos.domain.dtos;

import com.deliverar.pagos.domain.entities.Permission;
import com.deliverar.pagos.domain.entities.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private Role role;
    protected Set<Permission> UserPermissions;
}
