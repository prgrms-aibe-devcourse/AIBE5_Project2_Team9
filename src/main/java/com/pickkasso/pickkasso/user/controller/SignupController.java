package com.pickkasso.pickkasso.user.controller;

import com.pickkasso.pickkasso.user.dto.SignupRequestDto;
import com.pickkasso.pickkasso.user.entity.Role;

import com.pickkasso.pickkasso.user.service.SignupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class SignupController {

    private final SignupService signupService;

    @GetMapping("/signup")
    public String signupIndex() {
        return "redirect:/signup/user";
    }

    @GetMapping("/signup/user")
    public String signupForm(Model model){
        model.addAttribute("signupRequestDto", new SignupRequestDto());
        return "common/signup-user";
    }

    @GetMapping("/signup/photographer")
    public String signupPhotographerForm(Model model){
        model.addAttribute("signupRequestDto", new SignupRequestDto());
        return "common/signup-photographer";
    }

    @PostMapping("/signup/user")
    public String signupUser(@ModelAttribute SignupRequestDto dto, Model model) {
        try {
            dto.setRole(Role.MEMBER);
            signupService.signup(dto);
            return "redirect:/login?signup=success";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "common/signup-user";
        }
    }

    @PostMapping("/signup/photographer")
    public String signupPhotograper(@ModelAttribute SignupRequestDto dto, Model model) {
        try {
            dto.setRole(Role.PHOTOGRAPHER);
            signupService.signup(dto);
            return "redirect:/login?signup=success";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "common/signup-photographer";
        }
    }
}
