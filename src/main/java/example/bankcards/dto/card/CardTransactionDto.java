package example.bankcards.dto.card;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardTransactionDto {
    private Long id;
    private BigDecimal amount;
    private LocalDateTime timestamp;
    private Long fromCardId;
    private Long toCardId;
    private String description;
}

