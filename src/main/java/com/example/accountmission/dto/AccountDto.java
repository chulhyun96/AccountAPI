package com.example.accountmission.dto;

import com.example.accountmission.domain.Account;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AccountDto {
    private Long userId;
    private String accountNumber;
    private Long balance;


    private LocalDateTime registeredAt;
    private LocalDateTime unRegisteredAt;

    public static AccountDto fromEntity(Account account) {
        return AccountDto.builder()
                .userId(account.getAccountUser().getId())
                .accountNumber(account.getAccountNumber())
                .registeredAt(account.getRegisteredAt())
                .balance(account.getBalance())
                .unRegisteredAt(account.getUnRegisteredAt())
                .build();
    }
}
