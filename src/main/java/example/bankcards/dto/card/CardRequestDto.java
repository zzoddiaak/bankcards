package example.bankcards.dto.card;

import jakarta.validation.constraints.Pattern;
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
public class CardRequestDto {
    private String cardNumber;
    private LocalDate expirationDate;
    private BigDecimal balance;
}


