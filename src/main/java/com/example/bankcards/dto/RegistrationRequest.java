package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import com.example.bankcards.entity.Role;

@Data
@AllArgsConstructor
@Schema(description = "Запрос на регистрацию")
public class RegistrationRequest {
    @Schema(description = "Имя пользователя", example = "Anton")
    @Size(min = 3, max = 100, message = "Имя пользователя должно содержать от 3 до 100 символов")
    @NotBlank(message = "Имя пользователя не должно быть пустыми")
    private String username;

    @Schema(description = "Пароль", example = "hcsvugxd")
    @Size(min = 8, max = 100, message = "Длина пароля должна быть от 8 до 100 символов")
    @NotBlank(message = "Пароль не должен быть пустыми")
    private String password;

    @Schema(description = "Роль", example = "USER")
    @NotNull(message = "Роль пользователя не должно быть пустыми")
    private Role role;
}
