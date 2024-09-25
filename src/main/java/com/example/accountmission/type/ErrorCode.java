package com.example.accountmission.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    INTERNAL_SERVER_ERROR("내부 서버 오류가 발생했습니다"),
    USER_NOT_FOUND("사용자가 없습니다."),
    MAX_ACCOUNT_PER_USER_10("사용자 최대 계좌 개수는 10개 입니다."),
    ACCOUNT_NOT_FOUND("해당 사용자의 계좌를 찾지 못하였습니다."),
    USER_ACCOUNT_UN_MATCH("사용자와 계좌간의 정보가 일치하지 않습니다."),
    ACCOUNT_ALREADY_UNREGISTERED("계좌가 이미 해지 상태입니다."),
    BALANCE_NOT_EMPTY("잔액이 있는 계좌는 해지가 불가능합니다."),
    AMOUNT_EXCEED_BALANCE("잔액 부족."),
    TRANSACTION_NOT_FOUND("해당 거래가 없습니다."),
    TRANSACTION_ACCOUNT_UN_MATCH("해당 거래는 해당 계좌에서 발생한 거래가 아닙니다."),
    CANCEL_MUST_FULLY("거래 하신 실제금액과 취소 금액이 같아야 합니다."),
    INVALID_REQUEST("잘못된 요청입니다."),
    ACCOUNT_TRANSACTION_LOCK("해당 계좌는 사용중입니다.");
    private final String description;
}
