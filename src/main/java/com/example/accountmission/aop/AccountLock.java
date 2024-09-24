package com.example.accountmission.aop;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface AccountLock {
    long tryLockTime() default 5000L;
}
