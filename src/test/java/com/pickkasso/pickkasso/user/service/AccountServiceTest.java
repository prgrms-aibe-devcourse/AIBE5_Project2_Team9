package com.pickkasso.pickkasso.user.service;

import com.pickkasso.pickkasso.user.dto.AccountDto;
import com.pickkasso.pickkasso.user.entity.Account;
import com.pickkasso.pickkasso.user.entity.Role;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@RequiredArgsConstructor
class AccountServiceTest {

    @Autowired
    AccountService accountService;

    @Test
    @DisplayName("중복 회원가입 테스트")
    public void duplicateAccount() {
        // given
        AccountDto dto = new AccountDto(
            "테스트 username",
            "테스트 password",
            Role.MEMBER
        );
        Account account1 = accountService.saveAccount(dto);

        // when
        Throwable e = assertThrows(
            IllegalStateException.class, () -> {
                Account account2 = accountService.saveAccount(dto);
            }
        );

        // then
        assertEquals("이미 사용 중인 아이디입니다.", e.getMessage());
    }
}