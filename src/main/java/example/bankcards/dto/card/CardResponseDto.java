package example.bankcards.dto.card;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardResponseDto {
    private Long id;
    private String maskedCardNumber;
    private LocalDate expirationDate;
    private String status;
    private BigDecimal balance;
    private Long ownerId;
}

