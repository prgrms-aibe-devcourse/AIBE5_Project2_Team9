package com.pickkasso.pickkasso.global.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorPageController {

    @GetMapping("/403")
    public String accessDeniedPage() {
        return "common/errors/403";
    }
}
