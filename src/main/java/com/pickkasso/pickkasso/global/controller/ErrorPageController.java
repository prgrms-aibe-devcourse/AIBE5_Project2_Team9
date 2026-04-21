package com.pickkasso.pickkasso.global.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorPageController {

    @GetMapping("/403")
    public String accessDeniedPage() {
        return "common/errors/403";
    }

    @GetMapping("/404")
    public String notFoundPage() {
        return "common/errors/404";
    }

    @GetMapping("/500")
    public String internalServerErrorPage() {
        return "common/errors/500";
    }

    @GetMapping("/400")
    public String badRequestPage() {
        return "common/errors/400";
    }

    @GetMapping("/error-default")
    public String defaultErrorPage() {
        return "common/errors/error";
    }
}
