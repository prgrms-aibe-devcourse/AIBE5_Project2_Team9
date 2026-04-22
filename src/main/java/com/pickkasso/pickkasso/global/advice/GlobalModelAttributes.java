package com.pickkasso.pickkasso.global.advice;

import com.pickkasso.pickkasso.user.entity.Account;
import com.pickkasso.pickkasso.user.entity.Member;
import com.pickkasso.pickkasso.user.entity.Role;
import com.pickkasso.pickkasso.user.repository.AccountRepository;
import com.pickkasso.pickkasso.user.repository.MemberRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

/**
 * 모든 뷰에서 공통적으로 사용할 사용자 Role과 ID를 모델에 자동으로 추가
 * Header fragment에서 Role별 링크와 텍스트를 분기할 때 사용
 */
@ControllerAdvice
public class GlobalModelAttributes {

    private final AccountRepository accountRepository;
    private final MemberRepository memberRepository;

    // 생성자 주입
    public GlobalModelAttributes(AccountRepository accountRepository, MemberRepository memberRepository) {
        this.accountRepository = accountRepository;
        this.memberRepository = memberRepository;
    }

    /**
     * 모든 컨트롤러의 뷰 렌더링 전에 호출
     * 로그인한 사용자라면 Role과 ID를 Model에 담아 헤더 등에서 활용 가능
     */
    @ModelAttribute
    public void addUserRoleAndId(Authentication auth, Model model) {
        // 사용자가 로그인 상태인지 확인
        if(auth != null && auth.isAuthenticated()) {
            // 로그인한 계정 정보 조회
            Account account = accountRepository.findByUsername(auth.getName());
            
            if (account != null) {
                model.addAttribute("role", account.getRole().name());
                
                if (account.getRole() == Role.PHOTOGRAPHER) {
                    model.addAttribute("userId", account.getId());
                } else if (account.getRole() == Role.MEMBER) {
                    Member member = memberRepository.findByAccount(account);
                    if (member != null) {
                        model.addAttribute("userId", member.getId());
                    }
                }
            }

            // Role 확인 로그
            System.out.println("Authentication Authorities:");
            auth.getAuthorities().forEach(a -> System.out.println(a.getAuthority()));
        }
    }
}