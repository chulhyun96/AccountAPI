package com.example.accountmission.service;

import com.example.accountmission.exception.AccountException;
import com.example.accountmission.type.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class LockService {
    private final RedissonClient redissonClient;

    // accountNumber가 락의 Key값이 된다.
    public void lock(String accountNumber) {
        RLock lock = redissonClient.getLock(getLockKey(accountNumber));
        log.debug("Trying to lock accountNumber {}", accountNumber);
        try {
            boolean isLock = lock.tryLock(1, 5, TimeUnit.SECONDS);
            if (!isLock) {
                log.error("===Lock acquisition failed===");
                throw new AccountException(ErrorCode.ACCOUNT_TRANSACTION_LOCK);
            }
        } catch (InterruptedException e) {
            log.error("===Redis Lock acquisition failed===");
        }
    }

    public void unlock(String accountNumber) {
        log.debug("Trying to unlock accountNumber {}", accountNumber);
        redissonClient.getLock(getLockKey(accountNumber)).unlock();
    }

    private static String getLockKey(String accountNumber) {
        return "ACLK : " + accountNumber;
    }
}
