package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cards")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cardNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User cardholder;

    private String cardValidityPeriod;

    @Enumerated(EnumType.STRING)
    private CardStatus cardStatus;

    private double balance;

    public void blockCard() {
        this.cardStatus = CardStatus.BLOCKED;
    }

    public void activateCard() {
        this.cardStatus = CardStatus.ACTIVE;
    }

    public void increaseBalance(double amount) {
        this.balance += amount;
    }

    public void reduceBalance(double amount) {
        this.balance -= amount;
    }
}