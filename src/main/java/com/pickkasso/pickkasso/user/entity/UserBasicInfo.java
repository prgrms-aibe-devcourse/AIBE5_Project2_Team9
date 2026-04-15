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

    @Column(name = "cache")
    protected Integer cache;

    @Column(name = "deleted_at")
    protected LocalDateTime deletedAt;
}