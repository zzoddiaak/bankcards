package example.bankcards.until;

import example.bankcards.entity.Card;
import example.bankcards.entity.CardStatus;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CardSpecification {

    public static Specification<Card> hasOwnerId(Long ownerId) {
        return (root, query, cb) -> cb.equal(root.get("owner").get("id"), ownerId);
    }

    public static Specification<Card> hasStatus(CardStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Card> hasExpirationBefore(LocalDate date) {
        return (root, query, cb) -> cb.lessThan(root.get("expirationDate"), date);
    }

    public static Specification<Card> balanceGreaterThan(BigDecimal min) {
        return (root, query, cb) -> cb.greaterThan(root.get("balance"), min);
    }

    public static Specification<Card> balanceLessThan(BigDecimal max) {
        return (root, query, cb) -> cb.lessThan(root.get("balance"), max);
    }
}

