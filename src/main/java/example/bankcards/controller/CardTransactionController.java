package example.bankcards.controller;

import example.bankcards.dto.card.CardTransactionDto;
import example.bankcards.dto.card.CardTransferRequestDto;
import example.bankcards.service.api.CardTransactionServiceInterface;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Транзакции", description = "Операции перевода средств между картами")
public class CardTransactionController {

    private final CardTransactionServiceInterface cardTransactionService;

    @Operation(summary = "Перевод между собственными картами (USER)")
    @PostMapping("/transfer/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<CardTransactionDto> transfer(
            @PathVariable Long userId,
            @RequestBody CardTransferRequestDto dto
    ) {
        return ResponseEntity.ok(cardTransactionService.transferBetweenOwnCards(userId, dto));
    }

    @Operation(summary = "История транзакций карты пользователя (ADMIN)")
    @GetMapping("/history/{userId}/card/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CardTransactionDto>> history(
            @PathVariable Long userId,
            @PathVariable Long cardId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(cardTransactionService.getTransactionHistory(userId, cardId, pageable));
    }
}

