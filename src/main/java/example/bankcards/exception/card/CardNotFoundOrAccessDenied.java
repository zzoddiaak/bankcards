package example.bankcards.exception.card;

public class CardNotFoundOrAccessDenied extends RuntimeException {
    public CardNotFoundOrAccessDenied(Long cardId) {
        super("Card not found or access denied, card id - " + cardId);
    }
}
