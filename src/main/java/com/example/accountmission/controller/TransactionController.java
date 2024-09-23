package com.example.accountmission.controller;

import com.example.accountmission.dto.CancelBalance;
import com.example.accountmission.dto.QueryTransactionResponse;
import com.example.accountmission.dto.UseBalance;
import com.example.accountmission.exception.AccountException;
import com.example.accountmission.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 1. 잔액 사용
 * 2. 잔액 사용 취소
 * 3. 거래 확인
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping("/transaction/use")
    public UseBalance.Response useBalance(
            @RequestBody @Validated UseBalance.Request request) {
        try {
            return UseBalance.Response.from(transactionService.useBalance(
                    request.getUserId(),
                    request.getAccountNumber(),
                    request.getAmount()));
        } catch (AccountException e) {
            log.error("Failed to use Balance = {}{}",
                    e.getErrorMessage(), e.getErrorCode());
            transactionService.saveFailedUseTransaction(
                    request.getAccountNumber(),
                    request.getAmount()
            );
            throw e;
        }
    }
    @PostMapping("/transaction/cancel")
    public CancelBalance.Response useBalance(
            @RequestBody @Validated CancelBalance.Request request) {
        try {
            return CancelBalance.Response.from(transactionService.cancelBalance(
                    request.getTransactionId(),
                    request.getAccountNumber(),
                    request.getAmount()));
        } catch (AccountException e) {
            log.error("Failed to CancelBalance = {}{}",
                    e.getErrorMessage(), e.getErrorCode());
            transactionService.saveFailedCancelTransaction(
                    request.getAccountNumber(),
                    request.getAmount()
            );
            throw e;
        }
    }

    @GetMapping("/transaction/{transactionId}")
    public QueryTransactionResponse queryTransaction(
            @PathVariable String transactionId) {
        return QueryTransactionResponse.from(
                transactionService.queryTransaction(transactionId));
    }
}
