package com.deliverar.pagos.application.controllers;


import com.deliverar.pagos.domain.dtos.CreateUserRequest;
import com.deliverar.pagos.domain.dtos.CreateUserResponse;
import com.deliverar.pagos.domain.entities.User;
import com.deliverar.pagos.domain.usecases.user.CreateUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/user")
@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final CreateUser createUser;
    private final PasswordEncoder passwordEncoder;

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public CreateUserResponse create(@RequestBody CreateUserRequest request) {
        User user = createUser.create(request.getName(), request.getEmail().toLowerCase(), passwordEncoder.encode(request.getPassword()), request.getRole());
        return CreateUserResponse.builder().id(user.getId()).build();
    }
}
