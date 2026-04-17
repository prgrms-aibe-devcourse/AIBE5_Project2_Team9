package com.pickkasso.pickkasso.user.controller;

import lombok.Getter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccountController {

    @GetMapping("/find-account")
    public String findAccountPage(){
        return "common/find-account";
    }
}
