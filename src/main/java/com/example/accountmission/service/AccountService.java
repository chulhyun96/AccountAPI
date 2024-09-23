package com.example.accountmission.service;

import com.example.accountmission.domain.Account;
import com.example.accountmission.domain.AccountUser;
import com.example.accountmission.dto.AccountDto;
import com.example.accountmission.exception.AccountException;
import com.example.accountmission.repository.AccountRepository;
import com.example.accountmission.repository.AccountUserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static com.example.accountmission.type.AccountStatus.*;
import static com.example.accountmission.type.ErrorCode.*;


@Service
@Slf4j
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountUserRepository accountUserRepository;

    /**
     * 사용자가 있는지 조회
     * 계좌의 번호를 생성
     * 계좌를 저장하고, 그 정보를 넘긴
     */
    @Transactional
    public AccountDto createAccount(Long userId, Long initialBalance) {
        AccountUser accountUser = accountUserRepository.findById(userId)
                .orElseThrow(() -> {
                    log.info("User Id = {}", userId);
                    return new AccountException(USER_NOT_FOUND);
                });

        validateCountOfAccountPerUser(accountUser);

        String newAccountNumber = accountRepository.findFirstByOrderByIdDesc()
                .map(account -> (Integer.parseInt(account.getAccountNumber())) + 1 + "")
                .orElse("1234567890");

        return AccountDto.fromEntity(
                accountRepository.save(Account.builder()
                        .accountUser(accountUser)
                        .accountNumber(newAccountNumber)
                        .accountStatus(IN_USE)
                        .balance(initialBalance)
                        .registeredAt(LocalDateTime.now())
                        .build()));
    }

    private void validateCountOfAccountPerUser(AccountUser accountUser) {
        if (accountRepository.countByAccountUser(accountUser) >= 10) {
            throw new AccountException(MAX_ACCOUNT_PER_USER_10);
        }
    }

    @Transactional
    public Account getAccount(Long id) {
        return accountRepository.findById(id).orElseThrow(() ->
                new AccountException(USER_NOT_FOUND));
    }

    /**
     * 아이디로 사용자 정보 가지고오기
     * 계좌번호로 계좌 가지고오기
     * 계좌번호와 사용자 아이디가 동일한지 보기
     */
    @Transactional
    public AccountDto deleteAccount(Long userId, String accountNumber) {
        AccountUser accountUser = accountUserRepository.findById(userId)
                .orElseThrow(() -> new AccountException(USER_NOT_FOUND));
        Account findedAccount = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ACCOUNT_NOT_FOUND)
        );
        validateDeleteAccount(accountUser, findedAccount);
        findedAccount.setUnRegisteredAt(LocalDateTime.now());
        findedAccount.setAccountStatus(UNREGISTERED);
        return AccountDto.fromEntity(findedAccount);
    }

    /**
     * 사용자 아이디와 계좌 소유주가 다른 경우
     * 계좌가 이미 해지 상태인 경우
     * 잔액이 있는 경우 계좌 삭제 불가능
     */
    private void validateDeleteAccount(AccountUser accountUser, Account findedAccount) {
        if (!Objects.equals(accountUser.getId(), findedAccount.getAccountUser().getId())) {
            log.info("사용자 아이디 = {}, 계좌 사용자 = {}",
                    accountUser.getId(), findedAccount.getAccountUser().getId());
            throw new AccountException(USER_ACCOUNT_UN_MATCH);
        }
        if (findedAccount.getAccountStatus() != IN_USE) {
            throw new AccountException(ACCOUNT_ALREADY_UNREGISTERED);
        }
        if (findedAccount.getBalance() > 0) {
            throw new AccountException(BALANCE_NOT_EMPTY);
        }

    }
    @Transactional
    public List<AccountDto> getAccountsByUserId(Long userId) {
        AccountUser accountUser = accountUserRepository.findById(userId).orElseThrow(
                () -> new AccountException(USER_NOT_FOUND));
        List<Account> accounts = accountRepository.findByAccountUser(accountUser);
        return accounts.stream()
                .map(AccountDto::fromEntity)
                .toList();
    }
}
