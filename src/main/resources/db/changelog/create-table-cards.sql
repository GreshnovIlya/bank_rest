CREATE TABLE IF NOT EXISTS cards (
    id BIGINT GENERATED ALWAYS AS IDENTITY NOT NULL,
    card_number VARCHAR(19) NOT NULL,
    user_id BIGINT NOT NULL,
    card_validity_period VARCHAR(5) NOT NULL,
    card_status VARCHAR(7) NOT NULL,
    balance DOUBLE PRECISION NOT NULL,
    CONSTRAINT pk_cards PRIMARY KEY (id),
    CONSTRAINT uq_cards_card_number UNIQUE (card_number),
    CONSTRAINT fk_cards_user FOREIGN KEY (user_id) REFERENCES users(id)
);