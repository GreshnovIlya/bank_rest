package com.example.bankcards.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.NewCardDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.AccessException;
import com.example.bankcards.exception.CardException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public CardDto createCard(NewCardDto newCard) {
        User user = userService.getByUsername(newCard.getCardholderName());
        if (newCard.getCardNumber().isEmpty() || !newCard.getCardNumber().matches("\\d{4} \\d{4} \\d{4} \\d{4}")) {
            throw new IllegalArgumentException("Указан некорректный номер карты");
        }
        if (cardRepository.findByCardNumber(newCard.getCardNumber()).isPresent()) {
            throw new CardException("Карта с этим номером уже существует");
        }
        if (!newCard.getCardValidityPeriod().matches("(0[1-9]|1[0-2])/([0-9]{2})")) {
            throw new IllegalArgumentException("Указан некорректный срок действия карты");
        }

        Card card = cardRepository.save(new Card(null, newCard.getCardNumber(), user,
                newCard.getCardValidityPeriod(), CardStatus.ACTIVE, 0));
        return CardMapper.toCardDto(card);
    }

    public CardDto blockCard(String cardNumber) {
        Card card = getCard(cardNumber);
        if (card.getCardStatus() != CardStatus.ACTIVE) {
            throw new CardException("Карта уже заблокирована или истек срок ее действия");
        }

        card.blockCard();
        card = cardRepository.save(card);
        return CardMapper.toCardDto(card);
    }

    public CardDto activateCard(String cardNumber) {
        Card card = getCard(cardNumber);
        if (card.getCardStatus() != CardStatus.BLOCKED) {
            throw new CardException("Карта уже активна или истек срок ее действия");
        }

        card.activateCard();
        card = cardRepository.save(card);
        return CardMapper.toCardDto(card);
    }

    public void deleteCard(String cardNumber) {
        Card card = getCard(cardNumber);

        cardRepository.delete(card);
    }
    
    public List<CardDto> getCards(String username, String cardStatus, int page, int size, String sortBy,
                                  String sortMode) {
        Pageable pageable = splitIntoPages(page, size, sortBy, sortMode);

        User cardholder = userRepository.findByUsername(username).orElse(null);
        CardStatus status = null;
        if (!cardStatus.isBlank()) {
            status = CardStatus.valueOf(cardStatus);
        }

        List<Card> cards;
        if (cardholder != null && !cardStatus.isBlank()) {
            cards = cardRepository.findByCardholderAndCardStatus(pageable, cardholder, status);
        } else if (cardholder != null) {
            cards = cardRepository.findByCardholder(pageable, cardholder);
        } else if (!cardStatus.isBlank()) {
            cards = cardRepository.findByCardStatus(pageable, status);
        } else {
            return cardRepository.findAll(pageable).stream().map(CardMapper::toCardDto).toList();
        }
        return cards.stream().map(CardMapper::toCardDto).toList();
    }

    public List<CardDto> getCardsUser(int page, int size, String sortBy,
                                      String sortMode) {
        Pageable pageable = splitIntoPages(page, size, sortBy, sortMode);
        User user = userService.getCurrentUser();

        return cardRepository.findByCardholder(pageable, user).stream().map(CardMapper::toCardDto).toList();
    }

    public void transfersBetweenCards(String cardNumberSender, String cardNumberRecipient, double amount) {
        User user = userService.getCurrentUser();
        Card cardSender = getCard(cardNumberSender);
        Card cardRecipient = getCard(cardNumberRecipient);

        if (cardSender.getCardholder() != user || cardRecipient.getCardholder() != user) {
            throw new AccessException("Карта не доступна данному пользователю");
        }
        if (cardSender.getCardStatus() != CardStatus.ACTIVE || cardRecipient.getCardStatus() != CardStatus.ACTIVE) {
            throw new CardException("Карта заблокирована или истек срок ее действия");
        }
        if (cardSender.getBalance() >= amount) {
            cardSender.reduceBalance(amount);
            cardRepository.save(cardSender);
            cardRecipient.increaseBalance(amount);
            cardRepository.save(cardRecipient);
        } else {
            throw new CardException("Недостаточно средств на карте");
        }
    }

    public double getBalance(String cardNumber) {
        User user = userService.getCurrentUser();
        Card card = getCard(cardNumber);

        if (card.getCardholder() != user) {
            throw new AccessException("Карта не доступна данному пользователю");
        }
        if (card.getCardStatus() != CardStatus.ACTIVE) {
            throw new CardException("Карта заблокирована или истек срок ее действия");
        }

        return card.getBalance();
    }

    private Pageable splitIntoPages(int page, int size, String sortBy, String sortMode) {
        Sort sort = Sort.by(sortMode.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC,
                sortBy);
        return PageRequest.of(page, size, sort);
    }

    private Card getCard(String cardNumber) {
        return cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new NotFoundException("Карта не найдена"));
    }
}