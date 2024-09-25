package com.example.accountmission.service;

import com.example.accountmission.aop.AccountLock;
import com.example.accountmission.domain.Account;
import com.example.accountmission.domain.AccountUser;
import com.example.accountmission.domain.Transaction;
import com.example.accountmission.dto.TransactionDto;
import com.example.accountmission.exception.AccountException;
import com.example.accountmission.repository.AccountRepository;
import com.example.accountmission.repository.AccountUserRepository;
import com.example.accountmission.repository.TransactionRepository;
import com.example.accountmission.type.AccountStatus;
import com.example.accountmission.type.ErrorCode;
import com.example.accountmission.type.TransactionResultType;
import com.example.accountmission.type.TransactionType;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import static com.example.accountmission.type.TransactionResultType.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountUserRepository accountUserRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public TransactionDto useBalance(Long userId, String accountNumber, Long amount) {
        AccountUser user = accountUserRepository.findById(userId)
                .orElseThrow(() -> new AccountException(ErrorCode.USER_NOT_FOUND));
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

        validateUseBalance(user, account);
        account.useBalance(amount);

        return TransactionDto.fromEntity(
                saveTransactionStatus(TransactionType.USE, SUCCESS, amount, account)
        );
    }

    private void validateUseBalance(AccountUser user, Account account) {
        if (!Objects.equals(user.getId(), account.getAccountUser().getId())) {
            throw new AccountException(ErrorCode.USER_ACCOUNT_UN_MATCH);
        }
        if (account.getAccountStatus() != AccountStatus.IN_USE) {
            throw new AccountException(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED);
        }
    }

    @Transactional
    public void saveFailedUseTransaction(String accountNumber, Long amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

        saveTransactionStatus(TransactionType.USE, FAIL, amount, account);
    }

    private Transaction saveTransactionStatus(
            TransactionType transactionType, TransactionResultType resultType, Long amount, Account account) {
        return transactionRepository.save(
                Transaction.builder()
                        .transactionType(transactionType)
                        .transactionResultType(resultType)
                        .account(account)
                        .amount(amount)
                        .balanceSnapshot(account.getBalance())
                        .transactionId(UUID.randomUUID().toString().replace("-", ""))
                        .transactedAt(LocalDateTime.now())
                        .build()
        );
    }

    @Transactional
    public TransactionDto cancelBalance(String transactionId, String accountNumber, Long amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));
        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new AccountException(ErrorCode.TRANSACTION_NOT_FOUND));

        validateCancelBalance(account, transaction, amount);
        account.cancelBalance(amount);

        return TransactionDto.fromEntity(
                saveTransactionStatus(TransactionType.CANCEL, SUCCESS, amount, account)
        );
    }

    private void validateCancelBalance(Account account, Transaction transaction, Long amount) {
        if (!Objects.equals(transaction.getAccount().getId(), account.getId())) {
            throw new AccountException(ErrorCode.TRANSACTION_ACCOUNT_UN_MATCH);
        }
        if (!Objects.equals(transaction.getAmount(), amount)) {
            throw new AccountException(ErrorCode.CANCEL_MUST_FULLY);
        }
    }

    public void saveFailedCancelTransaction(String accountNumber, Long amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

        saveTransactionStatus(TransactionType.CANCEL, FAIL, amount, account);
    }

    @Transactional
    public TransactionDto queryTransaction(String transactionId) {
        return TransactionDto.fromEntity(transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new AccountException(ErrorCode.TRANSACTION_NOT_FOUND)));
    }
}
