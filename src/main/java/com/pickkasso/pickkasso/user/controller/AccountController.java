package com.pickkasso.pickkasso.user.controller;

import com.pickkasso.pickkasso.user.service.AccountService;
import lombok.Getter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/find-account")
    public String findAccountPage(){
        return "common/find-account";
    }

    @PostMapping("/find-id/email")
    @ResponseBody
    public Map<String, Object> findId(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        String email = body.get("email");

        Map<String, Object> result = new HashMap<>();

        try {
            String username = accountService.findUsername(name, email);
            result.put("success", true);
            result.put("loginId", username);
        } catch (Exception e) {
            result.put("success", false);
        }

        return result;
    }

    @PostMapping("/find-pw/email")
    @ResponseBody
    public Map<String, Object> findPw(@RequestBody Map<String, String> body) {
        String loginId = body.get("loginId");
        String email = body.get("email");

        Map<String, Object> result = new HashMap<>();

        try {
            String tempPw = accountService.createTempPassword(loginId, email);
            result.put("success", true);
            result.put("tempPw", tempPw);
        } catch (Exception e) {
            result.put("success", false);
        }

        return result;
    }
}
