package com.example.accountmission.domain;

import com.example.accountmission.exception.AccountException;
import com.example.accountmission.type.AccountStatus;
import com.example.accountmission.type.ErrorCode;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Entity
public class Account extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private AccountUser accountUser;

    @Size(min = 10, max = 10)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus;

    private Long balance;

    private LocalDateTime registeredAt;
    private LocalDateTime unRegisteredAt;

    public void useBalance(Long amount) {
        if (balance < amount) {
            throw new AccountException(ErrorCode.AMOUNT_EXCEED_BALANCE);
        }
        balance = balance - amount;
    }

    public void cancelBalance(Long amount) {
        if (amount < 0) {
            throw new AccountException(ErrorCode.INVALID_REQUEST);
        }
        balance = balance + amount;
    }
}


