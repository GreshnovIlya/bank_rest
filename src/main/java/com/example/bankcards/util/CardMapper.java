package com.example.bankcards.util;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.Card;

public class CardMapper {
    public static CardDto toCardDto(Card card) {
        return new CardDto(card.getCardNumber(),
                           card.getCardholder().getUsername(),
                           card.getCardValidityPeriod(),
                           card.getCardStatus(),
                           card.getBalance());
    }
}
