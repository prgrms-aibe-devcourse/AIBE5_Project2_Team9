package com.pickkasso.pickkasso.user.auth;

import com.pickkasso.pickkasso.user.entity.Account;
import com.pickkasso.pickkasso.user.entity.Role;
import com.pickkasso.pickkasso.user.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final AccountRepository accountRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException{
        OAuth2User user = super.loadUser(userRequest);

        Map<String, Object> attributes = user.getAttributes();
        System.out.println("카카오 전체 데이터" + attributes);

        return user;
    }

    public Account processOAuthUser(String email) {
        // 1. 기존 계정 조회
        Account account = accountRepository.findByUsername(email);

        // 2. 있으면 그대로 반환
        if (account != null) {
            return account;
        }

        // 3. 없으면 새로 생성 (자동 회원가입)
        Account newAccount = Account.createAccount(
                email,          // username = email
                "",             // 비밀번호 없음 (OAuth니까)
                Role.MEMBER
        );

        return accountRepository.save(newAccount);

    }
}
