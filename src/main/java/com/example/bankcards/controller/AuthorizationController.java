package com.example.bankcards.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.bankcards.dto.AuthorizationRequest;
import com.example.bankcards.dto.RegistrationRequest;
import com.example.bankcards.service.AuthorizationService;

@RestController
@RequestMapping("/authorization")
@RequiredArgsConstructor
@Tag(name = "Авторизация")
public class AuthorizationController {
    private final AuthorizationService authService;

    @Operation(summary = "Регистрация пользователя")
    @PostMapping("/registration")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<String> registration(@Valid @RequestBody RegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registration(request));
    }

    @Operation(summary = "Авторизация пользователя")
    @PostMapping("/authorization")
    public ResponseEntity<String> authorization(@Valid @RequestBody AuthorizationRequest request) {
        return ResponseEntity.ok(authService.authorization(request));
    }
}