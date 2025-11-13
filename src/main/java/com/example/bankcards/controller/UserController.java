package com.example.bankcards.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.service.CardService;

import java.util.List;

@RestController
@RequestMapping("/user")
@PreAuthorize("hasAuthority('USER')")
@RequiredArgsConstructor
@Tag(name = "Возможности пользователя")
public class UserController {
    private final CardService cardService;

    @Operation(summary = "Получение карт")
    @GetMapping("/cards")
    public List<CardDto> getCards(@Parameter(name = "page") @RequestParam(defaultValue = "0") int page,
                                  @Parameter(name = "size") @RequestParam(defaultValue = "10") int size,
                                  @Parameter(name = "sortBy") @RequestParam(defaultValue = "id") String sortBy,
                                  @Parameter(name = "sortMode") @RequestParam(defaultValue = "asc") String sortMode) {
        return cardService.getCardsUser(page, size, sortBy, sortMode);
    }

    @Operation(summary = "Перевод между картами одного пользователя")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping("/cards/transfers/{amount}/from/{cardNumberSender}/to/{cardNumberRecipient}")
    public void transfersBetweenCards(
                        @Parameter(name = "cardNumberSender") @PathVariable String cardNumberSender,
                        @Parameter(name = "cardNumberRecipient") @PathVariable String cardNumberRecipient,
                        @Parameter(name = "amount") @PathVariable double amount) {
        cardService.transfersBetweenCards(cardNumberSender, cardNumberRecipient, amount);
    }

    @Operation(summary = "Получение баланса")
    @GetMapping("/cards/{cardNumber}/balance")
    public double getBalance(@Parameter(name = "cardNumber") @PathVariable String cardNumber) {
        return cardService.getBalance(cardNumber);
    }

    @Operation(summary = "Блокировка карты")
    @PatchMapping("/cards/{cardNumber}/block")
    public CardDto blockCard(@Parameter(name = "cardNumber") @PathVariable String cardNumber) {
        return cardService.blockCard(cardNumber);
    }
}
