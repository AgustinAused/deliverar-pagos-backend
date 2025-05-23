package com.deliverar.pagos.application.controllers;

import com.deliverar.pagos.application.mappers.UserMapper;
import com.deliverar.pagos.domain.dtos.UserDto;
import com.deliverar.pagos.domain.entities.Role;
import com.deliverar.pagos.domain.entities.User;
import com.deliverar.pagos.domain.usecases.user.CreateUser;
import com.deliverar.pagos.domain.usecases.user.DeleteUser;
import com.deliverar.pagos.domain.usecases.user.GetUser;
import com.deliverar.pagos.domain.usecases.user.GetUserList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserMapper userMapper;
    @Mock
    private CreateUser createUser;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private GetUser getUser;
    @Mock
    private GetUserList getUserList;
    @Mock
    private DeleteUser deleteUser;

    private UserController controller;
    private UUID userId;
    private Instant now;

    @BeforeEach
    void setUp() {
        controller = new UserController(userMapper, createUser, passwordEncoder, getUser, getUserList, deleteUser);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        userId = UUID.randomUUID();
        now = Instant.now();
    }

    @Test
    void createUser_ReturnsCreated() throws Exception {
        String rawPassword = "SecretPass";
        String encodedPassword = "EncodedSecret";
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        User stubUser = User.builder()
                .id(userId)
                .name("John Doe")
                .email("user@example.com")
                .passwordHash(encodedPassword)
                .role(Role.ADMIN)
                .createdAt(now)
                .updatedAt(now)
                .build();
        when(createUser.create(eq("John Doe"), eq("user@example.com"), eq(encodedPassword), eq(Role.ADMIN)))
                .thenReturn(stubUser);

        String json = "{\"name\":\"John Doe\",\"email\":\"user@example.com\",\"password\":\"SecretPass\",\"role\":\"ADMIN\"}";

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userId.toString()));

        verify(passwordEncoder).encode("SecretPass");
        verify(createUser).create("John Doe", "user@example.com", encodedPassword, Role.ADMIN);
    }

    @Test
    void getUser_ReturnsUserDto() throws Exception {
        User stubUser = User.builder()
                .id(userId)
                .name("Alice")
                .email("alice@example.com")
                .passwordHash("hash")
                .role(Role.ADMIN)
                .createdAt(now)
                .updatedAt(now)
                .build();
        UserDto userDto = UserDto.builder()
                .id(userId)
                .name("Alice")
                .email("alice@example.com")
                .role(Role.ADMIN)
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(getUser.get(userId)).thenReturn(stubUser);
        when(userMapper.toDto(stubUser)).thenReturn(userDto);

        mockMvc.perform(get("/api/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        verify(getUser).get(userId);
    }
}
