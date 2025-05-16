package example.bankcards.config.mapper;

import example.bankcards.dto.card.CardRequestDto;
import example.bankcards.dto.card.CardResponseDto;
import example.bankcards.dto.user.UserResponseDto;
import example.bankcards.entity.Card;
import example.bankcards.entity.Role;
import example.bankcards.entity.User;
import example.bankcards.until.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final EncryptionUtil encryptionUtil;

    public UserResponseDto toDto(User user) {
        if (user == null) return null;

        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()))
                .cards(user.getCards().stream()
                        .map(this::mapCardToDto)
                        .collect(Collectors.toList()))
                .build();
    }

    private CardResponseDto mapCardToDto(Card card) {
        String decryptedNumber = encryptionUtil.decrypt(card.getCardNumber());
        return CardResponseDto.builder()
                .id(card.getId())
                .maskedCardNumber(card.getMaskedCardNumber(decryptedNumber))
                .expirationDate(card.getExpirationDate())
                .status(card.getStatus().name())
                .balance(card.getBalance())
                .ownerId(card.getOwner() != null ? card.getOwner().getId() : null)
                .build();
    }
}

