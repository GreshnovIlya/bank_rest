package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import com.example.bankcards.entity.Role;

@Getter
@AllArgsConstructor
@ToString
@Schema(description = "Пользователь")
public class UserDto {
    @Schema(description = "Имя пользователя", example = "Anton")
    @Size(min = 3, max = 100, message = "Имя пользователя должно содержать от 3 до 100 символов")
    private String username;

    @Schema(description = "Роль", example = "USER")
    private Role role;
}