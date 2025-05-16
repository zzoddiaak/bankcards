package example.bankcards.service;

import example.bankcards.config.mapper.CardMapper;
import example.bankcards.dto.card.CardRequestDto;
import example.bankcards.dto.card.CardResponseDto;
import example.bankcards.entity.*;
import example.bankcards.exception.card.*;
import example.bankcards.repository.CardRepository;
import example.bankcards.repository.UserRepository;
import example.bankcards.service.impl.CardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CardServiceTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CardMapper cardMapper;

    @InjectMocks
    private CardService cardService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createCard_success() {
        Long userId = 1L;
        CardRequestDto dto = CardRequestDto.builder()
                .cardNumber("1234567812345678")
                .expirationDate(LocalDate.now().plusYears(2))
                .balance(BigDecimal.TEN)
                .build();

        User user = new User();
        Card card = new Card();
        Card savedCard = new Card();
        CardResponseDto responseDto = new CardResponseDto();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cardRepository.existsByCardNumber(dto.getCardNumber())).thenReturn(false);
        when(cardMapper.toEntity(dto, user)).thenReturn(card);
        when(cardRepository.save(card)).thenReturn(savedCard);
        when(cardMapper.toDto(savedCard)).thenReturn(responseDto);

        CardResponseDto result = cardService.createCard(userId, dto);
        assertEquals(responseDto, result);
    }

    @Test
    void createCard_invalidCardNumber_throwsException() {
        Long userId = 1L;
        CardRequestDto dto = new CardRequestDto();
        dto.setCardNumber("123");

        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));

        assertThrows(InvalidCardNumberException.class, () -> cardService.createCard(userId, dto));
    }

    @Test
    void updateCard_success() {
        Long userId = 1L;
        Long cardId = 2L;
        CardRequestDto dto = CardRequestDto.builder()
                .cardNumber("1234567812345678")
                .expirationDate(LocalDate.now().plusYears(1))
                .balance(new BigDecimal("150.00"))
                .build();

        Card card = new Card();
        card.setCardNumber("8765432187654321");

        when(cardRepository.findByIdAndOwnerId(cardId, userId)).thenReturn(Optional.of(card));
        when(cardRepository.existsByCardNumber(dto.getCardNumber())).thenReturn(false);

        cardService.updateCard(userId, cardId, dto);

        assertEquals(dto.getCardNumber(), card.getCardNumber());
        assertEquals(dto.getExpirationDate(), card.getExpirationDate());
        assertEquals(dto.getBalance(), card.getBalance());
        verify(cardRepository).save(card);
    }

    @Test
    void getCardDetails_autoExpiresIfExpired() {
        Long userId = 1L;
        Long cardId = 10L;
        Card card = new Card();
        card.setStatus(CardStatus.ACTIVE);
        card.setExpirationDate(LocalDate.now().minusDays(1));
        CardResponseDto responseDto = new CardResponseDto();

        when(cardRepository.findByIdAndOwnerId(cardId, userId)).thenReturn(Optional.of(card));
        when(cardMapper.toDto(card)).thenReturn(responseDto);

        CardResponseDto result = cardService.getCardDetails(userId, cardId);
        assertEquals(CardStatus.EXPIRED, card.getStatus());
        assertEquals(responseDto, result);
        verify(cardRepository).save(card);
    }

    @Test
    void blockCard_setsStatusBlocked() {
        Card card = new Card();
        card.setStatus(CardStatus.ACTIVE);

        when(cardRepository.findByIdAndOwnerId(1L, 1L)).thenReturn(Optional.of(card));

        cardService.blockCard(1L, 1L);

        assertEquals(CardStatus.BLOCKED, card.getStatus());
        verify(cardRepository).save(card);
    }

    @Test
    void updateCardStatusAsAdmin_success() {
        Card card = new Card();
        card.setStatus(CardStatus.ACTIVE);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        cardService.updateCardStatusAsAdmin(1L, CardStatus.BLOCKED);

        assertEquals(CardStatus.BLOCKED, card.getStatus());
        verify(cardRepository).save(card);
    }

    @Test
    void deleteCard_deletesById() {
        Long cardId = 100L;
        cardService.deleteCard(cardId);
        verify(cardRepository).deleteById(cardId);
    }

    @Test
    void getAllCards_returnsMappedList() {
        Card card1 = new Card();
        Card card2 = new Card();
        List<Card> cards = List.of(card1, card2);

        CardResponseDto dto1 = new CardResponseDto();
        CardResponseDto dto2 = new CardResponseDto();

        when(cardRepository.findAll()).thenReturn(cards);
        when(cardMapper.toOpenDto(card1)).thenReturn(dto1);
        when(cardMapper.toOpenDto(card2)).thenReturn(dto2);

        List<CardResponseDto> result = cardService.getAllCards();
        assertEquals(2, result.size());
        assertTrue(result.contains(dto1));
        assertTrue(result.contains(dto2));
    }

    @Test
    void requestCardBlock_setsPendingBlock() {
        Card card = new Card();
        card.setStatus(CardStatus.ACTIVE);

        when(cardRepository.findByIdAndOwnerId(1L, 1L)).thenReturn(Optional.of(card));

        cardService.requestCardBlock(1L, 1L);

        assertEquals(CardStatus.PENDING_BLOCK, card.getStatus());
        verify(cardRepository).save(card);
    }

    @Test
    void findCardsPendingBlock_returnsMapped() {
        Card card = new Card();
        CardResponseDto dto = new CardResponseDto();

        when(cardRepository.findAllByStatus(CardStatus.PENDING_BLOCK)).thenReturn(List.of(card));
        when(cardMapper.toOpenDto(card)).thenReturn(dto);

        List<CardResponseDto> result = cardService.findCardsPendingBlock();
        assertEquals(1, result.size());
        assertEquals(dto, result.get(0));
    }

    @Test
    void checkAndExpireCards_setsStatusExpired() {
        Card expired = new Card();
        expired.setStatus(CardStatus.ACTIVE);
        expired.setExpirationDate(LocalDate.now().minusDays(1));

        Card valid = new Card();
        valid.setStatus(CardStatus.ACTIVE);
        valid.setExpirationDate(LocalDate.now().plusDays(5));

        when(cardRepository.findAllByStatus(CardStatus.ACTIVE)).thenReturn(List.of(expired, valid));

        cardService.checkAndExpireCards();

        assertEquals(CardStatus.EXPIRED, expired.getStatus());
        assertEquals(CardStatus.ACTIVE, valid.getStatus());
        verify(cardRepository).saveAll(List.of(expired, valid));
    }

    @Test
    void getUserCards_filtersCorrectly() {
        Long userId = 1L;
        Card card = new Card();
        CardResponseDto dto = new CardResponseDto();
        Page<Card> page = new PageImpl<>(List.of(card));
        Pageable pageable = PageRequest.of(0, 10);

        when(cardRepository.findAll((Specification<Card>) any(), eq(pageable))).thenReturn(page);
        when(cardMapper.toDto(card)).thenReturn(dto);

        Page<CardResponseDto> result = cardService.getUserCards(userId, null, null, null, null, pageable);
        assertEquals(1, result.getTotalElements());
        assertEquals(dto, result.getContent().get(0));
    }

}

