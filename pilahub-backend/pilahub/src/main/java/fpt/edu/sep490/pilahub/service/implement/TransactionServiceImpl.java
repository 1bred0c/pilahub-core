package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.TransactionDto;
import fpt.edu.sep490.pilahub.enums.TransactionType;
import fpt.edu.sep490.pilahub.exception.TransactionNotFoundException;
import fpt.edu.sep490.pilahub.mapper.TransactionMapper;
import fpt.edu.sep490.pilahub.pojo.Transaction;
import fpt.edu.sep490.pilahub.repository.AccountRepository;
import fpt.edu.sep490.pilahub.repository.TransactionRepository;
import fpt.edu.sep490.pilahub.service.TransactionService;
import fpt.edu.sep490.pilahub.util.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionMapper transactionMapper;
    private final SecurityUtil securityUtil;



    @Override
    public TransactionDto createTransaction(UUID accountId, TransactionType transactionType,
            BigDecimal amount, UUID referenceId, String description) {
        log.info("Creating new transaction for account ID: {} with type: {}", accountId, transactionType);

        // Verify account exists
        accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with ID: " + accountId));

        Transaction transaction = Transaction.builder()
                .accountId(accountId)
                .transactionType(transactionType)
                .amount(amount)
                .referenceId(referenceId)
                .description(description)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Transaction created successfully with ID: {}", savedTransaction.getTransactionId());

        return transactionMapper.toDto(savedTransaction);
    }

    @Override
    public List<TransactionDto> getTransactionsByAccountId(UUID accountId) {
        log.info("Fetching transactions for account ID: {}", accountId);

        List<Transaction> transactions = transactionRepository.findByAccountIdOrderByTransactionDateDesc(accountId);

        log.info("Found {} transaction(s) for account ID: {}", transactions.size(), accountId);
        return transactions.stream()
                .map(transactionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionDto> getAllTransactions() {
        log.info("Fetching all transactions");

        List<Transaction> transactions = transactionRepository.findAll();

        log.info("Found {} transaction(s) in total", transactions.size());
        return transactions.stream()
                .map(transactionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionDto getTransactionById(UUID transactionId) {
        log.info("Fetching transaction with ID: {}", transactionId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> {
                    log.error("Transaction not found with ID: {}", transactionId);
                    return new TransactionNotFoundException("Transaction not found with ID: " + transactionId);
                });

        return transactionMapper.toDto(transaction);
    }

    @Override
    public List<TransactionDto> getTransactionsByType(TransactionType transactionType) {
        log.info("Fetching transactions with type: {}", transactionType);

        List<Transaction> transactions = transactionRepository
                .findByTransactionTypeOrderByTransactionDateDesc(transactionType);

        log.info("Found {} transaction(s) with type: {}", transactions.size(), transactionType);
        return transactions.stream()
                .map(transactionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionDto> getTransactionsByAccountIdAndType(UUID accountId, TransactionType transactionType) {
        log.info("Fetching transactions for account ID: {} with type: {}", accountId, transactionType);

        List<Transaction> transactions = transactionRepository
                .findByAccountIdAndTransactionTypeOrderByTransactionTypeDesc(accountId, transactionType);

        log.info("Found {} transaction(s) for account ID: {} with type: {}",
                transactions.size(), accountId, transactionType);
        return transactions.stream()
                .map(transactionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionDto> getTransactionsByReferenceId(UUID referenceId) {
        log.info("Fetching transactions with reference ID: {}", referenceId);

        List<Transaction> transactions = transactionRepository.findByReferenceIdOrderByTransactionDateDesc(referenceId);

        log.info("Found {} transaction(s) with reference ID: {}", transactions.size(), referenceId);
        return transactions.stream()
                .map(transactionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionDto> getMyTransactionHistory() {
        UUID currentUserId = securityUtil.getCurrentUserId();
        log.info("Fetching transaction history for current user ID: {}", currentUserId);

        return getTransactionsByAccountId(currentUserId);
    }

    @Override
    public TransactionDto getMyTransactionById(UUID transactionId) {
        UUID currentUserId = securityUtil.getCurrentUserId();
        log.info("Fetching transaction ID: {} for current user ID: {}", transactionId, currentUserId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> {
                    log.error("Transaction not found with ID: {}", transactionId);
                    return new TransactionNotFoundException("Transaction not found with ID: " + transactionId);
                });

        // Verify transaction belongs to current user
        if (!transaction.getAccountId().equals(currentUserId)) {
            log.error("Transaction ID: {} does not belong to user ID: {}", transactionId, currentUserId);
            throw new TransactionNotFoundException("Transaction not found");
        }

        return transactionMapper.toDto(transaction);
    }
}
