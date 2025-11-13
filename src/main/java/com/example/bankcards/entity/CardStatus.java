package com.example.bankcards.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(enumAsRef = true)
public enum CardStatus {
    ACTIVE,
    BLOCKED,
    EXPIRED
}
