package com.pickkasso.pickkasso.user.auth;

import com.pickkasso.pickkasso.user.entity.Account;
import com.pickkasso.pickkasso.user.entity.Role;
import com.pickkasso.pickkasso.user.repository.AccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@SpringBootTest
@Transactional
public class OAuth2ServiceTest {

    @Autowired
    private CustomOAuth2UserService service;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    @DisplayName("카카오 로그인 시 자동 회원가입")
    void 카카오_회원_자동가입() {
        // given
        String email = "test@test.com";

        // when
        Account account = service.processOAuthUser(email);

        // then
        assertNotNull(account);
        assertEquals(email, account.getUsername());
    }

    @Test
    @DisplayName("이미 존재하는 회원이면 새로 생성하지 않는다")
    void 카카오_기존회원() {
        // given
        String email = "test@test.com";

        // 먼저 저장
        Account saved = Account.createAccount(email, "1234", Role.MEMBER);
        accountRepository.save(saved);

        // when
        Account result = service.processOAuthUser(email);

        // then
        assertEquals(saved.getId(), result.getId()); // 같은 계정인지 확인
    }
}
