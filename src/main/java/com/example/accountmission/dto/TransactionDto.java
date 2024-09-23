package com.example.accountmission.dto;

import com.example.accountmission.domain.Transaction;
import com.example.accountmission.type.TransactionResultType;
import com.example.accountmission.type.TransactionType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDto {
    private String accountNumber;
    private String transactionId;

    private TransactionType transactionType;
    private TransactionResultType transactionResultType;

    private Long amount;
    private Long balanceSnapshot;

    private LocalDateTime transactedAt;

    public static TransactionDto fromEntity(Transaction transaction) {
        return TransactionDto.builder()
                .accountNumber(transaction.getAccount().getAccountNumber())
                .transactionId(transaction.getTransactionId())
                .transactionType(transaction.getTransactionType())
                .transactionResultType(transaction.getTransactionResultType())
                .amount(transaction.getAmount())
                .balanceSnapshot(transaction.getBalanceSnapshot())
                .transactedAt(transaction.getTransactedAt())
                .build();
    }
}
