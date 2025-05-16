package example.bankcards.repository;


import example.bankcards.entity.Card;
import example.bankcards.entity.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long>, JpaSpecificationExecutor<Card> {

    Page<Card> findAllByOwnerId(Long ownerId, Pageable pageable);

    boolean existsByCardNumber(String cardNumber);

    List<Card> findAllByStatus(CardStatus status);

    Optional<Card> findByIdAndOwnerId(Long cardId, Long ownerId);
}


