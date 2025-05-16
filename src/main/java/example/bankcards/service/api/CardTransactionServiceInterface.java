package example.bankcards.service.api;

import example.bankcards.dto.card.CardTransactionDto;
import example.bankcards.dto.card.CardTransferRequestDto;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;

public interface CardTransactionServiceInterface {
    CardTransactionDto transferBetweenOwnCards(Long userId, CardTransferRequestDto dto);
    Page<CardTransactionDto> getTransactionHistory(Long userId, Long cardId, Pageable pageable);
}

