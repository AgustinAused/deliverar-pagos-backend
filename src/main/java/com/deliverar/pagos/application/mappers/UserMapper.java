package com.deliverar.pagos.application.mappers;

import com.deliverar.pagos.domain.dtos.UserDto;
import com.deliverar.pagos.domain.entities.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {
    public UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .role(user.getRole())
                .build();
    }

    public List<UserDto> toDtos(List<User> users) {
        return users.stream().map(this::toDto).collect(Collectors.toList());
    }
}