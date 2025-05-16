package example.bankcards.service;

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
import example.bankcards.service.impl.CardTransactionService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class CardTransactionServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardTransactionRepository transactionRepository;

    @Mock
    private CardTransactionMapper cardTransactionMapper;

    @InjectMocks
    private CardTransactionService cardTransactionService;

    private Card fromCard;
    private Card toCard;
    private CardTransferRequestDto requestDto;
    private CardTransaction transaction;

    @BeforeEach
    void setUp() {
        fromCard = Card.builder()
                .id(1L)
                .balance(new BigDecimal("100.00"))
                .status(CardStatus.ACTIVE)
                .build();

        toCard = Card.builder()
                .id(2L)
                .balance(new BigDecimal("50.00"))
                .status(CardStatus.ACTIVE)
                .build();

        requestDto = CardTransferRequestDto.builder()
                .fromCardId(1L)
                .toCardId(2L)
                .amount(new BigDecimal("30.00"))
                .description("test transfer")
                .build();

        transaction = CardTransaction.builder()
                .fromCard(fromCard)
                .toCard(toCard)
                .amount(requestDto.getAmount())
                .description(requestDto.getDescription())
                .build();
    }

    @Test
    void transferBetweenOwnCards_successfulTransfer() {
        when(cardRepository.findByIdAndOwnerId(1L, 1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdAndOwnerId(2L, 1L)).thenReturn(Optional.of(toCard));
        when(transactionRepository.save(any())).thenReturn(transaction);
        when(cardTransactionMapper.toDto(any())).thenReturn(new CardTransactionDto());

        CardTransactionDto result = cardTransactionService.transferBetweenOwnCards(1L, requestDto);

        assertNotNull(result);
        assertEquals(new BigDecimal("70.00"), fromCard.getBalance());
        assertEquals(new BigDecimal("80.00"), toCard.getBalance());

        verify(transactionRepository).save(any(CardTransaction.class));
        verify(cardRepository, times(2)).save(any(Card.class));
    }

    @Test
    void transferBetweenOwnCards_insufficientFunds() {
        requestDto.setAmount(new BigDecimal("200.00")); // больше баланса
        when(cardRepository.findByIdAndOwnerId(1L, 1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdAndOwnerId(2L, 1L)).thenReturn(Optional.of(toCard));

        assertThrows(RuntimeException.class,
                () -> cardTransactionService.transferBetweenOwnCards(1L, requestDto));
    }

    @Test
    void transferBetweenOwnCards_cardNotActive() {
        fromCard.setStatus(CardStatus.BLOCKED);
        when(cardRepository.findByIdAndOwnerId(1L, 1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdAndOwnerId(2L, 1L)).thenReturn(Optional.of(toCard));

        assertThrows(CardIsNotActive.class,
                () -> cardTransactionService.transferBetweenOwnCards(1L, requestDto));
    }

    @Test
    void transferBetweenOwnCards_cardNotFound() {
        when(cardRepository.findByIdAndOwnerId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundOrAccessDenied.class,
                () -> cardTransactionService.transferBetweenOwnCards(1L, requestDto));
    }

    @Test
    void getTransactionHistory_returnsPage() {
        Card card = Card.builder().id(5L).build();
        Pageable pageable = PageRequest.of(0, 10);
        Page<CardTransaction> page = new PageImpl<>(List.of(transaction));
        CardTransactionDto dto = new CardTransactionDto();

        when(cardRepository.findByIdAndOwnerId(5L, 1L)).thenReturn(Optional.of(card));
        when(transactionRepository.findAllByCardId(5L, pageable)).thenReturn(page);
        when(cardTransactionMapper.toDto(any())).thenReturn(dto);

        Page<CardTransactionDto> result = cardTransactionService.getTransactionHistory(1L, 5L, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(dto, result.getContent().get(0));
    }
}

