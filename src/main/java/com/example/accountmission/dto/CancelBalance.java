package com.example.accountmission.dto;

import com.example.accountmission.aop.AccountLockIdInterface;
import com.example.accountmission.type.TransactionResultType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class CancelBalance {
    /**
     * {
     *   "userId" : 1,
     *   "accountNumber" : "123456",
     *   "amount" : 1000
     * }
     */
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request implements AccountLockIdInterface {
        @NotNull
        private String transactionId;

        @NotBlank
        @Size(min = 10, max = 10)
        private String accountNumber;

        @NotNull
        @Min(10)
        @Max(1000_000_000)
        private Long amount;
    }

    /**
     * {
     *   "accountNumber" : "123456",
     *   "transactionResult": "5",
     *   "transactionId": "randomId",
     *   "amount": 1000,
     *   "transactedAt": "LocalDateTime.now()"
     * }
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private String accountNumber;
        private TransactionResultType transactionResultType;
        private String transactionId;
        private Long amount;
        private LocalDateTime transactedAt;

        public static CancelBalance.Response from(TransactionDto transactionDto) {
            return CancelBalance.Response.builder()
                    .accountNumber(transactionDto.getAccountNumber())
                    .transactionResultType(transactionDto.getTransactionResultType())
                    .transactionId(transactionDto.getTransactionId())
                    .amount(transactionDto.getAmount())
                    .transactedAt(transactionDto.getTransactedAt())
                    .build();
        }
    }
}
