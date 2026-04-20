package com.pickkasso.pickkasso.user.service;

import com.pickkasso.pickkasso.user.dto.AccountDto;
import com.pickkasso.pickkasso.user.entity.Account;
import com.pickkasso.pickkasso.user.entity.Member;
import com.pickkasso.pickkasso.user.repository.AccountRepository;
import com.pickkasso.pickkasso.user.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;

    private Account saveAccount(Account account) {
        validateDuplicateAccount(account);
        return accountRepository.save(account);
    }

    public Account saveAccount(AccountDto dto) {
        String encoded = passwordEncoder.encode(dto.getPassword());
        return saveAccount(dto.createAccount(encoded));
    }

    private void validateDuplicateAccount(Account account) {
        Account findAccount = accountRepository.findByUsername(account.getUsername());
        if (findAccount != null) {
            throw new IllegalStateException("이미 사용 중인 아이디입니다.");
        }
    }

    public String findUsername(String name, String email){
        Member member = memberRepository.findByNameAndEmail(name, email)
                .orElseThrow(() -> new IllegalStateException("회원 없음"));
        return member.getAccount().getUsername();
    }

    @Transactional
    public String createTempPassword(String username, String email) {

        // 1. account 조회
        Account account = accountRepository.findByUsername(username);
        if (account == null) {
            throw new IllegalStateException("아이디 없음");
        }

        // 2. member에서 이메일 검증
        Member member = memberRepository.findByAccount(account);
        if (member == null) {
            throw new IllegalStateException("회원 없음");
        }

        if (!member.getEmail().equals(email)) {
            throw new IllegalStateException("이메일 불일치");
        }

        // 3. 임시 비밀번호 생성
        String tempPw = UUID.randomUUID().toString().substring(0, 8);

        // 4. 암호화
        String encodedPw = passwordEncoder.encode(tempPw);

        // 5. 비밀번호 변경
        account.changePassword(encodedPw);

        return tempPw;
    }

}
