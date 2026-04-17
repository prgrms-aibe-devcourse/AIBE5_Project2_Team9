package com.pickkasso.pickkasso.user.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "t_account")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account {
    @Id
    @Column(name = "account_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", unique = true)
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Role role;

    private Account(String username, String password, Role role) {
        this.username = username;
        this.password = password; // encoder 써야함
        this.role = role;
    }

    //== 생성 method ==//
    public static Account createAccount(
        String username,
        String password,
        Role role) {
        return new Account(username, password, role);
    }
}
