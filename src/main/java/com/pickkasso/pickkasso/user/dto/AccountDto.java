package com.pickkasso.pickkasso.user.dto;

import com.pickkasso.pickkasso.user.entity.Account;
import com.pickkasso.pickkasso.user.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AccountDto {
    private String username;
    private String password;
    private Role role;

    public Account createAccount(String encoded) {
        return Account.createAccount(
            this.username,
            encoded,
            this.role
        );
    }
}
