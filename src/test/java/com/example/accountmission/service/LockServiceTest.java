package com.example.accountmission.service;

import com.example.accountmission.exception.AccountException;
import com.example.accountmission.type.ErrorCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class LockServiceTest {
    @Mock
    RedissonClient redissonClient;
    @Mock
    private RLock rLock;
    @InjectMocks
    LockService lockService;


    @Test
    @DisplayName("Lock Service")
    void lockServiceSuccess() throws InterruptedException {
        //given
        given(redissonClient.getLock(anyString()))
                .willReturn(rLock);
        given(rLock.tryLock(anyLong(),anyLong(),any()))
                .willReturn(true);
        String accountNumber = "12334567890";
        //when
        lockService.lock(accountNumber);
        //then
    }
    @Test
    @DisplayName("Lock Service Exception")
    void lockServiceFail() throws InterruptedException {
        //given
        given(redissonClient.getLock(anyString()))
                .willReturn(rLock);
        given(rLock.tryLock(anyLong(),anyLong(),any()))
                .willReturn(false);
        String accountNumber = "12334567890";
        //when
        AccountException exception =
                assertThrows(AccountException.class, () -> lockService.lock(accountNumber));
        //then
        assertEquals(ErrorCode.ACCOUNT_TRANSACTION_LOCK, exception.getErrorCode());
    }
}