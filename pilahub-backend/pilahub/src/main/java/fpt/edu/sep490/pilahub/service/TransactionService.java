package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.TransactionDto;
import fpt.edu.sep490.pilahub.enums.TransactionType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface TransactionService {

    /**
     * Create a new transaction
     */
    TransactionDto createTransaction(UUID accountId, TransactionType transactionType,
            BigDecimal amount, UUID referenceId, String description);

    /**
     * Get all transactions for a specific account
     */
    List<TransactionDto> getTransactionsByAccountId(UUID accountId);

    /**
     * Get all transactions in the system (Admin only)
     */
    List<TransactionDto> getAllTransactions();

    /**
     * Get transaction by ID
     */
    TransactionDto getTransactionById(UUID transactionId);

    /**
     * Get transactions by type
     */
    List<TransactionDto> getTransactionsByType(TransactionType transactionType);

    /**
     * Get transactions by account and type
     */
    List<TransactionDto> getTransactionsByAccountIdAndType(UUID accountId, TransactionType transactionType);

    /**
     * Get transactions by reference ID
     */
    List<TransactionDto> getTransactionsByReferenceId(UUID referenceId);

    /**
     * Get transaction history for the currently authenticated user
     */
    List<TransactionDto> getMyTransactionHistory();

    /**
     * Get transaction by ID for the currently authenticated user
     */
    TransactionDto getMyTransactionById(UUID transactionId);
}
