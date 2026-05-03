package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.enums.TransactionType;
import fpt.edu.sep490.pilahub.pojo.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findByAccountIdOrderByTransactionDateDesc(UUID accountId);

    List<Transaction> findByTransactionTypeOrderByTransactionDateDesc(TransactionType transactionType);

    List<Transaction> findByAccountIdAndTransactionTypeOrderByTransactionTypeDesc(UUID accountId,
            TransactionType transactionType);

    List<Transaction> findByReferenceIdOrderByTransactionDateDesc(UUID referenceId);

    boolean existsByReferenceIdAndTransactionType(UUID referenceId, TransactionType transactionType);

    Long countByTransactionDateBetween(Instant start, Instant end);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.transactionDate >= :start AND t.transactionDate < :end")
    BigDecimal sumAmountByTransactionDateBetween(@Param("start") Instant start, @Param("end") Instant end);
}
