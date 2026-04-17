package com.pickkasso.pickkasso.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends UserBasicInfo {
    @Id
    @Column(name = "member_id")
    private Long id;

    @MapsId
    @OneToOne
    @JoinColumn(name = "member_id")
    private Account account;

    private Member(
        Account account,
        String email,
        String name,
        Gender gender,
        String phone,
        Integer cache) {
        this.account = account;
        this.email = email;
        this.name = name;
        this.gender = gender;
        this.phone = phone;
        this.cache = cache;
    }

    //== 생성 method ==//
    public static Member createMember(
        Account account,
        String email,
        String name,
        Gender gender,
        String phone,
        Integer cache) {
        return new Member(account, email, name, gender, phone, cache);
    }
}
