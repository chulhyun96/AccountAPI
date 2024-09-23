package com.example.accountmission.controller;

import com.example.accountmission.domain.Account;
import com.example.accountmission.dto.AccountInfo;
import com.example.accountmission.dto.CreateAccount;
import com.example.accountmission.dto.DeleteAccount;
import com.example.accountmission.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // 백엔드 API
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;


    @PostMapping("/account")
    public CreateAccount.Response createAccount(
            @RequestBody @Validated CreateAccount.Request request) {
        return CreateAccount.Response.from(
                accountService.createAccount(
                        request.getUserId(),
                        request.getInitialBalance()
                )
        );
    }

    @GetMapping("/account/{id}")
    public Account getAccount(@PathVariable Long id) {
        return accountService.getAccount(id);
    }

    @GetMapping("/account")
    public List<AccountInfo> getAccountsByUserId(
            @RequestParam("user_id") Long userId
    ) {
        return accountService.getAccountsByUserId(userId)
                .stream()
                .map(accountDto ->
                        AccountInfo.builder()
                                .accountNumber(accountDto.getAccountNumber())
                                .balance(accountDto.getBalance())
                                .build())
                .toList();
    }

    @DeleteMapping("/account")
    public DeleteAccount.Response deleteAccount(
            @RequestBody @Validated DeleteAccount.Request request) {
        return DeleteAccount.Response.from(
                accountService.deleteAccount(
                        request.getUserId(),
                        request.getAccountNumber()
                )
        );
    }
}
