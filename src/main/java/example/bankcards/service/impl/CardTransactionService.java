package example.bankcards.service.impl;

import example.bankcards.config.mapper.CardTransactionMapper;
import example.bankcards.dto.card.CardTransactionDto;
import example.bankcards.dto.card.CardTransferRequestDto;
import example.bankcards.entity.Card;
import example.bankcards.entity.CardStatus;
import example.bankcards.entity.CardTransaction;
import example.bankcards.exception.card.CardIsNotActive;
import example.bankcards.exception.card.CardNotFoundOrAccessDenied;
import example.bankcards.repository.CardRepository;
import example.bankcards.repository.CardTransactionRepository;
import example.bankcards.service.api.CardTransactionServiceInterface;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
@Transactional
public class CardTransactionService implements CardTransactionServiceInterface {

    private final CardRepository cardRepository;
    private final CardTransactionRepository transactionRepository;
    private final CardTransactionMapper cardTransactionMapper;

    @Override
    public CardTransactionDto transferBetweenOwnCards(Long userId, CardTransferRequestDto dto) {
        Card from = cardRepository.findByIdAndOwnerId(dto.getFromCardId(), userId)
                .orElseThrow(() -> new CardNotFoundOrAccessDenied(dto.getFromCardId()));

        Card to = cardRepository.findByIdAndOwnerId(dto.getToCardId(), userId)
                .orElseThrow(() -> new CardNotFoundOrAccessDenied(dto.getFromCardId()));

        if (from.getStatus() != CardStatus.ACTIVE || to.getStatus() != CardStatus.ACTIVE) {
            throw new CardIsNotActive(to.getStatus());
        }

        if (from.getBalance().compareTo(dto.getAmount()) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        from.setBalance(from.getBalance().subtract(dto.getAmount()));
        to.setBalance(to.getBalance().add(dto.getAmount()));

        CardTransaction transaction = CardTransaction.builder()
                .fromCard(from)
                .toCard(to)
                .amount(dto.getAmount())
                .description(dto.getDescription())
                .build();

        transactionRepository.save(transaction);
        cardRepository.save(from);
        cardRepository.save(to);

        return cardTransactionMapper.toDto(transaction);
    }

    @Override
    public Page<CardTransactionDto> getTransactionHistory(Long userId, Long cardId, Pageable pageable) {
        Card card = cardRepository.findByIdAndOwnerId(cardId, userId)
                .orElseThrow(() -> new CardNotFoundOrAccessDenied(cardId));

        return transactionRepository.findAllByCardId(card.getId(), pageable)
                .map(cardTransactionMapper::toDto);
    }
}


