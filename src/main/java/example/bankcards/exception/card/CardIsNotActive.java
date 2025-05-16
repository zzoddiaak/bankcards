package example.bankcards.exception.card;

import example.bankcards.entity.CardStatus;

public class CardIsNotActive extends RuntimeException {
    public CardIsNotActive(CardStatus status) {
        super("One of the cards is not active: " + status);
    }
}
