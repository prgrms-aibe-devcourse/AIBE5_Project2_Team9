package com.pickkasso.pickkasso.user.service;

import com.pickkasso.pickkasso.user.dto.AccountDto;
import com.pickkasso.pickkasso.user.entity.Account;
import com.pickkasso.pickkasso.user.entity.Member;
import com.pickkasso.pickkasso.user.entity.Role;
import com.pickkasso.pickkasso.user.repository.AccountRepository;
import com.pickkasso.pickkasso.user.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@RequiredArgsConstructor
class AccountServiceTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    AccountService accountService;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private MemberRepository memberRepository;

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

    @Test
    @DisplayName("아이디 찾기 성공")
    void 아이디찾기_성공(){
        String name = "홍길동";
        String email = "test@test.com";

        Account account = Account.createAccount("testId", "1234", Role.MEMBER);
        accountRepository.save(account);

        Member member = Member.createMember(account, email, name, null, null, 0);
            memberRepository.save(member);

            //아이디 찾기 이름,이메일
        String username = accountService.findUsername(name, email);

        assertEquals("testId", username);


        }

        @Test
    @DisplayName("비밀번호 재발급")
    void 재발급_성공(){
        String username = "testID";
        String email = "test@test.com";

            Account account = Account.createAccount(username, "1234", Role.MEMBER);
            accountRepository.save(account);

            Member member = Member.createMember(account, email, "홍길동", null, null, 0);
            memberRepository.save(member);

            String tempPw = accountService.createTempPassword(username, email);

            assertNotNull(tempPw); // 임시 비번 생성됨
            assertNotEquals("1234", account.getPassword());

        }


        @Test
    @DisplayName("비밀번호 재발급 실패 -  이메일 불일치")
    void createTempPasswordFail(){
        String username = "testId";
        String email = "wrong@test.com";

        Account account = Account.createAccount(username, "1234", Role.MEMBER);
        accountRepository.save(account);

        Member member = Member.createMember(account, "test@test.com", "홍길동", null, null, 0);
        memberRepository.save(member);

        assertThrows(IllegalStateException.class, () -> {
            accountService.createTempPassword(username, email);
        });
        }

    @Test
    @DisplayName("비밀번호 변경 실패 - 새 비밀번호와 새 비밀번호 확인이 일치하지 않음")
    void changePasswordFailTest() {
        // given
        String username = "testUser";
        String oldPassword = "oldPassword123";
        String newPassword = "newPassword456";
        String newPasswordConfirm = "newPassword789";


        Account account = Account.createAccount(username, oldPassword, Role.MEMBER);
        accountRepository.save(account);

        assertThrows(IllegalArgumentException.class, () -> {
            // newPassword와 newPasswordConfirm 비교
            if (!newPassword.equals(newPasswordConfirm)) {
                throw new IllegalArgumentException("새 비밀번호와 새 비밀번호 확인이 일치하지 않습니다.");
            }

            String encodedNewPassword = passwordEncoder.encode(newPassword);
            account.changePassword(encodedNewPassword);
        });
    }

    }

