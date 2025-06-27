package com.deliverar.pagos.domain.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LdapAuthRequest {
    
    @NotBlank(message = "Email es requerido")
    @Email(message = "Email debe tener un formato válido")
    private String email;
    
    @NotBlank(message = "Password es requerido")
    private String password;
}