package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.NewCardDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.AccessException;
import com.example.bankcards.exception.CardException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CardServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private CardService cardService;

    User user = new User(1L, "Anton", "bgyfygvbhjnug", Role.USER);
    NewCardDto newCardDto = new NewCardDto("1111 1111 1111 1111", user.getUsername(),
            "12/26");
    CardDto cardDto = new CardDto("1111 1111 1111 1111", user.getUsername(),"12/26",
            CardStatus.ACTIVE, 100);
    Card cardFirst = new Card(1L, "1111 1111 1111 1111", user, "12/26",
            CardStatus.ACTIVE, 100);
    Card cardSecond = new Card(2L, "1111 1111 1111 1112", user, "12/26",
            CardStatus.ACTIVE, 100);
    Card cardBlock = new Card(1L, "1111 1111 1111 1111", user, "12/26",
            CardStatus.BLOCKED, 100);

    @Test
    void createCard_Successful() {
        when(userService.getByUsername(user.getUsername()))
                .thenReturn(user);
        when(cardRepository.findByCardNumber(newCardDto.getCardNumber()))
                .thenReturn(Optional.empty());
        when(cardRepository.save(any(Card.class)))
                .thenReturn(cardFirst);

        CardDto card = cardService.createCard(newCardDto);

        assertEquals(card.getCardValidityPeriod(), cardDto.getCardValidityPeriod());
        assertEquals(card.getCardStatus(), cardDto.getCardStatus());
        assertEquals(card.getCardNumber(), cardDto.getCardNumber());
        assertEquals(card.getCardholder(), cardDto.getCardholder());
        assertEquals(card.getBalance(), cardDto.getBalance());

        verify(userService).getByUsername(user.getUsername());
        verify(cardRepository).findByCardNumber(newCardDto.getCardNumber());
        verify(cardRepository).save(any());
    }

    @Test
    void createCard_CardWithCardNumberExist() {
        when(userService.getByUsername(user.getUsername()))
                .thenReturn(user);
        when(cardRepository.findByCardNumber(newCardDto.getCardNumber()))
                .thenReturn(Optional.of(cardFirst));

        assertThrows(CardException.class, () -> cardService.createCard(newCardDto));

        verify(userService).getByUsername(user.getUsername());
        verify(cardRepository).findByCardNumber(newCardDto.getCardNumber());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void createCard_CardNumberIsIncorrect() {
        NewCardDto incorrectCardDto = new NewCardDto("1111", user.getUsername(),
                "12/26");

        when(userService.getByUsername(user.getUsername()))
                .thenReturn(user);

        assertThrows(IllegalArgumentException.class, () -> cardService.createCard(incorrectCardDto));

        verify(userService).getByUsername(user.getUsername());
        verify(cardRepository, never()).findByCardNumber(any());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void createCard_CardValidityPeriodIsIncorrect() {
        NewCardDto incorrectCardDto = new NewCardDto("1111 1111 1111 1111", user.getUsername(),
                "13/26");
        when(cardRepository.findByCardNumber(newCardDto.getCardNumber()))
                .thenReturn(Optional.empty());

        when(userService.getByUsername(user.getUsername()))
                .thenReturn(user);

        assertThrows(IllegalArgumentException.class, () -> cardService.createCard(incorrectCardDto));

        verify(userService).getByUsername(user.getUsername());
        verify(cardRepository).findByCardNumber(newCardDto.getCardNumber());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void blockCard_Successful() {
        when(cardRepository.findByCardNumber(cardFirst.getCardNumber()))
                .thenReturn(Optional.of(cardFirst));
        when(cardRepository.save(cardFirst))
                .thenReturn(cardBlock);

        CardDto card = cardService.blockCard(cardFirst.getCardNumber());

        assertEquals(card.getCardStatus(), CardStatus.BLOCKED);

        verify(cardRepository).findByCardNumber(cardFirst.getCardNumber());
        verify(cardRepository).save(any());
    }

    @Test
    void blockCard_CardNotFound() {
        when(cardRepository.findByCardNumber(cardFirst.getCardNumber()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> cardService.blockCard(cardFirst.getCardNumber()));

        verify(cardRepository).findByCardNumber(cardFirst.getCardNumber());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void blockCard_CardIsBlocked() {
        when(cardRepository.findByCardNumber(cardBlock.getCardNumber()))
                .thenReturn(Optional.of(cardBlock));

        assertThrows(CardException.class, () -> cardService.blockCard(cardBlock.getCardNumber()));

        verify(cardRepository).findByCardNumber(cardFirst.getCardNumber());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void activateCard_Successful() {
        when(cardRepository.findByCardNumber(cardBlock.getCardNumber()))
                .thenReturn(Optional.of(cardBlock));
        when(cardRepository.save(cardBlock))
                .thenReturn(cardFirst);

        CardDto card = cardService.activateCard(cardFirst.getCardNumber());

        assertEquals(card.getCardStatus(), CardStatus.ACTIVE);

        verify(cardRepository).findByCardNumber(newCardDto.getCardNumber());
        verify(cardRepository).save(any());
    }

    @Test
    void activateCard_CardNotFound() {
        when(cardRepository.findByCardNumber(cardFirst.getCardNumber()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> cardService.activateCard(cardFirst.getCardNumber()));

        verify(cardRepository).findByCardNumber(cardFirst.getCardNumber());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void activateCard_CardIsActive() {
        when(cardRepository.findByCardNumber(cardFirst.getCardNumber()))
                .thenReturn(Optional.of(cardFirst));

        assertThrows(CardException.class, () -> cardService.activateCard(cardFirst.getCardNumber()));

        verify(cardRepository).findByCardNumber(cardFirst.getCardNumber());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void deleteCard_Successful() {
        when(cardRepository.findByCardNumber(cardFirst.getCardNumber()))
                .thenReturn(Optional.of(cardFirst));

        cardService.deleteCard(cardFirst.getCardNumber());

        verify(cardRepository).findByCardNumber(newCardDto.getCardNumber());
        verify(cardRepository).delete(cardFirst);
    }

    @Test
    void deleteCard_CardNotFound() {
        when(cardRepository.findByCardNumber(cardFirst.getCardNumber()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> cardService.deleteCard(cardFirst.getCardNumber()));

        verify(cardRepository).findByCardNumber(cardFirst.getCardNumber());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void getCards_ByUsernameAndCardStatus() {
        List<Card> cards = List.of(cardFirst, cardSecond);

        when(userRepository.findByUsername(user.getUsername()))
                .thenReturn(Optional.of(user));
        when(cardRepository.findByCardholderAndCardStatus(PageRequest.of(0, 10,
                Sort.by(Sort.Direction.ASC, "id")), user, CardStatus.ACTIVE))
                .thenReturn(cards);

        List<CardDto> cardDtos = cardService.getCards(user.getUsername(), "ACTIVE",
                0, 10, "id", "asc");

        assertEquals(cardDtos.size(), cards.size());

        verify(userRepository).findByUsername(user.getUsername());
        verify(cardRepository).findByCardholderAndCardStatus(any(Pageable.class), any(User.class),
                any(CardStatus.class));
    }

    @Test
    void getCards_ByUsername() {
        List<Card> cards = List.of(cardFirst, cardSecond);

        when(userRepository.findByUsername(user.getUsername()))
                .thenReturn(Optional.of(user));
        when(cardRepository.findByCardholder(PageRequest.of(0, 10,
                Sort.by(Sort.Direction.ASC, "id")), user))
                .thenReturn(cards);

        List<CardDto> cardDtos = cardService.getCards(user.getUsername(), "",
                0, 10, "id", "asc");

        assertEquals(cardDtos.size(), cards.size());

        verify(userRepository).findByUsername(user.getUsername());
        verify(cardRepository).findByCardholder(any(Pageable.class), any(User.class));
    }

    @Test
    void getCards_ByCardStatus() {
        List<Card> cards = List.of(cardFirst, cardSecond);

        when(userRepository.findByUsername(""))
                .thenReturn(Optional.empty());
        when(cardRepository.findByCardStatus(PageRequest.of(0, 10,
                Sort.by(Sort.Direction.ASC, "id")), CardStatus.ACTIVE))
                .thenReturn(cards);

        List<CardDto> cardDtos = cardService.getCards("", "ACTIVE",
                0, 10, "id", "asc");

        assertEquals(cardDtos.size(), cards.size());

        verify(userRepository).findByUsername("");
        verify(cardRepository).findByCardStatus(any(Pageable.class), any(CardStatus.class));
    }

    @Test
    void getCards_Nothing() {
        Page<Card> page = new PageImpl<>(List.of(cardFirst, cardSecond),
                PageRequest.of(0, 10), 1);

        when(userRepository.findByUsername(""))
                .thenReturn(Optional.empty());
        when(cardRepository.findAll(PageRequest.of(0, 10,
                Sort.by(Sort.Direction.ASC, "id"))))
                .thenReturn(page);

        List<CardDto> cardDtos = cardService.getCards("", "",
                0, 10, "id", "asc");

        assertEquals(cardDtos.size(), 2);

        verify(userRepository).findByUsername("");
        verify(cardRepository).findAll(any(Pageable.class));
    }

    @Test
    void getCardsUser_Successful() {
        List<Card> cards = List.of(cardFirst, cardSecond);

        when(userService.getCurrentUser())
                .thenReturn(user);
        when(cardRepository.findByCardholder(PageRequest.of(0, 10,
                Sort.by(Sort.Direction.ASC, "id")), user))
                .thenReturn(cards);

        List<CardDto> cardDtos = cardService.getCardsUser(0, 10, "id", "asc");

        assertEquals(cardDtos.size(), cards.size());

        verify(userService).getCurrentUser();
        verify(cardRepository).findByCardholder(any(Pageable.class), any(User.class));
    }

    @Test
    void transfersBetweenCards_Successful() {
        Card cardFirstAfter = new Card(1L, "1111 1111 1111 1111", user, "12/26",
                CardStatus.ACTIVE, 50);
        Card cardSecondAfter = new Card(2L, "1111 1111 1111 1112", user, "12/26",
                CardStatus.ACTIVE, 150);

        when(cardRepository.findByCardNumber(cardFirst.getCardNumber()))
                .thenReturn(Optional.of(cardFirst));
        when(cardRepository.findByCardNumber(cardSecond.getCardNumber()))
                .thenReturn(Optional.of(cardSecond));
        when(userService.getCurrentUser())
                .thenReturn(user);
        when(cardRepository.save(cardFirst))
                .thenReturn(cardFirstAfter);
        when(cardRepository.save(cardSecond))
                .thenReturn(cardSecondAfter);

        cardService.transfersBetweenCards(cardFirst.getCardNumber(), cardSecond.getCardNumber(), 50);

        verify(userService).getCurrentUser();
        verify(cardRepository, times(2)).save(any(Card.class));
        verify(cardRepository, times(2)).findByCardNumber(any());
    }

    @Test
    void transfersBetweenCards_NotFoundCard() {
        when(cardRepository.findByCardNumber("111"))
                .thenReturn(Optional.empty());
        when(userService.getCurrentUser())
                .thenReturn(user);

        assertThrows(NotFoundException.class, () ->
                cardService.transfersBetweenCards("111", cardSecond.getCardNumber(), 50));

        verify(userService).getCurrentUser();
        verify(cardRepository, never()).save(any(Card.class));
        verify(cardRepository).findByCardNumber(any());
    }

    @Test
    void transfersBetweenCards_UserIsNotCardholder() {
        User igor = new User(1L, "Igorr", "khjgcfgcvhjbjas", Role.USER);
        Card card = new Card(3L, "1111 1111 1111 1114", igor, "12/26",
                CardStatus.ACTIVE, 100);

        when(cardRepository.findByCardNumber(card.getCardNumber()))
                .thenReturn(Optional.of(card));
        when(cardRepository.findByCardNumber(cardSecond.getCardNumber()))
                .thenReturn(Optional.of(cardSecond));
        when(userService.getCurrentUser())
                .thenReturn(user);

        assertThrows(AccessException.class, () ->
                cardService.transfersBetweenCards(card.getCardNumber(), cardSecond.getCardNumber(), 50));

        verify(userService).getCurrentUser();
        verify(cardRepository, never()).save(any(Card.class));
        verify(cardRepository, times(2)).findByCardNumber(any());
    }

    @Test
    void transfersBetweenCards_CardBlocked() {
        Card card = new Card(3L, "1111 1111 1111 1113", user, "12/26",
                CardStatus.BLOCKED, 100);

        when(cardRepository.findByCardNumber(card.getCardNumber()))
                .thenReturn(Optional.of(card));
        when(cardRepository.findByCardNumber(cardSecond.getCardNumber()))
                .thenReturn(Optional.of(cardSecond));
        when(userService.getCurrentUser())
                .thenReturn(user);

        assertThrows(CardException.class, () ->
                cardService.transfersBetweenCards(card.getCardNumber(), cardSecond.getCardNumber(), 50));

        verify(userService).getCurrentUser();
        verify(cardRepository, never()).save(any(Card.class));
        verify(cardRepository, times(2)).findByCardNumber(any());
    }

    @Test
    void transfersBetweenCards_NotEnoughMoney() {
        when(cardRepository.findByCardNumber(cardFirst.getCardNumber()))
                .thenReturn(Optional.of(cardFirst));
        when(cardRepository.findByCardNumber(cardSecond.getCardNumber()))
                .thenReturn(Optional.of(cardSecond));
        when(userService.getCurrentUser())
                .thenReturn(user);

        assertThrows(CardException.class, () ->
                cardService.transfersBetweenCards(cardFirst.getCardNumber(), cardSecond.getCardNumber(), 150));

        verify(userService).getCurrentUser();
        verify(cardRepository, never()).save(any(Card.class));
        verify(cardRepository, times(2)).findByCardNumber(any());
    }

    @Test
    void getBalance_Successful() {
        when(cardRepository.findByCardNumber(cardFirst.getCardNumber()))
                .thenReturn(Optional.of(cardFirst));
        when(userService.getCurrentUser())
                .thenReturn(user);

        double balance = cardService.getBalance(cardFirst.getCardNumber());

        assertEquals(balance, cardFirst.getBalance());

        verify(userService).getCurrentUser();
        verify(cardRepository).findByCardNumber(any());
    }

    @Test
    void getBalance_CardBlocked() {
        when(cardRepository.findByCardNumber(cardBlock.getCardNumber()))
                .thenReturn(Optional.of(cardBlock));
        when(userService.getCurrentUser())
                .thenReturn(user);

        assertThrows(CardException.class, () -> cardService.getBalance(cardBlock.getCardNumber()));

        verify(userService).getCurrentUser();
        verify(cardRepository).findByCardNumber(any());
    }

    @Test
    void getBalance_UserIsNotCardholder() {
        User anton = new User(2L, "Igorr", "bgyfygvbhjnug", Role.USER);

        when(cardRepository.findByCardNumber(cardBlock.getCardNumber()))
                .thenReturn(Optional.of(cardBlock));
        when(userService.getCurrentUser())
                .thenReturn(anton);

        assertThrows(AccessException.class, () -> cardService.getBalance(cardBlock.getCardNumber()));

        verify(userService).getCurrentUser();
        verify(cardRepository).findByCardNumber(any());
    }
}
