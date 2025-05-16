package example.bankcards.service.api;

import example.bankcards.dto.card.CardRequestDto;
import example.bankcards.dto.card.CardResponseDto;
import example.bankcards.entity.CardStatus;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface CardServiceInterface {
    CardResponseDto createCard(Long ownerId, CardRequestDto requestDto);
    Page<CardResponseDto> getUserCards(
            Long userId,
            CardStatus status,
            LocalDate expirationBefore,
            BigDecimal minBalance,
            BigDecimal maxBalance,
            Pageable pageable
    );
    CardResponseDto getCardDetails(Long userId, Long cardId);
    void blockCard(Long userId, Long cardId);
    void updateCardStatusAsAdmin(Long cardId, CardStatus status);
    void deleteCard(Long cardId);
    List<CardResponseDto> getAllCards();
    void requestCardBlock(Long userId, Long cardId);
    List<CardResponseDto> findCardsPendingBlock();
    void updateCard(Long userId, Long cardId, CardRequestDto dto);

}

