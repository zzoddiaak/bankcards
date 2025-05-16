package example.bankcards.controller;

import example.bankcards.dto.card.CardRequestDto;
import example.bankcards.dto.card.CardResponseDto;
import example.bankcards.entity.CardStatus;
import example.bankcards.service.api.CardServiceInterface;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@Tag(name = "Карты", description = "Операции с банковскими картами")
public class CardController {

    private final CardServiceInterface cardService;

    @Operation(summary = "Создать новую карту пользователю (ADMIN)")
    @PostMapping("/{ownerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardResponseDto> create(
            @PathVariable Long ownerId,
            @RequestBody CardRequestDto dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cardService.createCard(ownerId, dto));
    }

    @Operation(summary = "Получить все карты пользователя (USER)")
    @GetMapping("/users/{userId}/cards")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public Page<CardResponseDto>  getUserCards(
            @PathVariable Long userId,
            @RequestParam(required = false) CardStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expirationBefore,
            @RequestParam(required = false) BigDecimal minBalance,
            @RequestParam(required = false) BigDecimal maxBalance,
            Pageable pageable
    ) {
        return cardService.getUserCards(userId, status, expirationBefore, minBalance, maxBalance, pageable);
    }

    @Operation(summary = "Частичное обновление карты пользователя")
    @PatchMapping("/user/{userId}/card/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateCard(
            @PathVariable Long userId,
            @PathVariable Long cardId,
            @RequestBody CardRequestDto dto
    ) {
        cardService.updateCard(userId, cardId, dto);
        return ResponseEntity.ok().build();
    }


    @Operation(summary = "Получить детали карты пользователя (USER)")
    @GetMapping("/user/{userId}/card/{cardId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<CardResponseDto> getCardDetails(
            @PathVariable Long userId,
            @PathVariable Long cardId
    ) {
        return ResponseEntity.ok(cardService.getCardDetails(userId, cardId));
    }

    @Operation(summary = "Заблокировать карту пользователя (ADMIN)")
    @PostMapping("/user/{userId}/card/{cardId}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> blockCard(
            @PathVariable Long userId,
            @PathVariable Long cardId
    ) {
        cardService.blockCard(userId, cardId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Изменить статус карты (ADMIN)")
    @PatchMapping("/admin/{cardId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long cardId,
            @RequestParam CardStatus status
    ) {
        cardService.updateCardStatusAsAdmin(cardId, status);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Удалить карту (ADMIN)")
    @DeleteMapping("/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получить все карты (ADMIN)")
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CardResponseDto>> getAllCards() {
        return ResponseEntity.ok(cardService.getAllCards());
    }

    @Operation(summary = "Проверить запросы на блокировку (ADMIN)")
    @GetMapping("/admin/cards/pending-block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CardResponseDto>> findCardsPendingBlock() {
        return ResponseEntity.ok(cardService.findCardsPendingBlock());
    }

    @Operation(summary = "Запрос на блокировку карты пользователя")
    @PostMapping("/request/user/{userId}/card/{cardId}/block")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Void> requestCardBlock(
            @PathVariable Long userId,
            @PathVariable Long cardId
    ) {
        cardService.requestCardBlock(userId, cardId);
        return ResponseEntity.ok().build();
    }

}

