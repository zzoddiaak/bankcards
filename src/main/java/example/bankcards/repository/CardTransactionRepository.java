package example.bankcards.repository;

import example.bankcards.entity.CardTransaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface CardTransactionRepository extends JpaRepository<CardTransaction, Long> {
    @Query("SELECT t FROM CardTransaction t WHERE t.fromCard.id = :cardId OR t.toCard.id = :cardId ORDER BY t.timestamp DESC")
    Page<CardTransaction> findAllByCardId(@Param("cardId") Long cardId, Pageable pageable);
}

