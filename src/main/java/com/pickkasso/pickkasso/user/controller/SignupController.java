package com.pickkasso.pickkasso.user.controller;

import com.pickkasso.pickkasso.user.dto.SignupDto;
import com.pickkasso.pickkasso.user.entity.Role;

import com.pickkasso.pickkasso.user.service.SignupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import jakarta.validation.Valid;

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
        model.addAttribute("signupRequestDto", new SignupDto());
        return "common/signup-user";
    }

    @GetMapping("/signup/photographer")
    public String signupPhotographerForm(Model model){
        model.addAttribute("signupRequestDto", new SignupDto());
        return "common/signup-photographer";
    }

    @PostMapping("/signup/user")
    public String signupUser(@Valid @ModelAttribute SignupDto dto, BindingResult result, Model model) {

        if (result.hasErrors()) {
            return "common/signup-user";
        }

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
    public String signupPhotograper(@Valid @ModelAttribute SignupDto dto, BindingResult result, Model model) {

        if (result.hasErrors()) {
            return "common/signup-photographer";
        }

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
