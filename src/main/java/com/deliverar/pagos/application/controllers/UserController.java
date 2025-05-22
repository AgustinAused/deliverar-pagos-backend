package com.deliverar.pagos.application.controllers;

import com.deliverar.pagos.domain.dtos.CreateUserRequest;
import com.deliverar.pagos.domain.dtos.CreateUserResponse;
import com.deliverar.pagos.domain.dtos.UserDto;
import com.deliverar.pagos.domain.entities.User;
import com.deliverar.pagos.domain.usecases.user.CreateUser;
import com.deliverar.pagos.domain.usecases.user.GetUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Usuarios", description = "Operaciones relacionadas con la gestión de usuarios")
@RequestMapping("/api/users")
@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final CreateUser createUser;
    private final PasswordEncoder passwordEncoder;
    private final GetUser getUser;

    @Operation(
            summary = "Crear un usuario",
            description = "Registra un nuevo usuario con los datos proporcionados."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuario creado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreateUserResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @ApiResponse(responseCode = "409", description = "Email ya registrado", content = @Content)
    })
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public CreateUserResponse create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos necesarios para crear el usuario",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateUserRequest.class))
            )
            @RequestBody CreateUserRequest request
    ) {
        User user = createUser.create(
                request.getName(),
                request.getEmail().toLowerCase(),
                passwordEncoder.encode(request.getPassword()),
                request.getRole()
        );
        return CreateUserResponse.builder().id(user.getId()).build();
    }

    @Operation(
            summary = "Obtener usuario por ID",
            description = "Devuelve los datos del usuario según el UUID proporcionado."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserDto get(
            @Parameter(description = "UUID del usuario", example = "56e1b7f1-27e7-4cbe-a8a6-d1b56d74d207")
            @PathVariable UUID id
    ) {
        User user = getUser.get(id);
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .role(user.getRole())
                .build();
    }
}
