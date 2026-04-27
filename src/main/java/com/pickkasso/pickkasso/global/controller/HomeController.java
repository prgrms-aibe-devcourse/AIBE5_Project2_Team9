package com.pickkasso.pickkasso.global.controller;

import com.pickkasso.pickkasso.global.tag.TagReference;
import com.pickkasso.pickkasso.global.tag.TagService;
import com.pickkasso.pickkasso.item.dto.ItemSearchFormDto;
import com.pickkasso.pickkasso.item.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final TagService tagService;
    private final ItemService itemService;

    @GetMapping("/")
    public String home(Model model) {
        List<TagReference> tagList = tagService.findAllTagReference();
        model.addAttribute("tagList", tagList);
        model.addAttribute("itemSearchFormDto", new ItemSearchFormDto());
        model.addAttribute("scoreItemList", itemService.getScoreItemList(5));
        model.addAttribute("customItemList", itemService.getCustomItemList(5));
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "common/login";
    }

    /*@GetMapping("/signup")
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
    }*/
}
