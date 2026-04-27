package com.pickkasso.pickkasso.user.controller;

import com.pickkasso.pickkasso.user.dto.SignupDto;
import com.pickkasso.pickkasso.user.entity.Account;
import com.pickkasso.pickkasso.user.entity.Role;

import com.pickkasso.pickkasso.user.repository.AccountRepository;
import com.pickkasso.pickkasso.user.service.AccountService;
import com.pickkasso.pickkasso.user.service.SignupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.boot.context.properties.source.ConfigurationPropertyName.isValid;

@Controller
@RequiredArgsConstructor
public class SignupController {

    private final SignupService signupService;
    private final AccountRepository accountRepository;
    private final AccountService accountService;

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

    @GetMapping("/api/check-username")
    public ResponseEntity<Map<String, Object>> checkUsername(@RequestParam String username) {
        Map<String, Object> result = new HashMap<>();
        boolean available = accountService.isUsernameAvailable(username);
        result.put("available", available);  // true/false만
        return ResponseEntity.ok(result);
    }
}
