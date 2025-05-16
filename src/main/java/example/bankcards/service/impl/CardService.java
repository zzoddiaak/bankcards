package example.bankcards.service.impl;

import example.bankcards.config.mapper.CardMapper;
import example.bankcards.dto.card.CardRequestDto;
import example.bankcards.dto.card.CardResponseDto;
import example.bankcards.entity.Card;
import example.bankcards.entity.CardStatus;
import example.bankcards.entity.User;
import example.bankcards.exception.card.CardAlreadyExistsException;
import example.bankcards.exception.card.CardNotFoundOrAccessDenied;
import example.bankcards.exception.card.InvalidCardNumberException;
import example.bankcards.exception.user.UserNotFound;
import example.bankcards.repository.CardRepository;
import example.bankcards.repository.UserRepository;
import example.bankcards.service.api.CardServiceInterface;
import example.bankcards.until.CardSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Transactional
@Service
@Slf4j
public class CardService implements CardServiceInterface {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardMapper cardMapper;

    @Override
    public CardResponseDto createCard(Long ownerId, CardRequestDto requestDto) {
        log.info("Создание карты для пользователя с ID: {}", ownerId);
        log.info("DTO: {}", requestDto);

        User user = userRepository.findById(ownerId)
                .orElseThrow(() -> new UserNotFound(ownerId));

        String cardNumber = requestDto.getCardNumber();
        if (!isValidCardNumberFormat(cardNumber)) {
            throw new InvalidCardNumberException(cardNumber);
        }

        if (cardRepository.existsByCardNumber(cardNumber)) {
            throw new CardAlreadyExistsException(cardNumber);
        }

        Card card = cardMapper.toEntity(requestDto, user);
        log.info("Карта до сохранения: {}", card);

        Card saved = cardRepository.save(card);
        log.info("Карта сохранена: {}", saved);
        return cardMapper.toDto(saved);
    }

    @Override
    public void updateCard(Long userId, Long cardId, CardRequestDto dto) {
        Card card = cardRepository.findByIdAndOwnerId(cardId, userId)
                .orElseThrow(() -> new CardNotFoundOrAccessDenied(cardId));

        if (dto.getCardNumber() != null) {
            if (!isValidCardNumberFormat(dto.getCardNumber())) {
                throw new InvalidCardNumberException(dto.getCardNumber());
            }
            if (!dto.getCardNumber().equals(card.getCardNumber()) &&
                    cardRepository.existsByCardNumber(dto.getCardNumber())) {
                throw new CardAlreadyExistsException(dto.getCardNumber());
            }
            card.setCardNumber(dto.getCardNumber());
        }

        if (dto.getExpirationDate() != null) {
            card.setExpirationDate(dto.getExpirationDate());
        }

        if (dto.getBalance() != null) {
            card.setBalance(dto.getBalance());
        }

        cardRepository.save(card);
    }

    @Override
    public Page<CardResponseDto> getUserCards(
            Long userId,
            CardStatus status,
            LocalDate expirationBefore,
            BigDecimal minBalance,
            BigDecimal maxBalance,
            Pageable pageable
    ) {
        Specification<Card> spec = Specification.where(CardSpecification.hasOwnerId(userId));

        if (status != null) {
            spec = spec.and(CardSpecification.hasStatus(status));
        }
        if (expirationBefore != null) {
            spec = spec.and(CardSpecification.hasExpirationBefore(expirationBefore));
        }
        if (minBalance != null) {
            spec = spec.and(CardSpecification.balanceGreaterThan(minBalance));
        }
        if (maxBalance != null) {
            spec = spec.and(CardSpecification.balanceLessThan(maxBalance));
        }

        return cardRepository.findAll(spec, pageable)
                .map(cardMapper::toDto);
    }

    @Override
    public CardResponseDto getCardDetails(Long userId, Long cardId) {
        Card card = cardRepository.findByIdAndOwnerId(cardId, userId)
                .orElseThrow(() -> new CardNotFoundOrAccessDenied(cardId));
        if (card.getStatus() == CardStatus.ACTIVE &&
                card.getExpirationDate().isBefore(LocalDate.now())) {
            card.setStatus(CardStatus.EXPIRED);
            cardRepository.save(card);
        }
        return cardMapper.toDto(card);
    }

    @Override
    public void blockCard(Long userId, Long cardId) {
        Card card = cardRepository.findByIdAndOwnerId(cardId, userId)
                .orElseThrow(() -> new CardNotFoundOrAccessDenied(cardId));
        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
    }

    @Override
    public void updateCardStatusAsAdmin(Long cardId, CardStatus status) {

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundOrAccessDenied(cardId));
        card.setStatus(status);
        cardRepository.save(card);
    }

    @Override
    public void deleteCard(Long cardId) {
        cardRepository.deleteById(cardId);
    }

    @Override
    public List<CardResponseDto> getAllCards() {

        return cardRepository.findAll()
                .stream()
                .map(cardMapper::toOpenDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CardResponseDto> findCardsPendingBlock() {
        return cardRepository.findAllByStatus(CardStatus.PENDING_BLOCK)
                .stream()
                .map(cardMapper::toOpenDto)
                .collect(Collectors.toList());
    }

    @Override
    public void requestCardBlock(Long userId, Long cardId) {
        Card card = cardRepository.findByIdAndOwnerId(cardId, userId)
                .orElseThrow(() -> new CardNotFoundOrAccessDenied(cardId));

        card.setStatus(CardStatus.PENDING_BLOCK);
        cardRepository.save(card);
    }

    public void checkAndExpireCards() {
        List<Card> activeCards = cardRepository.findAllByStatus(CardStatus.ACTIVE);
        LocalDate today = LocalDate.now();

        for (Card card : activeCards) {
            if (card.getExpirationDate().isBefore(today)) {
                card.setStatus(CardStatus.EXPIRED);
            }
        }

        cardRepository.saveAll(activeCards);
    }
    private boolean isValidCardNumberFormat(String cardNumber) {
        return cardNumber != null && cardNumber.matches("^\\d{16}$");
    }

}



