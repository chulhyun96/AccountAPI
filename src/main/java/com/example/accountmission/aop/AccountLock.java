package com.example.accountmission.aop;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface AccountLock {
    String value(); // 락의 이름 고유 값
}
