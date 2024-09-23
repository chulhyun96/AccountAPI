package com.example.accountmission.service;

import com.example.accountmission.domain.Account;
import com.example.accountmission.domain.AccountUser;
import com.example.accountmission.dto.AccountDto;
import com.example.accountmission.exception.AccountException;
import com.example.accountmission.repository.AccountRepository;
import com.example.accountmission.repository.AccountUserRepository;
import com.example.accountmission.type.AccountStatus;
import com.example.accountmission.type.ErrorCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountUserRepository accountUserRepository;

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(accountRepository, accountUserRepository);
    }

    @Test
    @DisplayName("계좌 생성 성공")
    void createAccountSuccess() {
        //given

        /**
         * Pobi라는 유저가 이미 존재.
         * findById 메서드를 통해 기존에 Pobi가 가지고 있던 계좌를 반환
         * 그리고 findFirstByOrderByIdDesc 메서드를 통해 Pobi가 가지고 있는 계좌중에서 가장 최신것을 반환
         * 그 후  Account Entity를 통해 새로운 Account 반환
         * 새로운 Account가 AccountDto로 반환됨.
         */
        AccountUser accountUser = AccountUser.builder()
                .id(12L)
                .name("Pobi").build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));

        given(accountRepository.findFirstByOrderByIdDesc())
                .willReturn(Optional.of(Account.builder()
                        .accountNumber("1000013").build()));

        given(accountRepository.save(any(Account.class)))
                .willReturn(Account.builder()
                        .accountUser(accountUser)
                        .accountNumber(anyString())
                        .build());
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        //when
        AccountDto accountDto = accountService.createAccount(1L, 1000L);


        //then
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(12L, accountDto.getUserId());
        assertEquals("1000014", captor.getValue().getAccountNumber());
    }

    @Test
    @DisplayName("계좌 첫 생성")
    void createFirstAccount() {
        AccountUser accountUser = AccountUser.builder()
                .id(15L)
                .name("Pobi").build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));

        given(accountRepository.findFirstByOrderByIdDesc())
                .willReturn(Optional.empty());

        given(accountRepository.save(any(Account.class)))
                .willReturn(Account.builder()
                        .accountUser(accountUser)
                        .accountNumber("100001")
                        .build());

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        //when
        AccountDto accountDto = accountService.createAccount(
                1L, 1000L
        );

        //then
        verify(accountRepository, times(1)).save(captor.capture());
        assertAll(() -> assertEquals(15L, accountDto.getUserId()),
                () -> assertEquals("1234567890",
                        captor.getValue().getAccountNumber()),
                () -> assertEquals("Pobi",
                        captor.getValue().getAccountUser().getName())
        );
    }

    @Test
    @DisplayName("해당 유저 없음, 계좌 생성 실패")
    void createAccount_UserNotFound() {
        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        //when
        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.createAccount(1L, 100L));
        //then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("유저당 최개 계좌 개수는 최대 10개")
    void createAccount_maxAccountIs10() {
        //given
        AccountUser accountUser = AccountUser.builder()
                .id(15L)
                .name("Pobi").build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));

        given(accountRepository.countByAccountUser(accountUser))
                .willReturn(10);
        //when
        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.createAccount(1L, 100L));
        //then

        assertEquals(ErrorCode.MAX_ACCOUNT_PER_USER_10, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌 해지 성공")
    void deleteAccountSuccess() {
        //given
        AccountUser accountUser = AccountUser.builder()
                .id(1L)
                .name("Pobi").build();
        Account account = Account.builder()
                .accountUser(accountUser)
                .accountStatus(AccountStatus.IN_USE)
                .accountNumber("123456")
                .balance(0L)
                .build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));
        //when
        AccountDto accountDto = accountService.deleteAccount(1L, "123456");
        //then
        verify(accountUserRepository, times(1)).findById(anyLong());
        verify(accountRepository, times(1)).findByAccountNumber(anyString());
        assertAll(() -> assertEquals(1L, accountDto.getUserId()),
                () -> assertEquals("123456", accountDto.getAccountNumber()),
                () -> assertEquals(AccountStatus.UNREGISTERED, account.getAccountStatus())
        );
    }

    @Test
    @DisplayName("계좌 해지 실패 - 해당 유저 없음")
    void deleteAccount_UserNotFound() {
        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        //when
        AccountException exception = assertThrows(AccountException.class, ()
                -> accountService.deleteAccount(1L, "123456"));
        //then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌 해지 실패 - 계좌 없음")
    void deleteAccount_AccountNotFound() {
        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(AccountUser.builder()
                        .id(1L)
                        .name("Pororo")
                        .build()));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());
        //when
        AccountException exception = assertThrows(AccountException.class, ()
                -> accountService.deleteAccount(1L, "123456"));
        //then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌 해지 실패 - 계좌 소유주가 다름")
    void deleteAccount_UserAccountUnMatch() {
        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(AccountUser.builder()
                        .id(1L)
                        .name("Pororo")
                        .build()));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(AccountUser.builder()
                                .id(2L)
                                .name("Ngumma")
                                .build())
                        .build()));
        //when
        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "123456"));
        //then
        assertEquals(ErrorCode.USER_ACCOUNT_UN_MATCH, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌 해지 실패 - 계좌가 이미 해지 상태인 경우")
    void deleteAccount_AccountAlreadyUnRegistered() {
        //given
        AccountUser pororo = AccountUser.builder()
                .id(1L)
                .name("Pororo")
                .build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(pororo));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(pororo)
                        .accountStatus(AccountStatus.UNREGISTERED)
                        .build()));
        //when
        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "123456"));
        //then
        assertEquals(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌 해지 실패 - 계좌에 잔액이 남아있는 경우")
    void deleteAccount_BalanceNotEmpty() {
        //given
        AccountUser pororo = AccountUser.builder()
                .id(1L)
                .name("Pororo")
                .build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(pororo));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(pororo)
                        .accountStatus(AccountStatus.IN_USE)
                        .balance(100L)
                        .build()));
        //when
        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "123456"));
        //then
        assertEquals(ErrorCode.BALANCE_NOT_EMPTY, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌 조회 - 리스트")
    void success_GetAccountsByUserId() {
        //given
        AccountUser accountUser = new AccountUser(1L, "TEST USER");
        List<Account> list = new ArrayList<>();
        String accountNumber = "123456";
        for (int i = 0; i < 5; i++) {
            list.add(Account.builder()
                    .accountUser(accountUser)
                    .accountNumber(accountNumber.substring(
                            0, accountNumber.length() - 1) + i)
                    .balance(1000L)
                    .build());
        }
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));

        given(accountRepository.findByAccountUser(any()))
                .willReturn(list);
        //when
        List<AccountDto> accountsByUserId = accountService.getAccountsByUserId(1L);

        //then
        Assertions.assertAll(
                () -> assertEquals(5, accountsByUserId.size()),
                () -> assertEquals("123450",
                        accountsByUserId.get(0).getAccountNumber()
                ));
    }

    @Test
    @DisplayName("계좌 조회 - 사용자 ID가 없을 경우")
    void failedGetAccountsByUserId() {
        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        //when
        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.getAccountsByUserId(1L));
        //then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }
}