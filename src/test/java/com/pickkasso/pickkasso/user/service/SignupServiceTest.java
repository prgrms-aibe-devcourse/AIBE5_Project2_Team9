package com.pickkasso.pickkasso.user.service;

import com.pickkasso.pickkasso.user.dto.SignupDto;
import com.pickkasso.pickkasso.user.entity.Member;
import com.pickkasso.pickkasso.user.entity.Role;
import com.pickkasso.pickkasso.user.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class SignupServiceIntegrationTest {

    @Autowired
    private SignupService signupService;

    @Autowired
    private MemberRepository memberRepository;

    private SignupDto createDto(String username, String email) {
        SignupDto dto = new SignupDto();
        dto.setUsername(username);
        dto.setPassword("Password1!");
        dto.setPasswordConfirm("Password1!");
        dto.setRole(Role.MEMBER);
        dto.setEmail(email);
        dto.setName("홍길동");
        return dto;
    }

    @Test
    @DisplayName("회원가입 성공")
    void 회원가입_성공() {
        // given
        SignupDto dto = createDto("user1", "test@test.com");

        // when
        signupService.signup(dto);

        // then
        Member member = memberRepository.findByEmail("test@test.com")
                .orElseThrow(() -> new RuntimeException("회원 없음"));

        assertEquals("홍길동", member.getName());
    }

    @Test
    @DisplayName("회원가입 실패 - 중복 아이디")
    void 회원가입_중복실패() {
        // given
        SignupDto dto1 = new SignupDto();
        dto1.setUsername("test123");
        dto1.setPassword("Password1!");
        dto1.setPasswordConfirm("Password1!");
        dto1.setRole(Role.MEMBER);
        dto1.setEmail("test1@test.com");
        dto1.setName("홍길동");

        SignupDto dto2 = new SignupDto();
        dto2.setUsername("test123"); // 같은 아이디
        dto2.setPassword("Password2!");
        dto2.setPasswordConfirm("Password2!");
        dto2.setRole(Role.MEMBER);
        dto2.setEmail("test2@test.com");
        dto2.setName("김철수");

        // when
        signupService.signup(dto1);

        // then
        assertThrows(IllegalStateException.class, () -> {
            signupService.signup(dto2);
        });
    }

    @Test
    @DisplayName("비밀번호 규칙 위반 시 회원가입 실패")
    void signup_fail_invalidPassword() {
        // given
        SignupDto dto = new SignupDto();
        dto.setUsername("test123");
        dto.setPassword("1234"); // 규칙 위반
        dto.setPasswordConfirm("1234");
        dto.setEmail("test@test.com");
        dto.setName("홍길동");

        // when & then
        assertThrows(Exception.class, () -> {
            signupService.signup(dto);
        });
    }
}