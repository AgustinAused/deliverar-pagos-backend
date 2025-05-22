package com.deliverar.pagos.application.controllers;


import com.deliverar.pagos.application.mappers.UserMapper;
import com.deliverar.pagos.domain.dtos.CreateUserRequest;
import com.deliverar.pagos.domain.dtos.CreateUserResponse;
import com.deliverar.pagos.domain.dtos.UserDto;
import com.deliverar.pagos.domain.entities.User;
import com.deliverar.pagos.domain.usecases.user.CreateUser;
import com.deliverar.pagos.domain.usecases.user.DeleteUser;
import com.deliverar.pagos.domain.usecases.user.GetUser;
import com.deliverar.pagos.domain.usecases.user.GetUserList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequestMapping("/api/users")
@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserMapper userMapper;
    private final CreateUser createUser;
    private final PasswordEncoder passwordEncoder;
    private final GetUser getUser;
    private final GetUserList getUserList;
    private final DeleteUser deleteUser;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getUserList() {
        List<User> users = getUserList.get();
        return userMapper.toDtos(users);
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public CreateUserResponse create(@RequestBody CreateUserRequest request) {
        User user = createUser.create(request.getName(), request.getEmail().toLowerCase(), passwordEncoder.encode(request.getPassword()), request.getRole());
        return CreateUserResponse.builder().id(user.getId()).build();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserDto get(@PathVariable UUID id) {
        return userMapper.toDto(getUser.get(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        deleteUser.delete(id);
    }
}