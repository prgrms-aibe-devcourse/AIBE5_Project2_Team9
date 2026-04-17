package com.pickkasso.pickkasso.user.service;

import com.pickkasso.pickkasso.user.dto.AccountDto;
import com.pickkasso.pickkasso.user.dto.SignupDto;
import com.pickkasso.pickkasso.user.entity.Account;
import com.pickkasso.pickkasso.user.entity.Member;
import com.pickkasso.pickkasso.user.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SignupService {

    private final MemberRepository memberRepository;
    private final AccountService accountService;


    public void signup(SignupDto dto) {

        // Account 생성
        Account account = accountService.saveAccount(
                new AccountDto(
                        dto.getUsername(),
                        dto.getPassword(),
                        dto.getRole()
                )
        );


        //  Member 생성
        Member member = Member.createMember(
                account,
                dto.getEmail(),
                dto.getName(),
                dto.getGender(),
                dto.getPhone(),
                0
        );

        memberRepository.save(member);
    }
}
