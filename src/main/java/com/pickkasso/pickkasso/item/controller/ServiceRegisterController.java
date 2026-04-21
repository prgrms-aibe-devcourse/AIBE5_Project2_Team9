package com.pickkasso.pickkasso.item.controller;

import com.pickkasso.pickkasso.global.tag.TagService;
import com.pickkasso.pickkasso.item.dto.ItemRegisterRequest;
import com.pickkasso.pickkasso.item.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/photographer/{photographerId}/service")
public class ServiceRegisterController {

    private final ItemService itemService;
    private final TagService tagService;

    private static final Map<String, String> CATEGORY_EMOJIS = Map.ofEntries(
        Map.entry("데이트스냅", "🌸"),
        Map.entry("데이트 스냅", "🌸"),
        Map.entry("프로필", "🤳"),
        Map.entry("졸업", "🎓"),
        Map.entry("졸업사진", "🎓"),
        Map.entry("웨딩", "💒"),
        Map.entry("웨딩 스냅", "💒"),
        Map.entry("가족/아이", "👨‍👩‍👧"),
        Map.entry("가족사진", "👨‍👩‍👧"),
        Map.entry("증명사진", "🪪"),
        Map.entry("반려동물", "🐾"),
        Map.entry("제품/커머셜", "🛍️"),
        Map.entry("제품 촬영", "🛍️"),
        Map.entry("음식", "🍽️"),
        Map.entry("건축", "🏛️"),
        Map.entry("야외", "🌿"),
        Map.entry("야외 촬영", "🌿"),
        Map.entry("스튜디오", "🎞️"),
        Map.entry("드론", "🚁"),
        Map.entry("공연", "🎭")
    );

    @GetMapping("/new")
    public String serviceRegisterForm(@PathVariable Long photographerId, Model model) {
        model.addAttribute("photographerId", photographerId);
        model.addAttribute("tagList", tagService.findAllTagReference());
        model.addAttribute("categoryEmojis", CATEGORY_EMOJIS);
        model.addAttribute("itemRegisterRequest", new ItemRegisterRequest());
        return "photographer/service-register";
    }

    @PostMapping("/new")
    public String registerService(
        @PathVariable Long photographerId,
        @ModelAttribute ItemRegisterRequest itemRegisterRequest,
        RedirectAttributes redirectAttributes) {

        try {
            Long itemId = itemService.registerItem(photographerId, itemRegisterRequest);
            redirectAttributes.addFlashAttribute("successMessage", "서비스가 등록되었습니다.");
            return "redirect:/photographer/" + photographerId + "/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/photographer/" + photographerId + "/service/new";
        }
    }
}
