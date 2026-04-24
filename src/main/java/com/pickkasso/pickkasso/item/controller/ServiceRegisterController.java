package com.pickkasso.pickkasso.item.controller;

import com.pickkasso.pickkasso.global.tag.TagService;
import com.pickkasso.pickkasso.item.dto.ItemRegisterRequest;
import com.pickkasso.pickkasso.item.entity.Item;
import com.pickkasso.pickkasso.item.service.ItemService;
import com.pickkasso.pickkasso.user.service.PhotographerProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/photographer/{photographerId}/service")
public class ServiceRegisterController {

    private final ItemService itemService;
    private final TagService tagService;
    private final PhotographerProfileService photographerProfileService;

    // private static final Map<String, String> CATEGORY_EMOJIS = Map.ofEntries(
    //     Map.entry("데이트스냅", "🌸"),
    //     Map.entry("데이트 스냅", "🌸"),
    //     Map.entry("프로필", "🤳"),
    //     Map.entry("졸업", "🎓"),
    //     Map.entry("졸업사진", "🎓"),
    //     Map.entry("웨딩", "💒"),
    //     Map.entry("웨딩 스냅", "💒"),
    //     Map.entry("가족/아이", "👨‍👩‍👧"),
    //     Map.entry("가족사진", "👨‍👩‍👧"),
    //     Map.entry("증명사진", "🪪"),
    //     Map.entry("반려동물", "🐾"),
    //     Map.entry("제품/커머셜", "🛍️"),
    //     Map.entry("제품 촬영", "🛍️"),
    //     Map.entry("음식", "🍽️"),
    //     Map.entry("건축", "🏛️"),
    //     Map.entry("야외", "🌿"),
    //     Map.entry("야외 촬영", "🌿"),
    //     Map.entry("스튜디오", "🎞️"),
    //     Map.entry("드론", "🚁"),
    //     Map.entry("공연", "🎭")
    // );

    @GetMapping("/{itemId}")
    public String serviceDetail(
        @PathVariable Long photographerId,
        @PathVariable Long itemId,
        Model model) {

        Item item = itemService.getItemById(itemId);
        model.addAttribute("item", item);
        model.addAttribute("profile", photographerProfileService.getProfileForm(photographerId));
        return "photographer/service-detail";
    }

    @GetMapping("/new")
    public String serviceRegisterForm(@PathVariable Long photographerId, Model model) {
        model.addAttribute("photographerId", photographerId);
        model.addAttribute("tagList", tagService.findAllTagReference());
        model.addAttribute("itemRegisterRequest", new ItemRegisterRequest());
        model.addAttribute("formAction", "/photographer/" + photographerId + "/service/new");
        return "photographer/service-register";
    }

    @PostMapping("/new")
    public String registerService(
        @PathVariable Long photographerId,
        @ModelAttribute ItemRegisterRequest itemRegisterRequest,
        @RequestParam(required = false) List<String> keptImgUrls,
        @RequestParam(required = false) List<Integer> keptImgOrders,
        @RequestParam(required = false) List<MultipartFile> newFiles,
        @RequestParam(required = false) List<Integer> newFileOrders,
        RedirectAttributes redirectAttributes) {

        try {
            Long itemId = itemService.registerItem(photographerId, itemRegisterRequest, newFiles, newFileOrders);
            redirectAttributes.addFlashAttribute("successMessage", "서비스가 등록되었습니다.");
            return "redirect:/photographer/" + photographerId + "/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/photographer/" + photographerId + "/service/new";
        }
    }


    @GetMapping("/{itemId}/edit")
    public String serviceEditForm(@PathVariable Long photographerId, @PathVariable Long itemId, Model model) {
        model.addAttribute("photographerId", photographerId);
        model.addAttribute("tagList", tagService.findAllTagReference());
        model.addAttribute("itemRegisterRequest", itemService.getItemRegisterRequest(photographerId, itemId));
        model.addAttribute("itemImgList", itemService.getItemImage(photographerId, itemId));
        model.addAttribute("formAction", "/photographer/" + photographerId + "/service/edit");
        return "photographer/service-register";
    }


    @PostMapping("/{itemId}/edit")
    public String updateService(
        @PathVariable Long photographerId,
        @PathVariable Long itemId,
        @ModelAttribute ItemRegisterRequest itemRegisterRequest,
        @RequestParam(required = false) List<String> keptImgUrls,
        @RequestParam(required = false) List<Integer> keptImgOrders,
        @RequestParam(required = false) List<MultipartFile> newFiles,
        @RequestParam(required = false) List<Integer> newFileOrders,
        RedirectAttributes redirectAttributes) {

        try {
            itemService.updateItem(photographerId, itemId, itemRegisterRequest, keptImgUrls, keptImgOrders, newFiles, newFileOrders);
            redirectAttributes.addFlashAttribute("successMessage", "서비스가 수정되었습니다.");
            return "redirect:/photographer/" + photographerId + "/" + itemId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/photographer/" + photographerId + "/" + itemId + "/edit";
        }
    }
}
