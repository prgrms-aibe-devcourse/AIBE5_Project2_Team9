package com.pickkasso.pickkasso.user.controller;

import com.pickkasso.pickkasso.item.service.ItemService;
import com.pickkasso.pickkasso.user.dto.photographer.PhotographerProfileEditRequest;
import com.pickkasso.pickkasso.user.dto.photographer.PhotographerProfileResponse;
import com.pickkasso.pickkasso.user.entity.Account;
import com.pickkasso.pickkasso.user.repository.AccountRepository;
import com.pickkasso.pickkasso.user.service.PhotographerProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequiredArgsConstructor
@RequestMapping("/photographer/{photographerId}/profile")
public class PhotographerProfileController {

    private final PhotographerProfileService photographerProfileService;
    private final ItemService itemService;
    private final AccountRepository accountRepository;

    @GetMapping
    public String profilePage(@PathVariable Long photographerId, Model model, Authentication authentication) {
        PhotographerProfileResponse response = photographerProfileService.getProfileForm(photographerId);
        model.addAttribute("profile", response);
        model.addAttribute("items", itemService.getItemsByPhotographerId(photographerId));

        boolean isOwner = false;
        if (authentication != null && authentication.isAuthenticated()) {
            Account account = accountRepository.findByUsername(authentication.getName());
            isOwner = account != null && account.getId().equals(photographerId);
        }
        model.addAttribute("isOwner", isOwner);

        return "photographer/profile";
    }

    @GetMapping("/edit")
    public String editProfilePage(@PathVariable Long photographerId, Model model) {
        PhotographerProfileResponse response = photographerProfileService.getProfileForm(photographerId);

        model.addAttribute("profile", response);
        return "photographer/editProfileForm";
    }

    @PostMapping("/edit")
    public String editProfile(
            @PathVariable Long photographerId,
            @ModelAttribute("profile") PhotographerProfileEditRequest request,
            @RequestParam(value = "profileImg", required = false) MultipartFile profileImg,
            Model model
    ) {
        try {
            photographerProfileService.createOrUpdateProfile(photographerId, request, profileImg);
            return "redirect:/photographer/" + photographerId + "/profile";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("profile", request);
            return "photographer/editProfileForm";
        }
    }
}