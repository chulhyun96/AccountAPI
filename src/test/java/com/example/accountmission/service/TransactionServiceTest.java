package com.example.accountmission.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.example.accountmission.type.TransactionResultType.FAIL;
import static com.example.accountmission.type.TransactionResultType.SUCCESS;
import static com.example.accountmission.type.TransactionType.CANCEL;
import static com.example.accountmission.type.TransactionType.USE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountUserRepository accountUserRepository;
    @Mock
    private AccountRepository accountRepository;

    private TransactionService transactionService;

    @BeforeEach
    void setup() {
        transactionService = new TransactionService(transactionRepository, accountUserRepository, accountRepository);
    }

    @Test
    @DisplayName("잔액 사용 성공")
    void successUseBalance() {
        //given
        AccountUser user = AccountUser.builder()
                .id(1L)
                .name("Test USER")
                .build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        Account account = Account.builder()
                .accountUser(user)
                .balance(10000L)
                .accountStatus(AccountStatus.IN_USE)
                .build();

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        given(transactionRepository.save(any()))
                .willReturn(Transaction.builder()
                        .account(account)
                        .transactionType(USE)
                        .transactionResultType(SUCCESS)
                        .amount(1000L)
                        .balanceSnapshot(9000L)
                        .build());

        //when
        TransactionDto transactionDto = transactionService.useBalance(
                1L, "123456", 1200L);
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        //then
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(transactionRepository, times(1)).save(captor.capture());
        assertAll(
                () -> assertEquals(1200L, captor.getValue().getAmount()),
                () -> assertEquals(10000L - 1200L, captor.getValue().getBalanceSnapshot()),
                () -> assertEquals(USE, transactionDto.getTransactionType()),
                () -> assertEquals(SUCCESS, transactionDto.getTransactionResultType())
        );
    }

    @Test
    @DisplayName("해당 유저 없음 - 잔액 사용 실패")
    void useBalance_UserNotFound() {
        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        //when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.useBalance(
                        1L, "12345", 1200L)
        );
        //then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("해당 계좌 없음 - 잔액 사용 실패")
    void useBalance_AccountNotFound() {
        //given
        AccountUser user = AccountUser.builder()
                .id(1L)
                .name("Test USER")
                .build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());
        //when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.useBalance(
                        1L, "12345", 1200L)
        );
        //then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌 소유주 다름 - 잔액 사용 실패")
    void useBalance_UserAccountUnMatch() {
        //given
        AccountUser user = AccountUser.builder()
                .id(1L)
                .name("Test USER")
                .build();

        Account account = Account.builder()
                .id(1L)
                .accountUser(AccountUser.builder()
                        .id(2L)
                        .name("Other User")
                        .build())
                .accountNumber("12345")
                .accountStatus(AccountStatus.IN_USE)
                .balance(10000L)
                .registeredAt(LocalDateTime.now())
                .build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));
        //when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.useBalance(
                        1L, "12345", 1200L)
        );
        //then
        assertEquals(ErrorCode.USER_ACCOUNT_UN_MATCH, exception.getErrorCode());
    }

    @Test
    @DisplayName("거래 금액이 잔액 보다 큰 경우 - 잔액 사용 실패")
    void doTest() {
        //given
        AccountUser user = AccountUser.builder()
                .id(1L)
                .name("Test USER")
                .build();
        Account account = Account.builder()
                .id(1L)
                .accountUser(user)
                .accountNumber("12345")
                .accountStatus(AccountStatus.IN_USE)
                .balance(0L)
                .registeredAt(LocalDateTime.now())
                .build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        //when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.useBalance(
                        1L, "12345", 1200L)
        );
        //then
        verify(accountRepository, times(1)).findByAccountNumber(anyString());
        assertEquals(ErrorCode.AMOUNT_EXCEED_BALANCE, exception.getErrorCode());
    }

    @Test
    @DisplayName("트랙잰션 성공 상태 저장 성공")
    void saveTransactionStatus() {
        //given
        AccountUser user = AccountUser.builder()
                .id(1L)
                .name("Test USER")
                .build();
        Account account = Account.builder()
                .id(1L)
                .accountUser(user)
                .accountNumber("12345")
                .accountStatus(AccountStatus.IN_USE)
                .balance(10000L)
                .registeredAt(LocalDateTime.now())
                .build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));
        given(transactionRepository.save(any()))
                .willReturn(Transaction.builder()
                        .transactionType(TransactionType.USE)
                        .transactionResultType(SUCCESS)
                        .account(account)
                        .amount(1000L)
                        .balanceSnapshot(9000L)
                        .build());
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        //when
        TransactionDto transactionDto = transactionService.useBalance(1L, "12345", 1000L);

        //then
        verify(transactionRepository, times(1)).save(captor.capture());
        assertAll(
                () -> assertEquals(transactionDto.getTransactionType(),
                        captor.getValue().getTransactionType()),
                () -> assertEquals(transactionDto.getTransactionResultType(),
                        captor.getValue().getTransactionResultType()),
                () -> assertEquals(account.getBalance(), captor.getValue().getBalanceSnapshot())
        );
    }

    @Test
    @DisplayName("트랙잰션 실패 상태 저장 성공")
    void saveFailedUseTransaction() {
        //given
        AccountUser user = AccountUser.builder()
                .id(1L)
                .name("Test USER")
                .build();
        Account account = Account.builder()
                .id(1L)
                .accountUser(user)
                .accountNumber("12345")
                .accountStatus(AccountStatus.IN_USE)
                .balance(10000L)
                .registeredAt(LocalDateTime.now())
                .build();

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));
        given(transactionRepository.save(any()))
                .willReturn(Transaction.builder()
                        .transactionType(TransactionType.USE)
                        .account(account)
                        .amount(1000L)
                        .balanceSnapshot(9000L)
                        .build());
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        //when
        transactionService.saveFailedUseTransaction("123456", 1000L);

        //then
        verify(transactionRepository, times(1)).save(captor.capture());
        assertAll(
                () -> assertEquals(FAIL,
                        captor.getValue().getTransactionResultType()),
                () -> assertEquals(account.getBalance(), captor.getValue().getBalanceSnapshot())
        );
    }

    @Test
    @DisplayName("거래 취소 성공")
    void successCancelBalance() {
        //given
        AccountUser user = AccountUser.builder()
                .id(1L)
                .name("Test USER")
                .build();


        Account account = Account.builder()
                .accountNumber("1234567890")
                .accountUser(user)
                .balance(10000L)
                .accountStatus(AccountStatus.IN_USE)
                .build();

        Transaction transaction = Transaction.builder()
                .account(account)
                .transactionType(USE)
                .transactionResultType(SUCCESS)
                .transactionId("TRANSACTION_ID")
                .transactedAt(LocalDateTime.now())
                .amount(1000L)
                .balanceSnapshot(9000L)
                .build();

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        given(transactionRepository.save(any()))
                .willReturn(Transaction.builder()
                        .account(account)
                        .transactionId("TRANSACTION_ID")
                        .transactionType(CANCEL)
                        .transactionResultType(SUCCESS)
                        .amount(1000L)
                        .balanceSnapshot(10000L)
                        .build());
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

        //when
        TransactionDto transactionDto = transactionService.cancelBalance(
                "TRANSACTION_ID", "1234567890", 1000L);

        //then
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(transactionRepository, times(1)).save(captor.capture());
        assertAll(
                () -> assertEquals(1000L, captor.getValue().getAmount()),
                () -> assertEquals(10000L + 1000L, captor.getValue().getBalanceSnapshot()),
                () -> assertEquals(CANCEL, transactionDto.getTransactionType()),
                () -> assertEquals(SUCCESS, transactionDto.getTransactionResultType())
        );
    }
    @Test
    @DisplayName("해당 계좌 없음 - 거래 취소 실패")
    void cancelBalance_AccountNotFound() {
        //given
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());
        //when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.cancelBalance(
                        "TRANSACTION_ID", "123457890", 1000L)
        );
        //then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
    }
    @Test
    @DisplayName("취소 금액이 원 금액보다 높음 - 거래 취소 실패")
    void cancelBalance_TransactionNotFound() {
        //given
        AccountUser user = new AccountUser(1L, "First");
        Account account = new Account(1L, user, "1234567890",
                AccountStatus.IN_USE, 10000L, LocalDateTime.now(), null);
        Transaction transaction = new Transaction(
                1L, CANCEL, FAIL, account, 1000L,
                account.getBalance() - 1000L, "TRANSACTION_ID", LocalDateTime.now());

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));
        //when
        AccountException exception = assertThrows(AccountException.class, () -> transactionService.
                cancelBalance(transaction.getTransactionId(), account.getAccountNumber(), 2000L));
        //then
        assertEquals(ErrorCode.CANCEL_MUST_FULLY, exception.getErrorCode());
    }
    @Test
    @DisplayName("거래 취소 실패 - 거래번호와 계좌가 서로 다름")
    void cancelBalance_Transaction_Account_Un_Match() {
        //given
        AccountUser user = new AccountUser(1L, "First");

        Account account1 = new Account(1L, user, "1234567890",
                AccountStatus.IN_USE, 10000L, LocalDateTime.now(), null);
        Account account2 = new Account(2L, user, "1234567891",
                AccountStatus.IN_USE, 10000L, LocalDateTime.now(), null);

        Transaction transaction = new Transaction(
                1L, CANCEL, FAIL, account2, 1000L,
                account2.getBalance() - 1000L,
                "TRANSACTION_ID", LocalDateTime.now());

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account1));
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));
        //when
        AccountException exception = assertThrows(AccountException.class, () -> transactionService.
                cancelBalance(transaction.getTransactionId(), account1.getAccountNumber(), 1000L));
        //then
        assertEquals(ErrorCode.TRANSACTION_ACCOUNT_UN_MATCH, exception.getErrorCode());
    }
    @Test
    @DisplayName("거래 조회 성공")
    void successQueryTransaction() {
        //given
        AccountUser user = new AccountUser(1L, "First");
        Account account = new Account(1L, user, "1234567890",
                AccountStatus.IN_USE, 10000L, LocalDateTime.now(), null);
        Transaction transaction = new Transaction(
                1L, USE, SUCCESS, account, 1000L,
                account.getBalance() - 1000L,
                "TRANSACTION_ID", LocalDateTime.now());

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));
        //when
        TransactionDto transactionDto = transactionService.queryTransaction(transaction.getTransactionId());

        //then
        assertAll(
                () -> assertEquals(USE, transactionDto.getTransactionType()),
                () -> assertEquals(SUCCESS, transactionDto.getTransactionResultType()),
                () -> assertEquals(1000L, transactionDto.getAmount()),
                () -> assertEquals(account.getBalance() - 1000L,
                        transactionDto.getBalanceSnapshot())
        );
    }
    @Test
    @DisplayName("거래 조회 실패")
    void queryTransaction_TransactionNotFound() {
        //given
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.empty());
        //when
        AccountException exception = assertThrows(AccountException.class, () ->
                transactionService.queryTransaction("TRANSACTION_ID"));
        //then
        assertEquals(ErrorCode.TRANSACTION_NOT_FOUND, exception.getErrorCode());
    }

}