package example.bankcards.exception.card;

public class CardAlreadyExistsException extends RuntimeException {
    public CardAlreadyExistsException(String cardNumber) {
        super("Card number already exists: " + cardNumber);
    }
}
