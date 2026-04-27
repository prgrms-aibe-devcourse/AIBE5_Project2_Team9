package com.pickkasso.pickkasso.user.cash.entity;

import com.pickkasso.pickkasso.user.entity.Account;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_cash_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CashHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CashHistoryType type;

    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private String memo;

    private CashHistory(Account account, int amount, CashHistoryType type, int balanceAfter, String memo) {
        this.account = account;
        this.amount = amount;
        this.type = type;
        this.balanceAfter = balanceAfter;
        this.memo = memo;
    }

    public static CashHistory ofCharge(Account account, int amount, int balanceAfter) {
        return new CashHistory(account, amount, CashHistoryType.CHARGE, balanceAfter,
                "CHARGE preset " + amount);
    }
}
