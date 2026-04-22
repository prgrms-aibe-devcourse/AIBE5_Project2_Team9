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
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class ReservationController {

    private final AccountRepository accountRepository;
    private final MemberRepository memberRepository;

    @GetMapping("/items/{itemId}/reserve")
    public String reservationPage(@PathVariable Long itemId, Authentication auth, Model model) {
        model.addAttribute("member", resolveMember(auth));
        return "user/reservation";
    }

    private Member resolveMember(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return null;
        }
        Account account = accountRepository.findByUsername(auth.getName());
        if (account == null) return null;
        return memberRepository.findByAccount(account);
    }
}