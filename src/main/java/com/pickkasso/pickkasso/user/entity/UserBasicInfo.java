package com.pickkasso.pickkasso.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
public class UserBasicInfo {
    @Column(name = "email")
    protected String email;

    @Column(name = "name")
    protected String name;

    @Column(name = "gender")
    @Enumerated(EnumType.STRING)
    protected Gender gender;

    @Column(name = "phone")
    protected String phone;

    @Column(name = "cash")
    protected Integer cash;

    @Column(name = "deleted_at")
    protected LocalDateTime deletedAt;

    public void deductCash(int amount) {
        int current = this.cash == null ? 0 : this.cash;
        if (current < amount) {
            throw new IllegalStateException("캐시 잔액이 부족합니다.");
        }
        this.cash = current - amount;
    }

    public void refundCash(int amount) {
        if (amount <= 0) return;
        this.cash = (this.cash == null ? 0 : this.cash) + amount;
    }
}