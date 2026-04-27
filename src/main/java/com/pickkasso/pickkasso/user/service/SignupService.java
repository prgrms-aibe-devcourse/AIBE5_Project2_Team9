package com.pickkasso.pickkasso.user.service;

import com.pickkasso.pickkasso.user.dto.AccountDto;
import com.pickkasso.pickkasso.user.dto.SignupDto;
import com.pickkasso.pickkasso.user.entity.Account;
import com.pickkasso.pickkasso.user.entity.Member;
import com.pickkasso.pickkasso.user.entity.Photographer;
import com.pickkasso.pickkasso.user.entity.Role;
import com.pickkasso.pickkasso.user.repository.MemberRepository;
import com.pickkasso.pickkasso.user.repository.PhotographerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SignupService {

    private final MemberRepository memberRepository;
    private final AccountService accountService;
    private final PhotographerRepository photographerRepository;


    @Transactional
    public void signup(SignupDto dto) {

        if (!dto.getPassword().matches("^(?=.*[a-z])(?=.*\\d).{8,30}$")) {
            throw new IllegalStateException("비밀번호 규칙 위반");
        }


        if (!dto.getPassword().equals(dto.getPasswordConfirm())) {
            throw new IllegalStateException("비밀번호 불일치");
        }

        // Account 생성
        Account account = accountService.saveAccount(
                new AccountDto(
                        dto.getUsername(),
                        dto.getPassword(),
                        dto.getRole()
                )
        );

        if (dto.getRole() == Role.MEMBER) {
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
        } else if (dto.getRole() == Role.PHOTOGRAPHER) {
            Photographer photographer = Photographer.createMember(
                account,
                dto.getEmail(),
                dto.getName(),
                dto.getGender(),
                dto.getPhone(),
                0
            );
            photographerRepository.save(photographer);
        } else if (dto.getRole() == Role.ADMIN) {
            // pass
        }
    }
}
