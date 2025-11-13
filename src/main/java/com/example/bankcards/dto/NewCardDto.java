package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NewCardDto {
    @Schema(description = "Номер карты", example = "**** **** **** 1234")
    @Size(min = 19, max = 19, message = "Номер карты должен содержать 19 символов")
    @NotBlank(message = "Номер карты не должен быть пустыми")
    private String cardNumber;

    @Schema(description = "Имя владельца", example = "Anton")
    @Size(min = 3, max = 100, message = "Имя пользователя должно содержать от 3 до 100 символов")
    @NotBlank(message = "Имя владельца не должно быть пустыми")
    private String cardholderName;

    @Schema(description = "Период действия", example = "03/26")
    @Size(min = 5, max = 5, message = "Период действия должен содержать 5 символов")
    @NotBlank(message = "Период действия не должно быть пустыми")
    private String cardValidityPeriod;
}
