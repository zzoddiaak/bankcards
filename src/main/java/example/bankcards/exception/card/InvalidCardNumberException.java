package example.bankcards.exception.card;

public class InvalidCardNumberException extends RuntimeException {
    public InvalidCardNumberException(String cardNumber) {
        super("Неверный формат номера карты. Номер должен состоять из 16 цифр: " + cardNumber);
    }
}
