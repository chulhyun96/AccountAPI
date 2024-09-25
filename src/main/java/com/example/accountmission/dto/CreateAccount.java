package com.example.accountmission.dto;

import com.example.accountmission.aop.AccountLockIdInterface;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

//보통 Request, Response용 따로 만들어서 사용하는 경우가 있지만
//이보다 너 나은 방식으로는 클래스를 만들고 request, response를 Inner클래스로 만듦.
public class CreateAccount {
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        @NotNull
        @Min(1)
        private Long userId;

        @NotNull
        @Min(0)
        private Long initialBalance;
    }
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long userId;
        private String accountNumber;
        private LocalDateTime registeredAt;

        public static Response from(AccountDto accountDto) {
            return Response.builder()
                    .userId(accountDto.getUserId())
                    .accountNumber(accountDto.getAccountNumber())
                    .registeredAt(LocalDateTime.now())
                    .build();
        }
    }
}
