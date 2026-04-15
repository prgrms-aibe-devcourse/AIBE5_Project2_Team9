package com.pickkasso.pickkasso.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "common/login";
    }

    @GetMapping("/signup")
    public String signupIndex() {
        return "redirect:/signup/user";
    }

    @GetMapping("/signup/user")
    public String signupUser() {
        return "common/signup-user";
    }

    @GetMapping("/signup/photographer")
    public String signupPhotographer() {
        return "common/signup-photographer";
    }

    // TODO(backend): 실제 회원 생성 로직으로 교체할 것
    // SignupController 분리 + SignupRequest DTO + SignupService + Repository 구현 필요
    // 성공 시 redirect:/login?signup=success 로 변경
    @PostMapping({"/signup/user", "/signup/photographer"})
    public String signupStub() {
        return "common/signup-pending";
    }
}
