package com.example.accountmission.service;

import com.example.accountmission.aop.AccountLock;
import com.example.accountmission.aop.AccountLockIdInterface;
import com.example.accountmission.dto.UseBalance;
import com.example.accountmission.exception.AccountException;
import com.example.accountmission.type.ErrorCode;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LockAopAspectTest {

    @Mock
    private LockService lockService;

    @Mock
    private ProceedingJoinPoint pjp;


    LockAopAspect lockAopAspect;

    @BeforeEach
    void setup () {
        this.lockAopAspect = new LockAopAspect(lockService);
    }

    @Test
    @DisplayName("Lock 테스트")
    void lockAndUnLock() throws Throwable {
        //given
        ArgumentCaptor<String> lockArgs = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> unlockArgs = ArgumentCaptor.forClass(String.class);

        String accountNumber = "1234567890";
        AccountLockIdInterface request
                = new UseBalance.Request(1L,accountNumber,1000L);
        //when
        lockAopAspect.aroundMethod(pjp, request);
        //then
        verify(lockService, times(1)).lock(lockArgs.capture());
        verify(lockService, times(1)).unlock(unlockArgs.capture());
        assertEquals("1234567890", lockArgs.getValue());
        assertEquals("1234567890", unlockArgs.getValue());
    }
    @Test
    @DisplayName("예외 발생시 unLock 테스트")
    void throwException_Unlock() throws Throwable {
        //given
        ArgumentCaptor<String> lockArgs = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> unlockArgs = ArgumentCaptor.forClass(String.class);

        String accountNumber = "1234567890";
        AccountLockIdInterface request
                = new UseBalance.Request(1L,accountNumber,1000L);
        given(pjp.proceed()).willThrow(new AccountException(ErrorCode.ACCOUNT_TRANSACTION_LOCK));
        //when
        assertThrows(AccountException.class, () -> lockAopAspect.aroundMethod(pjp, request));
        //then
        verify(lockService, times(1)).lock(lockArgs.capture());
        verify(lockService, times(1)).unlock(unlockArgs.capture());
        assertEquals("1234567890", lockArgs.getValue());
        assertEquals("1234567890", unlockArgs.getValue());
    }
}