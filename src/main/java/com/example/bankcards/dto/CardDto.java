package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import com.example.bankcards.entity.CardStatus;

@Getter
@AllArgsConstructor
@Schema(description = "Карта")
public class CardDto {
    @Schema(description = "Номер карты", example = "**** **** **** 1234")
    @Size(min = 19, max = 19, message = "Номер карты должен содержать 19 символов")
    private String cardNumber;

    @Schema(description = "Имя владельца", example = "Anton")
    @Size(min = 3, max = 100, message = "Имя пользователя должно содержать от 3 до 100 символов")
    private String cardholder;

    @Schema(description = "Период действия", example = "03/26")
    @Size(min = 5, max = 5, message = "Период действия должен содержать 5 символов")
    private String cardValidityPeriod;

    @Schema(description = "Статус", example = "ACTIVE")
    private CardStatus cardStatus;

    @Schema(description = "Баланс", example = "12.34")
    private double balance;

    @Override
    public String toString() {
        return "Card{" +
               "cardNumber='**** **** **** " + cardNumber.substring(cardNumber.length() - 4) + '\'' +
               ", cardholder='" + cardholder + '\'' +
               ", cardValidityPeriod='" + cardValidityPeriod + '\'' +
               ", cardStatus='" + cardStatus + '\'' +
               ", balance='" + balance + '\'';
    }
}