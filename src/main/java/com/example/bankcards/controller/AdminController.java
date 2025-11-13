package com.example.bankcards.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.NewCardDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Возможности админа")
public class AdminController {
    private final CardService cardService;
    private final UserService userService;

    @Operation(summary = "Создание карты")
    @PostMapping("/cards")
    @ResponseStatus(HttpStatus.CREATED)
    public CardDto createCard(@Valid @RequestBody NewCardDto newCard) {
        return cardService.createCard(newCard);
    }

    @Operation(summary = "Блокировка карты")
    @PatchMapping("/cards/{cardNumber}/block")
    public CardDto blockCard(@PathVariable String cardNumber) {
        return cardService.blockCard(cardNumber);
    }

    @Operation(summary = "Активация карты")
    @PatchMapping("/cards/{cardNumber}/activate")
    public CardDto activateCard(@PathVariable String cardNumber) {
        return cardService.activateCard(cardNumber);
    }

    @Operation(summary = "Удаление карты")
    @DeleteMapping("/cards/{cardNumber}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCard(@PathVariable String cardNumber) {
        cardService.deleteCard(cardNumber);
    }

    @Operation(summary = "Получение пользователей")
    @GetMapping("/users")
    public List<UserDto> getUsers(@Parameter(name = "page") @RequestParam(defaultValue = "0") int page,
                                  @Parameter(name = "size") @RequestParam(defaultValue = "10") int size,
                                  @Parameter(name = "sortBy") @RequestParam(defaultValue = "id") String sortBy,
                                  @Parameter(name = "sortMode") @RequestParam(defaultValue = "asc") String sortMode) {
        return userService.getUsers(page, size, sortBy, sortMode);
    }

    @Operation(summary = "Удаление пользователя")
    @DeleteMapping("/users/{username}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable String username) {
        userService.deleteUser(username);
    }

    @Operation(summary = "Получение карт")
    @GetMapping("/cards")
    public List<CardDto> getCards(@Parameter(name = "username") @RequestParam(defaultValue = "") String username,
                                  @Parameter(name = "cardStatus") @RequestParam(defaultValue = "") String cardStatus,
                                  @Parameter(name = "page") @RequestParam(defaultValue = "0") int page,
                                  @Parameter(name = "size") @RequestParam(defaultValue = "10") int size,
                                  @Parameter(name = "sortBy") @RequestParam(defaultValue = "id") String sortBy,
                                  @Parameter(name = "sortMode") @RequestParam(defaultValue = "asc") String sortMode) {
        return cardService.getCards(username, cardStatus, page, size, sortBy, sortMode);
    }
}
