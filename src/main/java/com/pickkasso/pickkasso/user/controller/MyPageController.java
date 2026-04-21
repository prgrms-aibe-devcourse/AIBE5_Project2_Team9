package com.pickkasso.pickkasso.user.controller;

import com.pickkasso.pickkasso.user.entity.Account;
import com.pickkasso.pickkasso.user.entity.Member;
import com.pickkasso.pickkasso.user.repository.AccountRepository;
import com.pickkasso.pickkasso.user.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/member/mypage")
public class MyPageController {

    private final MemberRepository memberRepository;
    private final AccountRepository accountRepository;

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
}
