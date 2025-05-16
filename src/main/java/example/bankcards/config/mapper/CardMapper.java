package example.bankcards.config.mapper;

import example.bankcards.dto.card.CardRequestDto;
import example.bankcards.dto.card.CardResponseDto;
import example.bankcards.entity.Card;
import example.bankcards.entity.CardStatus;
import example.bankcards.entity.User;
import example.bankcards.until.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class CardMapper {

    private final EncryptionUtil encryptionUtil;

    public CardResponseDto toOpenDto(Card card) {
        if (card == null) return null;
        String decrypted = encryptionUtil.decrypt(card.getCardNumber());
        return CardResponseDto.builder()
                .id(card.getId())
                .expirationDate(card.getExpirationDate())
                .status(card.getStatus().name())
                .balance(card.getBalance())
                .maskedCardNumber(decrypted)
                .ownerId(card.getOwner().getId())
                .build();
    }

    public CardResponseDto toDto(Card card) {
        if (card == null) return null;

        String decryptedNumber = encryptionUtil.decrypt(card.getCardNumber());

        return CardResponseDto.builder()
                .id(card.getId())
                .expirationDate(card.getExpirationDate())
                .status(card.getStatus() != null ? card.getStatus().name() : null)
                .balance(card.getBalance())
                .maskedCardNumber(card.getMaskedCardNumber(decryptedNumber))
                .ownerId(card.getOwner() != null ? card.getOwner().getId() : null)
                .build();
    }

    public Card toEntity(CardRequestDto dto, User owner) {
        if (dto == null || owner == null) return null;

        String encryptedNumber = encryptionUtil.encrypt(dto.getCardNumber());

        return Card.builder()
                .cardNumber(encryptedNumber)
                .expirationDate(dto.getExpirationDate())
                .status(CardStatus.ACTIVE)
                .balance(dto.getBalance() != null ? dto.getBalance() : BigDecimal.ZERO)
                .owner(owner)
                .build();
    }
}


