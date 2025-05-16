package example.bankcards.dto.card;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardTransferRequestDto {
    private Long fromCardId;
    private Long toCardId;
    private BigDecimal amount;
    private String description;
}

