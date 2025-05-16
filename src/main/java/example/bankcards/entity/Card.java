package example.bankcards.entity;

import example.bankcards.until.EncryptionUtil;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Setter
@Getter
@Entity
@Builder
@AllArgsConstructor
@Table(name = "cards")
public class Card {

    public Card() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_number", nullable = false)
    private String cardNumber;

    private LocalDate expirationDate;

    @Enumerated(EnumType.STRING)
    private CardStatus status;

    private BigDecimal balance = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    public String getMaskedCardNumber(String decryptedCardNumber) {
        try {
            return "**** **** **** " + decryptedCardNumber.substring(decryptedCardNumber.length() - 4);
        } catch (Exception e) {
            return "****";
        }
    }
}




