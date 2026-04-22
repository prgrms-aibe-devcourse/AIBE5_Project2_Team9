package com.pickkasso.pickkasso.user.controller;

import com.pickkasso.pickkasso.user.dto.PasswordChangeDto;
import com.pickkasso.pickkasso.user.entity.Account;
import com.pickkasso.pickkasso.user.entity.Member;
import com.pickkasso.pickkasso.user.repository.AccountRepository;
import com.pickkasso.pickkasso.user.repository.MemberRepository;
import com.pickkasso.pickkasso.user.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/member/mypage")
public class MyPageController {

    private final MemberRepository memberRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    private Member getCurrentMember(Authentication auth) {
        Account account = accountRepository.findByUsername(auth.getName());
        return memberRepository.findByAccount(account);
    }

    @GetMapping
    public String myPage() {
        return "redirect:/member/mypage/profile";
    }

    @GetMapping("/reservations")
    public String reservations(Authentication auth, Model model) {
        model.addAttribute("member", getCurrentMember(auth));
        model.addAttribute("activeTab", "reservations");
        return "user/mypage/reservations";
    }

    @GetMapping("/profile")
    public String profile(Authentication auth, Model model) {
        model.addAttribute("member", getCurrentMember(auth));
        model.addAttribute("activeTab", "profile");
        return "user/mypage/profile";
    }

    @PostMapping("/password")
    public String changePassword(@ModelAttribute PasswordChangeDto dto,
                                 Authentication auth,
                                 RedirectAttributes redirectAttributes) {

        Account account = accountRepository.findByUsername(auth.getName());

        // 1. 현재 비밀번호 불일치
        if(!passwordEncoder.matches(dto.getCurrentPassword(), account.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "current");
            return "redirect:/member/mypage/profile";
        }

        // 2. 새 비밀번호 불일치
        if(!dto.getNewPassword().equals(dto.getNewPasswordConfirm())) {
            redirectAttributes.addFlashAttribute("error", "mismatch");
            return "redirect:/member/mypage/profile";
        }

        // 3. 성공 시
        account.changePassword(passwordEncoder.encode(dto.getNewPassword()));
        accountRepository.save(account);

        redirectAttributes.addFlashAttribute("success", true);
        return "redirect:/member/mypage/profile";
    }

    @GetMapping("/member/mypage/profile")
    public String getProfile(Authentication auth, Model model) {
        return "user/mypage/profile"; // flash attribute가 model에 자동으로 담김
    }

    @GetMapping("/photographer/{photographerId}/profile")
    public String photographerProfile(@PathVariable Long photographerId, Model model) {
        // photographerId로 작가 정보 조회
        Member photographer = memberRepository.findById(photographerId).orElseThrow();
        model.addAttribute("photographer", photographer);
        return "photographer/profile"; // 작가 대시보드 뷰
    }

}
