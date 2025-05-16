package example.bankcards.config.mapper;

import example.bankcards.dto.card.CardTransactionDto;
import example.bankcards.entity.CardTransaction;
import org.springframework.stereotype.Component;

@Component
public class CardTransactionMapper {

    public CardTransactionDto toDto(CardTransaction tx) {
        return CardTransactionDto.builder()
                .id(tx.getId())
                .fromCardId(tx.getFromCard().getId())
                .toCardId(tx.getToCard().getId())
                .amount(tx.getAmount())
                .timestamp(tx.getTimestamp())
                .description(tx.getDescription())
                .build();
    }
}

