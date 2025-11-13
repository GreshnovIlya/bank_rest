package com.example.bankcards.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    Optional<Card> findByCardNumber(String cardNumber);

    List<Card> findByCardholderAndCardStatus(Pageable pageable, User cardholder, CardStatus cardStatus);

    List<Card> findByCardholder(Pageable pageable, User cardholder);

    List<Card> findByCardStatus(Pageable pageable, CardStatus cardStatus);
}